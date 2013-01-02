/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.scenegraph;

import redhorizon.geometry.Ray;
import redhorizon.geometry.Vector3f;

/**
 * A non-transformable box depicting a bounding volume over a collection of
 * objects.  Used when the camera projection mode is orthographic (ie: 2D
 * camera).
 * 
 * @author Emanuel Rabina
 */
public class BoundingBox extends BoundingVolume {

	public static final BoundingBox ZERO = new BoundingBox();

	private float width;
	private float height;
	private float depth;

	/**
	 * Default constructor, creates a bounding box with zero volume.
	 */
	public BoundingBox() {
	}

	/**
	 * Constructor, creates a new bounding box with the given values.
	 * 
	 * @param center
	 * @param width
	 * @param height
	 * @param depth
	 */
	public BoundingBox(Vector3f center, float width, float height, float depth) {

		this.center = center;
		this.width  = width;
		this.height = height;
		this.depth  = depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean intersects(Ray ray) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingVolume merge(BoundingVolume volume) {

		// This volume's edges
		float lx = center.x - (width / 2);
		float ux = center.x + (width / 2);
		float ly = center.y - (height / 2);
		float uy = center.y + (height / 2);
		float lz = center.z - (depth / 2);
		float uz = center.z + (depth / 2);

		// The other volume's edges
		if (volume instanceof BoundingBox) {
			BoundingBox bb = (BoundingBox)volume;
			float olx = bb.center.x - (bb.width / 2);
			float oux = bb.center.x + (bb.width / 2);
			float oly = bb.center.y - (bb.height / 2);
			float ouy = bb.center.y + (bb.height / 2);
			float olz = bb.center.z - (bb.depth / 2);
			float ouz = bb.center.z + (bb.depth / 2);

			return new BoundingBox(
					new Vector3f(
							(center.x + volume.center.x) / 2,
							(center.y + volume.center.y) / 2,
							(center.z + volume.center.z) / 2),
					Math.max(ux, oux) - Math.min(lx, olx),
					Math.max(uy, ouy) - Math.min(ly, oly),
					Math.max(uz, ouz) - Math.min(lz, olz)
			);
		}

		return null;
	}
}
