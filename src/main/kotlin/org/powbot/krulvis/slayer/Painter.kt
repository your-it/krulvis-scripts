package org.powbot.krulvis.slayer

import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics

class Painter(script: Slayer) : ATPaint<Slayer>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.addString("Last leaf") { script.lastLeaf.name }
            .addString("Current Task") { script.currentTask?.target?.name?.lowercase() }
            .addString("Remaining") { Slayer.taskRemainder().toString() }
            .trackSkill(Skill.Slayer)
            .trackSkill(Skill.Hitpoints)
            .trackSkill(Skill.Attack)
            .trackSkill(Skill.Strength)
            .trackSkill(Skill.Defence)
            .trackSkill(Skill.Ranged)
            .trackSkill(Skill.Magic)
            .build()
    }

    override fun paintCustom(g: Graphics) {
        val trgt = script.currentTarget ?: return
        trgt.tile().drawOnScreen(g)
        g.drawString("Attacking me: ${trgt.interacting() == Players.local()}", 850, 100)
        g.drawString("me attacking: ${Players.local().interacting() == trgt}", 850, 150)
    }
}