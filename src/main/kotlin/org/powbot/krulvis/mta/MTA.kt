package org.powbot.krulvis.mta

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.mta.tree.branches.CanCast

@ScriptManifest(
    name = "krul MagicTrainingArena",
    description = "Does MTA for points",
    version = "1.0.0",
    category = ScriptCategory.Magic
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Method",
            description = "Which room to clear?",
            allowedValues = arrayOf("Alchemy"),
            defaultValue = "Alchemy"
        ),
    ]
)
class MTA : ATScript() {

    val method by lazy { getOption<String>("Method") }

    var gainedPoints = 0
    var startCash = 0
    var gainedAlchemyPoints = 0


    override fun createPainter(): ATPaint<*> = MTAPainter(this)

    override val rootComponent: TreeComponent<*> = CanCast(this)

    override fun onStart() {
        super.onStart()
    }

    @Subscribe
    fun onMessageEvent(e: MessageEvent) {
        val txt = e.message
        if (txt == "The cupboard is empty.") {
            Alchemy.EMPTY_CUPBOARD = Alchemy.getCupboard().id
        }
    }

    @Subscribe
    fun onInventoryEvent(e: InventoryChangeEvent) {
        if(e.itemId == 995 && e.quantityChange > 0){

        }
    }
}

fun main() {
    MTA().startScript("127.0.0.1", "GIM", true)
}