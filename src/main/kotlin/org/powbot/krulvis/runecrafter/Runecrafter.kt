package org.powbot.krulvis.runecrafter

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafter.tree.branch.ShouldBank
import org.powerbot.script.InventoryChangeEvent
import org.powerbot.script.InventoryChangeListener
import org.powerbot.script.Script

@Script.Manifest(
    name = "krul Runecrafter",
    description = "Runecrafts",
    version = "1.0",
    markdownFileName = "RC.md",
    properties = "category=Runecrafting;",
    mobileReady = true
)
class Runecrafter : ATScript(), InventoryChangeListener {

    var profile = RunecrafterProfile()

    init {
        skillTracker.addSkill(Skill.RUNECRAFTING)
    }

    override val painter: ATPainter<*> = RunecrafterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    override fun startGUI() {
        started = true
    }

    fun hasTalisman(): Boolean {
        return ctx.equipment.toStream().id(profile.type.tiara)
            .isNotEmpty() || ctx.inventory.containsOneOf(profile.type.talisman)
    }

    fun atAltar(): Boolean {
        return ctx.objects.toStream(25).name("Altar", "Portal").isNotEmpty()
    }

    override fun onChange(evt: InventoryChangeEvent) {
        if (evt.itemId == profile.type.rune && evt.quantityChange > 0) {
            lootTracker.addLoot(evt.itemId, evt.quantityChange)
        }
    }
}