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



        private fun ensureReachVariety(deck: List<Card>): List<Card> {
            val reach0Cards = deck.filter { it.attributes.reach == 0 }
            val reach1Cards = deck.filter { it.attributes.reach == 1 }
            val reach2Cards = deck.filter { it.attributes.reach == 2 }

            // Si falta algún tipo de alcance, añadir cartas aleatorias de ese tipo
            val newDeck = deck.toMutableList()
            if (reach0Cards.isEmpty()) {
                deck.firstOrNull { it.attributes.reach != null }?.let { newDeck.add(it.copy(attributes = it.attributes.copy(reach = 0))) }
            }
            if (reach1Cards.isEmpty()) {
                deck.firstOrNull { it.attributes.reach != null }?.let { newDeck.add(it.copy(attributes = it.attributes.copy(reach = 1))) }
            }
            if (reach2Cards.isEmpty()) {
                deck.firstOrNull { it.attributes.reach != null }?.let { newDeck.add(it.copy(attributes = it.attributes.copy(reach = 2))) }
            }

            return newDeck
        }


        fun startGame(playerDeck: List<Card>, aiDeck: List<Card>) {
            // Asegurar que cada mazo tenga al menos 3 cartas de cada tipo de alcance
            val preparedPlayerDeck = prepareDeckWithReachVariety(playerDeck)
            val preparedAiDeck = prepareDeckWithReachVariety(aiDeck)

            gameState.player.deck = preparedPlayerDeck.toMutableList().apply { shuffle() }
            gameState.ai.deck = preparedAiDeck.toMutableList().apply { shuffle() }

            // Repartir 10 cartas asegurando al menos 1 de cada reach
            dealInitialHands()
        }

        private fun prepareDeckWithReachVariety(deck: List<Card>): List<Card> {
            val reach0Cards = deck.filter { it.attributes.reach == 0 }
            val reach1Cards = deck.filter { it.attributes.reach == 1 }
            val reach2Cards = deck.filter { it.attributes.reach == 2 }

            val newDeck = deck.toMutableList()

            // Añadir cartas si no hay suficientes de algún tipo
            if (reach0Cards.size < 3) {
                repeat(3 - reach0Cards.size) {
                    deck.firstOrNull { it.attributes.reach != null }?.let {
                        newDeck.add(it.copy(attributes = it.attributes.copy(reach = 0)))
                    }
                }
            }
            if (reach1Cards.size < 3) {
                repeat(3 - reach1Cards.size) {
                    deck.firstOrNull { it.attributes.reach != null }?.let {
                        newDeck.add(it.copy(attributes = it.attributes.copy(reach = 1)))
                    }
                }
            }
            if (reach2Cards.size < 3) {
                repeat(3 - reach2Cards.size) {
                    deck.firstOrNull { it.attributes.reach != null }?.let {
                        newDeck.add(it.copy(attributes = it.attributes.copy(reach = 2)))
                    }
                }
            }

            return newDeck
        }

        private fun dealInitialHands() {
            // Repartir al jugador asegurando al menos 1 carta de cada reach
            val playerHand = mutableListOf<Card>()
            val reachTypes = listOf(0, 1, 2)

            // Añadir al menos 1 carta de cada reach
            reachTypes.forEach { reach ->
                val card = gameState.player.deck.firstOrNull { it.attributes.reach == reach }
                card?.let {
                    playerHand.add(it)
                    gameState.player.deck.remove(it)
                }
            }

            // Completar hasta 10 cartas
            while (playerHand.size < 10 && gameState.player.deck.isNotEmpty()) {
                playerHand.add(gameState.player.deck.removeAt(0))
            }

            gameState.player.hand.addAll(playerHand)

            // Hacer lo mismo para la IA
            val aiHand = mutableListOf<Card>()
            reachTypes.forEach { reach ->
                val card = gameState.ai.deck.firstOrNull { it.attributes.reach == reach }
                card?.let {
                    aiHand.add(it)
                    gameState.ai.deck.remove(it)
                }
            }

            while (aiHand.size < 10 && gameState.ai.deck.isNotEmpty()) {
                aiHand.add(gameState.ai.deck.removeAt(0))
            }

            gameState.ai.hand.addAll(aiHand)
        }

        fun prepareNextRound() {
            gameState.currentRound++
            gameState.player.passed = false
            gameState.ai.passed = false

            // Limpiar tableros y mover cartas a descarte
            gameState.player.board.values.forEach { row ->
                gameState.player.discardPile.addAll(row)
                row.clear()
            }
            gameState.ai.board.values.forEach { row ->
                gameState.ai.discardPile.addAll(row)
                row.clear()
            }

            // Robar 2 cartas nuevas
            repeat(2) {
                gameState.player.drawCard()
                gameState.ai.drawCard()
            }
        }




}


















