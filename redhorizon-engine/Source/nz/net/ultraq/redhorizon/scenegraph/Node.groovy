/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.redhorizon.scenegraph

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.geometry.Ray
import nz.net.ultraq.redhorizon.geometry.Vector3f

import java.util.concurrent.ConcurrentSkipListSet

/**
 * A node in the scene graph which can be used to hold other nodes or scene
 * objects.
 * 
 * @author Emanuel Rabina
 */
class Node extends Spatial implements SceneElement, Comparable<Node> {

	// Node parts
	private Node parent
	private final SortedSet<Spatial> children = new ConcurrentSkipListSet<>()

	// Calculated parts
	private BoundingVolume boundingvolume = BoundingBox.ZERO
	private boolean volumeneedsupdate = true

	// Multi-threaded rendering bits
	private final List<AudioElement> audioinit   = []
	private final List<AudioElement> audiodelete = []
//	private final ArrayList<GraphicsObject> graphicsinit   = new ArrayList<>();
//	private final ArrayList<GraphicsObject> graphicsdelete = new ArrayList<>();

	/**
	 * @inheritDoc
	 */
	@Override
	void accept(SceneElementVisitor visitor) {

		visitor.visit(this)
		for (SceneElement element: children) {
			element.accept(visitor)
		}
	}

	/**
	 * Adds the given object as a child of this node.
	 * 
	 * @param child
	 */
	void addChild(Spatial child) {

		if (children.add(child)) {

			// Queue object for initialization
			if (child instanceof AudioElement) {
				audioinit << child
			}
//			if (child instanceof GraphicsObject) {
//				graphicsinit.add((GraphicsObject)child);
//			}

			if (child instanceof Node) {
				child.parent = this
			}

			volumeneedsupdate = true
		}
	}

	/**
	 * Calculates the bounding volume occupied by this node and its children.
	 * 
	 * @return Node's bounding volume.
	 */
	@Override
	BoundingVolume getBoundingVolume() {

		// Recalculate the volume if necessary
		if (volumeneedsupdate) {
			for (Spatial child: children) {
				boundingvolume = boundingvolume.merge(child.getBoundingVolume())
			}
			volumeneedsupdate = false
		}
		return boundingvolume
	}

	/**
	 * Does a comparison of this node to the other.  Node ordering is based on
	 * video rendering order for a 2D game, where the furthest object is first
	 * and the closest object is last.
	 * 
	 * @param other The other node to compare against.
	 * @return &lt; 0, 0, or &gt; 0 if this node is less-than, equal-to, or
	 * 		   greater-than the other node.
	 */
	@Override
	int compareTo(Node other) {

		return position.z != other.position.z ? position.z - other.position.z :
		       position.y != other.position.y ? other.position.y - position.y :
		       position.x - other.position.x
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean intersects(Ray ray) {

		return getBoundingVolume().intersects(ray)
	}

	/**
	 * Removes the given child object from this node.
	 * 
	 * @param child
	 */
	void removeChild(Spatial child) {

		if (children.remove(child)) {

			// Queue object for deletion
			if (child instanceof AudioElement) {
				audioinit.remove(child)
				audiodelete << child
			}
//			if (child instanceof GraphicsObject) {
//				graphicsinit.remove(child);
//				graphicsdelete.add((GraphicsObject)child);
//			}

			// If a node is being removed, work through that node's children, adding
			// any deletion objects to this node's deletion queue so they get processed
			if (child instanceof Node) {
				def getChildren
				getChildren = { Node node ->
					if (node instanceof AudioElement) {
						audiodelete << node
					}
					node.children.each { childNode ->
						if (childNode instanceof Node) {
							getChildren(childNode)
						}
					}
				}
				getChildren = getChildren.trampoline(child)

//				graphicsdelete.addAll(childnode.graphicsdelete);
			}

			volumeneedsupdate = true
		}
	}

	/**
	 * Initializes and plays back any audio objects within this node, including
	 * amongst its children.
	 * 
	 * @param renderer
	 */
	void render(AudioRenderer renderer) {

		// Initialize any items
		audioinit.removeAll { audioObject ->
			audioObject.init(renderer)
			return true
		}

		// Render items
		for (Spatial child: children) {
			if (child instanceof AudioElement) {
				child.render(renderer)
			}
			else if (child instanceof Node) {
				child.render(renderer)
			}
		}
	}

	/**
	 * Renders the graphics of the attached object and it's children, if they
	 * are instances of {@link Drawable}.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
/*	void renderNode(GL gl) {

		// Skip rendering if this node is not within view of the camera
//		if (!manager.currentViewingArea().intersects(absoluteArea())) {
//			return;
//		}

		// Adjust modelview matrix position
		gl.glPushMatrix();
		gl.glTranslatef(position.getX(), position.getY(), position.getZ());

		// Render target
		if (targetgl != null && targetgl.isDrawing()) {
			targetgl.render(gl);
		}

		for (SceneNode child: children) {
			child.renderNode(gl);
		}

		// Restore modelview matrix position
		gl.glPopMatrix();
	}
*/
	/**
	 * Update this node's position.
	 */
	void setPosition(Vector3f position) {

		this.position = position
		volumeneedsupdate = true
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void setRotation(float rotation) {

		super.setRotation(rotation)
		volumeneedsupdate = true
	}
}
