package org.powbot.krulvis.blastfurnace

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.blastfurnace.tree.branch.ShouldPay

@ScriptManifest(
    name = "krul BlastFurnace",
    description = "Smelts bars at Blast Furnace",
    author = "Krulvis",
    version = "1.1.4",
    markdownFileName = "BF.md",
    category = ScriptCategory.Smithing
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Bar",
            "Which bar do you want to smelt?",
            allowedValues = ["IRON", "STEEL", "MITHRIL", "ADAMANTITE", "RUNITE", "GOLD"],
            defaultValue = "GOLD"
        ),
        ScriptConfiguration(
            "Coffer deposit",
            "How much do you want to put in the coffer?",
            defaultValue = "12500",
            optionType = OptionType.INTEGER
        ),
        ScriptConfiguration(
            "Drink potions",
            "Do you want do drink energy potions?",
            defaultValue = "false",
            optionType = OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            "Potion",
            "Select a potion to drink",
            defaultValue = "SUPER_ENERGY",
            optionType = OptionType.STRING,
            allowedValues = ["ENERGY", "SUPER_ENERGY", "STAMINA"],
            visible = false
        )
    ]
)
class BlastFurnace : ATScript() {
    var filledCoalBag: Boolean = false

    val drinkPotion by lazy { getOption<Boolean>("Drink potions")!! }
    val potion by lazy { Potion.valueOf(getOption<String>("Potion")!!) }
    val cofferAmount by lazy { getOption<Int>("Coffer deposit")!! }
    val bar by lazy { Bar.valueOf(getOption<String>("Bar")!!) }
    override fun createPainter(): ATPaint<*> = BFPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldPay(this)

    var waitForBars = false
    val dispenserTile = Tile(1940, 4963, 0)

    fun interact(matrix: TileMatrix, action: String): Boolean {
        if (!Menu.opened()) {
            matrix.click()
        }
        if (Utils.waitFor(Utils.short()) { Menu.opened() }) {
            if (rightMenuOpen(action)) {
                return Menu.click(Menu.filter(action))
            } else {
                Menu.click(Menu.filter("Cancel"))
            }
        }
        return false
    }

    private fun rightMenuOpen(action: String): Boolean =
        Menu.opened() && Menu.contains { it.action.equals(action, true) }

    val foremanTimer = Timer(1)

    fun shouldPayForeman() = Skills.level(Constants.SKILLS_SMITHING) < 60 && foremanTimer.isFinished()

    fun cofferCount() = Varpbits.varpbit(795) / 2

    @ValueChanged("Drink potions")
    fun valueChange(drinkPotions: Boolean) {
        updateVisibility("Potion", drinkPotions)
    }

}

fun main() {
    BlastFurnace().startScript("127.0.0.1", "krullieman", false)
}