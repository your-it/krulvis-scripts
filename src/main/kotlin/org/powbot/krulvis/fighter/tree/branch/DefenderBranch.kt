package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Equipment
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

    override val failedComponent: TreeComponent<Fighter> = ShouldShowRuneDefender(script)

    override fun validate(): Boolean {
        val target = script.target()
        val currDefenderIndex = script.currentDefenderIndex()
        if (target == null || !target.reachable()) {
            script.lastDefenderIndex = currDefenderIndex
        }
        return script.warriorGuild && script.lastDefenderIndex < currDefenderIndex && currDefenderIndex < script.defenders.size - 1
    }
}


class ShouldShowRuneDefender(script: Fighter) : Branch<Fighter>(script, "Should show rune defender?") {

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Show rune defender") {
        val runeDefender = Equipment(emptyList(), org.powbot.api.rt4.Equipment.Slot.OFF_HAND, script.defenders[6])
        val tile = Tile(2907, 9968, 0)
        if (runeDefender.inEquipment()) {
            runeDefender.dequip()
        } else if (Chat.canContinue()) {
            if (Chat.clickContinue()) {
                waitFor { !validate() }
            }
        } else {
            val lorelai = Npcs.stream().name("Lorelai").firstOrNull()
            if (lorelai == null || (!lorelai.reachable() && tile.distance() > 5)) {
                Movement.walkTo(tile)
            } else if (Utils.walkAndInteract(lorelai, "Use", selectItem = script.defenders[6])) {
                waitFor { Chat.canContinue() }
            }
        }
    }

    override val failedComponent: TreeComponent<Fighter> = ShouldBank(script)

    override fun validate(): Boolean {
        return script.lastDefenderIndex == 6 && Varpbits.varpbit(788) and 4096 != 4096
    }
}