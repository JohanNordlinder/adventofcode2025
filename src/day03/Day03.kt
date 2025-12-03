package day03

import println
import readInputAsLines


private const val FOLDER_NAME = "day03"

fun main() {

    data class Bank(val batteries: List<Int>)

    fun findBiggestPossibleNumber(bank: Bank): Int {
        val allButLast = bank.batteries.subList(0, bank.batteries.size - 1)
        val max = allButLast.mapIndexed { index, battery ->
            val maxMatch = bank.batteries.subList(index + 1, bank.batteries.size).max()
            "$battery$maxMatch".toInt()
        }.max()
        return max
        //.also { println(it) }
    }

    fun parseBanks(input: List<String>): List<Bank> =
        input.map { line -> Bank(line.map { it.toString().toInt() }.toList()) }

    fun part1(input: List<String>): Int {
        val banks = parseBanks(input)
        return banks.sumOf { findBiggestPossibleNumber(it) }
    }

    fun findBiggestPossibleNumber(batteries: List<Int>, digitsLeftToFind: Int): Long {
        if (digitsLeftToFind == 1) {
            return batteries.max().toLong()
        }

        val possibleNumbers = batteries.take(batteries.size - digitsLeftToFind + 1)
        val max = possibleNumbers.max()
        val indexOfMax = possibleNumbers.indexOf(max)
        val nextMax =
            findBiggestPossibleNumber(batteries.subList(indexOfMax + 1, batteries.size), digitsLeftToFind - 1)
        return "$max$nextMax".toLong()//.also { println(it) }
    }

    fun part2(input: List<String>): Long {
        val banks = parseBanks(input)

        return banks.sumOf { findBiggestPossibleNumber(it.batteries, 12) }//.also { it.println() }}
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 357)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 3121910778619L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}
