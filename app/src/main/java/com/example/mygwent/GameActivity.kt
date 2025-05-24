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
                // Para cartas especiales/meteorológicas
                if (gameEngine.playCard(card, isPlayer = true)) {
                    updateUI()
                    resetRowHighlights()
                }
            }
        }

        // Configurar RecyclerView para la mano del jugador
        binding.playerHandRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerHandAdapter
            setHasFixedSize(true)

            // Añadir padding para centrar las cartas
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels * 0.67f
            val totalCardWidth = (screenWidth * 0.18f * 10) // Ancho de 10 cartas
            val horizontalPadding = ((screenWidth - totalCardWidth) / 2).coerceAtLeast(0f)

            setPadding(horizontalPadding.toInt(), 16, horizontalPadding.toInt(), 16)
            clipToPadding = false
        }

        // Configurar adaptadores para las filas del jugador y la IA
        playerMeleeAdapter = HandAdapter.BoardRowAdapter()
        playerRangedAdapter = HandAdapter.BoardRowAdapter()
        playerSiegeAdapter = HandAdapter.BoardRowAdapter()
        aiMeleeAdapter = HandAdapter.BoardRowAdapter()
        aiRangedAdapter = HandAdapter.BoardRowAdapter()
        aiSiegeAdapter = HandAdapter.BoardRowAdapter()

        // Configurar RecyclerViews para las filas
        listOf(
            binding.playerMeleeRow to playerMeleeAdapter,
            binding.playerRangedRow to playerRangedAdapter,
            binding.playerSiegeRow to playerSiegeAdapter,
            binding.aiMeleeRow to aiMeleeAdapter,
            binding.aiRangedRow to aiRangedAdapter,
            binding.aiSiegeRow to aiSiegeAdapter
        ).forEach { (recyclerView, adapter) ->
            recyclerView.layoutManager = LinearLayoutManager(
                this@GameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null // Desactivar animaciones para evitar parpadeos
        }

        // Configurar listeners para las filas
        binding.playerMeleeRow.setOnClickListener {
            if (selectedRow == "melee") playSelectedCard()
        }
        binding.playerRangedRow.setOnClickListener {
            if (selectedRow == "ranged") playSelectedCard()
        }
        binding.playerSiegeRow.setOnClickListener {
            if (selectedRow == "siege") playSelectedCard()
        }

        binding.btnPass.setOnClickListener {
            gameEngine.pass(isPlayer = true)
            updateUI()
            resetRowHighlights()
        }
    }






    private fun playSelectedCard() {
        selectedRow?.let { row ->
            playerHandAdapter.getSelectedCard()?.let { card ->
                if (gameEngine.playCard(card, isPlayer = true, row)) {
                    updateUI()
                    resetRowHighlights()
                    selectedRow = null
                    playerHandAdapter.clearSelection()
                }
            }
        }
    }




    private fun updateUI() {
        // Actualizar la mano del jugador
        playerHandAdapter.submitList(gameEngine.gameState.player.hand.toList())

        // Actualizar las filas del tablero
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


    private fun highlightRowForCard(card: Card) {
        resetRowHighlights()
        val reach = card.attributes.reach ?: -1
        val rowView = when (reach) {
            0 -> binding.playerMeleeRow
            1 -> binding.playerRangedRow
            2 -> binding.playerSiegeRow
            else -> null
        }
        rowView?.background?.mutate()?.alpha = 150 // Semi-transparente
    }

    private fun resetRowHighlights() {
        listOf(binding.playerMeleeRow, binding.playerRangedRow, binding.playerSiegeRow).forEach { row ->
            row.background?.mutate()?.alpha = 255 // Opaco (valor original)
        }
    }






}
