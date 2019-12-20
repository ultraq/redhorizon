
// ==============================================
// Scanner's Java - RA OverlayPack implementation
// ==============================================

package redhorizon.game.map;

import redhorizon.engine.scenegraph.SceneNode;
import redhorizon.filemanager.FileManager;
import redhorizon.media.Image;
import redhorizon.misc.geometry.Point2D;
import redhorizon.misc.geometry.Point3D;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.CNCCodec;

import java.nio.ByteBuffer;

/**
 * Red Alert implementation of the {@link OverlayPack} class.  Transforms the
 * <code>[OverlayPack]</code> section of a Red Alert scenario file into the
 * tiles used in Red Alert maps.
 * 
 * @author Emanuel Rabina
 */
public class OverlayPackRA extends OverlayPack {

	/**
	 * Set of available overlay tiles and their matching name and hex values in the
	 * scenario files.
	 */
	private enum OverlayPackTilesRA {

//		DEFAULT ("Blank", 0xff),

		// Walls
		WALL_SANDBAGS    ("SBAG", 0x00, true, false),
		WALL_CHAINLINK   ("CYCL", 0x01, true, false),
		WALL_CONCRETE    ("BRIK", 0x02, true, false),
		WALL_BARBEDWIRE  ("FENC", 0x17, true, false),
		WALL_WOODENFENCE ("WOOD", 0x04, true, false),

		// Ore & Gems
		ORE1  ("GOLD01", 0x05, false, true),
		ORE2  ("GOLD02", 0x06, false, true),
		ORE3  ("GOLD03", 0x07, false, true),
		ORE4  ("GOLD04", 0x08, false, true),
		GEMS1 ("GEM01", 0x09, false, true),
		GEMS2 ("GEM02", 0x0a, false, true),
		GEMS3 ("GEM03", 0x0b, false, true),
		GEMS4 ("GEM04", 0x0c, false, true),

		// Farm fields
		FIELD_HAYSTACKS ("V12", 0x0d, false, false),
		FIELD_HAYSTACK  ("V13", 0x0e, false, false),
		FIELD_WHEAT     ("V14", 0x0f, false, false),
		FIELD_FALLOW    ("V15", 0x10, false, false),
		FIELD_CORN      ("V16", 0x11, false, false),
		FIELD_CELERY    ("V17", 0x12, false, false),
		FIELD_POTATO    ("V18", 0x13, false, false),

		// Crates
		CRATE_WOOD   ("WCRATE", 0x15, false, false),
		CRATE_SILVER ("SCRATE", 0x16, false, false),
		CRATE_WATER  ("WWCRATE", 0x18, false, false),

		// Misc
		FLAGHOLDER ("FPLS", 0x14, false, false);

		// Tile attributes
		public final String name;
		public final byte value;
		public final boolean iswall;
		public final boolean isresource;

		/**
		 * Constructor, initializes each enumerated type.
		 * 
		 * @param name		 The name (and filename) for the map tile.
		 * @param value		 MapPack value for the tile.
		 * @param iswall	 Whether this overlay represents a wall.
		 * @param isresource Whether this overlay represents a harvestable resource.
		 */
		private OverlayPackTilesRA(String name, int value, boolean iswall, boolean isresource) {

			this.name       = name;
			this.value      = (byte)value;
			this.iswall     = iswall;
			this.isresource = isresource;
		}

		/**
		 * Attempts to locate the matching enumerated type given the match
		 * parameter - the value as used in the scenario files.
		 * 
		 * @param match The value of the tile as used in the OverlayPack data.
		 * @return The matching enumerated type.
		 */
		public static OverlayPackTilesRA getMatchingType(byte match) {

			for (OverlayPackTilesRA types: OverlayPackTilesRA.values()) {
				if (types.value == match) {
					return types;
				}
			}
			throw new EnumConstantNotPresentException(OverlayPackTilesRA.class, Short.toString(match));
		}
	}

	/**
	 * Constructor, takes the <code>[OverlayPack]</code> as a set of
	 * <code>String</code> (character) data.  Takes these characters and
	 * converts them into the indices used to indicate what tiles go where.
	 * 
	 * @param map		  Red Alert map this object belongs to.
	 * @param overlaydata Section of the map file containing the character data
	 * 					  to convert to tile indices.
	 */
	OverlayPackRA(MapRA map, java.util.Map<String, String> overlaydata) {

		super(map);
		ByteBuffer overlaybytes = convertToBytes(overlaydata);

		// Get the overlay tiles used in the map
		for (int y = 0; y < 128; y++) {
			for (int x = 0; x < 128; x++) {

				// Get the byte representing the tile
				byte tileval = overlaybytes.get();

				// Create the appropriate tile, skip empty tiles
				if (tileval != (byte)0xff) {
					OverlayPackTilesRA tile = OverlayPackTilesRA.getMatchingType(tileval);
					Point2D xycoords = map.translateMapCoordsXY(x, y);
					OverlayTile overlaytile =
						tile.iswall     ? new OverlayTileRAWall(xycoords.getX(), xycoords.getY(), tile.name):
						tile.isresource ? new OverlayTileRAResource(xycoords.getX(), xycoords.getY(), tile.name):
						                  new OverlayTileRA(xycoords.getX(), xycoords.getY(), tile.name);
					tiles.put(xycoords, overlaytile);
				}
				Thread.yield();
			}
		}

		// Initialize their images
		for (OverlayTile tile: tiles.values()) {
			tile.initImage();
			Point2D tilecoords = map.translateXYCoordsPixel(tile.x, tile.y);
			SceneNode tilenode = node.attachChildObject(tile);
			tilenode.setPosition(new Point3D(tilecoords.getX(), tilecoords.getY(), 0));

			Thread.yield();
		}
	}

	/**
	 * Converts the given <code>String</code> data into the byte data required
	 * by the mappack section.  Takes care of the 16-bit Java char -> 8-bit
	 * conversion, the base-64 encoding, and the format80 compression.
	 * 
	 * @param overlaydata Section of the map file containing the character data
	 * 					  representing map tiles.
	 * @return The converted map data.
	 */
	private static ByteBuffer convertToBytes(java.util.Map<String, String> overlaydata) {

		// Turn the section into 8-bit chars
		int overlaylength = ((overlaydata.size() - 1) * 70) + overlaydata.get(Integer.toString(overlaydata.size() - 1)).length();
		ByteBuffer sourcebytes = BufferUtility.newByteBuffer(overlaylength);

		for (int i = 1; i <= overlaydata.size(); i++) {
			String overlayline = overlaydata.get(Integer.toString(i));

			for (int j = 0; j < overlayline.length(); j++) {
				sourcebytes.put((byte)overlayline.charAt(j));
			}
		}
		sourcebytes.rewind();

		// Decode OverlayPack
		ByteBuffer overlaybytes = BufferUtility.newByteBuffer(16384);
		CNCCodec.decodeOverlayPack(sourcebytes, overlaybytes);

		return overlaybytes;
	}

	/**
	 * Red Alert specific implementation of the overlay tile.
	 */
	private class OverlayTileRA extends OverlayTile {

		/**
		 * Constructor, sets-up this tile's attributes and image.
		 * 
		 * @param x			 X co-ordinate of this tile.
		 * @param y			 Y co-ordinate of this tile.
		 * @param imagename	 The name of the file containing this overlay item.
		 */
		private OverlayTileRA(int x, int y, String imagename) {

			super(x, y, imagename);
		}

		/**
		 * @inheritDoc
		 */
		protected void initImage() {

			overlayimage = new Image(FileManager.getImageFile(imagename + map.theater.ext));
		}
	}

	/**
	 * Red Alert specific implementation of a resource-type overlay tile.
	 */
	private class OverlayTileRAResource extends OverlayTile {

		/**
		 * Constructor, sets-up this tile's attributes and image.
		 * 
		 * @param x			 X co-ordinate of this tile.
		 * @param y			 Y co-ordinate of this tile.
		 * @param imagename	 The name of the file containing this overlay item.
		 */
		private OverlayTileRAResource(int x, int y, String imagename) {

			super(x, y, imagename);
		}

		/**
		 * @inheritDoc
		 */
		protected void initImage() {

			// Pick the appropriate density of an ore or gem tile
			int densityfactor = imagename.startsWith("GEM") ? 3 : 12;
			overlayimage = new Image(FileManager.getImagesFile(imagename + map.theater.ext), densityfactor - 1);
		}
	}

	/**
	 * Red Alert specific implementation of a wall-type overlay tile.
	 */
	private class OverlayTileRAWall extends OverlayTile {

		/**
		 * Constructor, sets-up this tile's attributes and image.
		 * 
		 * @param x			 X co-ordinate of this tile.
		 * @param y			 Y co-ordinate of this tile.
		 * @param imagename	 The name of the file containing this overlay item.
		 */
		private OverlayTileRAWall(int x, int y, String imagename) {

			super(x, y, imagename);
		}

		/**
		 * @inheritDoc
		 */
		protected void initImage() {

			// Select the proper orientation for wall tiles
			byte connectionval = 0x00;

			if (tiles.get(new Point2D(x, y + 1)) instanceof OverlayTileRAWall) {
				connectionval |= 0x01;
			}
			if (tiles.get(new Point2D(x + 1, y)) instanceof OverlayTileRAWall) {
				connectionval |= 0x02;
			}
			if (tiles.get(new Point2D(x, y - 1)) instanceof OverlayTileRAWall) {
				connectionval |= 0x04;
			}
			if (tiles.get(new Point2D(x - 1, y)) instanceof OverlayTileRAWall) {
				connectionval |= 0x08;
			}

			overlayimage = new Image(FileManager.getImagesFile(imagename), connectionval);
		}
	}
}
