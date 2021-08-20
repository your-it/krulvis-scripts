package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "testBF", version = "1.0", description = "")
class TestBF : ATScript() {
    override val painter: ATPainter<*> = TestBFPainter(this)

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        Ore.values().forEach {
            println("${it.name}: ${it.blastFurnaceCount}")
        }
        Bar.values().forEach {
            println("${it.name}: ${it.blastFurnaceCount}")
        }
        val coffer = Varpbits.varpbit(795) / 2
        println("Coffer=$coffer")
        sleep(Random.nextInt(2000, 5000))
    }

}

class TestBFPainter(script: TestBF) : ATPainter<TestBF>(script) {
    override fun paint(g: Graphics, startY: Int) {
        TODO("Not yet implemented")
    }
}

fun main() {
    TestBF().startScript()
}
