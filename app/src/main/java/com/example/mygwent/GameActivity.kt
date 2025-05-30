package com.example.mygwent

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mygwent.adapter.HandAdapter
import com.example.mygwent.data.Card
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

    // Variables para manejar la carta seleccionada y filas válidas
    private var selectedCard: Card? = null
    private var selectedValidRows: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameEngine = GameEngine(this)

        setupAdapters()
        setupGemViews()

        lifecycleScope.launch {
            viewModel.allCards.collect { cards ->
                if (cards.isNotEmpty()) {
                    startNewGame(cards)
                } else {
                    viewModel.loadAllCards()
                }
            }
        }

        // En el onCreate de GameActivity
        binding.fullsizeCard.setOnClickListener {
            hideFullSizeCard()
            selectedCard = null
            playerHandAdapter.clearSelection()
            resetRowHighlights()
        }

    }

    private fun setupGemViews() {
        updateGemViews()
    }



    private fun highlightRows(rows: List<String>) {
        resetRowHighlights()

        rows.forEach { row ->
            when (row) {
                "melee" -> binding.playerMeleeRow.setBackgroundResource(R.color.gold_highlight)
                "ranged" -> binding.playerRangedRow.setBackgroundResource(R.color.gold_highlight)
                "siege" -> binding.playerSiegeRow.setBackgroundResource(R.color.gold_highlight)
            }
        }
    }

    private fun resetRowHighlights() {
        binding.playerMeleeRow.setBackgroundResource(R.drawable.playermeleeasset)
        binding.playerRangedRow.setBackgroundResource(R.drawable.playerrangeasset)
        binding.playerSiegeRow.setBackgroundResource(R.drawable.playersiegeasset)
    }

    private fun updateUI() {
        updateGemViews()

        // Actualizar contadores de mazo
        binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
        binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()


        // Actualizar la mano del jugador
        playerHandAdapter.submitList(gameEngine.gameState.player.hand.toList())

        // Actualizar las filas del tablero
        playerMeleeAdapter.submitList(gameEngine.gameState.player.board["melee"] ?: emptyList())
        playerRangedAdapter.submitList(gameEngine.gameState.player.board["ranged"] ?: emptyList())
        playerSiegeAdapter.submitList(gameEngine.gameState.player.board["siege"] ?: emptyList())
        aiMeleeAdapter.submitList(gameEngine.gameState.ai.board["melee"] ?: emptyList())
        aiRangedAdapter.submitList(gameEngine.gameState.ai.board["ranged"] ?: emptyList())
        aiSiegeAdapter.submitList(gameEngine.gameState.ai.board["siege"] ?: emptyList())

        /*
        // Actualizar puntuaciones y contadores
        binding.playerScore.text = "Jugador: ${gameEngine.calculatePlayerScore()}"
        binding.aiScore.text = "IA: ${gameEngine.calculateAIScore()}"
        binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
        binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()

         */


        // Actualizar puntuaciones
        binding.playerScore.text = "Jugador: ${gameEngine.calculatePlayerScore()}"
        binding.aiScore.text = "IA: ${gameEngine.calculateAIScore()}"
    }




    private fun setDeckImages(playerFaction: String, aiFaction: String) {
        val playerDeckRes = when (playerFaction.lowercase()) {
            "monsters" -> R.drawable.deckmonsters
            "nilfgaard" -> R.drawable.decknilfgaard
            "northernrealms" -> R.drawable.deckrealms
            "scoiatael" -> R.drawable.deckscoiatael
            "skellige" -> R.drawable.deckskellige
            else -> R.drawable.deckrealms
        }

        val aiDeckRes = when (aiFaction.lowercase()) {
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

            // Crear mazos con exactamente 47 cartas cada uno
            playerDeck = if (playerCards.size >= 47) {
                playerCards.shuffled().take(47)
            } else {
                // Si no hay suficientes cartas, duplicamos algunas aleatoriamente
                val needed = 47 - playerCards.size
                playerCards + (1..needed).map { playerCards.random().copy() }
            }

            aiDeck = if (aiCards.size >= 47) {
                aiCards.shuffled().take(47)
            } else {
                val needed = 47 - aiCards.size
                aiCards + (1..needed).map { aiCards.random().copy() }
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


    private fun setupAdapters() {
        // Inicializar todos los adaptadores primero
        playerHandAdapter = HandAdapter { card ->
            selectedCard = card
            showFullSizeCard(card)
            // Determinar filas válidas para la carta
            selectedValidRows = when (card.attributes.reach ?: 0) {
                0 -> listOf("melee")
                1 -> listOf("ranged")
                2 -> listOf("siege")
                else -> listOf("melee", "ranged", "siege")
            }
            highlightRows(selectedValidRows)


            // Configurar click listener para el contenedor del juego
            binding.gameContainer.setOnClickListener {
                selectedCard = null
                playerHandAdapter.clearSelection()
                resetRowHighlights()
                hideFullSizeCard()
            }

        }

        // Inicializar adaptadores para las filas del jugador y la IA
        playerMeleeAdapter = HandAdapter.BoardRowAdapter()
        playerRangedAdapter = HandAdapter.BoardRowAdapter()
        playerSiegeAdapter = HandAdapter.BoardRowAdapter()
        aiMeleeAdapter = HandAdapter.BoardRowAdapter()
        aiRangedAdapter = HandAdapter.BoardRowAdapter()
        aiSiegeAdapter = HandAdapter.BoardRowAdapter()

        // Configurar RecyclerViews
        binding.playerHandRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerHandAdapter
        }

        // Configurar RecyclerViews para las filas del jugador
        binding.playerMeleeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerMeleeAdapter
        }
        binding.playerRangedRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerRangedAdapter
        }
        binding.playerSiegeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerSiegeAdapter
        }

        // Configurar RecyclerViews para las filas de la IA
        binding.aiMeleeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiMeleeAdapter
        }
        binding.aiRangedRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiRangedAdapter
        }
        binding.aiSiegeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiSiegeAdapter
        }

        // Configurar listeners para las filas
        binding.playerMeleeRow.setOnClickListener {
            if (selectedValidRows.contains("melee") && selectedCard != null) {
                playSelectedCard("melee")
            }
        }
        binding.playerRangedRow.setOnClickListener {
            if (selectedValidRows.contains("ranged") && selectedCard != null) {
                playSelectedCard("ranged")
            }
        }
        binding.playerSiegeRow.setOnClickListener {
            if (selectedValidRows.contains("siege") && selectedCard != null) {
                playSelectedCard("siege")
            }
        }

        // Configurar botón de pasar
        binding.btnPass.setOnClickListener {
            // Mostrar primera banda informativa
            showRoundInfoBanner("Ronda cedida", R.drawable.roundpassedasset) {
                // Mostrar segunda banda informativa después de la primera
                showRoundInfoBanner("Turno del oponente", R.drawable.aiturnasset) {
                    // Lógica después de mostrar ambas bandas
                    gameEngine.pass(isPlayer = true)
                    updateUI()

                    // Actualizar gemas
                    updateGemViews()

                    // Verificar si el juego ha terminado
                    if (gameEngine.gameState.isGameOver()) {
                        showGameOver()
                    } else {
                        // Turno de la IA después de un breve retraso
                        Handler(Looper.getMainLooper()).postDelayed({
                            playAITurn()
                        }, 1000)
                    }
                }
            }
        }
    }



    private fun showRoundInfoBanner(message: String, iconRes: Int, duration: Long = 2000, callback: (() -> Unit)? = null) {
        val bannerView = LayoutInflater.from(this).inflate(R.layout.round_info_banner, binding.root, false)

        bannerView.findViewById<TextView>(R.id.bannerText).text = message
        bannerView.findViewById<ImageView>(R.id.bannerIcon).setImageResource(iconRes)

        binding.root.addView(bannerView)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.root.removeView(bannerView)
            callback?.invoke()
        }, duration)
    }



        private fun playAITurn() {

            if (gameEngine.gameState.ai.passed || gameEngine.gameState.ai.hand.isEmpty()) {
                // Mostrar bandas informativas cuando la IA pasa
                showRoundInfoBanner("Ronda cedida", R.drawable.roundpassedasset) {
                    showRoundInfoBanner("Tu turno", R.drawable.playerturnasset) {
                        gameEngine.pass(isPlayer = false)
                        updateUI()
                        updateGemViews()

                        if (gameEngine.gameState.isGameOver()) {
                            showGameOver()
                        }
                    }
                }
                return
            }

            if (gameEngine.gameState.ai.passed || gameEngine.gameState.ai.hand.isEmpty()) {
                gameEngine.pass(isPlayer = false)
                checkRoundEnd()
                return
            }

            // Seleccionar carta aleatoria de la mano de la IA
            val aiHand = gameEngine.gameState.ai.hand
            val randomCard = aiHand.random()

            // Determinar fila válida para la carta
            val row = when (randomCard.attributes.reach ?: 0) {
                0 -> "melee"
                1 -> "ranged"
                2 -> "siege"
                else -> listOf("melee", "ranged", "siege").random()
            }

            if (gameEngine.playCard(randomCard, isPlayer = false, row)) {
                updateUI()

                // Verificar si el juego no ha terminado
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!gameEngine.gameState.isGameOver()) {
                        Toast.makeText(this, "Tu turno", Toast.LENGTH_SHORT).show()
                    }
                }, 1000)
            }
        }



    private fun showGameOver() {
        val playerScore = gameEngine.calculatePlayerScore()
        val aiScore = gameEngine.calculateAIScore()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Partida terminada")
            .setMessage("Puntuación final:\nJugador: $playerScore\nIA: $aiScore")
            .setPositiveButton("Jugar de nuevo") { _, _ ->
                lifecycleScope.launch {
                    val allCards = viewModel.allCards.value
                    if (allCards.isNotEmpty()) {
                        startNewGame(allCards)
                    }
                }
            }
            .setNegativeButton("Menú principal") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

        private fun checkRoundEnd() {
            if (gameEngine.gameState.player.passed && gameEngine.gameState.ai.passed) {
                // Determinar ganador de la ronda
                val playerScore = gameEngine.calculatePlayerScore()
                val aiScore = gameEngine.calculateAIScore()

                if (aiScore > playerScore) {
                    gameEngine.gameState.playerLosesGem()
                    updateGemViews()
                    Toast.makeText(this, "La IA gana la ronda", Toast.LENGTH_SHORT).show()
                } else if (playerScore > aiScore) {
                    gameEngine.gameState.aiLosesGem()
                    updateGemViews()
                    Toast.makeText(this, "Ganas la ronda", Toast.LENGTH_SHORT).show()
                } else {
                    // Empate - ambos pierden gema
                    gameEngine.gameState.playerLosesGem()
                    gameEngine.gameState.aiLosesGem()
                    updateGemViews()
                    Toast.makeText(this, "Empate - Ambos pierden gema", Toast.LENGTH_SHORT).show()
                }

                // Preparar siguiente ronda si el juego no ha terminado
                if (!gameEngine.gameState.isGameOver()) {
                    gameEngine.prepareNextRound()
                    updateUI()
                }
            }
        }


    private fun updateGemViews() {
        // Actualizar gemas del jugador
        binding.playerGem1Image.setImageResource(
            if (gameEngine.gameState.playerGems >= 1) R.drawable.icon_gem_on
            else R.drawable.icon_gem_off
        )
        binding.playerGem2Image.setImageResource(
            if (gameEngine.gameState.playerGems >= 2) R.drawable.icon_gem_on
            else R.drawable.icon_gem_off
        )

        // Actualizar gemas de la IA
        binding.aiGem1Image.setImageResource(
            if (gameEngine.gameState.aiGems >= 1) R.drawable.icon_gem_on
            else R.drawable.icon_gem_off
        )
        binding.aiGem2Image.setImageResource(
            if (gameEngine.gameState.aiGems >= 2) R.drawable.icon_gem_on
            else R.drawable.icon_gem_off
        )
    }




    private fun hideFullSizeCard() {
        binding.fullSizeCardImage.visibility = View.GONE
    }


        private fun showFullSizeCard(card: Card) {
            binding.fullSizeCardImage.visibility = View.VISIBLE

            // Usar un CardView para mostrar todos los detalles
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_card, binding.fullsizeCard, false)

            // Configurar los elementos de la carta
            cardView.findViewById<TextView>(R.id.cardName).text = card.name
            cardView.findViewById<TextView>(R.id.cardFaction).text = card.faction?.replaceFirstChar { it.uppercase() }
            cardView.findViewById<TextView>(R.id.cardStrength).text = card.power?.toString() ?: ""
            cardView.findViewById<TextView>(R.id.cardStrength).visibility = if (card.power != null) View.VISIBLE else View.GONE

            // Configurar borde dorado si es carta dorada
            cardView.findViewById<ImageView>(R.id.cardBorder).visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE

            // Configurar ícono de alcance
            if (card.type == "Unit" && card.attributes.reach != null) {
                val reachIcon = cardView.findViewById<ImageView>(R.id.cardReachIcon)
                val reachIconRes = when (card.attributes.reach) {
                    0 -> R.drawable.card_reach0
                    1 -> R.drawable.card_reach1
                    2 -> R.drawable.card_reach2
                    else -> null
                }
                if (reachIconRes != null) {
                    reachIcon.setImageResource(reachIconRes)
                    reachIcon.visibility = View.VISIBLE
                } else {
                    reachIcon.visibility = View.GONE
                }
            }

            // Cargar imagen de la carta
            Glide.with(this)
                .load(card.art)
                .into(cardView.findViewById(R.id.cardImage))

            // Limpiar el contenedor y añadir la nueva vista
            binding.fullsizeCard.removeAllViews()
            binding.fullsizeCard.addView(cardView)
        }

        private fun playSelectedCard(row: String) {
            selectedCard?.let { card ->
                if (gameEngine.playCard(card, isPlayer = true, row)) {
                    // Actualizar UI
                    updateUI()

                    // Ocultar carta de tamaño completo
                    binding.fullsizeCard.visibility = View.GONE
                    binding.fullsizeCard.removeAllViews()

                    // Resetear selección
                    resetRowHighlights()
                    selectedCard = null
                    playerHandAdapter.clearSelection()

                    // Turno de la IA después de un breve retraso
                    Handler(Looper.getMainLooper()).postDelayed({
                        playAITurn()
                    }, 1000)
                }
            }
        }





}
