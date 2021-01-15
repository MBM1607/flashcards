package flashcards

import java.lang.NumberFormatException
import java.io.File
import kotlin.random.Random

val flashCards = mutableMapOf<String, String>()
var mistakes = mutableMapOf<String, Int>()
val ioLog = mutableListOf<String>()

fun println(input: String) {
    ioLog.add(input)
    kotlin.io.println(input)
}

fun readLine(): String {
    val input = kotlin.io.readLine()!!
    ioLog.add(input)
    return input
}

fun main(args: Array<String>) {
    for (i in args.indices step 2)
        if (args[i] == "-import") importCards(args[i + 1])

    while (true) {

        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        when (readLine()) {
            "add" -> addCard()
            "remove" -> removeCard()
            "ask" -> askFromCards()
            "import" -> importCards()
            "export" -> exportCards()
            "log" -> logToFile()
            "hardest card" -> getHardestCards()
            "reset stats" -> resetStats()
            "exit" -> {
                println("Bye bye!")
                break
            }
            else -> println("Unknown command")
        }
        println("")
    }

    for (i in args.indices step 2)
        if (args[i] == "-export") exportCards(args[i + 1])
}

fun addCard() {
    println("The card:")
    val term = readLine()
    if (!flashCards.containsKey(term)) {
        println("The definition of the card:")
        val definition = readLine()
        if (!flashCards.containsValue(definition)) {
            flashCards[term] = definition
            mistakes[term] = 0
            println("The pair (\"$term\":\"$definition\") has been added.")
        } else {
            println("The definition \"$definition\" already exists.")
        }
    } else {
        println("The card \"$term\" already exists.")
    }
}

fun removeCard() {
    println("Which card?")
    val term = readLine()
    if (flashCards.containsKey(term)) {
        flashCards.remove(term)
        mistakes.remove(term)
        println("The card has been removed.")
    } else {
        println("Can't remove \"$term\": there is no such card")
    }
}

fun askFromCards() {
    if (flashCards.isEmpty()) {
        println("No cards are available.")
        return
    }

    println("How many times to ask?")
    try {
        val numQuestions = readLine().toInt()
        for (i in 1..numQuestions) {
            val term = flashCards.entries.elementAt(Random.nextInt(flashCards.size)).key
            println("Print the definition of \"$term\":")
            val definition = readLine()
            if ( definition == flashCards[term]) println("Correct!")
            else {
                mistakes[term] = mistakes[term]!! + 1
                var message = "Wrong. The right answer is \"${flashCards[term]}\"."
                if (flashCards.containsValue(definition)) {
                    message += ", but your definition is correct for " +
                               "\"${flashCards.filter { it.value == definition }.keys.first()}\"."
                }
                println(message)
            }
        }
    } catch (e: NumberFormatException) {
        println("Not a number")
    }
}

fun readFileName(input: String): String {
    return if (input.isEmpty()) {
        println("File name:")
        readLine()
    } else input
}
fun importCards(input: String = "") {

    val file = File(readFileName(input))

    if (file.exists()) {
        try {
            val cards = file.readLines()
            for (i in cards.indices step 3) {
                flashCards[cards[i]] = cards[i + 1]
                mistakes[cards[i]] = cards[i + 2].toInt()
            }
            println("${cards.size / 3} cards have been loaded.\n")
        } catch (e: Exception) {
            println("There was an error in reading the File")
        }
    } else println("File not found.\n")
}

fun exportCards(input: String = "") {
    File(readFileName(input)).writeText(flashCardsToString())
    println("${flashCards.size} cards have been saved.\n")
}

fun logToFile() {
    val file = File(readFileName(""))
    var text = ""
    for (line in ioLog) text += "$line\n"
    file.writeText(text)
    println("The log has been saved.")
}

fun resetStats() {
    for (key in mistakes.keys) {
        mistakes[key] = 0
    }
    println("Card statistics have been reset.")
}

fun getHardestCards() {
    val max = mistakes.values.toTypedArray().maxOrNull()
    val hardestCards = mistakes.filterValues { it == max }

    when {
        max == 0 || max == null -> println("There are no cards with errors.")
        hardestCards.size == 1 -> println("The hardest card is \"${hardestCards.keys.first()}\"." +
                                          " You have $max errors answering it")
        hardestCards.size > 1 -> {
            var message = ""
            for (line in hardestCards.keys) {
                message += ", \"$line\""
            }
            message = message.removePrefix(", ")
            println("The hardest cards are $message. You have $max errors answering them.")
        }
    }
}

fun flashCardsToString(): String {
    var text = ""
    flashCards.forEach { text += "\n${it.key}\n${it.value}\n${mistakes[it.key]}" }
    return text.removePrefix("\n")
}
