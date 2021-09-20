package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.items.Item.Companion.BURNT_PAGE
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_SEAS
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_TOME
import org.powbot.krulvis.api.extensions.magic.Rune
import org.powbot.krulvis.api.extensions.magic.RunePower
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.requirements.InventoryRequirement
import org.powbot.krulvis.api.utils.requirements.Requirement
import org.powbot.krulvis.api.utils.requirements.SkillRequirement

enum class Staff constructor(
    override val id: Int,
    vararg val runePowers: RunePower,
    val emptyId: Int = 0,
    val chargeRequirements: List<InventoryRequirement> = emptyList()
) :
    EquipmentItem {
    STAFF(1379),
    STAFF_OF_AIR(1381, RunePower.AIR),
    STAFF_OF_WATER(1383, RunePower.WATER),
    STAFF_OF_EARTH(1385, RunePower.EARTH),
    STAFF_OF_FIRE(1387, RunePower.FIRE),
    MAGIC_STAFF(1389),
    BATTLESTAFF(1391) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30),
            SkillRequirement(Constants.SKILLS_MAGIC, 30)
        )
    },
    FIRE_BATTLESTAFF(1393, RunePower.FIRE) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30),
            SkillRequirement(Constants.SKILLS_MAGIC, 30)
        )
    },
    WATER_BATTLESTAFF(1395, RunePower.WATER) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30),
            SkillRequirement(Constants.SKILLS_MAGIC, 30)
        )
    },
    AIR_BATTLESTAFF(1395, RunePower.AIR) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30),
            SkillRequirement(Constants.SKILLS_MAGIC, 30)
        )
    },
    EARTH_BATTLESTAFF(1395, RunePower.EARTH) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30),
            SkillRequirement(Constants.SKILLS_MAGIC, 30)
        )
    },
    MYSTIC_FIRE_STAFF(1401, RunePower.FIRE) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 40),
            SkillRequirement(Constants.SKILLS_MAGIC, 40)
        )
    },
    MYSTIC_WATER_STAFF(1403, RunePower.WATER) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 40),
            SkillRequirement(Constants.SKILLS_MAGIC, 40)
        )
    },
    MYSTIC_AIR_STAFF(1405, RunePower.AIR) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 40),
            SkillRequirement(Constants.SKILLS_MAGIC, 40)
        )
    },
    MYSTIC_EARTH_STAFF(1407, RunePower.EARTH) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 40),
            SkillRequirement(Constants.SKILLS_MAGIC, 40)
        )
    },
    IBANS_STAFF(1409),
    SARADOMIN_STAFF(2415),
    GUTHIX_STAFF(2416),
    ZAMORAK_STAFF(2417),
    LAVA_BATTLESATFF(3053, RunePower.FIRE, RunePower.EARTH) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30)
        )
    },
    MYSTIC_LAVA_STAFF(3054, RunePower.FIRE, RunePower.EARTH) {
        override val requirements: List<Requirement> = listOf(
            SkillRequirement(Constants.SKILLS_ATTACK, 30)
        )
    },
    SLAYER_STAFF(4170),
    ANCIENT_STAFF(4675),
    AHRIMS_STAFF(4675),
    AHRIMS_STAFF_100(4862),
    AHRIMS_STAFF_75(4863),
    AHRIMS_STAFF_50(4864),
    AHRIMS_STAFF_25(4865),
    MUD_BATTLESTAFF(6562, RunePower.WATER, RunePower.EARTH),
    MYSTIC_MUD_SATFF(6563, RunePower.WATER, RunePower.EARTH),
    WHITE_MAGIC_SATFF(6603),
    LUNAR_STAFF(9084),
    STEAM_BATTLESTAFF(11787, RunePower.WATER, RunePower.FIRE),
    MYSTIC_STEAM_STAFF(11789, RunePower.WATER, RunePower.FIRE),
    STAFF_OF_THE_DEAD(11791),
    SMOKE_BATTLESTAFF(11998, RunePower.AIR, RunePower.FIRE),
    MYSTIC_SMOKE_STAFF(12000, RunePower.AIR, RunePower.FIRE),
    DUST_BATTLESTAFF(20736, RunePower.AIR, RunePower.EARTH),

    TRIDENT_OF_THE_SEAS(
        11907, emptyId = EMPTY_SEAS, chargeRequirements = listOf(
            InventoryRequirement(Rune.DEATH, 1),
            InventoryRequirement(Rune.CHAOS, 1),
            InventoryRequirement(Rune.FIRE, 5),
            InventoryRequirement(995, 10)
        )
    ) {
        override val requirements = listOf(SkillRequirement(Constants.SKILLS_MAGIC, 75))
    },

    TOME_OF_FIRE(
        20714, RunePower.FIRE, emptyId = EMPTY_TOME, chargeRequirements = listOf(
            InventoryRequirement(BURNT_PAGE, 1)
        )
    ) {
        override val requirements = listOf(SkillRequirement(Constants.SKILLS_MAGIC, 50))

        override fun getInventoryCharges(): Int {
            return super.getInventoryCharges() * 20
        }
    };

    override val ids: IntArray
        get() = intArrayOf(id)
    override val requirements: List<Requirement>
        get() = emptyList()

    fun withdraw(): Boolean {
        return hasWith() || Bank.withdrawExact(if (Bank.containsOneOf(id)) id else emptyId, 1)
    }

    override fun hasWith(): Boolean = inEquipment() || inInventory()

    fun hasEmpty(): Boolean = hasEmptyEquipped() || hasEmptyInInventory()

    fun hasEmptyEquipped() = Equipment.containsOneOf(emptyId)
    fun hasEmptyInInventory() = Inventory.containsOneOf(emptyId)

    open fun getInventoryCharges(): Int {
        val least = chargeRequirements.minByOrNull { it.item.getInventoryCount() / it.amount }
        return least!!.getCount() / least.amount
    }

    fun checkCharges(): Int {
        if (!hasWith()) {
            return 0
        } else if (Bank.close() && GrandExchange.close()) {
            val tome =
                if (inInventory()) Inventory.stream().id(id).firstOrNull() else Equipment.stream().id(id).firstOrNull()
            if (tome?.interact("Check") == true && waitFor { parseCharges() >= 0 }) {
                return parseCharges()
            }
        }
        return -1
    }

    private fun parseCharges(): Int {
        val tridentCharges = Widgets.widget(193).component(2)
        if (tridentCharges.visible()) {
            val t = tridentCharges.text()
            return t.substring(t.indexOf(":") + 2, t.length - 1).toInt()
        }

        val text =
            Components.stream(162).first { it.componentCount() > 199 }.components().find {
                val text = it.text()
                text.contains("Your weapon has ")
                        || text.contains("Your tome currently holds ")
            }?.text() ?: return -1
        val cutOff = if (text.contains("has ")) "has " else "lds "
        val amountText = text.substring(text.indexOf(cutOff) + 4, text.indexOf(" charges"))
        return if (amountText == "one") 1 else amountText.toInt()
    }


    companion object {

        fun getEquippedStaff(): Staff? {
            val staff = Equipment.itemAt(Equipment.Slot.MAIN_HAND)
            return values().firstOrNull { it.id == staff.id }
        }

        fun getEquippedPowers(): List<RunePower> {
            val staffPowers = getEquippedStaff()?.runePowers?.toMutableList() ?: mutableListOf()
            if (TOME_OF_FIRE.inEquipment()) {
                staffPowers.add(RunePower.FIRE)
            }
            return staffPowers
        }
    }
}