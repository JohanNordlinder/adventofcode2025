package day01

import com.sun.tools.jconsole.JConsolePlugin
import println
import readInputAsLines


private const val FOLDER_NAME = "day01"

enum class Direction { LEFT, RIGHT }
data class Instruction(val direction: Direction, val steps: Int)

fun main() {

    fun parseInput(input: List<String>): List<Instruction> {
        return input.map {
            Instruction(
                when (it.first()) {
                    'L' -> Direction.LEFT
                    'R' -> Direction.RIGHT
                    else -> {
                        throw Error(it.first().toString())
                    }
                },
                it.substring(1).toInt()
            )
        }
    }

    fun part1(input: List<String>): Int {
        val instructions = parseInput(input)

        var position = 50
        var zero = 0

        instructions.forEach { instruction ->
            repeat(instruction.steps) {
                when (instruction.direction) {
                    Direction.LEFT -> {
                        position = if (position == 0) 99 else position - 1
                    }

                    Direction.RIGHT -> {
                        position = if (position == 99) 0 else position + 1
                    }
                }
            }
            if (position == 0) {
                zero++
            }
        }
        return zero
    }

    fun part2(input: List<String>): Int {
        val instructions = parseInput(input)

        var position = 50
        var zero = 0

        instructions.forEach { instruction ->
            repeat(instruction.steps) {
                when (instruction.direction) {
                    Direction.LEFT -> {
                        position = if (position == 0) 99 else position - 1
                    }

                    Direction.RIGHT -> {
                        position = if (position == 99) 0 else position + 1
                    }
                }
                if (position == 0) {
                    zero++
                }
            }
        }
        return zero
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 3)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 6)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}
