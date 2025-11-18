package com.example.mygwent.game

import Player
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.mygwent.data.Card
import com.example.mygwent.data.GameState

class GameEngine(private val context: Context) {

    val gameState = GameState(
        player = Player(deck = mutableListOf()),
        ai = Player(deck = mutableListOf())
    )

    private var gameEnded = false

    // Variables para manejar la selección temporal de cartas
    private var selectedCard: Card? = null
    private var selectedValidRows: List<String> = emptyList()

    // Interfaz para comunicación con la Activity
    interface OnCardSelectionListener {
        fun onCardSelected(card: Card, validRows: List<String>)
        fun onCardDeselected()
        fun onCardPlayed(card: Card, row: String, success: Boolean)
    }

    private var selectionListener: OnCardSelectionListener? = null

    fun setSelectionListener(listener: OnCardSelectionListener) {
        this.selectionListener = listener
    }

    // Método para seleccionar una carta de la mano
    fun selectCardFromHand(card: Card): Boolean {
        val currentPlayer = gameState.player

        // Verificar que la carta esté en la mano del jugador
        if (!currentPlayer.hand.contains(card)) {
            Log.e("GameEngine", "Card not in player's hand: ${card.name}")
            return false
        }

        // Deseleccionar carta anterior si existe
        if (selectedCard != null) {
            selectionListener?.onCardDeselected()
        }

        // Seleccionar nueva carta
        selectedCard = card

        // Determinar filas válidas basado en el alcance de la carta
        selectedValidRows = when {
            card.isUnitCard() -> {
                when (card.attributes.reach ?: -1) {
                    0 -> listOf("melee")
                    1 -> listOf("ranged")
                    2 -> listOf("siege")
                    else -> listOf("melee", "ranged", "siege") // Cartas sin alcance definido pueden ir en cualquier fila
                }
            }
            card.isSpecialCard() || card.isWeatherCard() -> {
                // Cartas especiales y de clima no requieren fila específica
                emptyList()
            }
            else -> emptyList()
        }

        Log.d("GameEngine", "Card selected: ${card.name}, valid rows: $selectedValidRows")
        selectionListener?.onCardSelected(card, selectedValidRows)
        return true
    }

    // Método para colocar carta seleccionada en una fila
    fun placeSelectedCardOnRow(row: String): Boolean {
        val card = selectedCard ?: run {
            Log.e("GameEngine", "No card selected")
            return false
        }

        // Verificar que la fila sea válida para esta carta
        if (selectedValidRows.isNotEmpty() && !selectedValidRows.contains(row)) {
            Log.e("GameEngine", "Invalid row $row for card ${card.name}. Valid rows: $selectedValidRows")
            return false
        }

        // Jugar la carta
        val success = playCard(card, isPlayer = true, row)

        if (success) {
            // Limpiar selección
            clearCardSelection()
            selectionListener?.onCardPlayed(card, row, true)
            Log.d("GameEngine", "Card ${card.name} successfully placed on $row row")

            // Programar turno de la IA después de un delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (!gameState.isGameOver()) {
                    playAITurn()
                }
            }, 1000)
        } else {
            selectionListener?.onCardPlayed(card, row, false)
        }

        return success
    }

    // Método para limpiar la selección de carta
    fun clearCardSelection() {
        selectedCard = null
        selectedValidRows = emptyList()
        selectionListener?.onCardDeselected()
    }

    // Método para obtener la carta seleccionada actualmente
    fun getSelectedCard(): Card? = selectedCard

    // Método para obtener las filas válidas para la carta seleccionada
    fun getValidRows(): List<String> = selectedValidRows

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
            gameState.playerGems == 0 && gameState.aiGems > 0 -> "¡La IA ganó la partida!"
            gameState.aiGems == 0 && gameState.playerGems > 0 -> "¡Ganaste la partida!"
            gameState.playerGems == 0 && gameState.aiGems == 0 -> {
                when {
                    playerScore > aiScore -> "¡Ganaste la partida por puntos!"
                    aiScore > playerScore -> "La IA ganó la partida por puntos!"
                    else -> "¡Empate en la partida!"
                }
            }
            else -> {
                when {
                    playerScore > aiScore -> "¡Ganaste la partida por puntos!"
                    aiScore > playerScore -> "La IA ganó la partida por puntos!"
                    else -> "¡Empate en la partida!"
                }
            }
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun playCard(card: Card, isPlayer: Boolean, selectedRow: String? = null): Boolean {
        val currentPlayer = if (isPlayer) gameState.player else gameState.ai

        // Verificar que la carta esté en la mano del jugador
        if (!currentPlayer.hand.contains(card)) {
            Log.e("GameEngine", "Card not in player's hand: ${card.name}")
            return false
        }

        // Para cartas de unidad, verificar fila válida
        if (card.isUnitCard()) {
            val row = selectedRow ?: when (card.attributes.reach ?: -1) {
                0 -> "melee"
                1 -> "ranged"
                2 -> "siege"
                else -> {
                    Log.e("GameEngine", "Invalid reach for unit card: ${card.attributes.reach}")
                    return false
                }
            }

            // Verificar que la fila sea válida
            if (!currentPlayer.board.containsKey(row)) {
                Log.e("GameEngine", "Invalid row: $row")
                return false
            }

            // Añadir carta a la fila
            currentPlayer.board[row]?.add(card)
            applyCardEffects(card, currentPlayer, selectedRow.toString())
            Log.d("GameEngine", "Unit card ${card.name} played on $row row")
        }
        else if (card.isSpecialCard()) {
            applySpecialCard(card, isPlayer, selectedRow.toString())
            currentPlayer.discardPile.add(card)
            Log.d("GameEngine", "Special card ${card.name} played")
        }
        else if (card.isWeatherCard()) {
            applyWeatherCard(card)
            currentPlayer.discardPile.add(card)
            Log.d("GameEngine", "Weather card ${card.name} played")
        }
        else {
            Log.e("GameEngine", "Unplayable card type: ${card.type}")
            return false
        }

        // Remover carta de la mano
        currentPlayer.hand.remove(card)
        Log.d("GameEngine", "Card ${card.name} removed from hand. Hand size: ${currentPlayer.hand.size}")

        return true
    }

    // Método para que la IA juegue su turno
    fun playAITurn() {
        if (gameState.ai.passed || gameState.ai.hand.isEmpty()) {
            // Si la IA ya pasó o no tiene cartas, pasa automáticamente
            pass(isPlayer = false)
            return
        }

        val aiHand = gameState.ai.hand
        val randomCard = aiHand.random()
        val row = when (randomCard.attributes.reach ?: -1) {
            0 -> "melee"
            1 -> "ranged"
            2 -> "siege"
            else -> listOf("melee", "ranged", "siege").random()
        }

        if (playCard(randomCard, isPlayer = false, row)) {
            Log.d("GameEngine", "AI played card ${randomCard.name} on $row row")
        }
    }

    private fun applyCardEffects(card: Card, player: Player, row: String) {
        card.effects.forEach { effect ->
            when (effect) {
                "medic" -> {
                    val eligibleCards = player.discardPile.filter { it.isUnitCard() && !it.isGoldCard() }
                    if (eligibleCards.isNotEmpty()) {
                        val revived = eligibleCards.random()
                        player.discardPile.remove(revived)
                        player.board[row]?.add(revived)
                    }
                }
                "scorch" -> {
                    val opponent = if (player == gameState.player) gameState.ai else gameState.player
                    val allUnits = player.board.flatMap { it.value } + opponent.board.flatMap { it.value }
                    if (allUnits.isEmpty()) return@forEach
                    val maxPower = allUnits.maxOf { it.power ?: 0 }
                    val toDestroy = allUnits.filter { (it.power ?: 0) == maxPower && !it.isGoldCard() }
                    toDestroy.forEach { unit ->
                        val owner = if (player.board.any { it.value.contains(unit) }) player else opponent
                        owner.board.values.forEach { it.remove(unit) }
                        owner.discardPile.add(unit)
                    }
                }
                // Add more effects as needed
            }
        }
    }

    private fun applySpecialCard(card: Card, isPlayer: Boolean, selectedRow: String?) {
        val player = if (isPlayer) gameState.player else gameState.ai
        // Implement special card effects based on card name or ability
        // For example:
        when (card.name.lowercase()) {
            "commander's horn" -> {
                if (selectedRow != null) {
                    // Apply horn effect to the selected row
                    // But since horn is persistent, perhaps add a dummy card with horn effect
                    // For simplicity, assume immediate double, but in Gwent it's persistent
                    // Maybe add weather-like effect, but skip for now
                }
        }
            // Add more special cards
        }
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
        if (gameEnded) return false

        if (isPlayer) {
            gameState.player.passed = true
        } else {
            gameState.ai.passed = true
        }

        // Limpiar selección si el jugador pasa
        if (isPlayer) {
            clearCardSelection()
        }

        if (gameState.player.passed && gameState.ai.passed) {
            endRound()
        } else if (!isPlayer && !gameState.player.passed) {
            // Si la IA pasa y el jugador no ha pasado, continuar turno del jugador
            // No es necesario hacer nada, el jugador puede continuar jugando
        }

        return true
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
            if (gameState.weatherEffects.contains(row)) {
                // Under weather: non-gold = 1, gold = power
                totalScore += cards.sumOf { if (it.isGoldCard()) (it.power ?: 0) else 1 }
            } else {
                // Normal: apply bonds, etc.
                val groups = cards.groupBy { it.name }
                var rowPower = 0
                groups.forEach { (_, group) ->
                    if (group.isEmpty()) return@forEach
                    val firstCard = group.first()
                    val basePower = firstCard.power ?: 0
                    val hasBond = group.first().effects.contains("tight_bond")
                    val multiplier = if (hasBond && group.size > 1) group.size else 1
                    rowPower += basePower * multiplier * group.size
                }
                // TODO: Add morale, horn effects
                // For horn: if any card with "horn", double the power of non-horn cards
                val hasHorn = cards.any { it.effects.contains("horn") }
                if (hasHorn) {
                    val nonHornPower = cards.filter { !it.effects.contains("horn") }.sumOf { it.power ?: 0 }
                    val hornPower = cards.filter { it.effects.contains("horn") }.sumOf { it.power ?: 0 }
                    rowPower = nonHornPower * 2 + hornPower
                }
                totalScore += rowPower
            }
        }
        return totalScore
    }

    private fun prepareDeckWithoutZeroPower(deck: List<Card>): List<Card> {
        return deck.filter { card ->
            // Usar el método hasZeroPower() para filtrar cartas
            !card.hasZeroPower()
        }
    }

    fun startGame(playerDeck: List<Card>, aiDeck: List<Card>) {
        // Filtrar cartas con power = 0 antes de preparar los mazos
        val filteredPlayerDeck = prepareDeckWithoutZeroPower(playerDeck)
        val filteredAiDeck = prepareDeckWithoutZeroPower(aiDeck)

        val preparedPlayerDeck = prepareDeckWithReachVariety(filteredPlayerDeck)
        val preparedAiDeck = prepareDeckWithReachVariety(filteredAiDeck)

        gameState.player.deck = preparedPlayerDeck.toMutableList().apply { shuffle() }
        gameState.ai.deck = preparedAiDeck.toMutableList().apply { shuffle() }
        dealInitialHands()
        // Limpiar cualquier selección previa
        clearCardSelection()
    }



    private fun prepareDeckWithReachVariety(deck: List<Card>): List<Card> {
        val reach0Cards = deck.filter { it.attributes.reach == 0 }
        val reach1Cards = deck.filter { it.attributes.reach == 1 }
        val reach2Cards = deck.filter { it.attributes.reach == 2 }
        val newDeck = deck.toMutableList()
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
        val playerHand = mutableListOf<Card>()
        val aiHand = mutableListOf<Card>()

        // Función auxiliar para dibujar cartas válidas
        fun drawValidCards(player: Player, count: Int): MutableList<Card> {
            val drawnCards = mutableListOf<Card>()
            var attempts = 0
            val maxAttempts = player.deck.size * 2 // Evitar bucle infinito

            while (drawnCards.size < count && attempts < maxAttempts && player.deck.isNotEmpty()) {
                val card = player.deck.removeAt(0)

                // Usar el método hasZeroPower() para validar la carta
                if (!card.hasZeroPower()) {
                    drawnCards.add(card)
                } else {
                    // Descartar automáticamente cartas con power = 0
                    player.discardPile.add(card)
                    Log.d("GameEngine", "Descartada carta con power 0: ${card.name}")
                }
                attempts++
            }
            return drawnCards
        }

        // Dibujar cartas iniciales asegurando variedad de alcance
        val reachTypes = listOf(0, 1, 2)

        // Para el jugador
        reachTypes.forEach { reach ->
            val cardIndex = gameState.player.deck.indexOfFirst { it.attributes.reach == reach }
            if (cardIndex != -1) {
                val card = gameState.player.deck.removeAt(cardIndex)
                if (card.hasZeroPower()) {
                    // Descartar si tiene power 0 usando el método
                    gameState.player.discardPile.add(card)
                    Log.d("GameEngine", "Descartada carta inicial con power 0 (jugador): ${card.name}")
                } else {
                    playerHand.add(card)
                }
            }
        }

        // Completar mano del jugador con cartas válidas
        val remainingPlayerCards = 10 - playerHand.size
        if (remainingPlayerCards > 0) {
            playerHand.addAll(drawValidCards(gameState.player, remainingPlayerCards))
        }
        gameState.player.hand.addAll(playerHand)

        // Para la IA
        reachTypes.forEach { reach ->
            val cardIndex = gameState.ai.deck.indexOfFirst { it.attributes.reach == reach }
            if (cardIndex != -1) {
                val card = gameState.ai.deck.removeAt(cardIndex)
                if (card.hasZeroPower()) {
                    // Descartar si tiene power 0 usando el método
                    gameState.ai.discardPile.add(card)
                    Log.d("GameEngine", "Descartada carta inicial con power 0 (IA): ${card.name}")
                } else {
                    aiHand.add(card)
                }
            }
        }

        // Completar mano de la IA con cartas válidas
        val remainingAiCards = 10 - aiHand.size
        if (remainingAiCards > 0) {
            aiHand.addAll(drawValidCards(gameState.ai, remainingAiCards))
        }
        gameState.ai.hand.addAll(aiHand)

        Log.d("GameEngine", "Mano jugador: ${playerHand.size} cartas, Mano IA: ${aiHand.size} cartas")
        Log.d("GameEngine", "Descartes jugador: ${gameState.player.discardPile.size}, Descarte IA: ${gameState.ai.discardPile.size}")
    }

    private fun prepareNextRound() {
        gameState.currentRound++
        gameState.player.passed = false
        gameState.ai.passed = false

        // Limpiar tableros y mover cartas al cementerio
        gameState.player.board.values.forEach { row ->
            gameState.player.discardPile.addAll(row)
            row.clear()
        }
        gameState.ai.board.values.forEach { row ->
            gameState.ai.discardPile.addAll(row)
            row.clear()
        }

        // Dibujar nuevas cartas asegurando que no tengan power = 0
        repeat(2) {
            drawValidCard(gameState.player)
            drawValidCard(gameState.ai)
        }

        // Limpiar selección al comenzar nueva ronda
        clearCardSelection()
    }

    // Nuevo método para dibujar una carta válida
    private fun drawValidCard(player: Player) {
        if (player.deck.isEmpty()) return

        var validCardFound = false
        var attempts = 0
        val maxAttempts = player.deck.size

        while (!validCardFound && attempts < maxAttempts && player.deck.isNotEmpty()) {
            val card = player.deck.removeAt(0)

            // Usar el método hasZeroPower() para validar la carta
            if (!card.hasZeroPower()) {
                player.hand.add(card)
                validCardFound = true
                Log.d("GameEngine", "Carta válida añadida a la mano: ${card.name}")
            } else {
                // Descartar automáticamente cartas con power = 0
                player.discardPile.add(card)
                Log.d("GameEngine", "Descartada carta con power 0 durante robo: ${card.name}")
            }
            attempts++
        }

        if (!validCardFound) {
            Log.w("GameEngine", "No se encontró carta válida después de $attempts intentos")
        }
    }





}