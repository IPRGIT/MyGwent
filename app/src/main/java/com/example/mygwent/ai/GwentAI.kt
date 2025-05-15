package com.example.mygwent.ai

import com.example.mygwent.data.Card
import kotlin.random.Random

class GwentAI(private val deck: List<Card>) {
    private val hand = mutableListOf<Card>()
    private val board = mutableListOf<Card>()
    var hasPassed = false

    fun drawInitialHand() {
        hand.addAll(deck.shuffled().take(10))
    }

    /*
 fun playTurn(): Card? {


     if (hasPassed || hand.isEmpty()) return null

     // Estrategia mejorada

     return when {
         hasWeatherCards() -> playWeatherCard()
         hasSpecialCards() -> playSpecialCard()
         else -> playUnitCard()
     }


    }


     */
    private fun playUnitCard(): Card? {
        // Seleccionar carta basada en fila y efectos
        return hand.maxByOrNull { calculateCardValue(it) }
    }

    private fun calculateCardValue(card: Card): Int {
        var value = card.power ?: 0
        // Añadir lógica para valorar efectos especiales
        return value
    }

    fun decideToPass(currentRound: Int): Boolean {
        // IA pasa si tiene menos de 3 cartas o si en rondas avanzadas quiere guardar cartas
        hasPassed = hand.size < 3 || (currentRound > 1 && Random.nextBoolean())
        return hasPassed
    }

    fun calculateBoardStrength(): Int = board.sumOf { it.power ?: 0 }
}
