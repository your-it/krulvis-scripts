package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.telekenesis.TelekineticRoom

class InsideTelekinesis(script: MTA) : Branch<MTA>(script, "Inside telekinesis?") {
    override val failedComponent: TreeComponent<MTA> = SimpleLeaf(script, "Chilling") {
        sleep(600)
    }
    override val successComponent: TreeComponent<MTA> = ShouldInitialize(script)

    override fun validate(): Boolean {
        return TelekineticRoom.inside()
    }
}

class ShouldInitialize(script: MTA) : Branch<MTA>(script, "Should initialize room?") {
    override val failedComponent: TreeComponent<MTA> = SimpleLeaf(script, "What else") {
        script.log.info("Building moves")
        TelekineticRoom.buildMoves()
        script.log.info("Painting")
        TelekineticRoom.paint()
        sleep(600)
    }
    override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Initialize") {
        TelekineticRoom.instantiateRoom()
    }

    override fun validate(): Boolean {
        return TelekineticRoom.shouldInstantiate()
    }
}

class CanCastTelekinesis(script: MTA) : Branch<MTA>(script, "Can cast telekinesis") {
    override val failedComponent: TreeComponent<MTA> = SimpleLeaf(script, "What else") {

    }
    override val successComponent: TreeComponent<MTA> = CanDeposit(script)

    override fun validate(): Boolean {
        return false
    }
}