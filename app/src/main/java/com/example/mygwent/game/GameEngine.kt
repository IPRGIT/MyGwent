package com.example.mygwent.game

import Player
import android.content.Context
import com.example.mygwent.data.Card
import com.example.mygwent.data.GameState
import kotlin.random.Random

class GameEngine(private val context: Context) {
    val gameState = GameState(
        player = Player(deck = mutableListOf()),
        ai = Player(deck = mutableListOf())
    )

    private var gameEnded = false

    // In GameEngine.kt
    fun startGame(playerDeck: List<Card>, aiDeck: List<Card>) {
        gameEnded = false
        gameState.currentRound = 1
        gameState.weatherEffects.clear()

        gameState.player.deck = playerDeck.toMutableList().apply { shuffle() }
        gameState.ai.deck = aiDeck.toMutableList().apply { shuffle() }

        gameState.player.apply {
            hand.clear()
            discardPile.clear()
            board.values.forEach { it.clear() }
            passed = false
            lives = 2
        }

        gameState.ai.apply {
            hand.clear()
            discardPile.clear()
            board.values.forEach { it.clear() }
            passed = false
            lives = 2
        }

        // Draw 10 cards for each player
        repeat(10) {
            gameState.player.drawCard()?.let { card ->
                if (!gameState.player.hand.contains(card)) {
                    gameState.player.hand.add(card)
                }
            }
            gameState.ai.drawCard()?.let { card ->
                if (!gameState.ai.hand.contains(card)) {
                    gameState.ai.hand.add(card)
                }
            }
        }

        gameState.currentPlayer = Random.nextBoolean()
        if (!gameState.currentPlayer) {
            aiTurn()
        }
    }

    // En GameEngine.kt
    fun playCard(card: Card, isPlayer: Boolean, selectedRow: String? = null): Boolean {
        if (gameEnded || isPlayer != gameState.currentPlayer) return false

        val currentPlayer = if (isPlayer) gameState.player else gameState.ai

        if (!currentPlayer.hand.contains(card) || !card.isPlayable()) return false

        val success = when {
            card.isUnitCard() -> playUnitCard(currentPlayer, card, selectedRow)
            card.isSpecialCard() -> playSpecialCard(currentPlayer, card)
            card.isWeatherCard() -> playWeatherCard(currentPlayer, card)
            else -> false
        }

        if (success) {
            currentPlayer.hand.remove(card)
            switchTurn()
        }

        return success
    }

    private fun playUnitCard(player: Player, card: Card, selectedRow: String?): Boolean {
        val row = when {
            selectedRow != null -> selectedRow
            card.attributes.reach == 1 -> "ranged"
            card.attributes.reach == 2 -> "siege"
            else -> "melee"
        }

        player.board[row]?.add(card)
        applyCardEffects(card, player)
        return true
    }



    private fun playSpecialCard(player: Player, card: Card): Boolean {
        applySpecialCard(card, player == gameState.player)
        player.discardPile.add(card)
        return true
    }

    private fun playWeatherCard(player: Player, card: Card): Boolean {
        applyWeatherCard(card)
        player.discardPile.add(card)
        return true
    }

    private fun applyCardEffects(card: Card, player: Player) {
        // TODO: Implement card effects
    }

    private fun applySpecialCard(card: Card, isPlayer: Boolean) {
        // TODO: Implement special card effects
    }

    private fun applyWeatherCard(card: Card) {
        when (card.name.lowercase()) {
            "biting frost" -> gameState.weatherEffects.add("melee")
            "impenetrable fog" -> gameState.weatherEffects.add("ranged")
            "torrential rain" -> gameState.weatherEffects.add("siege")
            "clear weather" -> gameState.weatherEffects.clear()
        }
    }

    fun pass(isPlayer: Boolean): Boolean {
        if (gameEnded || isPlayer != gameState.currentPlayer) return false

        if (isPlayer) {
            gameState.player.passed = true
        } else {
            gameState.ai.passed = true
        }

        switchTurn()
        if (gameState.player.passed && gameState.ai.passed) {
            endRound()
        }
        return true
    }

    private fun switchTurn() {
        gameState.currentPlayer = !gameState.currentPlayer
        if (!gameState.currentPlayer && !gameState.ai.passed && !gameEnded) {
            aiTurn()
        }
    }

    private fun aiTurn() {
        // Simple AI: play a random card or pass
        if (gameState.ai.hand.isNotEmpty() && Random.nextFloat() < 0.8f) {
            val card = gameState.ai.hand.random()
            val row = when (card.attributes.reach ?: 0) {
                1 -> "ranged"
                2 -> "siege"
                else -> "melee"
            }
            playCard(card, isPlayer = false, selectedRow = row)
        } else {
            pass(isPlayer = false)
        }
    }

    private fun endRound() {
        val playerScore = calculatePlayerScore()
        val aiScore = calculateAIScore()

        when {
            playerScore > aiScore -> gameState.ai.lives--
            aiScore > playerScore -> gameState.player.lives--
        }

        if (gameState.player.lives <= 0 || gameState.ai.lives <= 0) {
            endGame()
            return
        }

        prepareNextRound()
    }

    private fun prepareNextRound() {
        gameState.currentRound++
        gameState.weatherEffects.clear()

        gameState.player.passed = false
        gameState.ai.passed = false

        gameState.player.apply {
            board.values.forEach { row ->
                discardPile.addAll(row)
                row.clear()
            }
            hand.clear() // Clear hand before drawing new cards
        }

        gameState.ai.apply {
            board.values.forEach { row ->
                discardPile.addAll(row)
                row.clear()
            }
            hand.clear() // Clear hand before drawing new cards
        }

        gameState.currentPlayer = calculatePlayerScore() < calculateAIScore()

        repeat(2) {
            gameState.player.drawCard()
            gameState.ai.drawCard()
        }
    }

    private fun endGame() {
        gameEnded = true
        // TODO: Handle game end
    }

    fun calculatePlayerScore(): Int {
        return calculateScore(gameState.player)
    }

    fun calculateAIScore(): Int {
        return calculateScore(gameState.ai)
    }

    private fun calculateScore(player: Player): Int {
        var totalScore = 0
        player.board.forEach { (row, cards) ->
            val rowPower = cards.sumOf { it.power ?: 0 }
            totalScore += if (gameState.weatherEffects.contains(row)) {
                cards.size
            } else {
                rowPower
            }
        }
        return totalScore
    }
}
