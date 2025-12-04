package com.example.memori2.model

enum class CardType { IMAGE, TEXT }

data class CardItem(
    val id: Int,
    val pairId: Int,
    val type: CardType,
    val content: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)