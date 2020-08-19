package minesweeper

import minesweeper.CellType.MINE
import minesweeper.State.CLOSED
import minesweeper.State.MARKED

data class Cell(val row: Int, val column: Int, var type: CellType, var minesAround: Int = 0, var state: State = CLOSED) {
    fun hasNumber() = minesAround > 0

    override fun toString(): String {
        return when {
            state == MARKED -> "*"
            state == CLOSED -> "."
            type == MINE -> "X"
            minesAround == 0 -> "/"
            else -> minesAround.toString()
        }
    }
}