package day08

import println
import readInputAsLines
import kotlin.math.pow
import kotlin.math.sqrt


private const val FOLDER_NAME = "day08"

data class Coordinate(val x: Int, val y: Int, val z: Int)
data class Group(val id: Int, val members: MutableSet<JBox> = mutableSetOf())

data class JBox(
    val id: Int,
    val coordinate: Coordinate,
    val distanceToOthers: MutableMap<JBox, Double> = mutableMapOf<JBox, Double>(),
    var group: Group = Group(id, mutableSetOf()),
) {
    init {
        group.members.add(this)
    }

    override fun hashCode(): Int {
        return id + coordinate.hashCode()
    }
}

fun main() {

    fun part1(input: List<String>, connectionsToMake: Int): Int {
        val jBoxes = parseBoxesAndCalculateDistances(input)

        repeat(connectionsToMake) {
            connectClosestPairToSameGroup(jBoxes)
        }

        val biggestGroups = jBoxes.map { it.group }
            .distinct()
            .map { it.members.size }
            .sortedDescending()
            .take(3)
            .reduce { acc, i -> acc * i }

        return biggestGroups
    }

    fun part2(input: List<String>): Long {
        val jBoxes = parseBoxesAndCalculateDistances(input)

        var lastConnectedBox: JBox
        do {
            val closestPair = connectClosestPairToSameGroup(jBoxes)
            lastConnectedBox = closestPair.second
        } while (jBoxes.first().group.members.size != jBoxes.size)

        return jBoxes.first().group.members.first().coordinate.x.toLong() * lastConnectedBox.coordinate.x.toLong()
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput, 10)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 40)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 25272L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input, 1000)}".println()
    "Part2: ${part2(input)}".println()
}

fun parseBoxesAndCalculateDistances(input: List<String>): List<JBox> {
    val jBoxes = input.mapIndexed { idx, line ->
        val (x, y, z) = line.split(",")
        JBox(idx, Coordinate(x.toInt(), y.toInt(), z.toInt()))
    }

    jBoxes.forEach { thisJunctionBox ->
        jBoxes.forEach { otherJunctionBox ->
            if (thisJunctionBox.id == otherJunctionBox.id) {
                return@forEach
            }
            val distance = getDistance(thisJunctionBox.coordinate, otherJunctionBox.coordinate)
            thisJunctionBox.distanceToOthers[otherJunctionBox] = distance
        }
    }
    return jBoxes
}

fun getDistance(first: Coordinate, second: Coordinate): Double {
    return sqrt(
        (first.x - second.x).toDouble().pow(2) +
                (first.y - second.y).toDouble().pow(2) +
                (first.z - second.z).toDouble().pow(2)
    )
}

fun connectClosestPairToSameGroup(jBoxes: List<JBox>): Pair<JBox, JBox> {
    var lowestDistanceToAnother = Double.MAX_VALUE
    var closestPair = Pair(jBoxes.first(), jBoxes.first())
    jBoxes.forEach { jb ->
        jb.distanceToOthers.forEach {
            if (it.value < lowestDistanceToAnother) {
                lowestDistanceToAnother = it.value
                closestPair = Pair(jb, it.key)
            }
        }
    }
    closestPair.first.distanceToOthers.remove(closestPair.second)
    closestPair.second.distanceToOthers.remove(closestPair.first)
    closestPair.first.group.members.addAll(closestPair.second.group.members)
    closestPair.second.group.members.forEach { it.group = closestPair.first.group }
    return closestPair
}