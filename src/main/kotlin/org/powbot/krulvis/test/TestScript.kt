package org.powbot.krulvis.test

import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.tempoross.Tempoross
import org.powerbot.script.Script
import java.awt.Graphics2D

@Script.Manifest(name = "TestScript", description = "Some testing", version = "1.0")
class TestScript : ATScript() {

    override fun rootComponent(): TreeComponent {
        return object : Leaf<TestScript>(this, "TestLeaf") {
            override fun loop() {

            }
        }
    }

    override val painter: ATPainter<*>
        get() = Painter(this)

    override fun startGUI() {
        debugComponents = true
        started = true
    }
}

class Painter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        drawSplitText(g, "Empty slots: ", inventory.emptySlots().toString(), x, y)
    }

    override fun drawProgressImage(g: Graphics2D, height: Int) {
        TODO("Not yet implemented")
    }
}