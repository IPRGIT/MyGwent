package com.example.mygwent.data

import com.example.mygwent.data.Card.Companion.empty
import kotlin.random.Random

class Deck {
    private var cards: MutableList<Card> = mutableListOf()
    val size: Int get() = cards.size
    val isEmpty: Boolean get() = cards.isEmpty()
    val isNotEmpty: Boolean get() = cards.isNotEmpty()

    // Constructor para crear un mazo vacío
    constructor()

    // Constructor para crear un mazo con cartas específicas
    constructor(initialCards: List<Card>) {
        cards.addAll(initialCards)
    }

    // Constructor para crear un mazo aleatorio de un tamaño específico
    constructor(size: Int, allPossibleCards: List<Card>, allowDuplicates: Boolean = true) {
        if (allPossibleCards.isEmpty()) return

        cards = if (allowDuplicates) {
            // Permite cartas duplicadas
            MutableList(size) { allPossibleCards.random() }
        } else {
            // No permite duplicados (hasta agotar las cartas disponibles)
            if (size > allPossibleCards.size) {
                throw IllegalArgumentException("No hay suficientes cartas únicas para crear el mazo")
            }
            allPossibleCards.shuffled().take(size).toMutableList()
        }
    }

    // Añadir una carta al mazo
    fun addCard(card: Card): Boolean {
        return cards.add(card)
    }

    // Añadir múltiples cartas al mazo
    fun addCards(newCards: List<Card>): Boolean {
        return cards.addAll(newCards)
    }

    // Robar la carta superior del mazo
    fun drawCard(): Card {
        if (cards.isEmpty()) return empty()
        return cards.removeAt(0)
    }

    // Robar múltiples cartas
    fun drawCards(count: Int): List<Card> {
        if (count <= 0) return emptyList()
        val drawn = cards.take(count)
        cards = cards.drop(count).toMutableList()
        return drawn
    }

    // Barajar el mazo
    fun shuffle() {
        cards = cards.shuffled().toMutableList()
    }

    // Ver la carta en la posición especificada sin robarla
    fun peekAt(position: Int): Card {
        if (position !in cards.indices) return empty()
        return cards[position]
    }

    // Ver la carta superior sin robarla
    fun peekTop(): Card {
        if (cards.isEmpty()) return empty()
        return cards.first()
    }

    // Ver la carta inferior sin robarla
    fun peekBottom(): Card {
        if (cards.isEmpty()) return empty()
        return cards.last()
    }

    // Filtrar cartas por algún criterio
    fun filter(predicate: (Card) -> Boolean): List<Card> {
        return cards.filter(predicate)
    }

    // Buscar una carta específica
    fun findCard(predicate: (Card) -> Boolean): Card? {
        return cards.find(predicate)
    }

    // Eliminar una carta específica del mazo
    fun removeCard(card: Card): Boolean {
        return cards.remove(card)
    }

    // Eliminar cartas que cumplan un criterio
    fun removeCards(predicate: (Card) -> Boolean): List<Card> {
        val toRemove = cards.filter(predicate)
        cards.removeAll(toRemove)
        return toRemove
    }

    // Obtener todas las cartas sin modificar el mazo
    fun getAllCards(): List<Card> {
        return cards.toList()
    }

    // Reemplazar todo el mazo con nuevas cartas
    fun replaceAll(newCards: List<Card>) {
        cards = newCards.toMutableList()
    }

    // Limpiar el mazo (eliminar todas las cartas)
    fun clear() {
        cards.clear()
    }

    // Mezclar este mazo con otro
    fun combineWith(other: Deck) {
        cards.addAll(other.getAllCards())
    }

    // Dividir el mazo en dos partes
    fun split(atIndex: Int): Pair<Deck, Deck> {
        val first = cards.take(atIndex)
        val second = cards.drop(atIndex)
        return Pair(Deck(first), Deck(second))
    }

    // Crear un mazo con una facción específica (opcional)
    fun filterByFaction(faction: String): Deck {
        return Deck(cards.filter { it.faction.equals(faction, ignoreCase = true) })
    }

    // Estadísticas del mazo (opcional)
    fun getDeckStats(): DeckStats {
        val totalPower = cards.sumOf { it.power ?: 0 }
        val unitCards = cards.count { it.isUnitCard() }
        val specialCards = cards.count { it.isSpecialCard() }
        val weatherCards = cards.count { it.isWeatherCard() }
        val goldCards = cards.count { it.isGoldCard() }

        return DeckStats(
            totalCards = size,
            totalPower = totalPower,
            unitCards = unitCards,
            specialCards = specialCards,
            weatherCards = weatherCards,
            goldCards = goldCards
        )
    }

    data class DeckStats(
        val totalCards: Int,
        val totalPower: Int,
        val unitCards: Int,
        val specialCards: Int,
        val weatherCards: Int,
        val goldCards: Int
    )

    companion object {
        // Métodos de fábrica útiles

        fun createRandomDeck(size: Int, allCards: List<Card>): Deck {
            return Deck(size, allCards, allowDuplicates = true)
        }

        fun createNorthernRealmsDeck(allCards: List<Card>): Deck {
            val factionCards = allCards.filter { it.faction.equals("northernrealms", ignoreCase = true) }
            return Deck(factionCards)
        }

        // Puedes añadir más métodos de fábrica para otras facciones
    }
}