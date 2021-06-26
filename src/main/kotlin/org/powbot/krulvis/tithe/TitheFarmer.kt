package org.powbot.krulvis.tithe

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data.NAMES
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powbot.krulvis.tithe.Patch.Companion.refresh
import org.powbot.krulvis.tithe.tree.branch.ShouldStart
import org.powerbot.script.*
import org.powerbot.script.rt4.ClientContext
import org.powerbot.script.rt4.GameObject

@Script.Manifest(
    name = "krul Tithe",
    description = "Tithe farming mini-game",
    version = "1.0.2",
    markdownFileName = "Tithe.md",
    properties = "category=Farming;",
    mobileReady = true
)
class TitheFarmer : ATScript(), GameActionListener {
    override val painter: ATPainter<*> = TithePainter(this)
    override val rootComponent: TreeComponent<*> = ShouldStart(this)

    init {
        skillTracker.addSkill(Skill.FARMING)
    }

    var lock = false
    var profile = TitheProfile()
    var startPoints = -1
    var gainedPoints = 0
    var patches = listOf<Patch>()
    val chillTimer = Timer(2500)
    var planting = false

    fun getCornerPatchTile(): Tile {
        val allPatches = ctx.objects.toStream(30).filter { it.isPatch() }.list()
        val maxX = allPatches.minOf { it.tile().x() }
        val maxY = allPatches.maxOf { it.tile().y() }
        return Tile(maxX, maxY, 0)
    }

    fun getPatchTiles(): List<Tile> {
        val tiles = mutableListOf(getCornerPatchTile())
        val columns = mutableListOf<Tile>()
        tiles.add(Tile(tiles[0].x(), tiles[0].y() - 15))
        tiles.forEach {
            for (y in 0..9 step 3) {
                columns.add(Tile(it.x(), it.y() - y))
                columns.add(Tile(it.x() + 5, it.y() - y))
            }
        }

        return columns.toList().subList(0, profile.patchCount)
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

    fun hasEnoughWater(): Boolean = getWaterCount() >= profile.patchCount * 3.5

    override fun startGUI() {
        if (System.getenv("LOCAL") == "true") {
            started = true
        } else {
            TitheGUI(this)
        }
    }

    override fun onAction(evt: GameActionEvent) {
        if (evt.rawOpcode == 4 || evt.opcode() == GameActionOpcode.InteractObject) {
            val tile = Tile(evt.var0 + 1, evt.widgetId + 1, 0).globalTile()
            println("Interacted with ${evt.rawEntityName}: at: $tile")
            if (NAMES.any { evt.rawEntityName.contains(it, true) }) {
                val patch = patches.first { it.tile == tile }
//                Utils.waitFor(2500) { tile.distance() < 3 }
                prepareNextInteraction(patch)
            }
        }
    }

    private fun Tile.globalTile(): Tile {
        val a = ClientContext.ctx().game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

    fun prepareNextInteraction(current: Patch) {
        lock = true
        patches = patches.refresh()
        val nextPatch = patches.minusElement(current)
            .sortedWith(compareBy(Patch::id, Patch::index))
            .firstOrNull { it.needsAction() }
        if (nextPatch != null) {
            waitFor { current.tile.distance() < 2.5 }
            if (ctx.client().isMobile) {
                nextPatch.go.click()
            } else {
                nextPatch.go.click(false)
            }
            println("Prepared next patch interaction...")
        }
        lock = false
    }


}