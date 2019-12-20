
// ======================================================
// Scanner's Java - Parent instance structure abstraction
// ======================================================

package redhorizon.game.objects.units;

import redhorizon.media.Image;

import javax.media.opengl.GL;

/**
 * Top-level abstraction of an instance of buildings in Red Horizon.  Contains
 * parts common to all specific structure types.
 * 
 * @author Emanuel Rabina
 */
public class Structure extends Unit<StructureImpl> {

	private int todraw;

	/**
	 * Constructor, links an instance to it's implementation.
	 * 
	 * @param impl <code>StructureImpl</code> that this instance stems from.
	 */
	Structure(StructureImpl impl) {

		super(impl);
	}

	/**
	 * @inheritDoc
	 */
	public void delete(GL gl) {

		for (Image[][] defaultanimn: impl.defaultanims) {
			for (Image[] defaultanima: defaultanimn) {
				for (Image defaultanim: defaultanima) {
					defaultanim.delete(gl);
				}
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {

		for (Image[][] defaultanimn: impl.defaultanims) {
			for (Image[] defaultanima: defaultanimn) {
				for (Image defaultanim: defaultanima) {
					defaultanim.init(gl);
				}
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void render(GL gl) {

		for (Image[][] defaultanimn: impl.defaultanims) {
			defaultanimn[Math.round(heading) % impl.defaultanims.length][todraw].render(gl);
		}
		if (selected) {
			status.render(gl);
		}
	}
}
