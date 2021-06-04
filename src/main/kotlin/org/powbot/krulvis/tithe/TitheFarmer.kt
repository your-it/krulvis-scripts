package org.powbot.krulvis.tithe

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powbot.krulvis.tithe.tree.branch.ShouldRefill
import org.powerbot.script.Script
import org.powerbot.script.Tile

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
    override val rootComponent: TreeComponent<*> = ShouldRefill(this)

    init {
        skillTracker.addSkill(Skill.FARMING)
    }

    var startPoints = -1
    var gainedPoints = 0
    var patches = listOf<Patch>()
    var tiles = listOf<Patch>()
    val chillTimer = Timer(2500)

    fun getTopMostTile(): Tile {
        val allPatches = ctx.objects.toStream().filter { it.isPatch() }.list()
        val maxX = allPatches.maxOf { it.tile().x() }
        val maxY = allPatches.maxOf { it.tile().y() }
        return Tile(maxX, maxY, 0)
    }

    fun getPatchTiles(): List<Tile> {
        val tmt = getTopMostTile()
        val startTiles = listOf(tmt, Tile(tmt.x() - 5, tmt.y()))

        val column = mutableListOf<Tile>()
        for (y in 0..9 step 3) {
            column.add(Tile(tmt.x(), tmt.y() - y))
            column.add(Tile(tmt.x() - 5, tmt.y() - y))
        }
        return column.toList()
    }

    fun refreshPatches() {
        val tiles = getPatchTiles()
        val onorderedPatches =
            ctx.objects.toStream().filter { it.name().isNotEmpty() && it.tile() in tiles }.list()
        patches = tiles.map { tile -> Patch(onorderedPatches.first { it.tile() == tile }) }

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

    fun hasEnoughWater(): Boolean = getWaterCount() >= 24

    override fun startGUI() {
        started = true
    }

}