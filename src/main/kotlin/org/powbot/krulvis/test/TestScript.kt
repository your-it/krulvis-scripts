package org.powbot.krulvis.test

import org.powbot.api.rt4.Varpbits
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "testscript", version = "1.0d", description = "")
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Rocks",
            "Which rocks do you want to mine?",
            optionType = OptionType.GAMEOBJECTS,
        ),
        ScriptConfiguration(
            "Npcs",
            "Which Npcs do you want to kill?",
            optionType = OptionType.NPCS,
        )
    ]
)
class TestScript : ATScript() {
    override val painter: ATPainter<*> = TestPainter(this)

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {

    }


}

class TestPainter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics) {
        var y = this.y
        val paydirtraw = Varpbits.varpbit(375)

        y = drawSplitText(g, "Pay-dirt raw:", paydirtraw.toString(), x, y)
        y = drawSplitText(g, "Actual: ", "${(paydirtraw and 65535) / 256}", x, y)
    }
}

fun main() {
    TestScript().startScript()
}
