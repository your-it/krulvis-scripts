package org.powbot.krulvis.mixology

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.krulvis.mixology.Data.Ingredient.*
import org.powbot.krulvis.mixology.Data.Ingredient.Companion.getStoredIngredients
import org.powbot.krulvis.mixology.Data.Ingredient.Companion.paired
import org.slf4j.LoggerFactory
import kotlin.math.max

//54914, 54915, 54916
//Mox 54907, 54910, 54913
//Lye 54906, 54909, 54912
//Aga 54905, 54908, 54911

object Data {
	const val FALLBACK_OPTION = "Fallback Potion"
	const val MAMMOTH_MIGHT_MIX = "Mammoth might mix"
	const val MYSTIC_MANA_AMALGAM = "Mystic mana amalgam"
	const val MARLEYS_MOONLIGHT = "Marleys moonlight"
	const val AZURE_AURA_MIX = "Azure aura mix"
	const val ALCO_AUGMENTOR = "Alco augmentator"
	const val MIXALOT = "Mixalot"
	const val AQUALUX_AMALGAM = "Aqualux amalgam"
	const val MEGALITE_LIQUID = "Megalite liquid"
	const val ANTI_LEECH_LOTION = "Anti leech lotion"
	const val LIPLACK_LIQUOR = "Lplack liquor"


	const val VARBIT_POTION_ORDER_1 = 11315
	const val VARBIT_MODIFIER_1 = 11316
	const val VARBIT_POTION_ORDER_2 = 11317
	const val VARBIT_MODIFIER_2 = 11318
	const val VARBIT_POTION_ORDER_3 = 11319
	const val VARBIT_MODIFIER_3 = 11320

	const val VARBIT_MOX_STOCK = 11321
	const val VARBIT_AGA_STOCK = 11322
	const val VARBIT_LYE_STOCK = 11323


	private val logger = LoggerFactory.getLogger(javaClass.simpleName)
	val LAB_AREA = Area(Tile(1387, 9319, 0), Tile(1401, 9333, 0))
	const val START_ID = 54905

	const val ROOT_ID = 882
	const val CHILD_ID = 2

	fun components() = Widgets.component(ROOT_ID, CHILD_ID).components()

	val mixerTiles = arrayOf(
		Tile(1393, 9323, 0),
		Tile(1394, 9323, 0),
		Tile(1395, 9323, 0),
	)

	enum class Ingredient(val textureId: Int) {
		Aga(5667),
		Lye(5668),
		Mox(5666)
		;

		fun getLever() = Objects.stream().name("$name lever").first()

		fun getInventoryCount() = Inventory.stream().name("$name paste").count(true)

		companion object {

			fun getActiveIngredients(): List<Ingredient> {
				val values = values()
				val mixers = mixerTiles.map { Objects.stream(it, GameObject.Type.INTERACTIVE).name("Mixer").first().id }
				val mappedIds = mixers.mapIndexed { index, id -> id - START_ID - index * 3 }
				return mappedIds.mapNotNull { if (it in 0..values.size) values[it] else null }
			}

			fun List<Ingredient>.paired(): Map<Ingredient, Int> =
				values().associateWith { ing -> this.count { it == ing } }

			fun getStoredIngredients(): Map<Ingredient, Int> {
				return mapOf(
					Mox to Varpbits.value(VARBIT_MOX_STOCK),
					Aga to Varpbits.value(VARBIT_AGA_STOCK),
					Lye to Varpbits.value(VARBIT_LYE_STOCK)
				)
			}

//			fun getStoredIngredients(): Map<Ingredient, Int> {
//				val comps = components()
//				if (comps.size <= 10) return mapOf(Mox to 0, Aga to 0, Lye to 0)
//				logger.info("comps=${comps.joinToString { "(${it.index()}: textureId=${it.textureId()}, text=${it.text()})" }}")
//				val map =
//					Ingredient.values()
//						.map { it to (comps.firstOrNull { comp -> comp.textureId() == it.textureId }?.index() ?: -1) }
//				return map.associate {
//					it.first to (comps.firstOrNull { c -> c.index() == it.second + 1 }?.text()?.toIntOrNull() ?: 0)
//				}
//			}
		}
	}

	enum class Modifier(
		val textureId: Int,
		val machineName: String,
		val machineTile: Tile,
	) {
		Homogenise(5674, "Agitator", Tile(1394, 9329, 0)),
		Concentrate(5672, "Retort", Tile(1397, 9326, 0)),
		Crystalise(5673, "Alembic", Tile(1391, 9326, 0)),
		;

		val action = "$name-potion"

		fun isActive() = Npcs.stream().at(machineTile).any { it.id > 1 }
		fun getMachine() = Objects.stream(20, GameObject.Type.INTERACTIVE).name(machineName).action(action).first()

		companion object {
			fun forTexture(textureId: Int) = values().firstOrNull { textureId == it.textureId } ?: Concentrate
		}
	}


	enum class MixPotion(val level: Int, val xp: Int, val finishedId: Int, val ingredients: List<Ingredient>) {
		Mammoth_might_mix(60, 135, 30021, listOf(Mox, Mox, Mox)),
		Mystic_mana_amalgam(60, 175, 30022, listOf(Mox, Mox, Aga)),
		Marleys_moonlight(60, 215, 30023, listOf(Mox, Mox, Lye)),
		Alco_augmentator(76, 255, 30024, listOf(Aga, Aga, Aga)),
		Azure_aura_mix(68, 215, 30026, listOf(Aga, Aga, Mox)),
		Aqualux_amalgam(72, 295, 30025, listOf(Aga, Lye, Aga)),
		Liplack_liquor(86, 335, 30027, listOf(Lye, Lye, Lye)),
		Megalite_liquid(80, 295, 30029, listOf(Lye, Lye, Mox)),
		Anti_leech_lotion(84, 335, 30028, listOf(Lye, Lye, Aga)),
		Mixalot(64, 255, 30030, listOf(Mox, Aga, Lye)),
		;

		val cleanName = name.cleanName()

		val ingredientsPaired = ingredients.paired()

		val hasLevel by lazy { Skills.realLevel(Skill.Herblore) >= level }

		fun hasFinishedPotion() = Inventory.stream().id(finishedId).isNotEmpty()
		fun hasUnfinished() = Inventory.stream()
			.any { it.name().cleanName().equals(cleanName, true) }

		fun hasStoredIngredients(): Boolean {
			val total = getStoredIngredients()
			return ingredientsPaired.all { total[it.key]!! >= it.value }
		}

		companion object {
			fun getOrders(): List<Pair<MixPotion, Modifier>> {
				val order1 = max(0, Varpbits.value(VARBIT_POTION_ORDER_1) - 1)
				val order2 = max(0, Varpbits.value(VARBIT_POTION_ORDER_2) - 1)
				val order3 = max(0, Varpbits.value(VARBIT_POTION_ORDER_3) - 1)
				val modifier1 = max(0, Varpbits.value(VARBIT_MODIFIER_1) - 1)
				val modifier2 = max(0, Varpbits.value(VARBIT_MODIFIER_2) - 1)
				val modifier3 = max(0, Varpbits.value(VARBIT_MODIFIER_3) - 1)
				return listOf(
					Pair(MixPotion.values()[order1], Modifier.values()[modifier1]),
					Pair(MixPotion.values()[order2], Modifier.values()[modifier2]),
					Pair(MixPotion.values()[order3], Modifier.values()[modifier3]),
				)
			}

			fun forName(name: String): MixPotion? =
				values().find { it.cleanName.equals(name.cleanName(), true) }
		}

	}

	val STRANGE_CHARACTER_REGEX = Regex("[_\\-' ]")
	fun String.cleanName() = this.replace(STRANGE_CHARACTER_REGEX, "")
}