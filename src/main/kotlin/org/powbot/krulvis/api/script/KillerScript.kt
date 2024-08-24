package org.powbot.krulvis.api.script

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptState
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.mobile.script.ScriptManager

abstract class KillerScript : ATScript() {
    abstract val ammoIds: IntArray
    var currentTarget: Npc = Npc.Nil
    var lootWachter: LootWatcher? = null
    val deathWatchers = mutableListOf<NpcDeathWatcher>()
    var kills: Int = 0
    private val slayerBraceletNames = arrayOf("Bracelet of slaughter", "Expeditious bracelet")
    fun getSlayerBracelet() = Inventory.stream().name(*slayerBraceletNames).first()
    val hasSlayerBracelet by lazy { getSlayerBracelet().valid() }
    fun wearingSlayerBracelet() = Equipment.stream().name(*slayerBraceletNames).isNotEmpty()

    var reducedStats = false

    val lootList = mutableListOf<GroundItem>()
    abstract fun GroundItem.isLoot(): Boolean

    fun isLootWatcherActive() = lootWachter?.active == true
    fun watchLootDrop(tile: Tile) {
        reducedStats = false
        if (!isLootWatcherActive()) {
            logger.info("Waiting for loot at $tile")
            lootWachter = LootWatcher(tile, ammoIds, lootList = lootList, isLoot = { it.isLoot() })
        } else {
            logger.info("Already watching loot at tile: $tile for loot")
        }
    }

    fun setCurrentTarget() {
        val interacting = me.interacting()
        if (interacting is Npc && interacting != Npc.Nil) {
            currentTarget = interacting
            val activeLW = lootWachter
            if (activeLW?.active == true && activeLW.tile.distanceTo(currentTarget.tile()) < 2) return
            val deathWatcher = deathWatchers.firstOrNull { it.npc == currentTarget }
            if (deathWatcher == null || !deathWatcher.active) {
                val newDW = NpcDeathWatcher(
                    interacting,
                    false
                ) {
                    kills++
                    if (hasSlayerBracelet && !wearingSlayerBracelet()) {
                        val slayBracelet = getSlayerBracelet()
                        if (slayBracelet.valid()) {
                            getSlayerBracelet().fclick()
                            logger.info("Wearing bracelet on death at ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
                        }
                    }
                    watchLootDrop(interacting.tile())
                }
                deathWatchers.add(newDW)
            }
        }
        deathWatchers.removeAll { !it.active }
    }

    @Subscribe
    fun onKillerTickEvent(_e: TickEvent) {
        if (ScriptManager.state() != ScriptState.Running) return
        setCurrentTarget()
    }

}