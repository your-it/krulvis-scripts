package org.powbot.krulvis.api.gui.equipment

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Utils.getItemImage
import org.powerbot.script.rt4.CacheItemConfig
import org.powerbot.script.rt4.ClientContext
import org.powerbot.script.rt4.Item
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class EquipmentPanel(val script: ATScript, var disabledSlots: ArrayList<VisualSlot>) : JPanel() {

    constructor(script: ATScript) : this(script, arrayListOf())

    val settingsPanel = JPanel()
    private val visualPanel = EquipmentVisualizationPanel(this)
    private val getEquipmentButton = JButton("Get Equipment")
    private val ammoCountLabel = JLabel("Ammo count: ")
    private val ammoCountField = JTextField("0")
    private var gbc = GridBagConstraints()

    var equipment: List<Equipment> = emptyList()

    init {
        build()
        getEquipmentButton.addActionListener {
            getEquipmentButton.text = "Loading Gear.."
            Thread(Runnable {
                val elist = mutableListOf<Equipment>()
                for (slot in VisualSlot.values()) {
                    if (slot in disabledSlots) {
                        if (slot == VisualSlot.AMMO) {
                            ammoCountField.isEnabled = false
                        }
                        continue
                    }
                    val i: Item = ctx.equipment.itemAt(slot.iSlot)
                    if (i != Item.NIL) {
                        val def = CacheItemConfig.load(ClientContext.ctx().bot().cacheWorker, i.id())
                        val e =
                            Equipment(emptyList(), def.stackId, slot = slot.iSlot, image = getItemImage(def.stackId))
                        elist.add(e)
                        println("Found item in slot: ${slot.iSlot}, with id: ${e.id}")
                        if (slot == VisualSlot.AMMO) {
                            ammoCountField.isEnabled = true
                        }
                    } else if (slot == VisualSlot.AMMO) {
                        ammoCountField.isEnabled = false
                        ammoCountField.text = "0"
                    }
                }
                equipment = elist
                visualPanel.repaint()
                getEquipmentButton.text = "Get Equipment"
            }).start()
        }
    }

    val ammoCount: Int
        get() {
            val text = ammoCountField.text ?: return -1
            if (text.isEmpty()) {
                return -1
            }
            return Integer.parseInt(text)
        }

    val gearSet: GearSet get() = GearSet(equipment, ammoCount)

    fun load(gearSet: GearSet) {
        getEquipmentButton.text = "Loading Gear.."
        equipment = gearSet.gear
        visualPanel.repaint()
        getEquipmentButton.text = "Get Equipment"
        ammoCountField.text = gearSet.ammoCount.toString()
    }

    fun build() {
        layout = GridBagLayout()
        settingsPanel.layout = GridBagLayout()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridheight = 2
        gbc.fill = GridBagConstraints.BOTH
        add(settingsPanel, gbc)

        settingsPanel.border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "")

        /**
         * Add ammo buttons
         */
        gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.HORIZONTAL
        settingsPanel.add(ammoCountLabel, gbc)
        gbc = GridBagConstraints()
        gbc.gridx = 1
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.HORIZONTAL
        settingsPanel.add(ammoCountField, gbc)

        /**
         * Get Equipment button
         */
        gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        settingsPanel.add(getEquipmentButton, gbc)

        val spacer1 = JPanel()
        gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.fill = GridBagConstraints.VERTICAL
        add(spacer1, gbc)

        /**
         * Visualization of equipment panel
         */
        visualPanel.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridx = 1
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.BOTH
        add(visualPanel, gbc)
    }
}

///**
// * Test creation of EquipmentPanel
// */
//fun main() {
//    val f = JFrame()
//    f.contentPane = EquipmentPanel(TestScript())
//    f.minimumSize = Dimension(10, 10)
//    f.pack()
//    f.isVisible = true
//}