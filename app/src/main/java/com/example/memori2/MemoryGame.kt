package com.example.memori2

import com.example.memori2.model.CardItem
import com.example.memori2.model.CardType

class MemoryGame(private val mode: Int, private val level: Int) {

    var cards: MutableList<CardItem> = mutableListOf()
    var currentPlayer = 0
    var scores = arrayListOf(0, 0)

    var modo = mode;

    var nivel = level;

    private var firstIndex: Int? = null

    init {
        resetGame()
    }


    fun resetGame() {
        scores[0] = 0
        scores[1] = 0
        currentPlayer = 0
        firstIndex = null

        val fruits = listOf(
            Pair("manzana", "Apple"),
            Pair("banana", "Banana"),
            Pair("naranja", "Orange"),
            Pair("uva", "Grape"),
            Pair("pera", "Pear"),
            Pair("mango", "Mango"),
            Pair("melon", "Melon"),
        )

        val numPairs = if (nivel == 2) 7 else 4 // Nivel 2: 7 pares, Nivel 1: 4 pares
        val selectedPairs = fruits.take(numPairs)

        cards.clear()
        var idCounter = 0
        var pairID = 0

        for ((image, word) in selectedPairs) {
            pairID++

            cards.add(
                CardItem(
                    id = idCounter++,
                    pairId = pairID,
                    type = CardType.IMAGE,
                    content = image
                )
            )

            cards.add(
                CardItem(
                    id = idCounter++,
                    pairId = pairID,
                    type = CardType.TEXT,
                    content = word
                )
            )
        }

        cards.shuffle()
    }

    fun flipCard(position: Int): Boolean {
        val card = cards[position]

        if (card.isFlipped || card.isMatched) return false

        card.isFlipped = true

        if (firstIndex == null) {
            firstIndex = position
            return true
        }

        val first = cards[firstIndex!!]

        if (first.pairId == card.pairId) {
            // acierto
            first.isMatched = true
            card.isMatched = true
            scores[currentPlayer]++

            firstIndex = null
            return true
        }

        // no acerto - cambiar jugador solo en modo 2
        if (mode == 2) {
            currentPlayer = 1 - currentPlayer
        }

        return false
    }

    fun flipBack(position1: Int, position2: Int) {
        cards[position1].isFlipped = false
        cards[position2].isFlipped = false
        firstIndex = null
    }
}
