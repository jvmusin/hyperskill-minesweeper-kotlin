package minesweeper

import minesweeper.GameState.*

fun <T> read(prompt: String, transform: (String) -> T): T {
    print(prompt)
    return transform(readLine()!!)
}

fun main() {
    val mineCount = read("How many mines do you want on the field? ", String::toInt)
    println()

    val field = Field(mineCount)
    println(field)

    while (field.gameState == IN_PROGRESS) {
        val input = read("Set/unset mines marks or claim a cell as free: ") { it.split(" ") }
        val c = input[0].toInt() - 1
        val r = input[1].toInt() - 1

        when (input[2]) {
            "mine" -> {
                field.mark(r, c)
                println()
                println(field)
            }
            "free" -> {
                val cell = field.get(r, c)
                if (cell.state == State.OPEN && cell.hasNumber()) {
                    println("There is a number here!")
                } else {
                    field.open(r, c)
                    println()
                    println(field)
                }
            }
            else -> error("Unknown command")
        }
    }

    println(when (field.gameState) {
        WON -> "Congratulations! You found all the mines!"
        LOST -> "You stepped on a mine and failed!"
        else -> error("Something went wrong")
    })
}
