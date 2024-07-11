/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.partioning

import nz.net.ultraq.redhorizon.engine.scenegraph.Node

import org.joml.FrustumIntersection
import org.joml.Intersectionf
import org.joml.Vector3fc
import org.joml.primitives.Circlef
import org.joml.primitives.Rectanglef

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * A class for partitioning the scene of 2D game objects so that spatial queries
 * (eg: frustrum culling) can be performant.
 * <a href="https://en.wikipedia.org/wiki/Quadtree">https://en.wikipedia.org/wiki/Quadtree</a>
 *
 * @author Emanuel Rabina
 */
class QuadTree {

	private final QuadTree parent
	private final Rectanglef area
	private final int capacity
	// TODO: Make this quadtree accept any element that can return a Vector2f position
	private BlockingQueue<Node> children
	private List<QuadTree> quadrants // Going from NW, NE, SE, SW

	/**
	 * Constructor, create a quadtree with the given area over which it resides
	 * and capacity before subdividing.
	 */
	QuadTree(Rectanglef area, int capacity = 10) {

		this(null, area, capacity)
	}

	/**
	 * Constructor, create a quadtree as a child quadrant of the given parent.
	 */
	private QuadTree(QuadTree parent, Rectanglef area, int capacity) {

		this.parent = parent
		this.area = area
		this.capacity = capacity
		children = new ArrayBlockingQueue<>(capacity, true)
	}

	/**
	 * Add an element to the quadtree.
	 *
	 * @return {@code true} if the element could be added to this quadtree, {@code
	 *   false} otherwise.
	 */
	synchronized boolean add(Node node) {

		var nodePosition = node.globalPosition

		// Node can live within this quadtree
		if (area.containsPoint(nodePosition.x(), nodePosition.y(), true)) {

			// Try to add the node as an immediate child
			if (!children?.offer(node)) {

				// Otherwise, subdivide into quadrants, move all children to quadrants
				if (!quadrants) {
					var halfX = area.minX + area.lengthX() / 2 as float
					var halfY = area.minY + area.lengthY() / 2 as float
					quadrants = [
						new QuadTree(this, new Rectanglef(area.minX, halfY, halfX, area.maxY), capacity),
						new QuadTree(this, new Rectanglef(halfX, halfY, area.maxX, area.maxY), capacity),
						new QuadTree(this, new Rectanglef(halfX, area.minY, area.maxX, halfY), capacity),
						new QuadTree(this, new Rectanglef(area.minX, area.minY, halfX, halfY), capacity)
					]
					var moveResult = children.every() { existingChild ->
						return addToQuadrant(existingChild.globalPosition, existingChild)
					}
					if (!moveResult) {
						throw new Exception('Unable to move children to new quadrants')
					}
					children = null
				}

				// Place new child in quadrants
				return addToQuadrant(nodePosition, node)
			}

			return true
		}

		return false
	}

	/**
	 * Add a child to one of the existing quadrants.
	 */
	private boolean addToQuadrant(Vector3fc position, Node node) {

		// For the literal edge case where the node lives right at the center of all
		// of the quadrants, pick a random unfilled quadrant to place it into
		if (position.x() == area.minX + area.lengthX() / 2 && position.y() == area.minY + area.lengthY() / 2) {
			var randomQuadrantIndex = (int)Math.floor(Math.random() * 4)
			var attempts = 0
			while (true) {
				var randomQuadrant = quadrants[randomQuadrantIndex]
				if (randomQuadrant.children?.offer(node)) {
					return true
				}
				else {
					randomQuadrantIndex = Math.wrap(randomQuadrantIndex + 1, 0, 4)
					attempts++
				}
				if (attempts == 4) {
					throw new Exception("Unable to find a home for node ${node} for position ${position}")
				}
			}
		}

		return quadrants.any { quadrant -> quadrant.add(node) }
	}

	/**
	 * Return all nodes that are within the given view frustum.
	 */
	List<Node> query(FrustumIntersection frustumIntersection, List<Node> results = []) {

		if (frustumIntersection.testPlaneXY(area)) {
			if (children) {
				results.addAll(children)
			}
			else if (quadrants) {
				quadrants*.query(frustumIntersection, results)
			}
		}
		return results
	}

	/**
	 * Return all nodes that are within the given range.
	 */
	List<Node> query(Circlef range, List<Node> results = []) {

		if (Intersectionf.testAarCircle(area.minX, area.minY, area.maxX, area.maxY, range.x, range.y, range.r)) {
			if (children) {
				results.addAll(children)
			}
			if (quadrants) {
				quadrants*.query(range, results)
			}
		}
		return results
	}

	/**
	 * Removes a node from this quadtree.  Nodes will attempt to rebalance
	 * themselves if quadrants can also be removed.
	 *
	 * @return {@code true} if the node was removed from the quadtree.
	 */
	synchronized boolean remove(Node node) {

		var nodePosition = node.globalPosition

		// Node potentially lives within this quadtree
		if (area.containsPoint(nodePosition.x(), nodePosition.y(), true)) {

			// Check children for node first
			if (children?.contains(node)) {
				children.remove(node)

				// If all children removed, rebalance the parent node
				if (!children && parent != null && !parent.size()) {
					// TODO: There are probably some smarts we can do here, but for now
					//       the only rebalancing we're doing is to collapse a node with
					//       4 empty quadrants
					parent.children = new ArrayBlockingQueue<>(capacity, true)
					parent.quadrants = null
				}

				return true
			}

			// Otherwise, must live in a quadrant, so remove from there
			return removeFromQuadrant(node)
		}

		return false
	}

	/**
	 * Remove a child from one of the existing quadrants
	 */
	private boolean removeFromQuadrant(Node node) {

		return quadrants.any { quadrant -> quadrant.remove(node) }
	}

	/**
	 * Returns the number of items held in this quadtree.
	 */
	int size() {

		return quadrants ?
			(int)quadrants.sum(0) { quadrant -> quadrant.size() } :
			children.size()
	}
}
