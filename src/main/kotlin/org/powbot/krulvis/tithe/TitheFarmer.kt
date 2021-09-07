package org.powbot.krulvis.tithe

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data.NAMES
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powbot.krulvis.tithe.Patch.Companion.refresh
import org.powbot.krulvis.tithe.tree.branch.Locked
import org.powbot.krulvis.tithe.tree.branch.ShouldStart
import java.util.logging.Logger

@ScriptManifest(
    name = "krul Tithe",
    description = "Tithe farming mini-game",
    author = "Krulvis",
    version = "1.0.4",
    markdownFileName = "Tithe.md",
    category = ScriptCategory.Farming
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Patches",
            description = "How many patches do you want to use? Max 16",
            optionType = OptionType.INTEGER,
            defaultValue = "14"
        )
    ]
)
class TitheFarmer : ATScript() {
    override fun createPainter(): ATPaint<*> = TithePainter(this)

    override val rootComponent: TreeComponent<*> = Locked(this)

    val patchCount by lazy { getOption<Int>("Patches")?.toInt() ?: 14 }
    var lastPatch: Patch? = null
    var lock = false
    var startPoints = -1
    var gainedPoints = 0
    var patches = listOf<Patch>()
    val chillTimer = Timer(2500)
    var planting = false

    fun getCornerPatchTile(): Tile {
        val allPatches = Objects.stream(30).filtered { it.isPatch() }.list()
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

        return columns.toList().subList(0, patchCount)
    }

    fun refreshPatches() {
        val tiles = getPatchTiles()
        val onorderedPatches =
            Objects.stream(35).filtered { it.name().isNotEmpty() && it.name() != "null" && it.tile() in tiles }
                .list()
        patches = tiles.mapIndexed { index, tile ->
            Patch(onorderedPatches.firstOrNull { it.tile() == tile } ?: GameObject.Nil, tile, index)
        }

    }

    fun hasSeeds(): Boolean = getSeed() > 0

    fun getSeed(): Int {
        val seed = Inventory.stream().id(*Data.SEEDS).findFirst()
        return if (seed.isPresent) seed.get().id() else -1
    }

    fun getPoints(): Int {
        val widget =
            Widgets.widget(241).components().firstOrNull { it.text().contains("Points:") } ?: return -1
        return widget.text().substring(8).toInt()
    }

    fun getWaterCount(): Int = Inventory.stream().id(*Data.WATER_CANS).list().sumOf { it.id() - 5332 }

    fun hasEnoughWater(): Boolean = getWaterCount() >= patchCount * 3.5

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 4 || evt.opcode() == GameActionOpcode.InteractObject) {
            val tile = Tile(evt.var0 + 1, evt.widgetId + 1, 0).globalTile()
            if (NAMES.any { evt.rawEntityName.contains(it, true) }) {
                lastPatch = patches.first { it.tile == tile }
                lock = true
                log.info("Interacted ${evt.interaction} on $lastPatch")
            }
        }
    }

    private fun Tile.globalTile(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }


}

fun main() {
    TitheFarmer().startScript(true)
}