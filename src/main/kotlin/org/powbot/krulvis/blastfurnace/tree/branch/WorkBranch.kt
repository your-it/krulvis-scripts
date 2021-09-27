package org.powbot.krulvis.blastfurnace.tree.branch

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES
import org.powbot.krulvis.blastfurnace.ICE_GLOVES
import org.powbot.krulvis.blastfurnace.tree.leaf.DrinkPotion
import org.powbot.krulvis.blastfurnace.tree.leaf.HandleBank
import org.powbot.krulvis.blastfurnace.tree.leaf.PutOre
import org.powbot.krulvis.blastfurnace.tree.leaf.TakeBars

class ShouldDrinkPotion(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should take bars") {
    override val successComponent: TreeComponent<BlastFurnace> = DrinkPotion(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldTake(script)

    override fun validate(): Boolean {
        return script.drinkPotion && Bank.opened()
                && (script.potion.inInventory() || Inventory.containsOneOf(VIAL) || script.potion.needsRestore(60))
    }
}

class ShouldTake(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should take bars") {
    override val successComponent: TreeComponent<BlastFurnace> = TakeBars(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldPutOre(script)

    override fun validate(): Boolean {
        return script.bar.blastFurnaceCount > 0
                && !Inventory.containsOneOf(
            script.bar.primary.id,
            script.bar.secondary.id
        )
    }
}

class ShouldPutOre(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Put Ore") {
    override val successComponent: TreeComponent<BlastFurnace> = PutOre(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldWaitAtDispenser(script)

    override fun validate(): Boolean {
        return Inventory.containsOneOf(script.bar.primary.id, script.bar.secondary.id)
    }
}

class ShouldWaitAtDispenser(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Wait @ Dispenser") {
    override val successComponent: TreeComponent<BlastFurnace> = SimpleLeaf(script, "Waiting for XP drop") {
        val xp = Skills.experience(Constants.SKILLS_SMITHING)
        if (script.dispenserTile.distance() >= 2) {
            Movement.step(script.dispenserTile.derive(-1, 0))
            if (waitFor(long()) { script.dispenserTile.distance() < 2 && Skills.experience(Constants.SKILLS_SMITHING) > xp }) {
                Inventory.stream().id(ICE_GLOVES).firstOrNull()?.interact("Wear")
            }
        }
    }
    override val failedComponent: TreeComponent<BlastFurnace> = HandleBank(script)

    override fun validate(): Boolean {
        if (Inventory.containsOneOf(script.bar.id)) {
            script.waitForBars = false
        }
        return !Inventory.isFull() && script.waitForBars
    }
}