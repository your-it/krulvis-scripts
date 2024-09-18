package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.leafs.GetNPCContactRunes
import org.powbot.krulvis.runecrafting.tree.leafs.RepairPouchesNPCContact

class ShouldRepairPouches(script: Runecrafter) : Branch<Runecrafter>(script, "Should repair with NPC Contact?") {
	override val failedComponent: TreeComponent<Runecrafter> = ShouldPrayAtAltar(script)
	override val successComponent: TreeComponent<Runecrafter> = OnLunarBook(script)

	override fun validate(): Boolean {
		if (script.spellBook == null && Game.loggedIn()) {
			script.spellBook = Magic.book()
		}
		val repairable = EssencePouch.inInventory().filter { it.shouldRepair() }
		if (repairable.isEmpty()) return false

		script.logger.info("Repairables=[${repairable.joinToString { it.itemName }}]")
		return repairable.isNotEmpty()
	}
}

class OnLunarBook(script: Runecrafter) : Branch<Runecrafter>(script, "OnLunarBook?") {
	override val failedComponent: TreeComponent<Runecrafter> = CanSwitchBook(script)
	override val successComponent: TreeComponent<Runecrafter> = IsChatting(script)

	override fun validate(): Boolean {
		return Magic.book() == Magic.Book.LUNAR
	}
}

class CanSwitchBook(script: Runecrafter) : Branch<Runecrafter>(script, "CanSwitchSpellbook?") {
	override val failedComponent: TreeComponent<Runecrafter> = ShouldPrayAtAltar(script)
	override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "SwitchSpellBook") {
		if (walkAndInteract(altar, "Lunar")) {
			waitForDistance(altar) { Magic.book() == Magic.Book.LUNAR }
		}
	}

	var altar = GameObject.Nil

	override fun validate(): Boolean {
		altar = script.getSpellBookAltar()
		return altar.valid()
	}
}

class IsChatting(script: Runecrafter) : Branch<Runecrafter>(script, "IsChatting?") {
	override val failedComponent: TreeComponent<Runecrafter> = HasRunes(script)
	override val successComponent: TreeComponent<Runecrafter> = RepairPouchesNPCContact(script)

	override fun validate(): Boolean {
		return Chat.chatting()
	}
}

class HasRunes(script: Runecrafter) : Branch<Runecrafter>(script, "Has runes to repair?") {
	override val failedComponent: TreeComponent<Runecrafter> = GetNPCContactRunes(script)
	override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Cast spell") {
		if (Bank.close() && Magic.LunarSpell.NPC_CONTACT.cast("Dark Mage")) {
			waitFor(10000) { Chat.chatting() }
		}
	}

	override fun validate(): Boolean {
		return Magic.LunarSpell.NPC_CONTACT.canCast()
	}
}
