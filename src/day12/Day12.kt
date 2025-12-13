package day12

import println
import readInputAsLines

private const val FOLDER_NAME = "day12"

fun main() {
    val testInput = readInputAsLines("$FOLDER_NAME/test_input")
    val testPart1Output = part1(testInput)
    "Part1 Test: $testPart1Output".println()
    check(testPart1Output == 2)

    val input = readInputAsLines("$FOLDER_NAME/input")
    "Part1: ${part1(input)}".println()
}

data class Shape(
    val index: Int,
    val grid: List<String>
)

data class RegionRequirement(
    val width: Int,
    val height: Int,
    val quantities: List<Int>
)

data class BitmaskShape(
    val width: Int,
    val height: Int,
    val mask: LongArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitmaskShape) return false
        return width == other.width && height == other.height && mask.contentEquals(other.mask)
    }

    override fun hashCode(): Int {
        var result = width.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + mask.contentHashCode()
        return result
    }
}

data class Placement(val shape: BitmaskShape, val top: Int, val left: Int)

fun parseInput(lines: List<String>): Pair<List<Shape>, List<RegionRequirement>> {
    val shapes = mutableListOf<Shape>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.isEmpty()) {
            i++; continue
        }

        if (Regex("""\d+x\d+:""").matches(line)) break

        val match = Regex("""(\d+):""").matchEntire(line)
        if (match != null) {
            val index = match.groupValues[1].toInt()
            i++

            val grid = mutableListOf<String>()
            while (i < lines.size && lines[i].contains('#')) {
                grid += lines[i].trim()
                i++
            }

            shapes += Shape(index, grid)
        } else {
            i++
        }
    }

    i = 0
    val regions = mutableListOf<RegionRequirement>()

    while (i < lines.size) {
        val line = lines[i].trim()
        i++
        if (line.isEmpty()) continue

        val match = Regex("""(\d+)x(\d+):\s+(.+)""").matchEntire(line)
        if (match != null) {
            val width = match.groupValues[1].toInt()
            val height = match.groupValues[2].toInt()
            val quantities = match.groupValues[3]
                .split(" ")
                .map { it.toInt() }

            regions += RegionRequirement(width, height, quantities)
        }
    }

    return shapes to regions
}

fun part1(input: List<String>): Int {
    val (shapes, regions) = parseInput(input)
    return regions.count { region ->
        val shapesRequiredByThisRegion =
            region.quantities.flatMapIndexed { index, quantity -> List(quantity) { shapes[index] } }
        val regionArea = region.height * region.width
        val shapeTotalArea = shapesRequiredByThisRegion.sumOf {
            it.grid.sumOf { it.count { it == '#' } }
        }

        // Check if there is even any point in trying
        if (shapeTotalArea > regionArea) {
            return@count false
        }
        // Pre-compute all orientations for each shape
        val shapeOrientations = shapes.map { shape ->
            shape.index to orientations(shape.grid)
        }.toMap()

        checkIfShapesFitInRegion(region, shapes.size, shapeOrientations)
    }
}

fun checkIfShapesFitInRegion(
    region: RegionRequirement,
    numberOfShapes: Int,
    shapeOrientations: Map<Int, List<BitmaskShape>>
): Boolean {

    // Build initial remaining counts
    val remainingShapesToPlace = IntArray(numberOfShapes)
    for (i in region.quantities.indices) {
        remainingShapesToPlace[i] = region.quantities[i]
    }

    val start = LongArray(region.height) { 0L }

    // Use DFS with pruning
    val visited = mutableSetOf<Int>()

    fun dfs(currentRegion: LongArray, remaining: IntArray): Boolean {
        // Check if all shapes placed
        if (remaining.all { it == 0 }) return true

        // Memoization
        val stateHash = hashState(currentRegion, remaining)
        if (stateHash in visited) return false
        visited.add(stateHash)

        // Find first shape type that still needs to be placed
        val shapeIndex = remaining.indexOfFirst { it > 0 }
        if (shapeIndex == -1) return false

        val orientationsList = shapeOrientations[shapeIndex] ?: return false

        // Try each orientation
        for (orientation in orientationsList) {
            val placements = findAllPlacements(currentRegion, region.width, region.height, orientation)

            // Try each valid placement
            for (placement in placements) {
                val newRegion = place(currentRegion, placement.shape, placement.top, placement.left)
                val newRemaining = remaining.copyOf()
                newRemaining[shapeIndex]--

                if (dfs(newRegion, newRemaining)) {
                    return true
                }
            }
        }

        return false
    }

    return dfs(start, remainingShapesToPlace)
}

fun hashState(region: LongArray, remaining: IntArray): Int {
    var result = region.contentHashCode()
    result = 31 * result + remaining.contentHashCode()
    return result
}

fun orientations(shaped: List<String>): List<BitmaskShape> {
    val r0 = shaped
    val r1 = rotate(r0)
    val r2 = rotate(r1)
    val r3 = rotate(r2)

    val f0 = flipHorizontal(r0)
    val f1 = flipHorizontal(r1)
    val f2 = flipHorizontal(r2)
    val f3 = flipHorizontal(r3)

    return listOf(r0, r1, r2, r3, f0, f1, f2, f3)
        .map(::gridToBitmask)
        .distinct()
}

fun rotate(grid: List<String>): List<String> {
    val h = grid.size
    val w = grid[0].length

    return List(w) { x ->
        buildString {
            for (y in h - 1 downTo 0) {
                append(grid[y][x])
            }
        }
    }
}

fun flipHorizontal(grid: List<String>): List<String> =
    grid.map { it.reversed() }

fun gridToBitmask(grid: List<String>): BitmaskShape {
    val normalized = normalize(grid)
    if (normalized.isEmpty()) return BitmaskShape(0, 0, longArrayOf())

    val height = normalized.size
    val width = normalized[0].length
    val mask = LongArray(height)

    for (y in normalized.indices) {
        var row = 0L
        for (x in normalized[y].indices) {
            if (normalized[y][x] == '#') {
                row = row or (1L shl x)
            }
        }
        mask[y] = row
    }

    return BitmaskShape(width, height, mask)
}

fun normalize(grid: List<String>): List<String> {
    val rows = grid.filter { row -> row.any { it == '#' } }
    if (rows.isEmpty()) return rows

    val left = rows.minOf { row -> row.indexOfFirst { it == '#' } }
    val right = rows.maxOf { row -> row.indexOfLast { it == '#' } }

    return rows.map { it.substring(left, right + 1) }
}

fun findAllPlacements(region: LongArray, regionWidth: Int, regionHeight: Int, shape: BitmaskShape): List<Placement> {
    val results = mutableListOf<Placement>()

    for (y in 0..regionHeight - shape.height) {
        for (x in 0..regionWidth - shape.width) {
            if (fitsAt(region, regionWidth, shape, y, x)) {
                results += Placement(shape, y, x)
            }
        }
    }

    return results
}

fun fitsAt(region: LongArray, regionWidth: Int, shape: BitmaskShape, top: Int, left: Int): Boolean {
    val regionHeight = region.size

    if (top + shape.height > regionHeight || left + shape.width > regionWidth) return false

    for (dy in 0 until shape.height) {
        val shiftedShapeMask = shape.mask[dy] shl left
        if ((region[top + dy] and shiftedShapeMask) != 0L) {
            return false
        }
    }
    return true
}

fun place(region: LongArray, shape: BitmaskShape, top: Int, left: Int): LongArray {
    val newRegion = region.copyOf()
    for (dy in 0 until shape.height) {
        val shiftedShapeMask = shape.mask[dy] shl left
        newRegion[top + dy] = newRegion[top + dy] or shiftedShapeMask
    }
    return newRegion
}