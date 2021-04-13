package org.powbot.krulvis.api.utils.actions

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.utils.actions.Action
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Npc

class NPCAction(
    val name: String,
    val position: Tile? = null,
    vararg val actions: String
) :
    Action {

    /**
     * Idk why, but could be useful..
     */
    constructor(npc: Npc, action: String) : this(npc.name(), npc.tile(), action)

    constructor(name: String, action: String) : this(name, null, action)

    fun getNPC(ctx: ATContext): Npc? =
        ctx.npcs.filter {
            it.name().equals(
                name,
                true
            ) && it.actions().any { action -> action != null && actions.any { a -> action.contains(a, true) } }
        }.minBy { it.tile().distanceTo(ctx.me) }

    /**
     * Last supplied action will be tried to used
     */
    override fun execute(ctx: ATContext): Boolean {
        val o = getNPC(ctx)
        if (position != null && (o == null || o.tile().distanceTo(ctx.me) > 15)) {
            ctx.walk(position)
        } else if (o != null) {
            val a = actions.lastOrNull { action -> o.actions().any { oa -> oa != null && oa.contains(action, true) } }
                ?: actions.first()
            return ctx.interact(o, a)
        }
        return false
    }

}