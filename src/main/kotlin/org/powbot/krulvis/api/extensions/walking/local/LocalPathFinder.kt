package org.powbot.krulvis.api.extensions.walking.local


import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.Flag.Rotation
import org.powbot.krulvis.api.extensions.walking.PathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalDoorAction
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalTileAction
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalWebAction
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartTile
import org.powbot.krulvis.api.script.ATScript
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.util.*


class LocalPathFinder(override val script: ATScript) :
    PathFinder {

    val maxAttempts = 2500
    lateinit var flags: ICollisionMap

    fun findPath(end: Tile?): LocalPath {
        return if (end != null) {
            findPath(me.tile(), end)
        } else {
            LocalPath(emptyList(), script)
        }
    }

    fun findPath(begin: Tile, end: Tile): LocalPath {
        this.flags = ctx.client().collisionMaps[begin.floor()]
        val end = if (end.blocked(flags)) end.getWalkableNeighbor() else end

        val startAction = StartTile(
            begin,
            end ?: begin
        )
        if (begin == end || end == null) {
            return LocalPath(listOf(startAction), script)
        }
        debug("FIND LOCAL PATH: $begin -> $end")
        var attempts = 0

        val open = getLocalNeighbors(startAction, end)
        val searched = mutableListOf<LocalWebAction>()

        while (open.isNotEmpty() && attempts < maxAttempts) {
            val next = getBest(open) ?: continue
            open.remove(next)
//            println("${next.javaClass.simpleName}: ${next.source} -> ${next.destination}")

            if (next.destination == end) {
                return buildPath(next)
            }
            val neighbors = next.getNeighbors(this).filterNot {
                open.contains(it.destination) || searched.contains(it.destination)
            }
//            println("Found ${neighbors.size} neighbors")
            open.addAll(neighbors)

            searched.add(next)
            attempts++
        }
        debug("Ran out of attempts trying to get LOCAL path")

        return LocalPath(emptyList(), script)
    }

    fun buildPath(last: LocalWebAction): LocalPath {
        val path = LinkedList<LocalWebAction>()
        var r = last
        while (r !is StartTile) {
            path.add(r)
            r = r.parent
        }
        return LocalPath(path.reversed(), script)
    }

    fun getBest(open: Collection<LocalWebAction>): LocalWebAction? {
        return open.minBy { it.getPathCost(this) + it.heuristics }
    }

    fun MutableList<LocalWebAction>.contains(destination: Tile): Boolean {
        return any { it.destination == destination }
    }

    /**
     * Used to find neighbors of LocalTile's.
     */
    fun getLocalNeighbors(
        src: LocalWebAction,
        finalDesination: Tile,
        flags: ICollisionMap = this.flags
    ): MutableList<LocalWebAction> {
        val neighbors = mutableListOf<LocalWebAction>()

        val current = src.destination
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
                    LocalTileAction(
                        src,
                        n,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.NORTH)
                if(door == GameObject.NIL) door = getDoor(n, Rotation.SOUTH)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorAction(
                            door,
                            src,
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
                    LocalTileAction(
                        src,
                        e,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.EAST)
                if(door == GameObject.NIL) door = getDoor(e, Rotation.WEST)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorAction(
                            door,
                            src,
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
                    LocalTileAction(
                        src,
                        s,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.SOUTH)
                if(door == GameObject.NIL) door = getDoor(s, Rotation.NORTH)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorAction(
                            door,
                            src,
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
                    LocalTileAction(
                        src,
                        w,
                        finalDesination
                    )
                )
            } else {
                var door = getDoor(current, Rotation.WEST)
                if(door == GameObject.NIL) door = getDoor(w, Rotation.EAST)
                if (door != GameObject.NIL) {
                    neighbors.add(
                        LocalDoorAction(
                            door,
                            src,
                            w,
                            finalDesination
                        )
                    )
                }
            }
        }

        if (!ne.blocked(flags)
            && !current.blocked(flags, Flag.W_NE or Flag.W_N or Flag.W_E)
            && !n.blocked(flags, Flag.W_E)
            && !e.blocked(flags, Flag.W_N)
        ) {
            neighbors.add(
                LocalTileAction(
                    src,
                    ne,
                    finalDesination
                )
            )
        }
        if (!se.blocked(flags)
            && !current.blocked(flags, Flag.W_SE or Flag.W_S or Flag.W_E)
            && !s.blocked(flags, Flag.W_E)
            && !e.blocked(flags, Flag.W_S)
        ) {
            neighbors.add(
                LocalTileAction(
                    src,
                    se,
                    finalDesination
                )
            )
        }
        if (!sw.blocked(flags)
            && !current.blocked(flags, Flag.W_SW or Flag.W_S or Flag.W_W)
            && !s.blocked(flags, Flag.W_W)
            && !w.blocked(flags, Flag.W_S)
        ) {
            neighbors.add(
                LocalTileAction(
                    src,
                    sw,
                    finalDesination
                )
            )
        }
        if (!nw.blocked(flags)
            && !current.blocked(flags, Flag.W_NW or Flag.W_N or Flag.W_W)
            && !n.blocked(flags, Flag.W_W)
            && !w.blocked(flags, Flag.W_N)
        ) {
            neighbors.add(
                LocalTileAction(
                    src,
                    nw,
                    finalDesination
                )
            )
        }

        return neighbors
    }
}