package org.powbot.krulvis.test

import org.powbot.api.Condition
import org.powbot.api.Condition.wait
import org.powbot.api.Filter
import org.powbot.api.MenuCommand
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Constants.MOBILE_TAB_OPEN_BUTTON_TEXTURE_ID
import org.powbot.api.rt4.Game.Tab
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPathFinder.isRockfall
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "testscript", version = "1.0d", description = "")
class TestScript : ATScript() {
    override val painter: ATPainter<*> = TestPainter(this)

    val dest = Tile(3208, 3221, 2) //Lummy top bank
    val nearHopper = Tile(3748, 5673, 0) //Hopper in Motherlode
    val topMineFloor = Tile(3757, 5680, 0) //Hopper in Motherlode
    val oddRockfall = Tile(x = 3216, y = 3210, floor = 0)
    var flags = emptyArray<IntArray>()

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        log.info(getCompletedQuests(399, 6).joinToString())
    }

    fun tab(tab: Tab): Boolean {
        if (tab == tab()) {
            return true
        }

        val c = tab.getByTexture()
        val interacted = c != null && c.click()
        return interacted && wait({ tab() == tab }, 200, 10)
    }

    fun tab(): Tab {
        val openTabButton =
            Components.stream(601).texture(MOBILE_TAB_OPEN_BUTTON_TEXTURE_ID).firstOrNull() ?: return Tab.NONE
        val actions = openTabButton.actions()
        return Tab.values().firstOrNull { tab -> actions.any { a -> a in tab.actions } } ?: Tab.NONE
    }

    val CompleteQuestColor = 901389

    private fun getCompletedQuests(parent: Int, child: Int, tries: Int = 5): Set<String> {
        val component = Widgets.component(parent, child)
        if (component.valid() && component.componentCount() > 0) {
            return component.components().toList()
                .filter { it.valid() && it.textColor() == CompleteQuestColor }
                .map { it.text() }
                .toSet()
        } else {
            tab(Tab.QUESTS)
            !openQuestTab(parent, child)
            return getCompletedQuests(parent, child, tries - 1)
        }
    }

    fun questTabOpen(parent: Int, child: Int) =
        Varpbits.varpbit(1141) == 16
                && Widgets.component(parent, child).componentCount() > 0

    fun openQuestTab(parent: Int, child: Int): Boolean {
        if (questTabOpen(parent, child)) {
            return true
        } else {
            val tabButton =
                Components.stream().filter { it.actions().contains("Quest List") }.firstOrNull()
            if (tabButton?.click() == true && wait { questTabOpen(parent, child) }) {
                return true
            }
        }
        return false
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        if (e.opcode() == GameActionOpcode.InteractObject) {
            val tile = Tile(e.var0, e.widgetId).globalTile()
            log.info("Interacted with obj at tile=${tile} $e")
        } else {
            log.info("GameActionEvent $e")
        }
    }

    fun Tile.globalTile(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }
}

class TestPainter(script: TestScript) : ATPainter<TestScript>(script, 10, 500) {
    override fun paint(g: Graphics, startY: Int) {
        var y = startY
        y = drawSplitText(g, "Tab: ", Game.tab().toString(), x, y)
        val qc = Components.stream(399).text("Quest Points").firstOrNull()
        if (qc != null) {
            y = drawSplitText(g, "Quests loaded: ", "${qc.visible()}", x, y)
            y = drawSplitText(g, "Widget: ${qc.widgetId()}", "$qc", x, y)

        }
    }

    fun Tile.rockfallBlock(flags: Array<IntArray>): Boolean {
        return collisionFlag(flags) in intArrayOf(Flag.ROCKFALL, Flag.ROCKFALL2)
                && Objects.stream().at(this).filter { it.isRockfall() }.isNotEmpty()
    }

    fun Tile.regionTile(): Tile {
        val (x1, y1) = Game.mapOffset()
        return derive(-x1, -y1)
    }

}

fun main() {
    TestScript().startScript()
}
