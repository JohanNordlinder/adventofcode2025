package day11

import println
import readInputAsLines


private const val FOLDER_NAME = "day11"

data class Device(val id: Int = 0, val name: String, val connectionsTo: HashSet<String>)

fun main() {

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 5)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input2")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 2L)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}

fun part1(input: List<String>): Int {
    val devices = parseConnections(input)

    var numberOfPaths = 0
    val queue = ArrayDeque<String>()

    queue.add("you")

    while (queue.isNotEmpty()) {
        val location = queue.removeFirst()

        if (location == "out") {
            numberOfPaths++
            continue
        }

        for (b in devices.find { it.name == location }!!.connectionsTo) {
            queue.add(b)
        }
    }

    return numberOfPaths
}

private fun parseConnections(input: List<String>): List<Device> {
    val devices = input.mapIndexed { i, line ->
        line.split(":").let {
            Device(i, it[0], it[1].split(" ").filter { it.isNotEmpty() }.toHashSet())
        }
    }
    return devices
}

fun part2(input: List<String>): Long {
    val devices = parseConnections(input)

    val devicesMap =
        devices.associate { c ->
            c.id to c.connectionsTo.map { ref ->
                devices.first { it.name == ref }.id
            }
        }

    val svrId = devices.first { it.name == "svr" }.id
    val outId = devices.first { it.name == "out" }.id
    val dacId = devices.first { it.name == "dac" }.id
    val fftId = devices.first { it.name == "fft" }.id

    var dacToOut = runBFS(devicesMap, dacId, outId)
    var fftToDac =
        runBFS(devicesMap.filter { !dacToOut.second.contains(it.key) }, fftId, dacId)
    var svrToFFT = runBFS(
        devicesMap.filter { !dacToOut.second.contains(it.key) && !fftToDac.second.contains(it.key) },
        svrId,
        fftId
    )

    return dacToOut.first.toLong() * fftToDac.first.toLong() * svrToFFT.first.toLong()
}

fun runBFS(
    connections: Map<Int, List<Int>>,
    startId: Int,
    goalId: Int
): Pair<Int, Set<Int>> {
    var numberOfPaths = 0
    val visitedNotes = mutableSetOf<Int>()

    val queue = ArrayDeque<Int>()

    queue.add(startId)

    while (queue.isNotEmpty()) {
        val location = queue.removeFirst()
        visitedNotes.add(location)

        if (location == goalId) {
            numberOfPaths++
            continue
        }

        for (b in connections[location] ?: emptyList()) {
            if (b != startId) {
                queue.add(b)

            }
        }
    }
    return Pair(numberOfPaths, visitedNotes)
}