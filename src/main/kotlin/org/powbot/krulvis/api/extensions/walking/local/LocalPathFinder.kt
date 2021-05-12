package org.powbot.krulvis.api.extensions.walking.local


import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.Flag.Rotation
import org.powbot.krulvis.api.extensions.walking.PathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalDoorEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalTileEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartEdge
import org.powbot.krulvis.api.ATContext.blocked
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.ClientContext
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


object LocalPathFinder : PathFinder {

    val maxAttempts = 2500
    lateinit var cachedFlags: ICollisionMap
    val logger = Logger.getLogger("LocalPathFinder")

    fun findPath(end: Tile?): LocalPath {
        return if (end != null) {
            findPath(ClientContext.ctx().players.local().tile(), end)
        } else {
            LocalPath(emptyList())
        }
    }

    fun findPath(begin: Tile, end: Tile): LocalPath {
        this.cachedFlags = ClientContext.ctx().client().collisionMaps[begin.floor()]
        val end = if (end.blocked(cachedFlags)) end.getWalkableNeighbor() else end

        val startAction = StartEdge(
            begin,
            end ?: begin
        )
        if (begin == end || end == null) {
            logger.info("Finding path from=$begin to=$end")
            return LocalPath(listOf(startAction))
        }
        logger.log(Level.INFO, "FIND LOCAL PATH: $begin -> $end, distance: ${begin.distanceTo(end)}")
        var attempts = 0

        val open = startAction.getLocalNeighbors(end)
        val searched = mutableListOf<LocalEdge>()

        while (open.isNotEmpty() && attempts < maxAttempts) {
            val next = getBest(open) ?: continue
            open.remove(next)
//            println("${next.javaClass.simpleName}: ${next.source} -> ${next.destination}")

            if (next.destination == end) {
                return buildPath(next)
            }
            val neighbors = next.getNeighbors().filterNot {
                open.contains(it.destination) || searched.contains(it.destination)
            }
//            println("Found ${neighbors.size} neighbors")
            open.addAll(neighbors)

            searched.add(next)
            attempts++
        }
        logger.log(Level.WARNING, "Ran out of attempts trying to get LOCAL path to: $end")

        return LocalPath(emptyList())
    }

    fun buildPath(last: LocalEdge): LocalPath {
        val path = LinkedList<LocalEdge>()
        var r = last
        while (r !is StartEdge) {
            path.add(r)
            r = r.parent
        }
        return LocalPath(path.reversed())
    }

    fun getBest(open: Collection<LocalEdge>): LocalEdge? {
        return open.minByOrNull { it.getPathCost() + it.heuristics }
    }

    fun MutableList<LocalEdge>.contains(destination: Tile): Boolean {
        return any { it.destination == destination }
    }

    /**
     * Used to find neighbors of LocalEdge
     */
    fun LocalEdge.getLocalNeighbors(
        finalDesination: Tile,
        flags: ICollisionMap = cachedFlags
    ): MutableList<LocalEdge> {
        val neighbors = mutableListOf<LocalEdge>()

        val current = destination
        val p = current.floor()

        val n = Tile(current.x(), current.y() + 1, p)
        val e = Tile(current.x() + 1, current.y(), p)
        val s = Tile(current.x(), current.y() - 1, p)
        val w = Tile(current.x() - 1, current.y(), p)

        val ne = Tile(current.x() + 1, current.y() + 1, p)
        val se = Tile(current.x() + 1, current.y() - 1, p)
        val sw = Tile(current.x() - 1, current.y() - 1, p)
        val nw = Tile(current.x() - 1, current.y() + 1, p)

        if (!n.blocked(flags)) {
            if (!current.blocked(flags, Flag.W_N)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        n,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.NORTH)
                if (door == GameObject.NIL) door = getDoor(n, Rotation.SOUTH)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            n,
                            finalDesination
                        )
                    )
                }
            }
        }
        if (!e.blocked(flags)) {
            if (!current.blocked(flags, Flag.W_E)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        e,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.EAST)
                if (door == GameObject.NIL) door = getDoor(e, Rotation.WEST)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            e,
                            finalDesination
                        )
                    )
                }
            }
        }
        if (!s.blocked(flags)) {
            if (!current.blocked(flags, Flag.W_S)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        s,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.SOUTH)
                if (door == GameObject.NIL) door = getDoor(s, Rotation.NORTH)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            s,
                            finalDesination
                        )
                    )
                }
            }
        }
        if (!w.blocked(flags)) {
            if (!current.blocked(flags, Flag.W_W)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        w,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.WEST)
                if (door == GameObject.NIL) door = getDoor(w, Rotation.EAST)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            w,
                            finalDesination
                        )
                    )
                }
            }
        }

        if (!current.blocked(flags, Flag.W_NE or Flag.W_N or Flag.W_E)
            && !n.blocked(flags, Flag.W_E)
            && !e.blocked(flags, Flag.W_N)
        ) {
            if (!ne.blocked(flags)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        ne,
                        finalDesination
                    )
                )
            } else {
                val door = getDoor(ne, Rotation.DIAGONAL)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            ne,
                            finalDesination
                        )
                    )
                }
            }
        }
        if (!current.blocked(flags, Flag.W_SE or Flag.W_S or Flag.W_E)
            && !s.blocked(flags, Flag.W_E)
            && !e.blocked(flags, Flag.W_S)
        ) {
            if (!se.blocked(flags)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        se,
                        finalDesination
                    )
                )
            } else {
                val door = getDoor(se, Rotation.DIAGONAL)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            se,
                            finalDesination
                        )
                    )
                }
            }
        }
        if (!current.blocked(flags, Flag.W_SW or Flag.W_S or Flag.W_W)
            && !s.blocked(flags, Flag.W_W)
            && !w.blocked(flags, Flag.W_S)
        ) {
            if (!sw.blocked(flags)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        sw,
                        finalDesination
                    )
                )
            } else {
                val door = getDoor(sw, Rotation.DIAGONAL)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            sw,
                            finalDesination
                        )
                    )
                }
            }
        }
        if (!current.blocked(flags, Flag.W_NW or Flag.W_N or Flag.W_W)
            && !n.blocked(flags, Flag.W_W)
            && !w.blocked(flags, Flag.W_N)
        ) {
            if (!nw.blocked(flags)) {
                neighbors.add(
                    LocalTileEdge(
                        this,
                        nw,
                        finalDesination
                    )
                )
            } else {
                val door = getDoor(nw, Rotation.DIAGONAL)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorEdge(
                            door,
                            this,
                            nw,
                            finalDesination
                        )
                    )
                }
            }
        }

        return neighbors
    }
}