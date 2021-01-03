/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.utilities.objectviewer.maps

import groovy.transform.TupleConstructor

/**
 * All of the tiles available across the snow and temperate tilesets for Red
 * Alert maps.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor
enum MapRAMapPackTiles {

	// Standard tiles
	DEFAULT    ('Clear1', 0xff),
	WATER      ('W1', 0x01),
	WATER_ANIM ('W2', 0x02),

	// Shore tiles, water on southern edge
	COAST_SS01 ('SH01', 0x03), COAST_SS02 ('SH02', 0x04), COAST_SS03 ('SH03', 0x05),
	COAST_SS04 ('SH04', 0x06), COAST_SS05 ('SH05', 0x07), COAST_SS06 ('SH06', 0x08),
	COAST_SS07 ('SH07', 0x09), COAST_SS08 ('SH08', 0x0a),
	FJORD_SS   ('SH09', 0x0b),
	COAST_SS09 ('SH10', 0x0c), COAST_SS10 ('SH11', 0x0d), COAST_SS11 ('SH12', 0x0e),

	// Shore tiles, water on western edge
	COAST_SW01 ('SH13', 0x0f), COAST_SW02 ('SH14', 0x10), COAST_SW03 ('SH15', 0x11),
	COAST_SW04 ('SH16', 0x12), COAST_SW05 ('SH17', 0x13), COAST_SW06 ('SH18', 0x14),
	FJORD_SW   ('SH19', 0x15),
	COAST_SW07 ('SH20', 0x16), COAST_SW08 ('SH21', 0x17),

	// Shore tiles, water on northern edge
	COAST_SN01 ('SH22', 0x18), COAST_SN02 ('SH23', 0x19), COAST_SN03 ('SH24', 0x1a),
	COAST_SN04 ('SH25', 0x1b), COAST_SN05 ('SH26', 0x1c), COAST_SN06 ('SH27', 0x1d),
	COAST_SN07 ('SH28', 0x1e), COAST_SN08 ('SH29', 0x1f),
	FJORD_SN   ('SH30', 0x20),
	COAST_SN09 ('SH31', 0x21), COAST_SN10 ('SH32', 0x22), COAST_SN11 ('SH33', 0x23),

	// Short tiles, water on eastern edge
	COAST_SE01 ('SH34', 0x24), COAST_SE02 ('SH35', 0x25), COAST_SE03 ('SH36', 0x26),
	COAST_SE04 ('SH37', 0x27), COAST_SE05 ('SH38', 0x28),
	FJORD_SE   ('SH39', 0x29),
	COAST_SE06 ('SH40', 0x2a), COAST_SE07 ('SH41', 0x2b), COAST_SE08 ('SH42', 0x2c),

	// Shore tiles, external corners
	COAST_COR_SW_EXT01 ('SH43', 0x2d), COAST_COR_SW_EXT02 ('SH44', 0x2e),
	COAST_COR_NW_EXT01 ('SH45', 0x2f), COAST_COR_NW_EXT02 ('SH46', 0x30),
	COAST_COR_NE_EXT01 ('SH47', 0x31), COAST_COR_NE_EXT02 ('SH48', 0x32),
	COAST_COR_SE_EXT01 ('SH49', 0x33), COAST_COR_SE_EXT02 ('SH50', 0x34),

	// Shore tiles, internal corners
	COAST_SCOR_SW_INT ('SH51', 0x35),
	COAST_SCOR_NW_INT ('SH52', 0x36),
	COAST_SCOR_NE_INT ('SH53', 0x37),
	COAST_SCOR_SE_INT ('SH54', 0x38),

	// Water debris
	ROCKS_WATER01 ('SH55', 0x39),
	ROCKS_WATER02 ('SH56', 0x3a),

	// Water cliff tiles, water on southern edge
	COAST_CS01 ('WC01', 0x3b), COAST_CS02 ('WC02', 0x3c),
	COAST_CS03 ('WC03', 0x3d), COAST_CS04 ('WC04', 0x3e), COAST_CS05 ('WC05', 0x3f),
	COAST_CS06 ('WC06', 0x40), COAST_CS07 ('WC07', 0x41),

	// Water cliff tiles, water on western edge
	COAST_CW01 ('WC08', 0x42), COAST_CW02 ('WC09', 0x43),
	COAST_CW03 ('WC10', 0x44), COAST_CW04 ('WC11', 0x45), COAST_CW05 ('WC12', 0x46),
	COAST_CW06 ('WC13', 0x47), COAST_CW07 ('WC14', 0x48),

	// Water cliff tiles, water on northern edge
	COAST_CN01 ('WC15', 0x49), COAST_CN02 ('WC16', 0x4a),
	COAST_CN03 ('WC17', 0x4b), COAST_CN04 ('WC18', 0x4c), COAST_CN05 ('WC19', 0x4d),
	COAST_CN06 ('WC20', 0x4e), COAST_CN07 ('WC21', 0x4f),

	// Water cliff tiles, water on eastern edge
	COAST_CE01 ('WC22', 0x50), COAST_CE02 ('WC23', 0x51),
	COAST_CE03 ('WC24', 0x52), COAST_CE04 ('WC25', 0x53), COAST_CE05 ('WC26', 0x54),
	COAST_CE06 ('WC27', 0x55), COAST_CE07 ('WC28', 0x56),

	// Water cliff tiles, external corners
	COAST_CCOR_SW_EXT ('WC29', 0x57),
	COAST_CCOR_NW_EXT ('WC30', 0x58),
	COAST_CCOR_NE_EXT ('WC31', 0x59),
	COAST_CCOR_SE_EXT ('WC32', 0x5a),

	// Water cliff tiles, internal corners
	COAST_CCOR_SW_INT ('WC33', 0x5b),
	COAST_CCOR_NW_INT ('WC34', 0x5c),
	COAST_CCOR_NE_INT ('WC35', 0x5d),
	COAST_CCOR_SE_INT ('WC36', 0x5e),

	// Water cliff tiles, corner joins
	COAST_CJOIN_NWSE ('WC37', 0x5f),
	COAST_CJOIN_NESW ('WC38', 0x60),

	// Rocks
	ROCKS1 ('B1', 0x61),
	ROCKS2 ('B2', 0x62),
	ROCKS3 ('B3', 0x63),

	// Coastal bridges
	COAST_BRIDGE_S1    ('BR1A', 0xeb), COAST_BRIDGE_S2    ('BR1B', 0xec), COAST_BRIDGE_S3    ('BR1C', 0xed),
	COAST_BRIDGE_BIT_S ('BR1X', 0x17c),
	COAST_BRIDGE_N1    ('BR2A', 0xee), COAST_BRIDGE_N2    ('BR2B', 0xef), COAST_BRIDGE_N3    ('BR2C', 0xf0),
	COAST_BRIDGE_BIT_N ('BR2X', 0x17d),

	COAST_BRIDGE_J1 ('BR3A', 0xf1), COAST_BRIDGE_J2 ('BR3B', 0xf2), COAST_BRIDGE_J3 ('BR3C', 0xf3),
	COAST_BRIDGE_J4 ('BR3D', 0xf4), COAST_BRIDGE_J5 ('BR3E', 0xf5), COAST_BRIDGE_J6 ('BR3F', 0xf6),

	COAST_SANDBAR_N  ('F01', 0xf7), COAST_SANDBAR_JV ('F02', 0xf8), COAST_SANDBAR_S  ('F03', 0xf9),
	COAST_SANDBAR_W  ('F04', 0xfa), COAST_SANDBAR_JH ('F05', 0xfb), COAST_SANDBAR_E  ('F06', 0xfc),

	// Various debris
	DEBRIS01 ('P01', 0x67),
	DEBRIS02 ('P02', 0x68),
	DEBRIS03 ('P03', 0x69),
	DEBRIS04 ('P04', 0x6a),
	DEBRIS05 ('P07', 0x6b),
	DEBRIS06 ('P08', 0x6c),
	DEBRIS07 ('P13', 0x6d),
	DEBRIS08 ('P14', 0x6e),

	// River pieces
	RIVER_HOR1   ('Rv01', 0x70),
	RIVER_HOR2   ('Rv02', 0x71),
	RIVER_HOR3   ('Rv03', 0x72),
	RIVER_HOR4   ('Rv04', 0x73),
	RIVER_HOR5   ('Rv14', 0xe5),
	RIVER_VER1   ('Rv05', 0x74),
	RIVER_VER2   ('Rv06', 0x75),
	RIVER_VER3   ('Rv07', 0x76),
	RIVER_VER4   ('Rv15', 0xe6),
	RIVER_COR_NW ('Rv08', 0x77),
	RIVER_COR_NE ('Rv09', 0x78),
	RIVER_COR_SW ('Rv10', 0x79),
	RIVER_COR_SE ('Rv11', 0x7a),
	RIVER_FORK1  ('Rv12', 0x7b),
	RIVER_FORK2  ('Rv13', 0x7c),

	// River crossings/waterfalls
	RIVER_FALL_HOR1       ('Falls1', 0x7d),
	RIVER_FALL_HOR2       ('Falls1a', 0x7e),
	RIVER_FALL_VER1       ('Falls2', 0x7f),
	RIVER_FALL_VER2       ('Falls2a', 0x80),
	RIVER_CROSS_VER       ('Ford1', 0x81),
	RIVER_CROSS_HOR       ('Ford2', 0x82),
	RIVER_BRIDGE_NESW_FIX ('Bridge1', 0x83),
	RIVER_BRIDGE_NESW_HLF ('Bridge1h', 0x17a),
	RIVER_BRIDGE_NESW_BRK ('Bridge1d', 0x84),
	RIVER_BRIDGE_NESW_BIT ('Bridge1x', 0x17e),
	RIVER_BRIDGE_NWSE_FIX ('Bridge2', 0x85),
	RIVER_BRIDGE_NWSE_HLF ('Bridge2h', 0x214),
	RIVER_BRIDGE_NWSE_BRK ('Bridge2d', 0x86),
	RIVER_BRIDGE_NWSE_BIT ('Bridge2x', 0x17f),

	// River caves
	RIVER_CAVE_S ('RC01', 0xe7),
	RIVER_CAVE_W ('RC02', 0xe8),
	RIVER_CAVE_N ('RC03', 0xe9),
	RIVER_CAVE_E ('RC04', 0xea),

	// Cliff pieces, facing south
	CLIFF_SOUTH1 ('S01', 0x87), CLIFF_SOUTH2 ('S02', 0x88),
	CLIFF_SOUTH3 ('S03', 0x89), CLIFF_SOUTH4 ('S04', 0x8a), CLIFF_SOUTH5 ('S05', 0x8b),
	CLIFF_SOUTH6 ('S06', 0x8c), CLIFF_SOUTH7 ('S07', 0x8d),

	// Cliff pieces, facing west
	CLIFF_WEST1 ('S08', 0x8e), CLIFF_WEST2 ('S09', 0x8f),
	CLIFF_WEST3 ('S10', 0x90), CLIFF_WEST4 ('S11', 0x91), CLIFF_WEST5 ('S12', 0x92),
	CLIFF_WEST6 ('S13', 0x93), CLIFF_WEST7 ('S14', 0x94),

	// Cliff pieces, facing north
	CLIFF_NORTH1 ('S15', 0x95), CLIFF_NORTH2 ('S16', 0x96),
	CLIFF_NORTH3 ('S17', 0x97), CLIFF_NORTH4 ('S18', 0x98), CLIFF_NORTH5 ('S19', 0x99),
	CLIFF_NORTH6 ('S20', 0x9a), CLIFF_NORTH7 ('S21', 0x9b),

	// Cliff pieces, facing east
	CLIFF_EAST1 ('S22', 0x9c), CLIFF_EAST2 ('S23', 0x9d),
	CLIFF_EAST3 ('S24', 0x9e), CLIFF_EAST4 ('S25', 0x9f), CLIFF_EAST5 ('S26', 0xa0),
	CLIFF_EAST6 ('S27', 0xa1), CLIFF_EAST7 ('S28', 0xa2),

	// Cliff pieces, external corners
	CLIFF_COR_SW_EXT ('S29', 0xa3),
	CLIFF_COR_NW_EXT ('S30', 0xa4),
	CLIFF_COR_NE_EXT ('S31', 0xa5),
	CLIFF_COR_SE_EXT ('S32', 0xa6),

	// Cliff pieces, internal corners
	CLIFF_COR_SW_INT ('S33', 0xa7),
	CLIFF_COR_NW_INT ('S34', 0xa8),
	CLIFF_COR_NE_INT ('S35', 0xa9),
	CLIFF_COR_SE_INT ('S36', 0xaa),

	// Cliff pieces, corner joins
	CLIFF_JOIN_NWSE ('S37', 0xab),
	CLIFF_JOIN_NESW ('S38', 0xac),

	// Road pieces
	ROAD_END_S ('D01', 0xad), ROAD_END_W ('D02', 0xae),
	ROAD_END_N ('D03', 0xaf), ROAD_END_E ('D04', 0xb0),

	ROAD_VER1 ('D05', 0xb1), ROAD_VER2 ('D06', 0xb2),
	ROAD_VER3 ('D07', 0xb3), ROAD_VER4 ('D08', 0xb4),
	ROAD_HOR1 ('D09', 0xb5), ROAD_HOR2 ('D10', 0xb6),
	ROAD_HOR3 ('D11', 0xb7), ROAD_HOR4 ('D12', 0xb8),
	ROAD_HOR5 ('D13', 0xb9),

	ROAD_FORK1 ('D14', 0xba), ROAD_FORK2 ('D15', 0xbb),
	ROAD_FORK3 ('D16', 0xbc), ROAD_FORK4 ('D17', 0xbd),
	ROAD_FORK5 ('D18', 0xbe), ROAD_FORK6 ('D19', 0xbf),

	ROAD_TURN_NE ('D20', 0xc0), ROAD_TURN_SE ('D21', 0xc1),
	ROAD_TURN_SW ('D22', 0xc2), ROAD_TURN_NW ('D23', 0xc3),

	ROAD_DIAG_NWSE1     ('D24', 0xc4), ROAD_DIAG_NWSE2     ('D25', 0xc5),
	ROAD_DIAG_BIT_NWSE1 ('D26', 0xc6), ROAD_DIAG_BIT_NWSE2 ('D27', 0xc7),
	ROAD_DIAG_TURN_WSE  ('D28', 0xc8), ROAD_DIAG_TURN_NSE  ('D29', 0xc9),
	ROAD_DIAG_FORK_NWSE ('D30', 0xca),
	ROAD_DIAG_TURN_ENW  ('D31', 0xcb), ROAD_DIAG_TURN_SNW  ('D32', 0xcc),
	ROAD_DIAG_FORK_SENW ('D33', 0xcd),

	ROAD_DIAG_NESW1     ('D34', 0xce), ROAD_DIAG_NESW2     ('D35', 0xcf),
	ROAD_DIAG_BIT_NESW1 ('D36', 0xd0), ROAD_DIAG_BIT_NESW2 ('D37', 0xd1),
	ROAD_DIAG_TURN_ESW  ('D38', 0xd2), ROAD_DIAG_TURN_NSW  ('D39', 0xd3),
	ROAD_DIAG_FORK_NESW ('D40', 0xd4),
	ROAD_DIAG_TURN_WNE  ('D41', 0xd5), ROAD_DIAG_TURN_SNE  ('D42', 0xd6),
	ROAD_DIAG_FORK_SWNE ('D43', 0xd7),

	ROAD_BIT_VER ('D44', 0xe3), ROAD_BIT_HOR ('D45', 0xe4),

	// Rock debris
	DEBRIS09 ('RF01', 0xd8), DEBRIS10 ('RF02', 0xd9),
	DEBRIS11 ('RF03', 0xda), DEBRIS12 ('RF04', 0xdb),
	DEBRIS13 ('RF05', 0xdc), DEBRIS14 ('RF06', 0xdd),
	DEBRIS15 ('RF07', 0xde), DEBRIS16 ('RF08', 0xdf),
	DEBRIS17 ('RF09', 0xe0), DEBRIS18 ('RF10', 0xe1),
	DEBRIS19 ('RF11', 0xe2)

	// Tile attributes
	final String name
	final int value
}
