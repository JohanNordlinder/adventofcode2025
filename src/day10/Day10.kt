package day10

import println
import readInputAsLines
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar

private const val FOLDER_NAME = "day10"

fun main() {

    fun part1(input: List<String>): Int {
        val machines = input.map { parseMachine(it) }
        return machines.sumOf { solveMachineUsingBreadthFirstSearch(it) }
    }

    fun part2(input: List<String>): Int {
        val machines = input.map { parseMachine(it) }

        fun solveMachineUsingCocoSolver(machine: Machine): Int {
            val buttons = machine.buttons.map { it.affectedLights }
            val desiredState = machine.joltageRequirements

            val numberOfButtonsThatCanBePressed = buttons.size
            val numberOfLevelsToConsider = desiredState.size

            val model = Model()

            // Create variable for each button
            val x: Array<IntVar> = Array(numberOfButtonsThatCanBePressed) { j ->
                model.intVar("x$j", 0, desiredState.sum())
            }

            // Constraints: sum of button effects = target for each machine
            for (affectedLight in 0 until numberOfLevelsToConsider) {
                val terms = mutableListOf<IntVar>()
                val coeffs = mutableListOf<Int>()
                for (buttonIndex in 0 until numberOfButtonsThatCanBePressed) {
                    if (affectedLight in buttons[buttonIndex]) {
                        terms.add(x[buttonIndex])
                        coeffs.add(1)
                    }
                }
                model.scalar(terms.toTypedArray(), coeffs.toIntArray(), "=", desiredState[affectedLight]).post()
            }

            val totalButtonPressed = model.intVar("totalButtonPressed", 0, desiredState.sum() * 100)
            model.sum(x, "=", totalButtonPressed).post()
            model.setObjective(Model.MINIMIZE, totalButtonPressed)

            val solver = model.solver
            val solution = solver.findOptimalSolution(totalButtonPressed, Model.MINIMIZE)

            if (solution != null) {
                val optimalValue = solution.getIntVal(totalButtonPressed)
                //println("Optimal solution: $optimalValue")
                //println("Button presses: ${x.map { solution.getIntVal(it) }}")
                return optimalValue
            } else {
                throw RuntimeException("No solution found")
            }
        }

        return machines.sumOf { solveMachineUsingCocoSolver(it) }
    }

    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 7)

    val test2Input = readInputAsLines("$FOLDER_NAME/test_input")

    val testPart2Output = part2(test2Input)
    "Part2 Test: $testPart2Output".println()
    check(testPart2Output == 33)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
    "Part2: ${part2(input)}".println()
}

data class Button(
    val affectedLights: List<Int>,
)

data class Machine(
    val desiredLightState: List<Boolean>,
    val buttons: List<Button>,
    val joltageRequirements: List<Int>,
)

fun parseMachine(line: String): Machine {
    val desiredLightState = parseDesiredLightState(line)
    val buttons = parseButtons(line)
    val desiredJoltage = parseJoltage(line)

    return Machine(
        desiredLightState = desiredLightState,
        buttons = buttons,
        joltageRequirements = desiredJoltage
    )
}

private fun parseDesiredLightState(line: String): List<Boolean> {
    val match = Regex("""\[(.+?)]""").find(line)
        ?: error("No indicator light diagram found in line: $line")

    val text = match.groupValues[1]

    val states = text.map { c ->
        when (c) {
            '.' -> false
            '#' -> true
            else -> error("Invalid light symbol '$c'")
        }
    }

    return states
}

private fun parseButtons(line: String): List<Button> {
    return Regex("""\((.*?)\)""").findAll(line)
        .map { match ->
            val inside = match.groupValues[1].trim()
            if (inside.isEmpty()) {
                Button(emptyList())
            } else {
                val nums = inside.split(',')
                    .map { it.trim().toInt() }
                Button(nums)
            }
        }
        .toList()
}

private fun parseJoltage(line: String): List<Int> {
    val match = Regex("""\{(.+?)}""").find(line)
        ?: error("No joltage section found in line: $line")

    return match.groupValues[1]
        .split(',')
        .map { it.trim().toInt() }
}

data class PossibleStateFromSomeButtomClicks(val stateAsMask: Int, val numberOfButtonPressesToGetHere: Int)

private fun solveMachineUsingBreadthFirstSearch(machine: Machine): Int {

    var desiredMachineStateAsMask = 0
    for (i in machine.desiredLightState.indices) {
        if (machine.desiredLightState[i]) desiredMachineStateAsMask = desiredMachineStateAsMask or (1 shl i)
    }

    val buttonActionsAsMasks = machine.buttons.map { it.affectedLights }.map { list ->
        var mask = 0
        for (i in list) mask = mask or (1 shl i)
        mask
    }

    val queue = ArrayDeque<PossibleStateFromSomeButtomClicks>()
    val exploredStates = HashSet<Int>()

    // We start at mask 0 with no buttons pressed
    queue.add(PossibleStateFromSomeButtomClicks(0, 0))
    exploredStates.add(0)

    while (queue.isNotEmpty()) {
        // Dequeue current state
        val (state, dist) = queue.removeFirst()

        // Check if we are there yet
        if (state == desiredMachineStateAsMask) {
            //println("Minimum presses = $dist")
            return dist
        }

        // Otherwise start pressing buttons
        for (b in buttonActionsAsMasks.indices) {
            // Press button and calculate next state
            val nextState = state xor buttonActionsAsMasks[b]
            // Maybe we already got here by some other combination, if so we don't need to explore this "timeline" any further
            if (nextState !in exploredStates) {
                exploredStates.add(nextState)
                // Continue down this path, increase numberOfButtonPressesToGetHere
                queue.add(PossibleStateFromSomeButtomClicks(nextState, dist + 1))
            }
        }
    }

    println("No solution found.")
    return -1
}