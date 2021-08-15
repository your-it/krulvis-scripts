package org.powbot.krulvis.api.extensions.walking.local


import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.Flag.Rotation
import org.powbot.krulvis.api.extensions.walking.PathFinder
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.extensions.walking.PathFinder.Companion.getPassableObject
import org.powbot.krulvis.api.extensions.walking.PathFinder.Companion.rockfallBlock
import org.powbot.krulvis.api.extensions.walking.local.nodes.*
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.mobile.BotManager
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


object LocalPathFinder : PathFinder<LocalPath> {

    val maxAttempts = 2500
    lateinit var cachedFlags: Array<IntArray>
    val logger = Logger.getLogger("LocalPathFinder")

    override fun findPath(end: Tile?): LocalPath {
        return if (end != null) {
            findPath(Players.local().tile(), end)
        } else {
            LocalPath(emptyList())
        }
    }

    fun findWalkablePath(end: Tile) = findWalkablePath(Players.local().tile(), end)

    fun findWalkablePath(begin: Tile, end: Tile) = findPath(begin, end, onlyWalk = true)

    fun findPath(begin: Tile, end: Tile, onlyWalk: Boolean = false, refreshFlags: Boolean = true): LocalPath {
        if (refreshFlags) {
            this.cachedFlags = Movement.collisionMap(begin.floor()).flags()
        }
        val end = if (end.blocked(cachedFlags)) end.getWalkableNeighbor() else end

        if (end == null || !end.loaded()) {
            logger.info("Tile not loaded: $end")
            return LocalPath(emptyList())
        }

        val startAction = StartEdge(
            begin,
            end
        )
        if (begin == end) {
            logger.info("Finding path from=$begin to=$end")
            return LocalPath(listOf(startAction))
        }
        logger.log(Level.INFO, "FIND LOCAL PATH: $begin -> $end, distance: ${begin.distanceTo(end)}")
        var attempts = 0

        val open = startAction.getLocalNeighbors(onlyWalk = true)
        val searched = mutableListOf<LocalEdge>()

        while (open.isNotEmpty() && attempts < maxAttempts) {
            val next = getBest(open) ?: continue
            open.remove(next)
//            println("${next.javaClass.simpleName}: ${next.source} -> ${next.destination}")

            if (next.destination == end) {
                return buildPath(next)
            }
            val neighbors = next.getNeighbors(onlyWalk).filterNot {
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

        fun getEdge(
            currentEdge: LocalEdge,
            flags: Array<IntArray>,
            onlyWalk: Boolean = false
        ): Optional<LocalEdge> {
            val current = currentEdge.destination
            val neighbor = when (this) {
                NORTH -> Tile(current.x(), current.y() + 1, current.floor())
                EAST -> Tile(current.x() + 1, current.y(), current.floor())
                SOUTH -> Tile(current.x(), current.y() - 1, current.floor())
                WEST -> Tile(current.x() - 1, current.y(), current.floor())
            }
            if (!neighbor.blocked(flags)) {
                /**
                 * Tiles are not blocked with a door, they have a special block flag that
                 * is dependent on the `currentFlag` which represents a side
                 */
                if (!current.blocked(flags, currentFlag) || current.rockfallBlock(flags)) {
                    return Optional.of(
                        LocalTileEdge(
                            currentEdge,
                            neighbor
                        )
                    )
                } else if (!onlyWalk) {
                    var door = current.getPassableObject(currentRotation)
                    if (!door.isPresent) door = neighbor.getPassableObject(nextRotation)
                    if (door.isPresent) {
                        return Optional.of(
                            LocalDoorEdge(
                                door.get(),
                                currentEdge,
                                neighbor
                            )
                        )
                    }
                }
            } else if (!onlyWalk && neighbor.rockfallBlock(flags)) {
                if (!current.blocked(flags, currentFlag)) {
                    return Optional.of(
                        LocalRockfallEdge(
                            currentEdge,
                            neighbor
                        )
                    )
                }
            }
            return Optional.empty()
        }
    }

    /**
     * Used to find neighbors of LocalEdge
     */
    fun LocalEdge.getLocalNeighbors(
        flags: Array<IntArray> = cachedFlags,
        onlyWalk: Boolean = false
    ): MutableList<LocalEdge> {
        val neighbors = mutableListOf<LocalEdge>()

        val current = destination
        val p = current.floor()

        /**
         * Straight neighbors get added here
         */
        NeighBors.values().forEach {
            val edge = it.getEdge(this, flags, onlyWalk)
            if (edge.isPresent) {
//                logger.info("Found neighbor ${it.name}: ${edge.get()}")
                neighbors.add(edge.get())
            }
        }

        if (onlyWalk) {
            return neighbors
        }

        /**
         * Now we build additional diagonal neighbors
         */
        val n = Tile(current.x(), current.y() + 1, p)
        val e = Tile(current.x() + 1, current.y(), p)
        val s = Tile(current.x(), current.y() - 1, p)
        val w = Tile(current.x() - 1, current.y(), p)
        val ne = Tile(current.x() + 1, current.y() + 1, p)
        val se = Tile(current.x() + 1, current.y() - 1, p)
        val sw = Tile(current.x() - 1, current.y() - 1, p)
        val nw = Tile(current.x() - 1, current.y() + 1, p)

        /**
         * Get diagonal edges
         */
        if (!current.blocked(flags, Flag.W_NE or Flag.W_N or Flag.W_E)
            && !n.blocked(flags, Flag.W_E)
            && !e.blocked(flags, Flag.W_N)
        ) {
            getEdgeForDiagonalNeighbor(ne, flags).ifPresent { neighbors.add(it) }
        }
        if (!current.blocked(flags, Flag.W_SE or Flag.W_S or Flag.W_E)
            && !s.blocked(flags, Flag.W_E)
            && !e.blocked(flags, Flag.W_S)
        ) {
            getEdgeForDiagonalNeighbor(se, flags).ifPresent { neighbors.add(it) }
        }
        if (!current.blocked(flags, Flag.W_SW or Flag.W_S or Flag.W_W)
            && !s.blocked(flags, Flag.W_W)
            && !w.blocked(flags, Flag.W_S)
        ) {
            getEdgeForDiagonalNeighbor(sw, flags).ifPresent { neighbors.add(it) }
        }
        if (!current.blocked(flags, Flag.W_NW or Flag.W_N or Flag.W_W)
            && !n.blocked(flags, Flag.W_W)
            && !w.blocked(flags, Flag.W_N)
        ) {
            getEdgeForDiagonalNeighbor(nw, flags).ifPresent { neighbors.add(it) }
        }

        return neighbors
    }

    /**
     * If the given tile (diagonal neighbor) is not blocked, it gets added to the neighbors.
     * If it is blocked, we check if there is a door that has a diagonal rotation [Flag.Rotation.DIAGONAL]
     */
    fun LocalEdge.getEdgeForDiagonalNeighbor(
        neighbor: Tile,
        flags: Array<IntArray>
    ): Optional<LocalEdge> {
        if (!neighbor.blocked(flags)) {
            return Optional.of(
                LocalTileEdge(
                    this,
                    neighbor
                )
            )
        } else {
            val door = neighbor.getPassableObject(Rotation.DIAGONAL)
            if (door.isPresent) {
                return Optional.of(
                    LocalDoorEdge(
                        door.get(),
                        this,
                        neighbor
                    )
                )
            }
        }
        return Optional.empty()
    }
}