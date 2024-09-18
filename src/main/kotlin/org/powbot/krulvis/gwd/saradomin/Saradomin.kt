package org.powbot.krulvis.gwd.saradomin

import org.powbot.api.Tile
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.gwd.GWD
import org.powbot.krulvis.gwd.GWDScript
import org.powbot.krulvis.gwd.branches.InsideGeneralRoom
import org.powbot.krulvis.gwd.saradomin.tree.CanAttack
import org.powbot.krulvis.gwd.saradomin.tree.ShouldWalk

@ScriptManifest(
    "krul GWD Saradomin",
    "Kills Commander Zilyana",
    "Krulvis",
    "1.0.0",
    priv = true,
    category = ScriptCategory.Combat
)
class Saradomin : GWDScript<Saradomin>(GWD.God.Saradomin) {

    override val rootComponent: TreeComponent<*> = InsideGeneralRoom(this)

    override fun generalAliveBranch(): TreeComponent<Saradomin> = CanAttack(this)
    override fun generalDeadBranch(): TreeComponent<Saradomin> {
        TODO("Not yet implemented")
    }

    override fun createPainter(): ATPaint<*> = SaradominPainter(this)

    val zilyanaTiles = arrayOf(
        Tile(2906, 5272,0),
        Tile(2906, 5266, 0),
        Tile(2900, 5258, 0),
        Tile(2896, 5258, 0),
        Tile(2890, 5264, 0),
        Tile(2891, 5269, 0),
        Tile(2896, 5275, 0),
        Tile(2901, 5274, 0),
    )
}

fun main() {
    Saradomin().startScript("127.0.0.1", "GIM", false)
}