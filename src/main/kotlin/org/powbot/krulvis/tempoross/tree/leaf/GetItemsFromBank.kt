package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.tempoross.Tempoross

class GetItemsFromBank(script: Tempoross) : Leaf<Tempoross>(script, "Getting Items") {
	override fun execute() {
		if (Bank.openNearest()) {
			val ids = script.inventoryBankItems.keys.toIntArray()
			if (!Inventory.emptyExcept(*ids)) {
				Bank.depositAllExcept(*ids)
			} else {
				val toWithdraw = script.inventoryBankItems
				Bank.withdrawExact(toWithdraw)
			}
		}

	}


}