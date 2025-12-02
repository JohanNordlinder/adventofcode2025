package day02

import println
import readInputAsLines

private const val FOLDER_NAME = "day02"

fun main() {

    fun parseRanges(input: String): List<Pair<Long, Long>> =
        input.split(",")
            .map {
                val (start, end) = it.split("-").map(String::toLong)
                start to end
            }

    fun findInvalidIdsInRange(from: Long, to: Long, regex: Regex): List<Long> =
        (from..to).filter { regex.matches(it.toString()) }

    fun sumInvalidIdsInRange(input: String, regexForThisPart: Regex): Long =
        parseRanges(input)
            .sumOf { (start, end) ->
                findInvalidIdsInRange(start, end, regexForThisPart)
                    //.also { it.println() }
                    .sum()
            }

    fun part1(input: String): Long = sumInvalidIdsInRange(input, Regex("(\\d*)\\1"))
    fun part2(input: String): Long = sumInvalidIdsInRange(input, Regex("(\\d*)\\1\\1*"))

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput.first())
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 1227775554L)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input.first())
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 4174379265L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input.first())}".println()
    "Part2: ${part2(input.first())}".println()
}
