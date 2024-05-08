package org.powbot.krulvis.runecrafting

import org.powbot.api.InteractableEntity
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.runecrafting.tree.branches.ShouldRepair
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
        name = "krul Runecrafter", version = "1.0.3",
        description = "Supports Astral & ZMI/Ourania, Repairs pouches, restores energy",
        scriptId = "329bdd0e-3813-4c39-917b-d943e79a0f47",
        markdownFileName = "Runecrafter.md",
        category = ScriptCategory.Runecrafting
)
@ScriptConfiguration.List([
    ScriptConfiguration(name = RUNE_ALTAR_CONFIGURATION, description = "Which rune to make?", optionType = OptionType.STRING, allowedValues = arrayOf(ASTRAL, ZMI), defaultValue = ASTRAL),
    ScriptConfiguration(name = VILE_VIGOUR_CONFIG, description = "Cast vile vigour?", optionType = OptionType.BOOLEAN, defaultValue = "false", visible = false),
    ScriptConfiguration(name = ZMI_PAYMENT_RUNE_CONFIG, description = "Payment rune?", optionType = OptionType.STRING, allowedValues = arrayOf(AIR, WATER, EARTH, FIRE), defaultValue = EARTH, visible = false),
    ScriptConfiguration(name = ESSENCE_TYPE_CONFIGURATION, description = "Which essence to use?", optionType = OptionType.STRING, allowedValues = arrayOf(RUNE_ESSENCE, PURE_ESSENCE, DAEYALT_ESSENCE), defaultValue = DAEYALT_ESSENCE),
    ScriptConfiguration(name = FOOD_CONFIGURATION, description = "Which food to use?", optionType = OptionType.STRING, allowedValues = arrayOf(SALMON, TUNA, LOBSTER, BASS, KARAMBWAN), defaultValue = BASS)
])
class Runecrafter : ATScript() {

    val altar by lazy { RuneAltar.valueOf(getOption(RUNE_ALTAR_CONFIGURATION)) }
    val essence by lazy { getOption<String>(ESSENCE_TYPE_CONFIGURATION) }
    val food by lazy { Food.valueOf(getOption(FOOD_CONFIGURATION)) }
    val zmiPayment by lazy { Rune.valueOf(getOption(ZMI_PAYMENT_RUNE_CONFIG)) }
    val vileVigour by lazy { getOption<Boolean>(VILE_VIGOUR_CONFIG) }
    override fun createPainter(): ATPaint<*> = RCPainter(this)

    fun getBank(): InteractableEntity {
        val eniola = Npcs.stream().name("Eniola").first()
        if (eniola.valid()) return eniola
        return Objects.stream().type(GameObject.Type.INTERACTIVE).name("Bank booth").action("Bank").nearest().first()
    }

    fun getAltar() = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Chaos altar").action("Pray-at").first()

    override val rootComponent: TreeComponent<*> = object : Branch<Runecrafter>(this, "Should logout?") {
        override val failedComponent: TreeComponent<Runecrafter> = ShouldRepair(script)//Pouch repair branches
        override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Logging out cuz death") {
            if (Game.logout()) {
                ScriptManager.stop()
            }
        }

        var died = false
        override fun validate(): Boolean {
            if (ATContext.currentHP() == 0) {
                died = true
            }
            return died
        }
    }


    @ValueChanged(RUNE_ALTAR_CONFIGURATION)
    fun onAltarChange(altar: String) {
        updateVisibility(VILE_VIGOUR_CONFIG, altar == ZMI)
        updateVisibility(ZMI_PAYMENT_RUNE_CONFIG, altar == ZMI)
        if (altar != ZMI) updateOption(VILE_VIGOUR_CONFIG, false, OptionType.BOOLEAN)
    }
}

fun main() {
    Runecrafter().startScript("127.0.0.1", "GIM", false)
}