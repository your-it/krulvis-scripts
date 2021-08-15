package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.Condition.wait
import org.powbot.api.action.ActionWaiter
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Bank.withdrawXAmount
import org.powbot.api.rt4.Constants.BANK_ITEMS
import org.powbot.api.rt4.Constants.BANK_SCROLLBAR
import org.powbot.api.rt4.Constants.BANK_WIDGET
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.blastfurnace.Bar
import org.powbot.krulvis.blastfurnace.Ore
import org.powbot.mobile.input.Keyboard
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@ScriptManifest(name = "testBF", version = "1.0", description = "")
class TestBF : ATScript() {

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        Ore.values().forEach {
            println("${it.name}: ${it.amount}")
        }
        Bar.values().forEach {
            println("${it.name}: ${it.amount}")
        }
        val coffer = Varpbits.varpbit(795) / 2
        println("Coffer=$coffer")
        sleep(Random.nextInt(2000, 5000))
    }

}

fun main() {
    TestBF().startScript()
}
