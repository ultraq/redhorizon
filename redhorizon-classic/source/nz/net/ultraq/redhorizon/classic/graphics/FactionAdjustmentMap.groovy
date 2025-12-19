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

package nz.net.ultraq.redhorizon.classic.graphics

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.graphics.Texture
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture

import java.nio.ByteBuffer

/**
 * A texture used to swap index values to adjust the output colour from a
 * palette.  Used for things like remapping the 'gold' value in sprites to a
 * faction-specific colour, or for palette-swapping animations like water and
 * glowing lights.
 *
 * @author Emanuel Rabina
 */
class FactionAdjustmentMap implements AutoCloseable {

	private Faction faction
	private boolean factionChanged
	private final ByteBuffer buffer
	final Texture texture

	/**
	 * Constructor, builds an adjustment map for the given faction.
	 */
	FactionAdjustmentMap(Faction faction) {

		this.faction = faction
		buffer = ByteBuffer.allocateNative(256)
		256.times { i ->
			if (i in 80..95) {
				buffer.put(faction.colours[i - 80] as byte)
			}
			else {
				buffer.put(i as byte)
			}
		}
		buffer.flip()
		texture = new OpenGLTexture(256, 1, 1, buffer)
	}

	@Override
	void close() {

		texture?.close()
	}

	/**
	 * Set the faction to use.  This will cause the adjustment map to be updated
	 * with the next call to {@link #update()}.
	 */
	void setFaction(Faction faction) {

		this.faction = faction
		factionChanged = true
	}

	/**
	 * Update the if needed, eg: for faction changes.
	 */
	void update() {

		if (factionChanged) {
			(80..95).each { i ->
				buffer.put(i, faction.colours[i - 80] as byte)
			}
			texture.update(buffer)
			factionChanged = false
		}
	}
}
