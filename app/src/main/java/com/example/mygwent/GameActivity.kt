package com.example.mygwent

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mygwent.adapter.HandAdapter
import com.example.mygwent.data.Card
import com.example.mygwent.data.GameState
import com.example.mygwent.databinding.ActivityGameBinding
import com.example.mygwent.game.GameEngine
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameEngine: GameEngine
    private lateinit var playerHandAdapter: HandAdapter
    private val viewModel: CardViewModel by viewModels()
    private lateinit var playerMeleeAdapter: HandAdapter.BoardRowAdapter
    private lateinit var playerRangedAdapter: HandAdapter.BoardRowAdapter
    private lateinit var playerSiegeAdapter: HandAdapter.BoardRowAdapter
    private lateinit var aiMeleeAdapter: HandAdapter.BoardRowAdapter
    private lateinit var aiRangedAdapter: HandAdapter.BoardRowAdapter
    private lateinit var aiSiegeAdapter: HandAdapter.BoardRowAdapter
    private var selectedRow: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameEngine = GameEngine(this)
        setupAdapters()

        lifecycleScope.launch {
            viewModel.allCards.collect { cards ->
                if (cards.isNotEmpty()) {
                    startNewGame(cards)
                } else {
                    viewModel.loadAllCards()
                }
            }
        }
    }

    private fun setupAdapters() {

        playerHandAdapter = HandAdapter { card ->
                if (card.isUnitCard()) {
                    highlightRowForCard(card)
                    selectedRow = when (card.attributes.reach ?: 0) {
                        0 -> "melee"
                        1 -> "ranged"
                        2 -> "siege"
                        else -> "melee"
                    }
                } else {
                    gameEngine.playCard(card, isPlayer = true)
                    updateUI()
                    resetRowHighlights()
                }
            }

            binding.playerHandRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false).apply {
                    isItemPrefetchEnabled = false
                }
                adapter = playerHandAdapter
                setHasFixedSize(true)


                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels * 0.67f // 67% del ancho de pantalla
                val cardWidth = (screenWidth * 0.67f * 0.5f / 5).toInt() // Mitad del ancho disponible para 5 cartas
                val cardHeight = (cardWidth * 1.4f).toInt()


                val horizontalPadding = (screenWidth - (cardWidth * 5)) / 2
                setPadding(horizontalPadding.toInt(), 8, horizontalPadding.toInt(), 8)
                clipToPadding = false
            }


        binding.playerMeleeRow.setOnClickListener {
            if (selectedRow == "melee") {
                playSelectedCard()
            }
        }
        binding.playerRangedRow.setOnClickListener {
            if (selectedRow == "ranged") {
                playSelectedCard()
            }
        }
        binding.playerSiegeRow.setOnClickListener {
            if (selectedRow == "siege") {
                playSelectedCard()
            }
        }

        // Adapters para las filas del jugador
        playerMeleeAdapter = HandAdapter.BoardRowAdapter()
        binding.playerMeleeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerMeleeAdapter
            setHasFixedSize(true)
        }

        playerRangedAdapter = HandAdapter.BoardRowAdapter()
        binding.playerRangedRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerRangedAdapter
            setHasFixedSize(true)
        }

        playerSiegeAdapter = HandAdapter.BoardRowAdapter()
        binding.playerSiegeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerSiegeAdapter
            setHasFixedSize(true)
        }

        // Adapters para las filas de la IA
        aiMeleeAdapter = HandAdapter.BoardRowAdapter()
        binding.aiMeleeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiMeleeAdapter
            setHasFixedSize(true)
        }

        aiRangedAdapter = HandAdapter.BoardRowAdapter()
        binding.aiRangedRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiRangedAdapter
            setHasFixedSize(true)
        }

        aiSiegeAdapter = HandAdapter.BoardRowAdapter()
        binding.aiSiegeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiSiegeAdapter
            setHasFixedSize(true)
        }

        binding.btnPass.setOnClickListener {
            gameEngine.pass(isPlayer = true)
            updateUI()
            resetRowHighlights()
        }
    }

    // En GameActivity.kt
    private fun playSelectedCard() {
        selectedRow?.let { row ->
            playerHandAdapter.getSelectedCard()?.let { card ->
                if (gameEngine.playCard(card, isPlayer = true, row)) {
                    // Actualizar UI inmediatamente después de jugar la carta
                    updateUI()
                    resetRowHighlights()
                    selectedRow = null
                    playerHandAdapter.clearSelection()

                    // Verificar si el juego ha terminado
                    if (gameEngine.gameState.player.lives <= 0 || gameEngine.gameState.ai.lives <= 0) {
                        // Mostrar resultado del juego
                        val message = if (gameEngine.gameState.player.lives <= 0)
                            "Has perdido la partida" else "¡Has ganado la partida!"
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateUI() {
        playerHandAdapter.submitList(gameEngine.gameState.player.hand.toList())
        playerMeleeAdapter.submitList(gameEngine.gameState.player.board["melee"] ?: emptyList())
        playerRangedAdapter.submitList(gameEngine.gameState.player.board["ranged"] ?: emptyList())
        playerSiegeAdapter.submitList(gameEngine.gameState.player.board["siege"] ?: emptyList())
        aiMeleeAdapter.submitList(gameEngine.gameState.ai.board["melee"] ?: emptyList())
        aiRangedAdapter.submitList(gameEngine.gameState.ai.board["ranged"] ?: emptyList())
        aiSiegeAdapter.submitList(gameEngine.gameState.ai.board["siege"] ?: emptyList())

        // Actualizar contadores
        binding.playerScore.text = gameEngine.calculatePlayerScore().toString()
        binding.aiScore.text = gameEngine.calculateAIScore().toString()
        binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
        binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()

        // Forzar redibujado de las vistas
        binding.playerHandRecyclerView.invalidate()
        binding.playerMeleeRow.invalidate()
        binding.playerRangedRow.invalidate()
        binding.playerSiegeRow.invalidate()
    }



    private fun setDeckImages(playerFaction: String, aiFaction: String) {
        val playerDeckRes = when(playerFaction.lowercase()) {
            "monsters" -> R.drawable.deckmonsters
            "nilfgaard" -> R.drawable.decknilfgaard
            "northernrealms" -> R.drawable.deckrealms
            "scoiatael" -> R.drawable.deckscoiatael
            "skellige" -> R.drawable.deckskellige
            else -> R.drawable.deckrealms
        }

        val aiDeckRes = when(aiFaction.lowercase()) {
            "monsters" -> R.drawable.deckmonsters
            "nilfgaard" -> R.drawable.decknilfgaard
            "northernrealms" -> R.drawable.deckrealms
            "scoiatael" -> R.drawable.deckscoiatael
            "skellige" -> R.drawable.deckskellige
            else -> R.drawable.deckrealms
        }

        binding.playerDeckImage.setImageResource(playerDeckRes)
        binding.aiDeckImage.setImageResource(aiDeckRes)
    }

    private fun highlightRowForCard(card: Card) {
        resetRowHighlights()
        val reach = card.attributes.reach ?: -1
        val rowView = when (reach) {
            0 -> binding.playerMeleeRow
            1 -> binding.playerRangedRow
            2 -> binding.playerSiegeRow
            else -> null
        }
        rowView?.setBackgroundColor(ContextCompat.getColor(this, R.color.gold_highlight))
    }

    private fun resetRowHighlights() {
        binding.playerMeleeRow.setBackgroundColor(Color.TRANSPARENT)
        binding.playerRangedRow.setBackgroundColor(Color.TRANSPARENT)
        binding.playerSiegeRow.setBackgroundColor(Color.TRANSPARENT)
    }




    // In GameActivity.kt
    private fun startNewGame(allCards: List<Card>) {
        if (allCards.isEmpty()) {
            Toast.makeText(this, "No cards available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val factions = listOf("monsters", "nilfgaard", "northernrealms", "scoiatael", "skellige")
        var playerFaction: String
        var aiFaction: String
        var playerDeck: List<Card>
        var aiDeck: List<Card>

        do {
            playerFaction = factions.random()
            aiFaction = factions.random()

            val playerCards = allCards.filter { it.faction.equals(playerFaction, ignoreCase = true) }
            val aiCards = allCards.filter { it.faction.equals(aiFaction, ignoreCase = true) }

            // Create decks with 47 cards each
            playerDeck = List(47.coerceAtMost(playerCards.size)) {
                playerCards.random().copy()
            }

            aiDeck = List(47.coerceAtMost(aiCards.size)) {
                aiCards.random().copy()
            }
        } while (playerDeck.size < 47 || aiDeck.size < 47)

        setDeckImages(playerFaction, aiFaction)
        gameEngine.startGame(playerDeck, aiDeck)
        updateUI()
    }







}
