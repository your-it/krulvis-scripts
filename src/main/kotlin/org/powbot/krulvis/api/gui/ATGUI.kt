package org.powbot.krulvis.api.gui


import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.ScriptSettings
import org.powbot.krulvis.api.utils.resources.ATGson
import org.powbot.krulvis.api.utils.resources.ATImage
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileReader
import javax.swing.DefaultComboBoxModel
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants


abstract class ATGUI<S : ATScript, SS : ScriptSettings>(val script: S) : JFrame() {

    private val settingsPanel = SettingsPanel()

    fun initialize(rootPanel: JPanel) {
        script.started = false
        title = script.manifest?.name ?: "EmptyScript"
        contentPane = rootPanel
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        rootPanel.add(settingsPanel, gbc)
        iconImage = ATImage.SKULL_PNG.getImage()

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                println("Closed gui. Stopping script...")
                script.stop()
            }
        })

        settingsPanel.startButton.addActionListener {
            onStart(script, getCurrentSettings())
            script.started = true
            isVisible = false
            dispose()
        }

        settingsPanel.saveSettingsButton.addActionListener {
            val newName = settingsPanel.newSettingsField.text
            if (newName == null || newName.isEmpty()) {
                println("You need to type a settings profile name before saving!")
            } else {
                val settingsFile = File(script.settingsFolder().absolutePath + File.separator + newName + ".json")
                println("Saving new settings file with: $settingsFile")
//                script.debug("Saving new settings file with: $settingsFile")
                if (!settingsFile.exists()) {
//                    println("File does not yet exist")
                    settingsFile.parentFile.mkdirs()
                    settingsFile.createNewFile()
                }
                val settings = getCurrentSettings()
                settingsFile.writeText(settings.toJson(ATGson.Gson()))
                setLoadComboBox()
            }
        }

        setLoadComboBox()

        settingsPanel.loadButton.addActionListener {
            val settingsFile =
                File(script.settingsFolder().absolutePath + File.separator + settingsPanel.loadSettingsComboBox.selectedItem + ".json")
            println("Loading settings from: $settingsFile...")
//            script.debug("Loading settings from: $settingsFile...")
            val inputText = FileReader(settingsFile).readText()
            settingsPanel.newSettingsField.text = settingsPanel.loadSettingsComboBox.selectedItem.toString()
            loadSettings(parseSettings(inputText))
        }
        minimumSize = Dimension(10, 10)
        pack()
        isVisible = true
        repaint()
    }

    /**
     * Load the GUI's current settings: SS to the script: S
     */
    abstract fun onStart(script: S, settings: SS)

    /**
     * Should return the settings that the GUI currently has as input
     */
    abstract fun getCurrentSettings(): SS

    /**
     * Should parse the settingsString as SS and fill the GUI with the settings
     */
    abstract fun loadSettings(settings: SS)

    /**
     * return the inputText converted to ScriptSettings
     */
    abstract fun parseSettings(inputText: String): SS

    /**
     * Loads the found `getSettingsFiles()` as comboBoxModel
     */
    private fun setLoadComboBox() {
        settingsPanel.loadSettingsComboBox.model =
            DefaultComboBoxModel(script.getSettingsFiles().map { it.name.substring(0, it.name.indexOf(".")) }
                .toTypedArray())
    }
}