package com.example.mygwent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mygwent.adapter.BoardRowAdapter
import com.example.mygwent.adapter.HandAdapter
import com.example.mygwent.data.Card
import com.example.mygwent.databinding.ActivityGameBinding
import com.example.mygwent.game.GameEngine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameEngine: GameEngine
    private lateinit var playerHandAdapter: HandAdapter
    private lateinit var aiHandAdapter: HandAdapter
    private val viewModel: CardViewModel by viewModels()
    private lateinit var playerMeleeAdapter: BoardRowAdapter
    private lateinit var playerRangedAdapter: BoardRowAdapter
    private lateinit var playerSiegeAdapter: BoardRowAdapter
    private lateinit var aiMeleeAdapter: BoardRowAdapter
    private lateinit var aiRangedAdapter: BoardRowAdapter
    private lateinit var aiSiegeAdapter: BoardRowAdapter




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
            // Adapters para las manos
            playerHandAdapter = HandAdapter { card ->
                if (card.isUnitCard()) {
                    // Mostrar diálogo para seleccionar fila si la carta es de unidad
                    showRowSelectionDialog(card)
                } else {
                    // Jugar carta especial/weather directamente
                    gameEngine.playCard(card, isPlayer = true)
                    updateUI()
                }
            }

            binding.playerHandRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = playerHandAdapter
            }

            aiHandAdapter = HandAdapter { /* No action */ }
            binding.aiHandRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = aiHandAdapter
            }

            // Adapters para las filas del jugador
            playerMeleeAdapter = BoardRowAdapter { /* No action */ }
            binding.playerMeleeRow.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = playerMeleeAdapter
            }

            playerRangedAdapter = BoardRowAdapter { /* No action */ }
            binding.playerRangedRow.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = playerRangedAdapter
            }

            playerSiegeAdapter = BoardRowAdapter { /* No action */ }
            binding.playerSiegeRow.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = playerSiegeAdapter
            }

            // Adapters para las filas de la IA
            aiMeleeAdapter = BoardRowAdapter { /* No action */ }
            binding.aiMeleeRow.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = aiMeleeAdapter
            }

            aiRangedAdapter = BoardRowAdapter { /* No action */ }
            binding.aiRangedRow.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = aiRangedAdapter
            }

            aiSiegeAdapter = BoardRowAdapter { /* No action */ }
            binding.aiSiegeRow.apply {
                layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = aiSiegeAdapter
            }

            binding.btnPass.setOnClickListener {
                gameEngine.pass(isPlayer = true)
                updateUI()
            }
        }

        private fun showRowSelectionDialog(card: Card) {
            val reach = card.attributes.reach ?: 0
            val rows = when (reach) {
                0 -> listOf("Melee")
                1 -> listOf("Ranged")
                2 -> listOf("Siege")
                else -> listOf("Melee", "Ranged", "Siege")
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Seleccionar fila para ${card.name}")

            builder.setItems(rows.toTypedArray()) { _, which ->
                val selectedRow = when (which) {
                    0 -> "melee"
                    1 -> if (rows.size > 1) "ranged" else "melee"
                    2 -> "siege"
                    else -> "melee"
                }
                gameEngine.playCard(card, isPlayer = true, selectedRow)
                updateUI()
            }

            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

        private fun startNewGame(allCards: List<Card>) {
            if (allCards.isEmpty()) {
                Toast.makeText(this, "No cards available", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            val factions = listOf("monsters", "nilfgaard", "northernrealms", "scoiatael", "skellige")
            val playerFaction = factions.random()
            val aiFaction = factions.random()

            val playerDeck = List(47) {
                allCards.filter { it.faction.equals(playerFaction, ignoreCase = true) }.random().copy()
            }
            val aiDeck = List(47) {
                allCards.filter { it.faction.equals(aiFaction, ignoreCase = true) }.random().copy()
            }

            setDeckImages(playerFaction, aiFaction)
            gameEngine.startGame(playerDeck, aiDeck)
            updateUI()
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

        private fun updateUI() {
            playerHandAdapter.submitList(gameEngine.gameState.player.hand)
            aiHandAdapter.submitList(gameEngine.gameState.ai.hand)

            // Actualizar filas del jugador
            playerMeleeAdapter.submitList(gameEngine.gameState.player.board["melee"] ?: emptyList())
            playerRangedAdapter.submitList(gameEngine.gameState.player.board["ranged"] ?: emptyList())
            playerSiegeAdapter.submitList(gameEngine.gameState.player.board["siege"] ?: emptyList())

            // Actualizar filas de la IA
            aiMeleeAdapter.submitList(gameEngine.gameState.ai.board["melee"] ?: emptyList())
            aiRangedAdapter.submitList(gameEngine.gameState.ai.board["ranged"] ?: emptyList())
            aiSiegeAdapter.submitList(gameEngine.gameState.ai.board["siege"] ?: emptyList())

            binding.playerScore.text = gameEngine.calculatePlayerScore().toString()
            binding.aiScore.text = gameEngine.calculateAIScore().toString()
            binding.tvRound.text = "Round ${gameEngine.gameState.currentRound}"
            binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
            binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()
        }





































}








































