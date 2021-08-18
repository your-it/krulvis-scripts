package org.powbot.krulvis.api.script

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color.WHITE
import org.powbot.api.event.RenderEvent
import org.powbot.api.script.tree.TreeScript
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.randoms.BondPouch
import org.powbot.krulvis.api.extensions.randoms.RandomHandler
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.trackers.LootTracker
import org.powbot.krulvis.api.utils.trackers.SkillTracker
import org.powbot.mobile.script.ScriptManager
import java.io.File

abstract class ATScript : TreeScript() {

    override fun onStart() {
        log.info("Starting..")
        startTracking()
    }

    abstract val painter: ATPainter<*>
    val timer = Timer()
    val skillTracker = SkillTracker(this)
    val lootTracker = LootTracker(this)
    val oddsModifier = OddsModifier()
    val walkDelay = DelayHandler(500, 700, oddsModifier, "Walk Delay")
    var nextRun: Int = Random.nextInt(1, 6)
    val randomHandlers = mutableListOf<RandomHandler>(BondPouch())

    fun startTracking() {
        Thread {
            log.info("Started tracking thread")
            while (!ScriptManager.isStopping()) {
                skillTracker.track()
//            inventoryWatcher.watch()
//            animationWatcher.watch()
                Thread.sleep(500)
            }
            log.info("Ended tracking thread")
        }.start()
    }

    override fun poll() {
        val rh = randomHandlers.firstOrNull { it.validate() }
        if (rh != null) rh.execute() else super.poll()
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

    @Subscribe
    fun onRender(e: RenderEvent) {
        val g = e.graphics
        painter.onRepaint(g)
    }

    /**
     * Called whenever the script is stopped, This doesn't actually stop the script
     */
    override fun onStop() {
//        if (timer.getElapsedTime() > 50 * 60 * 1000) {
//            val img = painter.saveProgressImage()
//            val link = Imgur.upload(img)
//            println("Uploaded proggy to imgur: $link")
//            Discord.upload(link)
//        }
    }

}
