package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Varpbits
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class GiantsFoundryPainter(script: GiantsFoundry) : ATPaint<GiantsFoundry>(script) {


    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.trackSkill(Skill.Smithing)
            .addString("Heat") {
                "${currentTemp()}"
            }
            .addString("Action") { "${script.currentAction}" }
            .addString("Can Perform") { "${script.currentAction?.canPerform()}" }
            .addString("Job") { Varpbits.varpbit(JOB_VARP).toString(2) }
            .addString("Heat") { Varpbits.varpbit(HEAT_VARP, 10, 2147483647).toString(2) }
//            .addString("Forte") { MouldType.Forte.selected().toString(2) }
//            .addString("Blades") { MouldType.Blades.selected().toString(2) }
//            .addString("Tips") { MouldType.Tips.selected().toString(2) }
//            .addString("SelectedAll") { MouldType.selectedAll().toString() }
//            .addString("OpenPage") { MouldType.openPage().toString() }
        Bar.ELEMENTALS.forEachIndexed { index, bar ->
            paintBuilder.addString(bar.name) { "${bar.giantsFoundryCount}" }
        }
        return paintBuilder.build()
    }

    override fun paintCustom(g: Rendering) {

    }
}