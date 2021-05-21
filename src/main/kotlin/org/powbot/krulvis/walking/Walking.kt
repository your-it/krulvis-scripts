package org.powbot.krulvis.walking

import org.powbot.krulvis.api.ATContext.loaded
import org.powbot.krulvis.api.extensions.walking.local.LocalPath
import org.powbot.krulvis.api.extensions.walking.local.LocalPath.Companion.getNext
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartEdge
import org.powbot.krulvis.api.utils.Random
import org.powbot.walking.model.Edge
import org.powbot.walking.model.EdgeType
import org.powbot.walking.model.TileInteraction
import org.powerbot.script.ClientContext.*
import org.powerbot.script.Condition
import org.powerbot.script.Tile
import org.powerbot.script.rt4.ClientContext
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable

object Walking {

    val logger = LoggerFactory.getLogger("Walking")

    val maxNextTileDistance = 25
    private fun findNext(path: List<Edge<*>>, visited: Set<Edge<*>>, finalDestination: Tile): Edge<*>? {
        if (path.isEmpty()) {
            logger.info("Path is empty")
            return null
        }

        /**
         * Filter only edges that are a feasible candidate
         */
        val filteredEdges = path.filter { edge ->
            val from = edge.from
            !visited.contains(edge) && (from == null || from.toRegularTile().distance() <= maxNextTileDistance)
        }

//        logger.info("findNext() filteredEdges: ${filteredEdges.size}")

        /**
         * Check if there is any edge within the feasible edges that requires a special interaction
         */
        val specialEdge = filteredEdges.firstOrNull { it.type != EdgeType.Tile }
        if (specialEdge != null) {
            return specialEdge
        }

        /**
         * If the final destination is close, make a new edge with the destination as `to`
         */
        return if (finalDestination.distance() < maxNextTileDistance && finalDestination.loaded()) object :
            Edge<TileInteraction>(
                from = path.last().to,
                to = finalDestination.toWebTile(),
                type = EdgeType.Tile,
                interaction = TileInteraction()
            ) {} else filteredEdges.lastOrNull {
            val to = it.to.toRegularTile()
            to.distance() <= maxNextTileDistance && to.loaded()
        }
    }

    private fun waitTillIdle() {
        Condition.wait({
            ClientContext.ctx().movement.destination() == Tile.NIL &&
                    !ClientContext.ctx().players.local().inMotion()
        }, 600, 15)
    }

    private fun atDestination(destination: Tile, waitIdle: Boolean = false): Boolean {
        if (waitIdle) {
            waitTillIdle()
        }
        val myDest = ClientContext.ctx().movement.destination() ?: Tile.NIL
        val myTile = if (myDest != Tile.NIL) myDest else ClientContext.ctx().players.local().tile()
        val a = myTile.distanceTo(destination).toInt() <= 1
//        println("AtDestination: $a, myTile: $myTile, destination: $destination")
        return a
    }

    fun walkPath(
        _path: List<Edge<*>>,
        finalDestination: Tile,
        walkUntil: Callable<Boolean>,
        runMin: Int,
        runMax: Int
    ): PBWebWalkingResult {
        var path = _path
        var attempts = 0
        val visited = mutableSetOf<Edge<*>>()
        var nextEdge: Edge<*>? = null
        var nextTile = Tile.NIL

        logger.info("Path is [$_path]")
        while (
            !atDestination(finalDestination)
            && ctx().controller.script() != null
            && attempts < 5
        ) {
            if (walkUntil.call()) {
                break
            }

            nextEdge = findNext(path, visited, finalDestination)
            if (nextEdge == null) {
                logger.info("Could not find next Edge")
                break
            }
            nextTile = nextEdge.to.toRegularTile()

            logger.info("Next step is $nextEdge, distance: ${nextTile.distance()}")

            val runOn = Random.nextInt(runMin, runMax) >= ctx().movement.energyLevel()
            val result = if (nextEdge.type == EdgeType.Tile) {
                traverseLocally(
                    nextTile,
                    walkUntil,
                    runOn,
                    nextTile == finalDestination
                )
            } else {
                if (nextEdge.from != null && nextEdge.from!!.toRegularTile().distance() > 2) {
                    traverseLocally(nextEdge.from!!.toRegularTile(), walkUntil, runOn)
                }
                logger.info("Handling special edge: $nextEdge")

                nextEdge.interaction.handle()
            }

            if (result) {
                visited.add(nextEdge)
                attempts = 0
                if (path.indexOf(nextEdge) + 1 >= path.size) {
                    path = path.subList(path.indexOf(nextEdge), path.size)
                } else {
                    path = path.subList(path.indexOf(nextEdge) + 1, path.size)
                }
            } else {
                attempts++
            }
        }

        if (atDestination(finalDestination, true) || walkUntil.call()) {
            return PBWebWalkingResult(true, true, null)
        }

        val failureReason = if (nextEdge == null) {
            logger.info("Next node is null")
            FailureReason.CantReachNextNode
        } else {
            if (nextEdge.type != EdgeType.Tile) {
                FailureReason.FailedInteract
            } else if (!ClientContext.ctx().movement.reachable(
                    ClientContext.ctx().players.local().tile(),
                    nextTile
                )
            ) {
                logger.info("Can't react next to edge [$nextEdge] loaded: ${nextTile.loaded()}")

                FailureReason.CantReachNextNode
            } else {
                FailureReason.Unknown
            }
        }
        return PBWebWalkingResult(true, false, failureReason)
    }

    private fun nearLocalDestination(localDest: Tile, path: LocalPath): Boolean {
        val dest = ClientContext.ctx().movement.destination()
        return if (path.containsSpecialNode()) {
            localDest.distance() < 1
        } else if (dest != Tile.NIL) {
            dest.distanceTo(localDest) <= 2
        } else {
            localDest.distance() <= 6
        }
    }

    fun traverseLocally(
        edgeDest: Tile,
        walkUntil: Callable<Boolean>,
        runOn: Boolean,
        finalTile: Boolean = false
    ): Boolean {
        logger.info("Traversing locally to=$edgeDest, is final tile=$finalTile")
        if (edgeDest.floor() != ClientContext.ctx().client().floor) {
            return false
        }

        if (atDestination(edgeDest)) {
            return true
        }

        var path = LocalPathFinder.findPath(edgeDest)
        logger.info("Traversing locally created path to=$edgeDest, with size: ${path.size}")
        var attempts = 0
        while (
            path.isNotEmpty() &&
            (if (finalTile) !atDestination(edgeDest) else !nearLocalDestination(edgeDest, path)) &&
            !ctx().controller.isStopping &&
            attempts <= 5
        ) {
            if (walkUntil.call()) {
                return true
            }
            if (runOn && !ctx().movement.running() && ctx().movement.energyLevel() > 0) {
                ctx().movement.running(true)
            }
            val next = path.actions.getNext() ?: break
            logger.info("LocalTraverse next: $next")
            if (next is StartEdge && next.destination.distance() <= 1) {
                logger.info("Standing next to StartTile: $next")
                return true
            }
            if (next.execute()) {
                Condition.wait {
                    walkUntil.call() || nearLocalDestination(next.destination, path)
                }
            }
            path = LocalPathFinder.findPath(edgeDest)
            attempts++
        }
        val success = nearLocalDestination(edgeDest, path)
        logger.info("Traversing locally to=$edgeDest, was ${if (success) "successful" else "unsuccessful"}")
        return success
    }
}
