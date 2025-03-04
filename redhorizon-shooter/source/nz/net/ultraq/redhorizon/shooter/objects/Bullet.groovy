/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.shooter.objects

import nz.net.ultraq.redhorizon.classic.nodes.PalettedSprite
import nz.net.ultraq.redhorizon.classic.nodes.Rotatable
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import org.joml.Vector2f

/**
 * A projectile fired by the player unit.
 *
 * @author Emanuel Rabina
 */
class Bullet extends PalettedSprite implements Rotatable {

	private static final float MOVEMENT_SPEED = 100

	// TODO: Load once, create instances

	private final Vector2f velocity // TODO: Velocity = ECS component candidate
	private final Vector2f movement = new Vector2f()

	/**
	 * Constructor, load the sprite for the bullet.
	 */
	Bullet(ImagesFile imagesFile, float heading) {

		super(imagesFile)
		bounds { ->
			setMax(imagesFile.width, imagesFile.height)
		}

		var headings = 32 // "dragon" sprite has 32 headings
		var degreesPerHeading = 360f / headings

		// NOTE: C&C unit headings were ordered in a counter-clockwise order
		//       (maybe to match how radians work?), the reverse from how
		//       degrees-based headings are done.
		var closestHeading = Math.round(heading / degreesPerHeading)
		frame = closestHeading ? headings - closestHeading as int : 0

		var headingInRadians = Math.toRadians(heading)
		velocity = new Vector2f((float)Math.sin(headingInRadians), (float)Math.cos(headingInRadians)).normalize()
	}

	@Override
	void update(float delta) {

		movement.set(velocity).normalize().mul(MOVEMENT_SPEED).mul(delta).add(position.x(), position.y())
		setPosition(movement.x, movement.y)

		super.update(delta)
	}
}
