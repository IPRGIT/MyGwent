package com.example.mygwent.game

import Player
import android.content.Context
import android.widget.Toast
import com.example.mygwent.data.Card
import com.example.mygwent.data.GameState
import kotlin.random.Random

class GameEngine(private val context: Context) {

    val gameState = GameState(
        player = Player(deck = mutableListOf()),
        ai = Player(deck = mutableListOf())
    )

    private var gameEnded = false


    private fun endRound() {
        val playerScore = calculatePlayerScore()
        val aiScore = calculateAIScore()

        when {
            playerScore > aiScore -> {
                gameState.aiLosesGem()
                Toast.makeText(context, "¡Ganaste la ronda!", Toast.LENGTH_SHORT).show()
            }
            aiScore > playerScore -> {
                gameState.playerLosesGem()
                Toast.makeText(context, "La IA ganó la ronda", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Empate - ambos pierden una gema
                gameState.playerLosesGem()
                gameState.aiLosesGem()
                Toast.makeText(context, "Empate - Ambos pierden una gema", Toast.LENGTH_SHORT).show()
            }
        }

        if (gameState.isGameOver()) {
            endGame()
            return
        }

        prepareNextRound()
    }

    private fun endGame() {
        gameEnded = true
        val playerScore = calculatePlayerScore()
        val aiScore = calculateAIScore()

        val message = when {
            gameState.playerGems == 0 -> "¡La IA ganó la partida!"
            gameState.aiGems == 0 -> "¡Ganaste la partida!"
            playerScore > aiScore -> "¡Ganaste la partida por puntos!"
            aiScore > playerScore -> "La IA ganó la partida por puntos"
            else -> "¡Empate en la partida!"
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()


    }



    fun startGame(playerDeck: List<Card>, aiDeck: List<Card>) {


        gameState.currentPlayer = true // El jugador siempre empieza primero
        gameEnded = false
        gameState.currentRound = 1
        gameState.weatherEffects.clear()

        gameState.player.deck = playerDeck.toMutableList().apply { shuffle() }
        gameState.ai.deck = aiDeck.toMutableList().apply { shuffle() }

        // Limpiar estados
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

        // Repartir 10 cartas al jugador (visible)
        repeat(10) {
            gameState.player.drawCard()?.let { card ->
                if (!gameState.player.hand.contains(card)) {
                    gameState.player.hand.add(card)
                }
            }
        }

        // Repartir 10 cartas a la IA (no visible para el jugador)
        repeat(10) {
            gameState.ai.drawCard()
        }


    }


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
            currentPlayer.hand.remove(card) // Asegurar que la carta se elimina de la mano
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
        if (gameState.ai.hand.isNotEmpty() && !gameState.ai.passed && Random.nextFloat() < 0.8f) {
            val card = gameState.ai.hand.random()
            val row = when (card.attributes.reach ?: 0) {
                0 -> "melee"
                1 -> "ranged"
                2 -> "siege"
                else -> "melee"
            }
            playCard(card, isPlayer = false, selectedRow = row)
        } else {
            pass(isPlayer = false)
        }
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