package com.example.mygwent.data

import Player


data class GameState(
    val player: Player,
    val ai: Player,
    var currentRound: Int = 1,
    var currentPlayer: Boolean = true, // true for human, false for AI
    var weatherEffects: MutableSet<String> = mutableSetOf(),
    var coinTossWinner: Boolean = true // true if player won coin toss
)