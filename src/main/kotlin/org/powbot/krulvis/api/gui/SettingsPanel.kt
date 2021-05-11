package org.powbot.krulvis.api.gui

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.*

class SettingsPanel : JPanel() {

    val loadSettingsComboBox = JComboBox<String>()
    val loadButton = JButton("Load")
    val newSettingsField = JTextField()
    val saveSettingsButton = JButton("Save")
    val startButton = JButton("Start Script")
    private var gbc = GridBagConstraints()

    init {
        layout = GridBagLayout()
        border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Settings")
        gbc.gridx = 1
        gbc.gridy = 0
        gbc.anchor = GridBagConstraints.EAST
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.ipadx = 110
        add(loadSettingsComboBox, gbc)

        gbc = GridBagConstraints()
        gbc.gridx = 1
        gbc.gridy = 1
        gbc.anchor = GridBagConstraints.EAST
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(5, 5, 5, 5)
        add(loadButton, gbc)

        gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.anchor = GridBagConstraints.WEST
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.ipadx = 110
        newSettingsField.toolTipText = "Settings profile name"
        add(newSettingsField, gbc)

        gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.anchor = GridBagConstraints.WEST
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(5, 5, 5, 5)
        add(saveSettingsButton, gbc)

        gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 2
        gbc.anchor = GridBagConstraints.WEST
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(5, 5, 5, 5)
        add(startButton, gbc)
        repaint()
    }
}