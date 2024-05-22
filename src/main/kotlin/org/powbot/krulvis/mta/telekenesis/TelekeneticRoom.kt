package org.powbot.krulvis.mta.telekenesis


import org.powbot.api.Area
import org.powbot.api.Color
import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.krulvis.api.ATContext.me
import org.powbot.util.TransientGetter2D
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TelekineticRoom {
    const val TELEKINETIC_METHOD = "Telekenesis"
    const val WIDGET_ID = 198
    private val MAZE_GUARDIAN_MOVING: Int = 6778
    private val TELEKINETIC_WALL: Int = 10755
    private val TELEKINETIC_FINISH: Int = 23672

    private var telekineticWalls: MutableList<GameObject> = ArrayList<GameObject>()

    private var moves: Stack<Direction> = Stack<Direction>()
    private var destination: Tile = Tile.Nil
    var finishLocation: Tile = Tile.Nil
    private var bounds: Rectangle = Rectangle(-1, -1, -1, -1)
    private var guardian: Npc = Npc.Nil
    private var numMazeWalls = 0

    fun resetRoom() {
        finishLocation = Tile.Nil
        telekineticWalls.clear()
    }

    fun shouldInstantiate() = finishLocation == Tile.Nil

    fun instantiateRoom() {
        finishLocation = Objects.stream().type(GameObject.Type.FLOOR_DECORATION).id(TELEKINETIC_FINISH).first().tile
        guardian = getGuardian()
        val walls = Objects.stream().type(GameObject.Type.BOUNDARY).id(TELEKINETIC_WALL).toList()
        numMazeWalls = walls.size
        bounds = getBounds(walls)
    }

    fun getGuardian() = Npcs.stream().name("", "Maze Guardian").first()


    fun buildMoves() {
        guardian = getGuardian()
        val flags = Movement.collisionMap(0).flags()
        destination = guardianDestination(flags)
        moves = build(flags)
    }

    //
//    @Subscribe
//    fun onGameTick(event: TickEvent) {
//        if (!inside()) {
//            numMazeWalls = 0
//            moves.clear()
//            return
//        }
//
//        if (telekineticWalls.size != numMazeWalls) {
//            bounds = getBounds(telekineticWalls.toArray(arrayOfNulls<WallObject>(0)))
//            numMazeWalls = telekineticWalls.size
//            client.clearHintArrow()
//        } else if (guardian != null) {
//            val current: Tile
//            if (guardian.getId() === MAZE_GUARDIAN_MOVING) {
//                destination = guardianDestination
//                current = Tile.fromLocal(client, destination)
//            } else {
//                destination = null
//                current = guardian.getWorldLocation()
//            }
//
//            //Prevent unnecessary updating when the guardian has not moved
//            if (current.equals(location)) {
//                return
//            }
//
//            log.debug("Updating guarding location {} -> {}", location, current)
//
//            location = current
//
//            if (location.equals(finishLocation)) {
//                client.clearHintArrow()
//            } else {
//                log.debug("Rebuilding moves due to guardian move")
//                this.moves = build()
//            }
//        } else {
//            client.clearHintArrow()
//            moves.clear()
//        }
//    }
//
    fun inside(): Boolean {
        return Components.stream(WIDGET_ID).viewable().isNotEmpty()
    }

    //
    fun paint() {
        if (inside() && numMazeWalls > 0) {
            val guardianDestinationColor = if (guardian.id == MAZE_GUARDIAN_MOVING) Color.ORANGE else Color.GREEN
            destination.drawOnScreen(outlineColor = guardianDestinationColor)
            if (!moves.isEmpty()) {
                val moveColor =
                    if (moves.peek() == direction()) {
                        Color.GREEN
                    } else {
                        Color.RED
                    }

                guardian.tile().drawOnScreen(outlineColor = moveColor)

                optimal().drawOnScreen(outlineColor = moveColor)
            }
        }
    }

    private fun optimal(): Tile {
        val current: Tile = me.tile()

        val next: Direction = moves.pop()
        val areaNext: Area = getIndicatorLine(next)
        val nearestNext: Tile = nearest(areaNext, current)

        if (moves.isEmpty()) {
            moves.push(next)

            return nearestNext
        }

        val after: Direction = moves.peek()
        moves.push(next)
        val areaAfter: Area = getIndicatorLine(after)
        val nearestAfter: Tile = nearest(areaAfter, nearestNext)

        return nearest(areaNext, nearestAfter)
    }

    private fun nearest(area: Area, tile: Tile): Tile {
        var dist = Int.MAX_VALUE
        var nearest: Tile = Tile.Nil

        for (areaPoint in area.tiles) {
            val currDist = manhattan(areaPoint, tile)
            if (dist > currDist) {
                nearest = areaPoint
                dist = currDist
            }
        }

        return nearest
    }


    private fun build(flags: TransientGetter2D<Int>): Stack<Direction> {
        return if (guardian.id == MAZE_GUARDIAN_MOVING) {
            build(guardianDestination(flags), flags)
        } else {
            build(guardian.tile(), flags)
        }
    }

    private fun guardianDestination(flags: TransientGetter2D<Int>): Tile {
        val direction = Direction.fromOrientation(guardian.orientation())
        return neighbour(guardian.tile(), direction, flags)
    }

    private fun build(start: Tile, flags: TransientGetter2D<Int>): Stack<Direction> {
        val visit: Queue<Tile> = LinkedList()
        val closed: MutableSet<Tile> = HashSet<Tile>()
        val scores: MutableMap<Tile, Int> = HashMap<Tile, Int>()
        val edges: MutableMap<Tile, Tile> = HashMap<Tile, Tile>()
        scores[start] = 0
        visit.add(start)

        while (!visit.isEmpty()) {
            val next: Tile = visit.poll()
            closed.add(next)
            val neighbours: Array<Tile> = neighbours(next, flags)

            for (neighbour in neighbours) {
                if (neighbour != next
                    && !closed.contains(neighbour)
                ) {
                    val score = scores[next]!! + 1

                    if (!scores.containsKey(neighbour) || scores[neighbour]!! > score) {
                        scores[neighbour] = score
                        edges[neighbour] = next
                        visit.add(neighbour)
                    }
                }
            }
        }

        return build(edges, finishLocation)
    }

    private fun build(edges: Map<Tile, Tile>, finish: Tile): Stack<Direction> {
        val path: Stack<Direction> = Stack<Direction>()
        var current: Tile = finish

        while (edges.containsKey(current)) {
            val next: Tile = edges[current]!!

            if (next.x > current.x) {
                path.add(Direction.WEST)
            } else if (next.x < current.x) {
                path.add(Direction.EAST)
            } else if (next.y > current.y) {
                path.add(Direction.SOUTH)
            } else {
                path.add(Direction.NORTH)
            }

            current = next
        }

        return path
    }

    private fun neighbours(point: Tile, flags: TransientGetter2D<Int>): Array<Tile> {
        return arrayOf(
            neighbour(point, Direction.NORTH, flags), neighbour(point, Direction.SOUTH, flags),
            neighbour(point, Direction.EAST, flags), neighbour(point, Direction.WEST, flags)
        )
    }

    private fun neighbour(tile: Tile, direction: Direction, flags: TransientGetter2D<Int>): Tile {
        val dx: Int
        val dy: Int

        when (direction) {
            Direction.NORTH -> {
                dx = 0
                dy = 1
            }

            Direction.SOUTH -> {
                dx = 0
                dy = -1
            }

            Direction.EAST -> {
                dx = 1
                dy = 0
            }

            Direction.WEST -> {
                dx = -1
                dy = 0
            }
        }

        var travelTile = tile
        do {
            travelTile = travelTile.derive(dx, dy)
        } while (!travelTile.blocked(flags, direction.blockFlag) && bounds.contains(travelTile))

        return travelTile
    }

    private fun Rectangle.contains(tile: Tile) = contains(tile.x, tile.y)

    private fun getBounds(walls: List<GameObject>): Rectangle {
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE

        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE

        for (wall in walls) {
            val point: Tile = wall.tile
            minX = min(minX, point.x)
            minY = min(minY, point.y)

            maxX = max(maxX, point.x)
            maxY = max(maxY, point.y)
        }

        return Rectangle(minX, minY, maxX - minX, maxY - minY)
    }

    private fun direction(): Direction {
        val mine = me.tile()
        if (mine.y >= bounds.y + bounds.height && mine.x < bounds.x + bounds.width && mine.x > bounds.x) {
            return Direction.NORTH
        } else if (mine.y <= bounds.y && mine.x < bounds.x + bounds.width && mine.x > bounds.x) {
            return Direction.SOUTH
        } else if (mine.x >= bounds.x + bounds.width && mine.y < bounds.y + bounds.height && mine.y > bounds.y) {
            return Direction.EAST
        } else if (mine.x <= bounds.x && mine.y < bounds.y + bounds.height && mine.y > bounds.y) {
            return Direction.WEST
        }


        return Direction.NORTH
    }


    enum class Direction(val blockFlag: Int, vararg val orientations: Int) {
        NORTH(Flag.W_S, 4),
        EAST(Flag.W_W, 6),
        SOUTH(Flag.W_N, 0),
        WEST(Flag.W_E, 2),
        ;

        companion object {
            fun fromOrientation(orientation: Int): Direction {
                return values().first { orientation in it.orientations }
            }
        }
    }


    private fun boundedArea(x: Int, y: Int, width: Int, height: Int) = Area(Tile(x, y), Tile(x + width, y + height))

    private fun getIndicatorLine(direction: Direction): Area {
        return when (direction) {
            Direction.NORTH -> boundedArea(bounds.x + 1, bounds.y + bounds.height, bounds.width - 1, 1)
            Direction.SOUTH -> boundedArea(bounds.x + 1, bounds.y, bounds.width - 1, 1)
            Direction.WEST -> boundedArea(bounds.x, bounds.y + 1, 1, bounds.height - 1)
            Direction.EAST -> boundedArea(bounds.x + bounds.width, bounds.y + 1, 1, bounds.height - 1)
        }
    }


    private fun manhattan(point1: Tile, point2: Tile): Int {
        return abs(point1.x - point2.x) + abs(point2.y - point1.y)
    }
}