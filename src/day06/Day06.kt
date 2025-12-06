package day06

import println
import readInputAsLines


private const val FOLDER_NAME = "day06"

fun main() {

    fun parseProblemsPart1(input: List<String>): Pair<List<String>, MutableMap<Int, MutableList<Long>>> {
        val raknesatt = input.last().split(" ").filter { it.isNotBlank() }
        val numbers = mutableMapOf<Int, MutableList<Long>>()
        raknesatt.forEachIndexed { index, _ -> numbers[index] = mutableListOf() }
        input.take(input.size - 1).forEach {
            val n = it.split(" ").filter { it.isNotBlank() }.map { it.toLong() }
            n.forEachIndexed { index, value ->
                numbers[index]!!.add(value)
            }
        }
        return Pair(raknesatt, numbers)
    }

    fun part1(input: List<String>): Long {
        val (raknesatt, numbers) = parseProblemsPart1(input)

        return raknesatt.withIndex().sumOf {
            val valuesForThis = numbers[it.index]
            when (it.value) {
                "+" -> valuesForThis!!.sum()
                "*" -> valuesForThis!!.reduce { acc, value -> acc * value }
                else -> error("Unsupported operator: ${it.value}")
            }
        }
    }

    fun parseProblemsPart2(input: List<String>): List<Pair<Char, MutableList<Long>>> {
        val problems = mutableListOf<Pair<Char, MutableList<Long>>>()
        for (i in 0..input.last().length - 1) {
            if (input.last()[i] in listOf('+', '*')) {
                problems.add(Pair(input.last()[i], mutableListOf<Long>()))
            }
            val numbersInColumn = input.take(input.size - 1).mapNotNull { it.getOrNull(i) }.filter { it.isDigit() }
            if (numbersInColumn.any()) {
                problems.last().second.add(numbersInColumn.joinToString("").toLong())
            }
        }

        return problems
    }

    fun part2(input: List<String>): Long {
        val toCalculate = parseProblemsPart2(input)

        return toCalculate.sumOf {
            when (it.first) {
                '+' -> it.second.sum()
                '*' -> it.second.reduce { acc, value -> acc * value }
                else -> error("Unsupported operator: ${it.first}")
            }
        }
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 4277556L)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 3263827L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}