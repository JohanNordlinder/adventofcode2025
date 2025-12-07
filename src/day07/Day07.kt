package day07

import Coordinate
import println
import readInputAsLines

private const val FOLDER_NAME = "day07"

enum class State { EMPTY, BEAM, SPLITTER }

fun main() {

    fun parseTree(input: List<String>): MutableMap<Coordinate, State> {
        val tree = mutableMapOf<Coordinate, State>()
        input.forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                tree[Coordinate(x, y)] = when (c) {
                    'S' -> State.BEAM
                    '^' -> State.SPLITTER
                    '.' -> State.EMPTY
                    else -> throw IllegalArgumentException("Cannot parse $c")
                }
            }
        }
        return tree
    }

    fun printGrid(grid: MutableMap<Coordinate, State>) {
        for (y in 0..grid.keys.maxOf { it.y }) {
            for (x in 0..grid.keys.maxOf { it.x }) {
                val state = grid[Coordinate(x, y)]
                val sign = when (state) {
                    State.BEAM -> if (y == 0) 'S' else '|'
                    State.SPLITTER -> '^'
                    State.EMPTY -> '.'
                    else -> throw IllegalArgumentException("Cannot parse $state")
                }
                print(sign)
            }
            print("\n")
        }
    }

    fun part1(input: List<String>): Int {
        val tree = parseTree(input)
        //printGrid(tree)

        var beamSplits = 0
        for (y in 1..tree.keys.maxOf { it.y }) {
            for (x in 0..tree.keys.maxOf { it.x }) {
                val stateHere = tree[Coordinate(x, y)]
                val stateAbove = tree[Coordinate(x, y - 1)]
                if (stateAbove == State.BEAM) {
                    if (stateHere == State.SPLITTER) {
                        tree[Coordinate(x - 1, y)] = State.BEAM
                        tree[Coordinate(x + 1, y)] = State.BEAM
                        beamSplits++
                    } else {
                        tree[Coordinate(x, y)] = State.BEAM
                    }
                }
            }
        }
        //"".println()
        //printGrid(tree)

        return beamSplits
    }

    fun part2(input: List<String>): Long {
        val grid = parseTree(input)
        val beamPaths = mutableMapOf<Coordinate, Long>()
        //printGrid(grid)

        val maxY = grid.keys.maxOf { it.y }
        val maxX = grid.keys.maxOf { it.x }

        fun setBeam(coord: Coordinate, pathsFromAbove: Long) {
            grid[coord] = State.BEAM
            val otherPathsToThisLocation = beamPaths[coord] ?: 0
            beamPaths[coord] = otherPathsToThisLocation + pathsFromAbove
        }

        for (y in 1..maxY) {
            for (x in 0..maxX) {
                val here = Coordinate(x, y)
                val above = Coordinate(x, y - 1)
                if (grid[above] == State.BEAM) {
                    val beamPathsToPreserve = beamPaths[above] ?: 1

                    if (grid[here] == State.SPLITTER) {
                        listOf(
                            Coordinate(x - 1, y),
                            Coordinate(x + 1, y)
                        ).forEach { target -> setBeam(target, beamPathsToPreserve) }
                    } else {
                        setBeam(here, beamPathsToPreserve)
                    }
                }
            }
        }
        //"".println()
        //printGrid(grid)

        return beamPaths.filter { it.key.y == grid.keys.maxOf { it.y } }.values.sum()
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 21)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 40L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}
