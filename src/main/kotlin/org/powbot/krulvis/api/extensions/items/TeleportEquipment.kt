package org.powbot.krulvis.api.extensions.items

import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TeleportItem")

interface ITeleportItem : Item {
	fun teleport(destination: String): Boolean
}

enum class TeleportItem(val itemName: String, override val ids: IntArray) : ITeleportItem {
	ROYAL_SEED_POD("Royal seed pod", intArrayOf(19564)),
	;

	override val stackable: Boolean = false

	override fun hasWith(): Boolean = inInventory()

	override fun getCount(countNoted: Boolean): Int = getInventoryCount(false)
	override fun teleport(destination: String): Boolean {
		logger.info("$name Teleport to: $destination")
		Bank.close()
		val w = if (Widgets.widget(300).componentCount() >= 91) Widgets.widget(300).component(91) else null
		if (w != null && w.interact("Close")) {
			sleep(Random.nextInt(200, 500))
		}
		return Inventory.stream().id(*ids).firstOrNull()?.interact(destination) == true
	}
}

enum class TeleportEquipment(
	val itemName: String,
	override val slot: Equipment.Slot,
	override vararg val ids: Int,
) : IEquipmentItem, ITeleportItem {
	GLORY("Glory", Equipment.Slot.NECK, 11978, 11976, 1712, 1710, 1708, 1706),
	GAMES("Games", Equipment.Slot.NECK, 3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867),
	ROD("Duel Ring", Equipment.Slot.RING, 2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566),
	ROW("Wealth Ring", Equipment.Slot.RING, 11980, 11982, 11984, 11986, 11988),
	SKILLS("Skills Necklace", Equipment.Slot.NECK, 11968, 11970, 11105, 11107, 11109, 11111),
	WARRIORS("Warriors Bracelet", Equipment.Slot.HANDS, 11972, 11974, 11118, 11120, 11122, 11124),
	BURNING("Burning Amulet", Equipment.Slot.NECK, 21166, 21169, 21171, 21173, 21175),
	COMBAT("Combat Bracelet", Equipment.Slot.HANDS, 11972, 11974, 11118, 11120, 11122, 11124),
	SLAYER("Slayer ring", Equipment.Slot.RING, 21268, 11866, 11867, 11868, 11869, 11870, 11871, 11872, 11873),
	ARD_CLOAK("Ardougne Cloak", Equipment.Slot.CAPE, 13121);

	val bestId: Int = ids[0]

	val worseId: Int = ids[ids.size - 1]

	override val stackable: Boolean = false
	fun isPartOfSet(id: Int): Boolean {
		return ids.contains(id)
	}

	fun getCharges(): Int {
		val item = Equipment.stream().id(*ids).firstOrNull() ?: return 0
		for (i in ids.indices.reversed()) {
			if (ids[i] == item.id) {
				return ids.size - i
			}
		}
		return -1
	}

	fun getNotedCharges(): Int {
		var tps = 0
		ids.reversed().forEachIndexed { i, id ->
			tps += Inventory.getCount(true, id, id + 1) * (i + 1)
		}
		return tps
	}

	fun canTeleport(): Boolean {
		return when (this) {
			GLORY, ROW -> Combat.wildernessLevel() <= 30
			else -> Combat.wildernessLevel() <= 20
		}
	}

	override fun teleport(place: String): Boolean {
		logger.info("$name Teleport to: $place")
		Bank.close()
		val w = if (Widgets.widget(300).componentCount() >= 91) Widgets.widget(300).component(91) else null
		if (w != null && w.interact("Close")) {
			sleep(Random.nextInt(200, 500))
		}

		if (equip()) {
			if (this == SLAYER) {
				return teleportSlayerRing(place)
			}
			var teleport = place
			when (place.lowercase()) {
				"ferox", "enclave" -> teleport = "Ferox Enclave"
				"barb", "barbarian" -> teleport = "Barbarian Outpost"
				"corp" -> teleport = "Corporeal Beast"
				"ge" -> teleport = "Grand Exchange"
				"duel" -> teleport = "Duel Arena"
				"castle", "cw" -> teleport = "Castle Wars"
				"clan" -> teleport = "Clan Wars"
				"edge" -> teleport = "Edgeville"
				"draynor" -> teleport = "Draynor"
				"monastery" -> teleport = "Monastery"
				"crafting" -> teleport = "Crafting Guild"
				"warrior" -> teleport = "Warriors' Guild"
				"champion" -> teleport = "Champions' Guild"
				"ranging" -> teleport = "Ranging Guild"
				"motherlode" -> teleport = "Motherlode Mine"
				"fishing" -> teleport = "Fishing Guild"
				"cooking" -> teleport = "Cooking Guild"
			}
			return Equipment.stream().id(*ids).firstOrNull()?.interact(teleport) == true
		}
		return false
	}


	private fun teleportSlayerRing(location: String): Boolean {
		var teleportWidget = slayerRingTeleportWidget(location)
		logger.info("SlayerRing Teleport to location=$location, componentVisible=${teleportWidget.visible()}")
		if (!teleportWidget.visible()) {
			if (Equipment.stream().id(*ids).first().interact("Teleport")) {
				waitFor {
					teleportWidget = slayerRingTeleportWidget(location)
					teleportWidget.visible()
				}
			}
		}

		if (teleportWidget.valid() && teleportWidget.visible()) {
			return teleportWidget.click()
		}
		return false
	}

	private fun slayerRingTeleportWidget(location: String) = Components.stream(219, 1).textContains(location).first()


	fun withdraw(): Boolean {
		if (!inEquipment() && !inInventory()) {
			if (Bank.withdrawModeNoted(false) && inBank()
				&& Bank.withdraw(1, getBankId(true))
			) {
				waitFor(5000) { inInventory() }
			}
		}
		return inInventory() || inEquipment()
	}


	companion object {
		val GLORY_EMPTY = 1704
		val ROW_EMPTY = 2572
		val COMBAT_EMPTY = 11126

		fun isTeleportItem(id: Int): Boolean {
			for (ti in values()) {
				for (i in ti.ids) {
					if (i == id) {
						return true
					}
				}
			}
			return false
		}

		fun getTeleportItem(id: Int): TeleportEquipment? {
			for (ti in values()) {
				for (i in ti.ids) {
					if (i == id) {
						return ti
					}
				}
			}
			return null
		}
	}
}


