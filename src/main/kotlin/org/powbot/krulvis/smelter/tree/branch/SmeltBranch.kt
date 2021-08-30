package org.powbot.krulvis.smelter.tree.branch

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Item.Companion.CANNONBALL
import org.powbot.krulvis.api.utils.LastMade
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.smelter.Smelter

class ShouldSmelt(script: Smelter) : Branch<Smelter>(script, "Should Smelt") {
    override val failedComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Chilling") {}
    override val successComponent: TreeComponent<Smelter> = IsWidgetOpen(script)

    override fun validate(): Boolean {
        return if (script.cannonballs) LastMade.stoppedMaking(
            CANNONBALL,
            10000
        ) else LastMade.stoppedMaking(script.bar.id)
    }
}

class IsWidgetOpen(script: Smelter) : Branch<Smelter>(script, "IsWidgetOpen") {

    override val failedComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Smelt Furnace") {
        val anvil = Objects.stream().name("Furnace").action("Smelt").nearest().firstOrNull()
        if (anvil != null && Utils.walkAndInteract(anvil, "Smelt")) {
            waitFor(long()) { getComponent() != null }
        }
    }

    override val successComponent: TreeComponent<Smelter> = SimpleLeaf(script, "Clicking Widget") {
        val comp = getComponent()
        if (comp?.interact(if (script.cannonballs) "Make sets:" else "Smelt", false) == true) {
            waitFor(long()) { !LastMade.stoppedMaking(script.bar.id) }
        }
    }

    override fun validate(): Boolean {
        return getComponent() != null
    }

    fun getComponent(): Component? = if (script.cannonballs) Components.stream().action("Make sets:")
        .firstOrNull() else script.bar.getSmeltComponent()

}
