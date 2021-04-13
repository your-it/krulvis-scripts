package org.powbot.krulvis.api.script

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.EmptyLeaf
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Discord
import org.powbot.krulvis.api.utils.Imgur
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.trackers.SkillTracker
import org.powerbot.script.PaintListener
import org.powerbot.script.PollingScript
import org.powerbot.script.rt4.ClientContext
import java.awt.Graphics
import java.awt.Graphics2D
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStream


abstract class ATScript : PollingScript<ClientContext>(), ATContext, PaintListener {

    override fun start() {
        println("Starting..")
        startGUI()
        startTracking()
    }

    override fun poll() {
        if (started) {
            if (rootComponent == null) {
                rootComponent = rootComponent()
            }
            rootComponent!!.execute()
        }
        sleep(Random.nextInt(100, 200))
    }

    override val script: ATScript get() = this
    override val ctx: ClientContext get() = super<PollingScript>.ctx
    override val lpf: LocalPathFinder = LocalPathFinder(this)

    /**
     * Components necessary to run the script
     */
    abstract fun rootComponent(): TreeComponent
    abstract val painter: ATPainter<*>

    /**
     * Variables used throughout script
     */
    var debugComponents = false
    var started = false
    var rootComponent: TreeComponent? = null
    var lastLeaf: Leaf<*> = EmptyLeaf(this, "Init")
    val timer = Timer()
    val skillTracker = SkillTracker(this)
    val oddsModifier = OddsModifier()
    val walkDelay = DelayHandler(500, 700, oddsModifier, "Walk Delay")
    var nextRun: Int = Random.nextInt(1, 6)


    abstract fun startGUI()

    fun startTracking() {
        println("Started tracking thread")
        Thread {
            while (!controller.isStopping) {
                skillTracker.track()
//            inventoryWatcher.watch()
//            animationWatcher.watch()
                Thread.sleep(500)
            }
        }.start()
    }


    override fun repaint(g: Graphics?) {
        painter.onRepaint(g as Graphics2D)
    }

    /**
     * Returns the actual script's settings files
     */
    fun getSettingsFiles(): Array<out File> {
        val settingsFolder = settingsFolder()
        if (settingsFolder.exists()) {
            return settingsFolder.listFiles()!!
        }
        return emptyArray()
    }

    /**
     * Powbot folder
     */
    fun powbotFolder() = System.getProperty("user.home") + File.separator + ".powbot"

    /**
     * Returns a Folder where the current script's settings have to be stored/loaded from
     */
    fun settingsFolder(): File {
        val pb = powbotFolder()
        return File(pb + File.separator + "ScriptSettings" + File.separator + (manifest?.name ?: "EmptyScript"))
    }

    override fun stop() {
        if (timer.getElapsedTime() > 50 * 60 * 1000) {
            val img = painter.saveProgressImage()
            val link = Imgur.upload(img)
            println("Uploaded proggy to imgur: $link")
            Discord.upload(link)
        }
    }

}