package org.powbot.krulvis.api.utils.actions

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.utils.actions.Action
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import org.powerbot.script.rt4.Objects

open class ObjectAction(
    val name: String,
    val position: Tile? = null,
    vararg val actions: String
) :
    Action {

    /**
     * Idk why, but could be useful..
     */
    constructor(obj: GameObject, action: String) : this(obj.name(), obj.tile(), action)

    fun getObject(ctx: ATContext): GameObject {
        return if (position != null) ctx.objects.toStream().at(position).name(name).first()
        else ctx.objects.toStream().name(name).action(*actions).first()
    }


    /**
     * Last supplied action will be tried to used
     */
    override fun execute(ctx: ATContext): Boolean {
        val o = getObject(ctx)
        if (position != null && (o == GameObject.NIL || o.tile().distanceTo(ctx.me) > 15)) {
            ctx.walk(position)
        } else if (o != GameObject.NIL) {
            val a = actions.lastOrNull { action -> o.actions().any { oa -> oa.contains(action, true) } }
                ?: actions.first()
            return o.interact(a)
        }
        return false
    }

}