package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class ShouldExitRoom(script: Fighter) : Branch<Fighter>(script, "Should Exit Room?") {
    val doorTile = Tile(2847, 3541, 2)
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Exit room") {
        val door = Objects.stream().at(doorTile).name("Door").firstOrNull()
        if (door != null && Utils.walkAndInteract(door, "Open")) {
            waitFor(long()) { Players.local().tile() == doorTile }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldBank(script)

    override fun validate(): Boolean {
        if (script.target()?.reachable() == false) script.lastDefenderIndex = script.currentDefenderIndex()
        return script.warriorGuild && script.lastDefenderIndex < script.currentDefenderIndex()
    }
}