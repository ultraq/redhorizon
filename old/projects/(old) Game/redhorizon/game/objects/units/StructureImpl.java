
// ==========================================
// Scanner's Java - Common structure template
// ==========================================

package redhorizon.game.objects.units;

import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.ImagesFile;
import redhorizon.game.faction.SubFaction;
import redhorizon.media.Image;
import redhorizon.xml.media.XMLAnimation;
import redhorizon.xml.units.XMLIncludedAnim;
import redhorizon.xml.units.XMLStructure;
import redhorizon.xml.units.XMLStructureAnimation;

import java.util.List;

/**
 * Blueprint from which all other structure instances will be drawn from.
 * 
 * @author Emanuel Rabina
 */
public class StructureImpl extends UnitImpl<Structure> {

	final Image[][][] defaultanims;
//	final HashMap<XMLSpecialAnims,Image[][]> specialanims;
	final Image[] buildanim;

	/**
	 * Constructor, creates an implementation of the given name and mapping of
	 * structure specifications to their values.
	 * 
	 * @param subfaction Side this structure belongs to.
	 * @param specs		 Structure specifications from the XML.
	 */
	public StructureImpl(SubFaction subfaction, XMLStructure specs) {

		super(subfaction, specs);

		// Standard structure animation/s
		List<XMLStructureAnimation> bldganims = specs.getAnimation();
		defaultanims = new Image[bldganims.size()][][];

		for (int i = 0; i < defaultanims.length; i++) {
			XMLStructureAnimation bldganim = bldganims.get(i);
			ImagesFile imagesfile = FileManager.getImagesFile(bldganim.getFilename());

			XMLIncludedAnim defaultanim = bldganim.getDefaultAnim();
			defaultanims[i] = new Image[defaultanim.getAngles()][defaultanim.getFramesPerAngle()];

			for (int j = 0; j < defaultanims[i].length; j++) {
				for (int k = 0; k < defaultanims[i][j].length; k++) {
					defaultanims[i][j][k] = new Image(imagesfile, (j * defaultanims[j].length) + k);
				}
			}
		}

		// TODO: Special structure animations

		// Structure build animation
		XMLAnimation buildanimation = specs.getBuildAnimation();
		if (buildanimation != null) {
			ImagesFile imagesfile = FileManager.getImagesFile(buildanimation.getFilename());
			buildanim = new Image[imagesfile.numImages()];
			for (int i = 0; i < buildanim.length; i++) {
				buildanim[i] = new Image(imagesfile, i);
			}
		}
		else {
			buildanim = null;
		}
	}

	/**
	 * Builds a new Structure instance based off this blueprint.
	 * 
	 * @return New structure instance.
	 */
	public Structure createInstance() {

		return new Structure(this);
	}
}
