package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter

class HandleBank(script: Woodcutter) : Leaf<Woodcutter>(script, "Handle bank") {
    override fun execute() {
        Bank.depositAllExcept(*script.TOOLS)
    }
}