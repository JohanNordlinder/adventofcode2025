package day04

import Coordinate
import println
import readInputAsLines


private const val FOLDER_NAME = "day04"

fun main() {

    fun countNearby(coordinate: Coordinate, map: MutableMap<Coordinate, Boolean>) =
        listOf(
            -1 to 0,  // left
            -1 to -1,  // top-left
            0 to -1,  // top
            1 to -1,  // top-right
            1 to 0,  // right
            -1 to 1,  // bottom-left
            0 to 1,  // bottom
            1 to 1   // bottom-right
        ).map { (dx, dy) ->
            map[coordinate.copy(x = coordinate.x + dx, y = coordinate.y + dy)] == true
        }.filter { it == true }.size

    fun parseMap(input: List<String>): HashMap<Coordinate, Boolean> {
        val map = hashMapOf<Coordinate, Boolean>()
        for (y in 0..input.size - 1) {
            for (x in 0..input[y].length - 1) {
                map[Coordinate(x, y)] = input[y][x] == '@'
            }
        }
        return map
    }

    fun part1(input: List<String>): Int {
        val map = parseMap(input)
        return map.filter { it.value }.count { countNearby(it.key, map) < 4 }
    }

    fun part2(input: List<String>): Int {
        var map = parseMap(input).toMutableMap()
        var totalRemoved = 0
        var removedThisRound = 0

        do {
            val canBeRemoved = map.filter { it.value && countNearby(it.key, map) < 4 }
            removedThisRound = canBeRemoved.size
            canBeRemoved.forEach { map.remove(it.key) }
            totalRemoved += removedThisRound

        } while (removedThisRound != 0)

        return totalRemoved
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 13)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 43)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}
