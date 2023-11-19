package org.powbot.krulvis.api.extensions.items

import org.powbot.api.Random
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.api.rt4.Equipment as Equipment1


enum class TeleportItem constructor(
    val itemName: String,
    override vararg val ids: Int,
    override val requirements: List<Requirement> = emptyList()
) : EquipmentItem {
    GLORY("Glory", 11978, 11976, 1712, 1710, 1708, 1706),
    GAMES("Games", 3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867),
    ROD("Duel Ring", 2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566),
    ROW("Wealth Ring", 11980, 11982, 11984, 11986, 11988),
    SKILLS("Skills Necklace", 11968, 11970, 11105, 11107, 11109, 11111),
    WARRIORS("Warriors Bracelet", 11972, 11974, 11118, 11120, 11122, 11124),
    BURNING("Burning Amulet", 21166, 21169, 21171, 21173, 21175),
    COMBAT("Combat Bracelet", 11972, 11974, 11118, 11120, 11122, 11124),
    ARD_CLOAK("Ardougne Cloak", 13121);

    val bestId: Int = ids[0]

    val worseId: Int = ids[ids.size - 1]

    fun isPartOfSet(id: Int): Boolean {
        return ids.contains(id)
    }

    fun getCharges(): Int {
        val item = Equipment1.stream().id(*ids).firstOrNull() ?: return 0
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
        return true
    }

    fun teleport(place: String): Boolean {
        println("$name Teleport to: $place")
        Bank.close()
//        continueLevelUp()
//        ge.close()
        val w = if (Widgets.widget(300).componentCount() >= 91) Widgets.widget(300).component(91) else null
        if (w != null && w.interact("Close")) {
            sleep(Random.nextInt(200, 500))
        }

        if (equip()) {
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
            return Equipment1.stream().id(*ids).firstOrNull()?.interact(teleport) == true
        }
        return false
    }


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

        fun getTeleportItem(id: Int): TeleportItem? {
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


