package com.example.mygwent.data

import Player

data class GameState(

val player: Player,
val ai: Player,
var currentRound: Int = 1,
var currentPlayer: Boolean = true, // true for human, false for AI
var weatherEffects: MutableSet<String> = mutableSetOf(),
var coinTossWinner: Boolean = true, // true if player won coin toss
var playerGems: Int = 2, // Start with 2 gems (icon_gem_on)
var aiGems: Int = 2    // Start with 2 gems (icon_gem_on)
) {

    companion object {
        val currentRound:  Int = 1
    }

    fun playerLosesGem() {
        playerGems = (playerGems - 1).coerceAtLeast(0)
    }

    fun aiLosesGem() {
        aiGems = (aiGems - 1).coerceAtLeast(0)
    }

    fun isGameOver(): Boolean {
        return playerGems == 0 || aiGems == 0
    }



 }




























