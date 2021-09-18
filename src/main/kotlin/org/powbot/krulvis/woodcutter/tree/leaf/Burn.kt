package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Item.Companion.TINDERBOX
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager

class Burn(script: Woodcutter) : Leaf<Woodcutter>(script, "Burning") {
    override fun execute() {
        val logs = Inventory.stream().firstOrNull { it.id !in script.TOOLS }
        if (Players.local().animation() == 733) {
            //Stil tryna burn
            waitFor { Players.local().animation() != 733 }
            return
        }
        if (logs == null) {
            script.burning = false
            return
        } else {
            script.burning = true
        }

        val burnTile = findGoodSpot()
        if (burnTile == null) {
            script.log.info("Can't find good burning spot...")
            script.burning = true
            return
        } else if (burnTile != Players.local().tile()) {
            if (Movement.walkTo(burnTile)) {
                waitFor { burnTile == Players.local().tile() }
            }
        }

        Game.tab(Game.Tab.INVENTORY)
        if (Inventory.selectedItem() == Item.Nil) {
            val tinderBox = Inventory.stream().id(TINDERBOX).firstOrNull()
            if (tinderBox == null) {
                script.log.info("No tinderbox, stopping script")
                ScriptManager.stop()
            } else if (tinderBox.interact("Use")) {
                waitFor { Inventory.selectedItem().id == TINDERBOX }
            }
        }

        if (Inventory.selectedItem().id == TINDERBOX) {
            if (logs.click()) {
                waitFor(long()) { Objects.stream().at(Players.local()).name("Fire").isNotEmpty() }
            }
        }
    }

    fun findGoodSpot(): Tile? {
        val t = Players.local().tile()
        if (Objects.stream().at(t).firstOrNull { it.name.isNotEmpty() } == null) {
            return t
        }

        val flags = Movement.collisionMap(0).flags()

        var longestStreak = 0
        var bestTile: Tile? = null
        val myTile = Players.local().tile()
        for (y in myTile.y - 5..myTile.y + 5) {
            var startTileY = Tile(myTile.x + 5, y)
            var streakY = 0
            for (x in myTile.x + 5..myTile.x - 5) {
                val tile = Tile(x, y)
                if (!tile.blocked(flags)) {
                    if (streakY == 0) {
                        startTileY = tile
                    }
                    streakY++
                    if (streakY > longestStreak) {
                        bestTile = startTileY
                    }
                } else {
                    streakY = 0
                }
            }
        }

        return bestTile
    }

}