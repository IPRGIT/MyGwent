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

        // AI turn logic
        if (!gameState.currentPlayer && !gameState.ai.passed) {
            // TODO: Implement AI decision making
        }
    }

    private fun endRound() {
        val playerScore = calculatePlayerScore()
        val aiScore = calculateAIScore()

        when {
            playerScore > aiScore -> gameState.ai.lives--
            aiScore > playerScore -> gameState.player.lives--
            // Tie - no lives lost
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

        // Reset passed status
        gameState.player.passed = false
        gameState.ai.passed = false

        // Move all cards from board to discard pile
        gameState.player.apply {
            board.values.forEach { row ->
                discardPile.addAll(row)
                row.clear()
            }
        }

        gameState.ai.apply {
            board.values.forEach { row ->
                discardPile.addAll(row)
                row.clear()
            }
        }

        // Loser of previous round starts next round
        gameState.currentPlayer = calculatePlayerScore() < calculateAIScore()

        // Draw 2 cards for next round
        repeat(2) {
            gameState.player.drawCard()
            gameState.ai.drawCard()
        }
    }

    private fun endGame() {
        gameEnded = true
        // TODO: Handle game end (show winner, etc.)
    }

    // Scoring functions
    fun calculatePlayerScore(): Int {
        return calculateScore(gameState.player)
    }

    fun calculateAIScore(): Int {
        return calculateScore(gameState.ai)
    }



    // Card effect implementations
    private fun applyCardEffects(card: Card, player: Player) {
        // TODO: Implement card effects (abilities, etc.)
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


        fun startGame(playerDeck: List<Card>, aiDeck: List<Card>) {
            // Reset game state
            gameEnded = false
            gameState.currentRound = 1
            gameState.weatherEffects.clear()

            // Initialize decks and shuffle them
            gameState.player.deck = playerDeck.toMutableList().apply { shuffle() }
            gameState.ai.deck = aiDeck.toMutableList().apply { shuffle() }

            // Clear hands and boards
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

            // Deal initial cards (10 each)
            repeat(10) {
                gameState.player.drawCard()
                gameState.ai.drawCard()
            }

            // Random starting player (50/50 chance)
            gameState.currentPlayer = Random.nextBoolean()
        }



        fun playCard(card: Card, isPlayer: Boolean, selectedRow: String? = null): Boolean {
            if (gameEnded || isPlayer != gameState.currentPlayer) return false

            val currentPlayer = if (isPlayer) gameState.player else gameState.ai

            if (!currentPlayer.hand.contains(card) || !card.isPlayable()) return false

            return when {
                card.isUnitCard() -> playUnitCard(currentPlayer, card, selectedRow)
                card.isSpecialCard() -> playSpecialCard(currentPlayer, card)
                card.isWeatherCard() -> playWeatherCard(currentPlayer, card)
                else -> false
            }.also { success ->
                if (success) {
                    currentPlayer.hand.remove(card)
                    switchTurn()
                }
            }
        }

        private fun playUnitCard(player: Player, card: Card, selectedRow: String?): Boolean {
            val row = when {
                selectedRow != null -> selectedRow
                card.attributes.reach == 1 -> "ranged"
                card.attributes.reach == 2 -> "siege"
                else -> "melee" // Por defecto o reach 0
            }

            player.board[row]?.add(card)
            applyCardEffects(card, player)
            return true
        }


        private fun calculateScore(player: Player): Int {
            var totalScore = 0

            // Calcular puntuación para cada fila
            player.board.forEach { (row, cards) ->
                val rowPower = cards.sumOf { it.power ?: 0 }
                totalScore += if (gameState.weatherEffects.contains(row)) {
                    cards.size // Weather reduce todas las unidades a 1 de poder
                } else {
                    rowPower
                }
            }

            return totalScore
        }



}