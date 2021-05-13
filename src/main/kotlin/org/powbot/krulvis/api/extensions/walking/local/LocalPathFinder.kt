package org.powbot.krulvis.api.extensions.walking.local


import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.Flag.Rotation
import org.powbot.krulvis.api.extensions.walking.PathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalDoorEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalTileEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartEdge
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.loaded
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
        if (!end.loaded()) {
            logger.info("Tile not loaded: $end")
            return LocalPath(emptyList())
        }
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


    enum class NeighBors(val currentFlag: Int, val currentRotation: Int, val nextRotation: Int) {
        NORTH(Flag.W_N, Rotation.NORTH, Rotation.SOUTH),
        EAST(Flag.W_E, Rotation.EAST, Rotation.WEST),
        SOUTH(Flag.W_S, Rotation.SOUTH, Rotation.NORTH),
        WEST(Flag.W_W, Rotation.WEST, Rotation.EAST);

        fun getEdge(currentEdge: LocalEdge, flags: ICollisionMap): Optional<LocalEdge> {
            val current = currentEdge.destination
            val neighbor = when (this) {
                NORTH -> Tile(current.x(), current.y() + 1, current.floor())
                EAST -> Tile(current.x() + 1, current.y(), current.floor())
                SOUTH -> Tile(current.x(), current.y() - 1, current.floor())
                WEST -> Tile(current.x() - 1, current.y(), current.floor())
            }
            if (!neighbor.blocked(flags)) {
                if (!current.blocked(flags, currentFlag)) {
                    return Optional.of(
                        LocalTileEdge(
                            currentEdge,
                            neighbor,
                            currentEdge.finalDestination
                        )
                    )
                } else {
                    var door = getDoor(current, currentRotation)
                    if (door == GameObject.NIL) door = getDoor(neighbor, nextRotation)
                    if (door != GameObject.NIL) {
                        return Optional.of(
                            LocalDoorEdge(
                                door,
                                currentEdge,
                                neighbor,
                                currentEdge.finalDestination
                            )
                        )
                    }
                }
            }
            return Optional.empty()
        }
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

        NeighBors.values().forEach {
            val edge = it.getEdge(this, flags)
            if (edge.isPresent) {
                logger.info("Found neighbor ${it.name}: ${edge.get()}")
                neighbors.add(edge.get())
            }
        }

        val n = Tile(current.x(), current.y() + 1, p)
        val e = Tile(current.x() + 1, current.y(), p)
        val s = Tile(current.x(), current.y() - 1, p)
        val w = Tile(current.x() - 1, current.y(), p)
        val ne = Tile(current.x() + 1, current.y() + 1, p)
        val se = Tile(current.x() + 1, current.y() - 1, p)
        val sw = Tile(current.x() - 1, current.y() - 1, p)
        val nw = Tile(current.x() - 1, current.y() + 1, p)

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