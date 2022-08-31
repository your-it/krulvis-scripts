package org.powbot.krulvis.daeyalt

import org.powbot.api.Condition
import org.powbot.api.Production
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.rt4.walking.local.Utils.getWalkableNeighbor
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleBranch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint

@ScriptManifest(
    name = "krul DaeyaltMiner",
    description = "Mines Daeyalt essence",
    author = "Krulvis",
    version = "1.0.0"
)
class DaeyaltMiner : ATScript() {

    override fun createPainter(): ATPaint<*> {
        return DaeyaltPainter(this)
    }

    val mineLeaf = SimpleLeaf(this, "Mine") {
        val mine = Objects.stream().name("Daeyalt Essence").action("Mine").firstOrNull() ?: return@SimpleLeaf
        if (mine.distance() > 5) {
            Movement.step(mine.tile)
            Condition.wait({ mine.distance() <= 5 }, 500, 10)
        } else if (Utils.walkAndInteract(mine, "Mine")) {
            Condition.wait({ !Production.stoppedMaking(ESSENCE) }, 500, 10)
        }
    }
    override val rootComponent: TreeComponent<*> =
        SimpleBranch(this, "Should Mine?", mineLeaf, SimpleLeaf(this, "Mining...") {}) {
            Production.stoppedMaking(ESSENCE)
        }


}

val ESSENCE = 24706

class DaeyaltPainter(script: DaeyaltMiner) : ATPaint<DaeyaltMiner>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.trackInventoryItem(ESSENCE).trackSkill(Skill.Mining).build()
    }
}

fun main() {
    DaeyaltMiner().startScript()
}