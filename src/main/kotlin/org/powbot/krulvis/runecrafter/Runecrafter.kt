package org.powbot.krulvis.runecrafter

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.runecrafter.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Runecrafter",
    description = "Runecrafts",
    author = "Krulvis",
    version = "1.0.1",
    markdownFileName = "RC.md",
    category = ScriptCategory.Runecrafting
)
class Runecrafter : ATScript() {

    var profile = RunecrafterProfile()

    override fun createPainter(): ATPaint<*> = RunecrafterPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBank(this)


    fun hasTalisman(): Boolean {
        return Equipment.containsOneOf(profile.type.tiara) || Inventory.containsOneOf(profile.type.talisman)
    }

    fun atAltar(): Boolean {
        return Objects.stream(25).name("Altar", "Portal").isNotEmpty()
    }
}

fun main() {
    Runecrafter().startScript()
}