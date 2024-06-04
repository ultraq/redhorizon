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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.classic.units.Unit
import nz.net.ultraq.redhorizon.classic.units.UnitData
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.primitives.Rectanglef
import spock.lang.Specification

import groovy.json.JsonSlurper
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Tests for the Unit class.
 *
 * @author Emanuel Rabina
 */
class UnitTests extends Specification {

	def "Can attach a unit to a scene"() {
		given:
			// Epic scene mocking 😅
			var scene = new Scene().tap {
				graphicsRequestHandler = Mock(GraphicsRequests) {
					requestCreateOrGet(_) >> { args ->
						return args[0].class == SpriteSheetRequest ?
							Mock(CompletableFuture) {
								thenAcceptAsync(_ as Consumer) >> { Consumer action ->
									return CompletableFuture.runAsync { ->
										action(Mock(SpriteSheet) {
											getAt(0) >> new Rectanglef(0, 0, 1, 1)
										})
									}
								}
							} :
							Mock(CompletableFuture) {
								thenAcceptAsync(_ as Consumer) >> { Consumer action ->
									return CompletableFuture.runAsync { ->
										action(null)
									}
								}
							}
					}
				}
			}
			var spriteStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/explorer/harv.shp'))
			var paletteStream = new BufferedInputStream(getResourceAsStream('nz/net/ultraq/redhorizon/explorer/ra-temperate.pal'))
			var spriteFile = new ShpFile(spriteStream)
			var palette = new PalFile(paletteStream)
			var unitData = getResourceAsStream('nz/net/ultraq/redhorizon/classic/units/data/harv.json').withBufferedReader { reader ->
				return new JsonSlurper().parseText(reader.text) as UnitData
			}
		when:
			var unit = new Unit(spriteFile, unitData)
			scene << unit
		then:
			notThrown(Exception)
		cleanup:
			spriteStream?.close()
			paletteStream?.close()
	}
}
