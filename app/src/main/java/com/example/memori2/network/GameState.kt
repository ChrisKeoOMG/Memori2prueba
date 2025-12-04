package com.example.memori2.network

import com.example.memori2.model.CardItem

data class GameState(
    val cards: List<CardItem>,    // Lista completa de cartas y su estado
    val scores: List<Int>,        // Puntajes [P1, P2]
    val currentPlayer: Int,       // De quién es el turno (0 o 1)
    val isGameOver: Boolean = false,
    val winner: Int? = null,
    val level: Int = 1
)