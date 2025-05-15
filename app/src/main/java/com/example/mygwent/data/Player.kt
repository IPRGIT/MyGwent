import com.example.mygwent.data.Card


data class Player(
    var deck: MutableList<Card> = mutableListOf(),
    val hand: MutableList<Card> = mutableListOf(),
    val discardPile: MutableList<Card> = mutableListOf(),
    val board: MutableMap<String, MutableList<Card>> = mutableMapOf(
        "melee" to mutableListOf(),
        "ranged" to mutableListOf(),
        "siege" to mutableListOf()
    ),
    var lives: Int = 2,
    var passed: Boolean = false
) {
    fun drawCard(): Card? {
        if (deck.isEmpty()) return null
        val card = deck.removeAt(0)
        hand.add(card)
        return card
    }

    fun drawCards(count: Int): List<Card> {
        val drawnCards = mutableListOf<Card>()
        repeat(count) {
            drawCard()?.let { drawnCards.add(it) }
        }
        return drawnCards
    }
}