package day05

import println
import readInputAsLines
import kotlin.collections.component1


private const val FOLDER_NAME = "day05"

data class Range(val start: Long, val end: Long)

fun main() {

    fun parseInput(input: List<String>): Pair<List<Range>, List<Long>> {
        val split = input.indexOf("")
        val ranges = input.subList(0, split).map {
            it.split("-").map(String::toLong).let { (start, end) ->
                Range(start, end)
            }
        }
        //.also { it.println() }

        val ingredients = input.subList(split + 1, input.size)
            .map { it.toLong() }
        //.also { it.println() }
        return Pair(ranges, ingredients)
    }

    fun part1(input: List<String>): Int {
        val (ranges, ingredients) = parseInput(input)
        return ingredients.count { i -> ranges.any { range -> i in range.start..(range.end + 1) } }
    }

    fun part2(input: List<String>): Long {
        val (ranges, _) = parseInput(input)

        var countFresh = 0L
        var lastEnd = 0L
        ranges.sortedBy { it.start }
            //.also { it.println() }
            .forEach { (start, end) ->
                if (end < lastEnd) {
                    return@forEach
                }
                val startWithoutOverlap = maxOf(start, lastEnd + 1L)
                countFresh += end - startWithoutOverlap + 1L
                lastEnd = end
            }
        return countFresh
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 3)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 14L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}
