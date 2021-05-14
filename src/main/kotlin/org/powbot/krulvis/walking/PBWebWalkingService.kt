package org.powbot.krulvis.walking

import org.powbot.krulvis.api.ATContext.loaded
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.walking.model.*
import org.powerbot.script.*
import org.powerbot.script.rt4.ClientContext
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.concurrent.Callable

fun Tile.toWebTile(): WebTile {
    return WebTile(x(), y(), floor())
}

fun WebTile.toRegularTile(): Tile {
    return Tile(x, y, z)
}

object PBWebWalkingService : WebWalkingService {

//    companion object {
//    }

    val logger = LoggerFactory.getLogger(PBWebWalkingService::class.java)

    val playerState = PlayerState()

    override fun walkTo(loc: Locatable, refreshQuests: Boolean): Boolean {
        return move(loc, refreshQuests).success
    }

    override fun moveTo(
        loc: Locatable,
        refreshQuests: Boolean,
        forceWeb: Boolean
    ): org.powbot.walking.WebWalkingResult {
        TODO("Not required")
    }

    override fun moveTo(
        loc: Locatable,
        refreshQuests: Boolean,
        forceWeb: Boolean,
        walkUntil: Callable<Boolean>,
        runMin: Int,
        runMax: Int
    ): org.powbot.walking.WebWalkingResult {
        TODO("Not implemented Yet")
    }

    fun move(loc: Locatable, refreshQuests: Boolean): PBWebWalkingResult {
        return move(loc, refreshQuests, { false }, Random.nextInt(50, 55), Random.nextInt(56, 60))
    }

    fun move(
        loc: Locatable,
        refreshQuests: Boolean,
        walkUntil: Callable<Boolean>,
        runMin: Int,
        runMax: Int
    ): PBWebWalkingResult {
        try {
            if (ClientContext.ctx().players.local().tile().distanceTo(loc) <= 0) {
                return PBWebWalkingResult(false, true, null)
            }
            if (loc.tile().distance() <= 16 && loc.tile().loaded()) {
//                &&                Walking.traverseLocally(loc.tile(), walkUntil, runMin, runMax){
                /**
                 * Added custom localwalker
                 */
                val localPath = LocalPathFinder.findPath(loc.tile())
                localPath.traverse()
                return PBWebWalkingResult(false, localPath.isNotEmpty(), null)
            }

            val player = playerState.player(refreshQuests)
            val path = WebWalkingClient.instance.getPath(
                GeneratePathRequest(
                    player = player ?: return PBWebWalkingResult(true, false, FailureReason.CantLoadPlayer),
                    destination = loc.tile().toWebTile()
                )
            )

            if (path == null || path.isEmpty()) {
                logger.info("Failed to find path to $loc from ${player.location}")
                return PBWebWalkingResult(true, false, FailureReason.NoPath)
            }

            return Walking.walkPath(path, loc.tile(), walkUntil, runMin, runMax)
        } catch (t: Throwable) {
            logger.error("Failed to walk", t)
            return PBWebWalkingResult(true, false, FailureReason.ExceptionThrown)
        }
    }
}
