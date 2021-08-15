package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.stream.widget.ComponentStream
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.sleep
import java.util.concurrent.atomic.AtomicInteger

@ScriptManifest(name = "testscript", version = "1.0d", description = "")
class TestScript : ATScript() {

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        val coins = Bank.stream().id(995).first()
        val inventoryCount: Int = Inventory.stream().count(true).toInt()
        val count = AtomicInteger(0)
//        val w = Widgets.widget(162)
//        println("Widget=$w")
//        val children = w.components()
//        println("Children count=${children.size}")
        println("Found continue: ${getContinue()}")
        sleep(Random.nextInt(2000, 5000))
    }

    fun getContinue(): Component? {
        for (a in Constants.CHAT_CONTINUES) {
            println("Checking continue component for widget: ${a[0]}")
            val c: Component =
                ComponentStream(get(true, listOf(Widgets.widget(a[0]))).stream()).textContains(" here to continue")
                    .first()
            if (!c.valid()) {
                continue
            }
            return c
        }
        return null
    }

    private fun get(children: Boolean, widgets: Iterable<Widget?>): List<Component> {
        return widgets.filterNotNull().flatMap {
            getComponents(children, it)
        }
    }

    private fun getComponents(children: Boolean, w: Widget): List<Component> {
        return w.components().flatMap {
            val l = mutableListOf(it)
            if (children && it.components().isNotEmpty()) {
                l.addAll(getComponents(children, it))
            }

            l
        }
    }

    private fun getComponents(children: Boolean, w: Component): List<Component> {
        return w.components().flatMap {
            val l = mutableListOf(it)
            if (children && it.components().isNotEmpty()) {
                l.addAll(getComponents(children, it))
            }

            l
        }
    }

}

fun main() {
    TestScript().startScript()
}
