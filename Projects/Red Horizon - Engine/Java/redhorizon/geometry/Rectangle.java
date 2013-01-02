
package redhorizon.geometry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Representation of a rectangle in 2-dimensional space, specified by a point in
 * 2D space, a width, and a height.
 * 
 * @author Emanuel Rabina
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
	name = "Rectangle",
	propOrder = {"x","y","width","height"}
)
public class Rectangle {

	@XmlAttribute public float x;
	@XmlAttribute public float y;
	@XmlAttribute public float width;
	@XmlAttribute public float height;

	/**
	 * Default constructor, creates a new rectangle at (0,0) with 0 width and
	 * height.
	 */
	public Rectangle() {

		this(0, 0, 0, 0);
	}

	/**
	 * Constructor, creates a new rectangle with the given values.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle(float x, float y, float width, float height) {

		this.x      = x;
		this.y      = y;
		this.width  = width;
		this.height = height;
	}

	/**
	 * Compares against another rectangle to see if they're equal.
	 * 
	 * @param obj
	 * @return <tt>true</tt> if the other rectangle has the same points.
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Rectangle) {
			Rectangle other = (Rectangle)obj;
			return x == other.x && y == other.y && width == other.width && y == other.y;
		}
		return false;
	}

	/**
	 * Returns the X co-ordinate of this rectangle.  Primarily used by
	 * frameworks which expect Java bean methods - use the publicly accessible
	 * value instead.
	 * 
	 * @return X co-ordinate.
	 */
	public float getX() {

		return x;
	}

	/**
	 * Returns the Y co-ordinate of this rectangle.  Primarily used by
	 * frameworks which expect Java bean methods - use the publicly accessible
	 * value instead.
	 * 
	 * @return Y co-ordinate.
	 */
	public float getY() {

		return y;
	}

	/**
	 * Return the width of this rectangle.  Primarily used by frameworks which
	 * expect Java bean methods - use the publicly accessible value instead.
	 * 
	 * @return Rectangle width.
	 */
	public float getWidth() {

		return width;
	}

	/**
	 * Return the height of this rectangle.  Primarily used by frameworks which
	 * expect Java bean methods - use the publicly accessible value instead.
	 * 
	 * @return Rectangle height.
	 */
	public float getHeight() {

		return height;
	}

	/**
	 * Redefines <tt>hashCode()</tt> so that equal rectangles have the same hash
	 * value.
	 * 
	 * @return Hash value for this rectangle.
	 */
	@Override
	public int hashCode() {

		return (int)(x * y * width * height);
	}

	/**
	 * Set the X co-ordinate.  Primarily used by frameworks which expect Java
	 * bean methods - set the publicly accessible value instead.
	 * 
	 * @param x
	 */
	public void setX(float x) {

		this.x = x;
	}

	/**
	 * Set the Y co-ordinate.  Primarily used by frameworks which expect Java
	 * bean methods - set the publicly accessible value instead.
	 * 
	 * @param y
	 */
	public void setY(float y) {

		this.y = y;
	}

	/**
	 * Set the width of the rectangle.  Primarily used by frameworks which
	 * expect Java bean methods - set the publicly accessible value instead.
	 * 
	 * @param width
	 */
	public void setWidth(float width) {

		this.width = width;
	}

	/**
	 * Set the height of the rectangle.  Primarily used by frameworks which
	 * expect Java bean methods - set the publicly accessible value instead.
	 * 
	 * @param height
	 */
	public void setHeight(float height) {

		this.height = height;
	}

	/**
	 * Returns this rectangle represented as (x,y,width,height).
	 * 
	 * @return Rectangle(x,y,z).
	 */
	@Override
	public String toString() {

		return "Rectangle(" + x + "," + y + "," + width + "," + height + ")";
	}
}
