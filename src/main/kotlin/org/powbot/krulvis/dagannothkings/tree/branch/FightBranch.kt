package org.powbot.krulvis.dagannothkings.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king
import org.powbot.krulvis.dagannothkings.tree.leaf.*

class FightingKing(script: DagannothKings) : Branch<DagannothKings>(script, "FightingKing?") {
	override val failedComponent: TreeComponent<DagannothKings> = IsWearingCorrectEquipment(script)
	override val successComponent: TreeComponent<DagannothKings> = ShouldLure(script)

	override fun validate(): Boolean {
		return script.target.valid() && !script.target.dead() && me.interacting() == script.target
	}
}

class ShouldLure(script: DagannothKings) : Branch<DagannothKings>(script, "LureRex?") {
	override val failedComponent: TreeComponent<DagannothKings> = ShouldConsume(script, ShouldConsume(script, Fight(script)))
	override val successComponent: TreeComponent<DagannothKings> = LureRex(script)

	override fun validate(): Boolean {
		if (script.target.king() == Data.King.Rex && script.safeSpotRex) {
			if (script.target.distanceTo(script.rexTile) > 0 || script.safeTile.distance() > 0) {
				return true
			}
		}
		return false
	}
}

class IsWearingCorrectEquipment(script: DagannothKings) : Branch<DagannothKings>(script, "WearingCorrectEquipment?") {
	override val failedComponent: TreeComponent<DagannothKings> = EquipGear(script)
	override val successComponent: TreeComponent<DagannothKings> = IsPrayerOn(script)

	override fun validate(): Boolean {
		return true
	}
}


class IsPrayerOn(script: DagannothKings) : Branch<DagannothKings>(script, "IsPrayerOn?") {
	override val failedComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "ActivatePrayers") {
		if (protectionPrayer != null) {
			Prayer.prayer(protectionPrayer!!, true)
		}
		if (offensivePrayer != null) {
			Prayer.prayer(offensivePrayer!!, true)
		}
		prayTimer.reset()
	}
	override val successComponent: TreeComponent<DagannothKings> = ShouldConsume(script, ShouldLoot(script))

	val prayTimer = Timer(600)
	var protectionPrayer: Prayer.Effect? = Data.King.Rex.protectionPrayer
	var offensivePrayer: Prayer.Effect? = Data.King.Rex.offensivePrayer
	override fun validate(): Boolean {
		if (!prayTimer.isFinished()) return true
		val king = script.target.king() ?: return true
		if (king == Data.King.Rex) return true
		protectionPrayer = king.protectionPrayer
		offensivePrayer = king.offensivePrayer
		return (offensivePrayer == null || Prayer.prayerActive(offensivePrayer!!)) && (protectionPrayer == null || Prayer.prayerActive(protectionPrayer!!))
	}
}

class ShouldLoot(script: DagannothKings) : Branch<DagannothKings>(script, "ShouldLoot?") {
	override val failedComponent: TreeComponent<DagannothKings> = Attack(script)
	override val successComponent: TreeComponent<DagannothKings> = Loot(script)

	override fun validate(): Boolean {
		return script.lootList.isNotEmpty() && script.lootList.any { Food.forName(it.name()) == null || Inventory.emptySlotCount() > 0 }
	}
}