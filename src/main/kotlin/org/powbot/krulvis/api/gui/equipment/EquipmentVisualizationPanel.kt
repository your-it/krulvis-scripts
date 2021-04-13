package org.powbot.krulvis.api.gui.equipment

import org.powbot.krulvis.api.utils.resources.ATImage
import java.awt.Dimension
import java.awt.Graphics
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JPanel

class EquipmentVisualizationPanel(val panel: EquipmentPanel) : JPanel() {

    private var backgroundImage: ATImage = ATImage(
        File("equipment" + File.separator + "background.png"),
        "https://www.dropbox.com/s/sh93oei5yrlwg66/equipment.png?dl=1"
    )

    private var disabledImage: ATImage = ATImage(
        File("equipment" + File.separator + "disabledImg.png"),
        "https://www.dropbox.com/s/v13l0r2wyx0eno9/equipment_slot_disabled.png?dl=1"
    )

    private var emptyImage: ATImage = ATImage(
        File("equipment" + File.separator + "emptyImg.png"),
        "https://www.dropbox.com/s/b9arlp8khr6wpoo/equipment_slot_empty.png?dl=1"
    )

    init {
        println("Creating Equipment Visualizer")
        val bgImg = backgroundImage.getImage()
        if (bgImg != null) {
            size = Dimension(bgImg.getWidth(null), bgImg.getHeight(null) + 7)
            println("Successfully obtained background image")
            preferredSize = size
            minimumSize = size
            maximumSize = size
            border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Gear")
            isOpaque = true
            size = size
            layout = null
            isVisible = true
            isVisible = true
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(backgroundImage.getImage(), 0, 7, null)
        for (s in VisualSlot.values()) {
            val image = panel.equipment.getImage(s)
            if (panel.disabledSlots.contains(s)) {
                g.drawImage(disabledImage.getImage(), s.location.x - 3, s.location.y - 2, null)
            } else if (image != null) {
                g.drawImage(emptyImage.getImage(), s.location.x - 3, s.location.y - 2, null)
                g.drawImage(image, s.location.x, s.location.y, null)
            }
        }
    }
}