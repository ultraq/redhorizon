
package redhorizon.geometry;

/**
 * Representation of a cube in 3-dimensional space.  The point of x,y,z defines
 * the bottom-left-closest corner, and width,height,depth help define the
 * volume.
 * 
 * @author Emanuel Rabina
 */
public class Cube extends Rectangle {

	float z;
	float depth;

	/**
	 * Constructor, sets this cube's initial values.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 * @param depth
	 */
	public Cube(float x, float y, float z, float width, float height, float depth) {

		super(x, y, width, height);
		this.z     = z;
		this.depth = depth;
	}

	/**
	 * Compares against another cube to see if they're equal.
	 * 
	 * @param obj
	 * @return <tt>true</tt> if the other object is a cube with the same
	 * 		   x, y, z, width, height, and depth values.
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Cube) {
			Cube other = (Cube)obj;
			return x == other.x && y == other.y && z == other.z &&
					width == other.width && height == other.height && depth == other.depth;
		}
		return false;
	}

	/**
	 * Return the Z co-ordinate.
	 * 
	 * @return Z co-ordinate.
	 */
	public float getZ() {

		return z;
	}

	/**
	 * Return the depth of this object.
	 * 
	 * @return Object depth.
	 */
	public float getDepth() {

		return depth;
	}

	/**
	 * Redefines <tt>hashCode()</tt> so that equal cubes have the same hash
	 * value.
	 * 
	 * @return Hash value for this cube.
	 */
	@Override
	public int hashCode() {

		return super.hashCode() + Float.floatToIntBits(z) + Float.floatToIntBits(depth);
	}

	/**
	 * Set the Z co-ordinate.
	 * 
	 * @param z
	 * @return This object.
	 */
	public Cube setZ(float z) {

		this.z = z;
		return this;
	}

	/**
	 * Set the depth of this object.
	 * 
	 * @param depth
	 * @return This object.
	 */
	public Cube setDepth(float depth) {

		this.depth = depth;
		return this;
	}

	/**
	 * Returns this cube represented as (x,y,z,width,height,depth).
	 * 
	 * @return Cube(x,y,z,width,height,depth).
	 */
	@Override
	public String toString() {

		return "Cube(" + x + "," + y + "," + z + "," + width + "," + height + "," + depth + ")";
	}
}
