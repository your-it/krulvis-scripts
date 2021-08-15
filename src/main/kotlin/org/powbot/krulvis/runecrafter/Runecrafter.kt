package org.powbot.krulvis.runecrafter

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafter.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Runecrafter",
    description = "Runecrafts",
    version = "1.0",
    markdownFileName = "RC.md",
    category = ScriptCategory.Runecrafting
)
class Runecrafter : ATScript() {

    var profile = RunecrafterProfile()

    init {
        skillTracker.addSkill(Skill.RUNECRAFTING)
    }

    override val rootComponent: TreeComponent<*> = ShouldBank(this)


    fun hasTalisman(): Boolean {
        return Equipment.stream().id(profile.type.tiara)
            .isNotEmpty() || Inventory.containsOneOf(profile.type.talisman)
    }

    fun atAltar(): Boolean {
        return Objects.stream(25).name("Altar", "Portal").isNotEmpty()
    }

//    override fun onChange(evt: InventoryChangeEvent) {
//        if (evt.itemId == profile.type.rune && evt.quantityChange > 0) {
//            lootTracker.addLoot(evt.itemId, evt.quantityChange)
//        }
//    }
}