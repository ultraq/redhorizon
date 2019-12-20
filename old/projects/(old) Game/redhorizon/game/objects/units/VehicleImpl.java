
// ========================================
// Scanner's Java - Basic vehicle unit type
// ========================================

package redhorizon.game.objects.units;

import redhorizon.filemanager.FileManager;
import redhorizon.filetypes.ImagesFile;
import redhorizon.game.faction.SubFaction;
import redhorizon.media.Image;
import redhorizon.xml.units.XMLIncludedAnim;
import redhorizon.xml.units.XMLUnitAnimation;
import redhorizon.xml.units.XMLVehicle;

/**
 * Representation of a vehicle unit type.
 * 
 * @author Emanuel Rabina
 */
public class VehicleImpl extends UnitImpl<Vehicle> {

	final Image[][] defaultanims;
//	final Image[][] attackanims;
//	final int       imgsperrot;

	/**
	 * Constructor, creates a bare-bones land-based unit.
	 * 
	 * @param subfaction Subfaction this vehicle belongs to.
	 * @param specs		 Specification of the vehicle from the XML.
	 */
	public VehicleImpl(SubFaction subfaction, XMLVehicle specs) {

		super(subfaction, specs);

		// Vehicle animations
		XMLUnitAnimation unitanim = specs.getAnimation();
		ImagesFile imagesfile = FileManager.getImagesFile(unitanim.getFilename());

		// Default (stationary/movement) animations
		XMLIncludedAnim defaultanim = unitanim.getDefaultAnim();
		defaultanims = new Image[defaultanim.getAngles()][defaultanim.getFramesPerAngle()];
		for (int i = 0; i < defaultanims.length; i++) {
			for (int j = 0; j < defaultanims[i].length; j++) {
				defaultanims[i][j] = new Image(imagesfile, (i * defaultanims[i].length) + j);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public Vehicle createInstance() {

		return new Vehicle(this);
	}
}
