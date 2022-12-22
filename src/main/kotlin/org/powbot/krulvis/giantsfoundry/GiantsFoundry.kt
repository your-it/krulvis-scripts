package org.powbot.krulvis.giantsfoundry

import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.giantsfoundry.tree.branch.IsSmithing


@ScriptManifest(
    name = "krul GiantFoundry",
    description = "Makes swords for big giant.",
    author = "Krulvis",
    version = "0.0.1",
    category = ScriptCategory.Smithing,
    priv = true
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Inventory", "What bars do you want to smelt?",
            optionType = OptionType.INVENTORY,
            defaultValue = "{\"2353\":14,\"2359\":14}"
        )
    ]
)
class GiantsFoundry : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return GiantsFoundryPainter(this)
    }

    var currentAction: Action? = null
    val barsToUse by lazy { getOption<Map<Int, Int>>("Inventory") }

    override val rootComponent: TreeComponent<*> = IsSmithing(this)
//    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "SimpleLeaf") { sleep(2000) }

    fun getInvBar() = Inventory.stream().id(*Bar.ELEMENTALS.map { it.id }.toIntArray()).firstOrNull()

    fun getCrucibleBars(): List<Pair<Bar, Int>> {
        return Bar.ELEMENTALS.map { it to it.giantsFoundryCount }.filter { it.second > 0 }
    }

    fun isSmithing() = Equipment.stream().name("Preform").isNotEmpty()

    fun areBarsPoured() = Objects.stream().name("Mould jig (Poured metal)").isNotEmpty()

    fun activeActionComp(): Component = Widgets.component(ROOT, 76)

    fun kovac() = Npcs.stream().name("Kovac").firstOrNull()

    fun hasCommission() = Varpbits.varpbit(VARP, 0, 127) != 0

    fun mouldWidget() = Widgets.widget(718)

    fun mouldWidgetOpen() = mouldWidget().component(2).any { it?.text() == "Giants' Foundry Mould Setup" }

    fun activeAction(): Action? {
        val activeComp = activeActionComp() ?: return null
        val x = activeComp.x()
        val maxX = x + activeComp.width()
        val actionComps = Components.stream(ROOT, 75)
            .filtered { it.x() in x..maxX }.list()
        val actionComp = actionComps.firstOrNull { Action.forTexture(it.textureId()) != null } ?: return null
        return Action.forTexture(actionComp.textureId())
    }

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        currentAction = activeAction()
    }

    fun stopActivity() = Movement.step(me.tile())


    enum class Action(
        val textureId: Int,
        val interactable: String,
        val min: Int,
        val max: Int,
        val heats: Boolean = false
    ) {
        HAMMER(4442, "Trip hammer", 711, 957),
        GRIND(4443, "Grindstone", 374, 620, true),
        POLISH(4444, "Polishing wheel", 45, 291);

        fun canPerform() = getHeat() in min..max

        fun getObj() = Objects.stream().name(interactable).firstOrNull()

        companion object {
            fun forTexture(texture: Int) = values().firstOrNull { it.textureId == texture }
        }

    }

    companion object {
        //123093914
        fun getHeat(): Int = Varpbits.varpbit(3433, 2047)

        val ROOT = 754
    }

}

fun main() {
    GiantsFoundry().startScript("127.0.0.1", "GIM", true)
}