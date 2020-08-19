package minesweeper

import minesweeper.CellType.FREE
import minesweeper.CellType.MINE
import minesweeper.GameState.*
import minesweeper.State.*
import java.util.*
import kotlin.random.Random

class Field(private val mineCount: Int) {
    companion object SIZE {
        const val n = 9
        val indices = 0 until n
        val positions = indices.flatMap { r -> indices.map { c -> Pair(r, c) } }
        fun contains(r: Int, c: Int) = r in indices && c in indices
        fun neighbours8(row: Int, column: Int): List<Pair<Int, Int>> {
            return (row - 1..row + 1).flatMap { r ->
                (column - 1..column + 1)
                        .filter { c -> r != row || c != column }
                        .filter { c -> contains(r, c) }
                        .map { c -> Pair(r, c) }
            }
        }
    }

    private val data: Array<Array<Cell>> = Array(n) { r -> Array(n) { c -> Cell(r, c, FREE) } }
    private lateinit var mines: List<Pair<Int, Int>>
    private var markedMines = 0
    private var markedFree = 0
    private var opened = 0
    private var initialized = false
    var gameState = IN_PROGRESS
        private set

    private fun putMines() {
        for ((r, c) in mines) data[r][c].type = MINE
        for (p in mines) {
            val (r, c) = p
            data[r][c].type = MINE
            for ((r1, c1) in neighbours8(r, c)) {
                if (contains(r1, c1) && data[r1][c1].type == FREE) {
                    data[r1][c1].minesAround++
                }
            }
        }
    }

    private fun initialize(restrictedRow: Int, restrictedColumn: Int) {
        mines = generateSequence { Pair(Random.nextInt(n), Random.nextInt(n)) }
                .filter { (r, c) -> r != restrictedRow || c != restrictedColumn }
                .distinct()
                .take(mineCount)
                .toList()
        putMines()
        for ((r, c) in positions) {
            val cell = data[r][c]
            if (cell.state == MARKED) {
                if (cell.type == FREE) markedFree++
                else markedMines++
            }
        }
        initialized = true
        if (isFinished()) gameState = WON
    }

    override fun toString(): String {
        val res = StringJoiner(System.lineSeparator())
        res.add(" |123456789|")
        res.add("-|---------|")
        for (r in indices) {
            val row = indices.joinToString("", "${r + 1}|", "|") { c -> data[r][c].toString() }
            res.add(row)
        }
        res.add("-|---------|")
        return res.toString()
    }

    fun get(r: Int, c: Int) = data[r][c]

    fun mark(r: Int, c: Int) {
        check(gameState == IN_PROGRESS) { "Game is not in progress" }
        val cell = data[r][c]
        check(cell.state != OPEN) { "Marking an already open cell" }
        val delta: Int
        if (cell.state == MARKED) {
            cell.state = CLOSED
            delta = -1
        } else {
            cell.state = MARKED
            delta = 1
        }
        if (initialized) {
            if (cell.type == FREE) markedFree += delta
            else markedMines += delta
        }
        if (isFinished()) gameState = WON
    }

    fun open(r: Int, c: Int) {
        if (!initialized) initialize(r, c)
        check(gameState == IN_PROGRESS) { "Game is not in progress" }
        val cell = data[r][c]
        check(cell.state != OPEN) { "Opening an already open cell" }
        if (cell.type == MINE) {
            gameState = LOST
            openMines()
        } else {
            cell.state = OPEN
            opened++
            if (cell.minesAround == 0) {
                neighbours8(r, c).asSequence()
                        .filter { (r1, c1) -> data[r1][c1].state != OPEN }
                        .forEach { (r1, c1) -> open(r1, c1) }
            }
            if (isFinished()) gameState = WON
        }
    }

    private fun openMines() {
        for ((r, c) in mines) data[r][c].state = OPEN
    }

    private fun isFinished() = (markedMines == mineCount && markedFree == 0) || opened == n * n - mineCount
}