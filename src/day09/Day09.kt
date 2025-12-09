package day09

import println
import readInputAsLines
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val FOLDER_NAME = "day09"

fun main() {

    fun part1(input: List<String>): Long {
        val redTiles = parseTilesAndCalculateAreas(input)
        var highestArea = Long.MIN_VALUE

        redTiles.forEach { t ->
            t.areaToOthersMap.forEach {
                if (it.value == highestArea) {
                    println("same distance!")
                }
                if (it.value >= highestArea) {
                    highestArea = it.value
                }
            }
        }

        return highestArea
    }

    fun part2(input: List<String>): Long {
        var redTiles = parseTilesAndCalculateAreas(input)
        val greenTiles = createGreenTiles(redTiles)

        //printGrid(redTiles, greenTiles.toHashSet())

        val outerBoundaryTilesByX: Map<Long, List<Coordinate>> = (redTiles.map { it.coordinate } + greenTiles)
            .groupBy { it.x }

        val outerBoundaryTilesByY: Map<Long, List<Coordinate>> = (redTiles.map { it.coordinate } + greenTiles)
            .groupBy { it.y }

        while (true) {
            returnBiggestAreaPairIfValidElseDiscardIt(
                redTiles, outerBoundaryTilesByX, outerBoundaryTilesByY
            )?.let { return it }
        }
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 50L)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 24L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}

data class RedTile(
    val id: Int,
    val coordinate: Coordinate,
    val areaToOthersMap: MutableMap<RedTile, Long> = mutableMapOf<RedTile, Long>(),
) {
    override fun hashCode(): Int {
        return id + coordinate.hashCode()
    }

    override fun toString(): String {
        return "Id: $id, Coordinate: $coordinate"
    }
}

data class Coordinate(val x: Long, val y: Long)

fun getAreaBetween(first: Coordinate, second: Coordinate): Long {
    val dx = abs((first.x - second.x))
    val dy = abs((first.y - second.y))
    return (dx + 1) * (dy + 1)
}

fun parseTilesAndCalculateAreas(input: List<String>): List<RedTile> {
    val redTiles = input.mapIndexed { idx, line ->
        val (x, y) = line.split(",")
        RedTile(idx, Coordinate(x.toLong(), y.toLong()))
    }

    val alreadyChecked = hashSetOf<Pair<Int, Int>>()
    redTiles.forEach { thisTile ->
        redTiles.forEach { otherTile ->
            val key = Pair(min(thisTile.id, otherTile.id), max(thisTile.id, otherTile.id))
            if (thisTile.id == otherTile.id || alreadyChecked.contains(key)) {
                return@forEach
            }
            val distance = getAreaBetween(thisTile.coordinate, otherTile.coordinate)
            thisTile.areaToOthersMap[otherTile] = distance
            alreadyChecked.add(key)
        }
    }
    return redTiles
}

fun createGreenTiles(redTiles: List<RedTile>): Set<Coordinate> {
    val greenTiles = mutableSetOf<Coordinate>()
    val minY = redTiles.map { it.coordinate.y }.min()
    val maxY = redTiles.map { it.coordinate.y }.max()
    for (y in minY..maxY) {
        val tilesOnSameRow = redTiles.filter { it.coordinate.y == y }
        if (tilesOnSameRow.isNotEmpty()) {
            val minX = tilesOnSameRow.minOf { it.coordinate.x } + 1
            val maxX = tilesOnSameRow.maxOf { it.coordinate.x } - 1
            for (x in minX..maxX) {
                greenTiles.add(Coordinate(x, y))
            }

            tilesOnSameRow.forEach { tileOnRow ->
                val tilesOnSameColumn = redTiles.filter { it.coordinate.x == tileOnRow.coordinate.x }

                if (tilesOnSameColumn.isNotEmpty()) {
                    val minY = tilesOnSameColumn.minOf { it.coordinate.y } + 1
                    val maxY = tilesOnSameColumn.maxOf { it.coordinate.y } - 1
                    for (y in minY..maxY) {
                        greenTiles.add(Coordinate(tileOnRow.coordinate.x, y))
                    }
                }
            }
        }
    }

    return greenTiles
}

fun returnBiggestAreaPairIfValidElseDiscardIt(
    redTiles: List<RedTile>,
    outerBoundaryTilesByX: Map<Long, List<Coordinate>>,
    outerBoundaryTilesByY: Map<Long, List<Coordinate>>,
): Long? {
    var highestAreaBetweenAnyPair = Long.MIN_VALUE
    var biggestAreaPair = Pair(redTiles.first(), redTiles.first())
    redTiles.forEach { t ->
        t.areaToOthersMap.forEach {
            if (it.value >= highestAreaBetweenAnyPair) {
                highestAreaBetweenAnyPair = it.value
                biggestAreaPair = Pair(t, it.key)
            }
        }
    }

    biggestAreaPair.first.areaToOthersMap.remove(biggestAreaPair.second)
    biggestAreaPair.second.areaToOthersMap.remove(biggestAreaPair.first)

    val isThisPairValid = validatePair(biggestAreaPair, outerBoundaryTilesByX, outerBoundaryTilesByY)

    return if (isThisPairValid) highestAreaBetweenAnyPair else null
}

fun inRange(
    coord: Coordinate,
    tiles: List<Coordinate>,
    axis: (Coordinate) -> Long,
): Boolean {
    if (tiles.isEmpty()) return false
    val min = tiles.minOf(axis)
    val max = tiles.maxOf(axis)
    val value = axis(coord)
    return value in min..max
}

fun validatePair(
    pair: Pair<RedTile, RedTile>,
    outerBoundaryTilesByX: Map<Long, List<Coordinate>>,
    outerBoundaryTilesByY: Map<Long, List<Coordinate>>,
): Boolean {

    fun checkIfWithinBoundary(c: Coordinate): Boolean =
        inRange(c, outerBoundaryTilesByX[c.x].orEmpty()) { it.y } &&
                inRange(c, outerBoundaryTilesByY[c.y].orEmpty()) { it.x }

    val (firstCorner, secondCorner) = pair
    val thirdCorner = Coordinate(firstCorner.coordinate.x, secondCorner.coordinate.y)
    val fourthCorner = Coordinate(secondCorner.coordinate.x, firstCorner.coordinate.y)

    if (!checkIfWithinBoundary(thirdCorner) || !checkIfWithinBoundary(fourthCorner)) return false

    val xmin = min(firstCorner.coordinate.x, secondCorner.coordinate.x)
    val xmax = max(firstCorner.coordinate.x, secondCorner.coordinate.x)
    val ymin = min(firstCorner.coordinate.y, secondCorner.coordinate.y)
    val ymax = max(firstCorner.coordinate.y, secondCorner.coordinate.y)

    // Check top and bottom sides
    for (x in xmin..xmax) {
        val c1 = Coordinate(x, ymin)
        if (!checkIfWithinBoundary(c1)) return false

        val c2 = Coordinate(x, ymax)
        if (!checkIfWithinBoundary(c2)) return false
    }

    // Check left and right sides
    for (y in ymin..ymax) {
        val c1 = Coordinate(xmin, y)
        if (!checkIfWithinBoundary(c1)) return false

        val c2 = Coordinate(xmax, y)
        if (!checkIfWithinBoundary(c2)) return false
    }

    return true
}

fun printGrid(
    redTiles: List<RedTile>,
    greenTiles: MutableSet<Coordinate>,
) {
    BufferedWriter(FileWriter("output.txt")).use { writer ->
        for (y in redTiles.map { it.coordinate }.minOf { it.y }..redTiles.map { it.coordinate }.maxOf { it.y }) {
            for (x in redTiles.map { it.coordinate }.minOf { it.x }..redTiles.map { it.coordinate }
                .maxOf { it.x }) {
                if (redTiles.any { it.coordinate.x == x && it.coordinate.y == y }) {
                    writer.write("#")
                } else if (greenTiles.any { it.x == x && it.y == y }) {
                    writer.write("X")
                } else {
                    writer.write(".")
                }
            }
            writer.write("\n")
        }
    }
}

