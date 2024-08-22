package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class WaitForLoot(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Waiting for loot...") {
	override fun execute() {
		waitFor { script.lootList.isNotEmpty() }
	}

}