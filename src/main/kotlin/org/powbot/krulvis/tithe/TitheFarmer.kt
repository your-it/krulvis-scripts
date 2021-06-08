package org.powbot.krulvis.tithe

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powbot.krulvis.tithe.tree.branch.ShouldStart
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject

@Script.Manifest(
    name = "krul Tithe",
    description = "Tithe farming mini-game",
    version = "1.0",
    markdownFileName = "Tithe.md",
    properties = "category=Farming;",
    mobileReady = true
)
class TitheFarmer : ATScript() {
    override val painter: ATPainter<*> = TithePainter(this)
    override val rootComponent: TreeComponent<*> = ShouldStart(this)

    init {
        skillTracker.addSkill(Skill.FARMING)
    }

    var startPoints = -1
    var gainedPoints = 0
    var patchCount = 16
    var patches = listOf<Patch>()
    val chillTimer = Timer(2500)
    var planting = false

    fun getCornerPatchTile(): Tile {
        val allPatches = ctx.objects.toStream(25).filter { it.isPatch() }.list()
        val maxX = allPatches.minOf { it.tile().x() }
        val maxY = allPatches.maxOf { it.tile().y() }
        return Tile(maxX, maxY, 0)
    }

    fun getPatchTiles(): List<Tile> {
        val tmt = getCornerPatchTile()
        val columns = mutableListOf<Tile>()
        listOf(tmt, Tile(tmt.x(), tmt.y() - 15)).forEach {
            for (y in 0..9 step 3) {
                columns.add(Tile(it.x(), it.y() - y))
                columns.add(Tile(it.x() + 5, it.y() - y))
            }
        }

        return columns.toList()
    }

    fun refreshPatches() {
        val tiles = getPatchTiles()
        val onorderedPatches =
            ctx.objects.toStream(35).filter { it.name().isNotEmpty() && it.name() != "null" && it.tile() in tiles }
                .list()
        patches = tiles.mapIndexed { index, tile ->
            Patch(onorderedPatches.firstOrNull { it.tile() == tile } ?: GameObject.NIL, tile, index)
        }

    }

    fun hasSeeds(): Boolean = getSeed() > 0

    fun getSeed(): Int {
        val seed = ctx.inventory.toStream().id(*Data.SEEDS).findFirst()
        return if (seed.isPresent) seed.get().id() else -1
    }

    fun getPoints(): Int {
        val widget =
            ctx.widgets.widget(241).components().filter { it.text().contains("Points:") }.firstOrNull() ?: return -1
        return widget.text().substring(8).toInt()
    }

    fun getWaterCount(): Int = ctx.inventory.toStream().id(*Data.WATER_CANS).list().sumBy { it.id() - 5332 }

    fun hasEnoughWater(): Boolean = getWaterCount() >= patchCount * 3

    override fun startGUI() {
        started = true
    }

}