package com.example.mygwent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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


    private var isCardSelected = false
    private var selectedCard: Card? = null
    private var selectedValidRows: List<String> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Asegurar que el botón esté visible inmediatamente
        binding.btnPass.visibility = View.VISIBLE
        binding.btnPass.isEnabled = true

        gameEngine = GameEngine(this)
        setupAdapters()
        setupGemViews()
        setupBoardAdapters()
        updatePassButton()

        // Agregar listener para debug
        setupButtonDebugListener()

        lifecycleScope.launch {
            viewModel.allCards.collect { cards ->
                if (cards.isNotEmpty()) {
                    startNewGame(cards)
                } else {
                    viewModel.loadAllCards()
                }
            }
        }

        binding.fullsizeCard.setOnClickListener {
            hideFullSizeCard()
            clearCardSelection()
        }
    }

    private fun setupGemViews() {
        updateGemViews()
    }

    private fun updateUI() {
        updateGemViews()
        binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
        binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()
        playerHandAdapter.submitList(gameEngine.gameState.player.getHandSnapshot())
        playerMeleeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("melee"))
        playerRangedAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("ranged"))
        playerSiegeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("siege"))
        aiMeleeAdapter.submitList(gameEngine.gameState.ai.getBoardSnapshot("melee"))
        aiRangedAdapter.submitList(gameEngine.gameState.ai.getBoardSnapshot("ranged"))
        aiSiegeAdapter.submitList(gameEngine.gameState.ai.getBoardSnapshot("siege"))
        binding.playerScore.text = "Jugador: ${gameEngine.calculatePlayerScore()}"
        binding.aiScore.text = "IA: ${gameEngine.calculateAIScore()}"

        // Asegurar visibilidad del botón
        ensureButtonVisibility()
    }


    private fun setupAdapters() {
        playerHandAdapter = HandAdapter { card ->
            selectCardFromHand(card)
        }

        binding.playerHandRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@GameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                stackFromEnd = false
            }
            adapter = playerHandAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        setupRowClickListeners()
        setupBoardAdapters()
        setupPassButton()

        playerMeleeAdapter = HandAdapter.BoardRowAdapter()
        playerRangedAdapter = HandAdapter.BoardRowAdapter()
        playerSiegeAdapter = HandAdapter.BoardRowAdapter()
        aiMeleeAdapter = HandAdapter.BoardRowAdapter()
        aiRangedAdapter = HandAdapter.BoardRowAdapter()
        aiSiegeAdapter = HandAdapter.BoardRowAdapter()

        binding.playerMeleeRow.apply {
            layoutManager = LinearLayoutManager(
                this@GameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                stackFromEnd = true
            }
            adapter = playerMeleeAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.playerRangedRow.apply {
            layoutManager = LinearLayoutManager(
                this@GameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                stackFromEnd = true
            }
            adapter = playerRangedAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.playerSiegeRow.apply {
            layoutManager = LinearLayoutManager(
                this@GameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                stackFromEnd = true
            }
            adapter = playerSiegeAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.aiMeleeRow.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiMeleeAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.aiRangedRow.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiRangedAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.aiSiegeRow.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aiSiegeAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

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
    }



    private fun handlePassTurn() {
        showRoundInfoBanner("Ronda cedida", R.drawable.roundpassedasset) {
            showRoundInfoBanner("Turno del oponente", R.drawable.aiturnasset) {
                gameEngine.pass(isPlayer = true)
                updateUI()
                updateGemViews()
                if (gameEngine.gameState.isGameOver()) {
                    showGameOver()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        playAITurn()
                        // Asegurar visibilidad después del turno de IA
                        ensureButtonVisibility()
                    }, 1000)
                }
            }
        }
    }

    private fun updatePassButton() {
        runOnUiThread {
            try {
                if (isCardSelected && selectedCard != null) {
                    binding.btnPass.text = "Jugar Carta"
                    binding.btnPass.setBackgroundColor(ContextCompat.getColor(this, R.color.gold_highlight))
                } else {
                    binding.btnPass.text = "Pasar"
                    binding.btnPass.setBackgroundColor(ContextCompat.getColor(this, R.color.Primary))
                }

                // Asegurar siempre que esté visible y habilitado
                ensureButtonVisibility()

                Log.d("GameActivity", "Botón actualizado - Texto: ${binding.btnPass.text}, Visible: ${binding.btnPass.visibility}")
            } catch (e: Exception) {
                Log.e("GameActivity", "Error actualizando botón: ${e.message}")
            }
        }
    }

    private fun selectCardFromHand(card: Card) {
        // Si ya hay una carta seleccionada, deseleccionarla primero
        if (isCardSelected && selectedCard != null) {
            if (selectedCard == card) {
                // Click en la misma carta - deseleccionar
                clearCardSelection()
                return
            } else {
                // Click en otra carta - deseleccionar la anterior
                clearCardSelection()
            }

            // Habilitar el botón de jugar carta
            binding.btnPlayCard.isEnabled = true
        }

        // Seleccionar nueva carta
        isCardSelected = true
        selectedCard = card

        // Determinar filas válidas basado en el alcance de la carta
        selectedValidRows = when {
            card.isUnitCard() -> {
                when (card.attributes.reach ?: -1) {
                    0 -> listOf("melee")
                    1 -> listOf("ranged")
                    2 -> listOf("siege")
                    else -> listOf("melee", "ranged", "siege")
                }
            }
            card.isSpecialCard() || card.isWeatherCard() -> {
                // Cartas especiales y de clima no requieren fila específica
                emptyList()
            }
            else -> emptyList()
        }

        // Mostrar carta en tamaño completo
        showFullSizeCard(card)

        // Resaltar filas válidas
        highlightRows(selectedValidRows)

        // Actualizar botón
        updatePassButton()

        Log.d("GameActivity", "Selected card: ${card.name}, valid rows: $selectedValidRows")
    }

    private fun clearCardSelection() {
        isCardSelected = false
        selectedCard = null
        selectedValidRows = emptyList()
        playerHandAdapter.clearSelection()
        resetRowHighlights()
        hideFullSizeCard()
        updatePassButton()

        // Deshabilitar el botón de jugar carta
        binding.btnPlayCard.isEnabled = false
    }

    private fun playSelectedCardOnFirstValidRow() {
        selectedCard?.let { card ->
            // Para cartas especiales o de clima que no requieren fila
            if (card.isSpecialCard() || card.isWeatherCard()) {
                if (gameEngine.playCard(card, isPlayer = true, null)) {
                    Log.d("GameActivity", "Special card played successfully")
                    updateGameUIAfterCardPlay()
                    clearCardSelection()

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!gameEngine.gameState.isGameOver()) {
                            playAITurn()
                        }
                        ensureButtonVisibility()
                    }, 1000)
                } else {
                    Toast.makeText(this, "No se pudo jugar la carta especial", Toast.LENGTH_SHORT).show()
                }
                return
            }

            // Para cartas de unidad, encontrar la primera fila válida
            val validRow = selectedValidRows.firstOrNull()

            if (validRow != null) {
                playSelectedCard(validRow)
            } else {
                Toast.makeText(this, "No hay filas válidas para esta carta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playSelectedCard(row: String) {
        Log.d("GameActivity", "Attempting to play card on $row")

        // Asegurar visibilidad antes de la acción
        ensureButtonVisibility()

        selectedCard?.let { card ->
            if (gameEngine.playCard(card, isPlayer = true, row)) {
                Log.d("GameActivity", "Card played successfully on $row")

                // Actualizar UI
                updateGameUIAfterCardPlay()

                // Limpiar selección
                clearCardSelection()

                // Turno de la IA después de un delay
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!gameEngine.gameState.isGameOver()) {
                        playAITurn()
                    }
                    ensureButtonVisibility()
                }, 1000)
            } else {
                Toast.makeText(this, "No se pudo jugar la carta", Toast.LENGTH_SHORT).show()
                Log.e("GameActivity", "Failed to play card: ${card.name}")
            }
        }

        // Asegurar visibilidad después de la acción
        ensureButtonVisibility()
    }

    // Método para asegurar que el botón siempre esté visible y habilitado
    private fun ensureButtonVisibility() {
        runOnUiThread {
            binding.btnPass.visibility = View.VISIBLE
            binding.btnPass.isEnabled = true
            binding.btnPass.alpha = 1.0f

            // Forzar un redibujado si es necesario
            binding.btnPass.invalidate()
            binding.btnPass.requestLayout()
        }
    }

    // Método para debug del botón
    private fun setupButtonDebugListener() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                ensureButtonVisibility()
                handler.postDelayed(this, 2000) // Verificar cada 2 segundos
            }
        }
        handler.postDelayed(runnable, 2000)
    }

    override fun onResume() {
        super.onResume()
        ensureButtonVisibility()
        updatePassButton()
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

        // Filtrar cartas válidas (excluir cartas con power == 0)
        val validCards = allCards.filter { card ->
            card != null && (card.power == null || card.power!! > 0)
        }

        if (validCards.isEmpty()) {
            Toast.makeText(this, "No valid cards available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val factions = listOf("monsters", "nilfgaard", "northernrealms", "scoiatael", "skellige")
        var playerFaction: String
        var aiFaction: String
        var playerDeck: List<Card>
        var aiDeck: List<Card>

        var attempts = 0
        do {
            playerFaction = factions.random()
            aiFaction = factions.random()

            val playerCards = validCards.filter { it.faction.equals(playerFaction, ignoreCase = true) }
            val aiCards = validCards.filter { it.faction.equals(aiFaction, ignoreCase = true) }

            // Crear mazos asegurando que no estén vacíos y no tengan cartas con power = 0
            playerDeck = if (playerCards.isNotEmpty()) {
                if (playerCards.size >= 47) {
                    playerCards.shuffled().take(47)
                } else {
                    // Si no hay suficientes cartas, duplicar las existentes de forma segura
                    val needed = 47 - playerCards.size
                    val repeatedCards = mutableListOf<Card>()
                    var safeAttempts = 0
                    while (repeatedCards.size < needed && safeAttempts < 100) {
                        val randomCard = playerCards.randomOrNull()
                        if (randomCard != null) {
                            // Verificar que la carta no sea nula y sea válida antes de duplicar
                            repeatedCards.add(randomCard.copy())
                        }
                        safeAttempts++
                        // Si después de muchos intentos no podemos completar, usar cualquier carta válida
                        if (safeAttempts >= 50 && repeatedCards.size < needed) {
                            validCards.randomOrNull()?.let { repeatedCards.add(it.copy()) }
                        }
                    }
                    playerCards + repeatedCards
                }
            } else {
                // Si no hay cartas de esta facción, usar cualquier carta válida
                validCards.shuffled().take(47)
            }

            aiDeck = if (aiCards.isNotEmpty()) {
                if (aiCards.size >= 47) {
                    aiCards.shuffled().take(47)
                } else {
                    val needed = 47 - aiCards.size
                    val repeatedCards = mutableListOf<Card>()
                    var safeAttempts = 0
                    while (repeatedCards.size < needed && safeAttempts < 100) {
                        val randomCard = aiCards.randomOrNull()
                        if (randomCard != null) {
                            repeatedCards.add(randomCard.copy())
                        }
                        safeAttempts++
                        if (safeAttempts >= 50 && repeatedCards.size < needed) {
                            validCards.randomOrNull()?.let { repeatedCards.add(it.copy()) }
                        }
                    }
                    aiCards + repeatedCards
                }
            } else {
                validCards.shuffled().take(47)
            }

            attempts++
            if (attempts > 10) {
                // Si después de 10 intentos no se pueden crear mazos válidos, usar cualquier carta válida
                playerDeck = validCards.shuffled().take(47)
                aiDeck = validCards.shuffled().take(47)
                break
            }
        } while (playerDeck.size < 47 || aiDeck.size < 47)

        // Verificación final para asegurar que los mazos tengan el tamaño correcto
        val finalPlayerDeck = if (playerDeck.size < 47) {
            val needed = 47 - playerDeck.size
            val additionalCards = validCards.shuffled().take(needed)
            playerDeck + additionalCards
        } else {
            playerDeck
        }

        val finalAiDeck = if (aiDeck.size < 47) {
            val needed = 47 - aiDeck.size
            val additionalCards = validCards.shuffled().take(needed)
            aiDeck + additionalCards
        } else {
            aiDeck
        }

        // Verificar que no haya cartas con power 0 en los mazos finales
        val playerZeroPowerCards = finalPlayerDeck.count { it.hasZeroPower() }
        val aiZeroPowerCards = finalAiDeck.count { it.hasZeroPower() }

        if (playerZeroPowerCards > 0 || aiZeroPowerCards > 0) {
            Log.w("GameActivity", "ADVERTENCIA: Mazos contienen cartas con power 0 - Jugador: $playerZeroPowerCards, IA: $aiZeroPowerCards")
        }

        setDeckImages(playerFaction, aiFaction)
        gameEngine.startGame(finalPlayerDeck, finalAiDeck)
        updateUI()
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

        val aiHand = gameEngine.gameState.ai.hand
        val randomCard = aiHand.random()
        val row = when (randomCard.attributes.reach ?: -1) {
            0 -> "melee"
            1 -> "ranged"
            2 -> "siege"
            else -> listOf("melee", "ranged", "siege").random()
        }

        if (gameEngine.playCard(randomCard, isPlayer = false, row)) {
            updateUI()
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

    private fun updateGemViews() {
        binding.playerGem1Image.setImageResource(
            if (gameEngine.gameState.playerGems >= 1) R.drawable.icon_gem_on else R.drawable.icon_gem_off
        )
        binding.playerGem2Image.setImageResource(
            if (gameEngine.gameState.playerGems >= 2) R.drawable.icon_gem_on else R.drawable.icon_gem_off
        )
        binding.aiGem1Image.setImageResource(
            if (gameEngine.gameState.aiGems >= 1) R.drawable.icon_gem_on else R.drawable.icon_gem_off
        )
        binding.aiGem2Image.setImageResource(
            if (gameEngine.gameState.aiGems >= 2) R.drawable.icon_gem_on else R.drawable.icon_gem_off
        )
    }


    private fun showFullSizeCard(card: Card) {
        binding.fullSizeCardImage.visibility = View.VISIBLE
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_card, binding.fullsizeCard, false)
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.3f).toInt()
        val height = (width * 1.4f).toInt()
        cardView.layoutParams = ViewGroup.LayoutParams(width, height)
        cardView.findViewById<TextView>(R.id.cardName).text = card.name
        cardView.findViewById<TextView>(R.id.cardFaction).text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        cardView.findViewById<TextView>(R.id.cardStrength).text = card.power?.toString() ?: ""
        cardView.findViewById<TextView>(R.id.cardStrength).visibility = if (card.power != null) View.VISIBLE else View.GONE
        cardView.findViewById<ImageView>(R.id.cardBorder).visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE
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
        Glide.with(this).load(card.art).into(cardView.findViewById(R.id.cardImage))
        binding.fullsizeCard.removeAllViews()
        binding.fullsizeCard.addView(cardView)
    }



    private fun debugButtonState() {
        Log.d("GameActivity", "=== DEBUG BOTÓN ===")
        Log.d("GameActivity", "Visible: ${binding.btnPass.visibility == View.VISIBLE}")
        Log.d("GameActivity", "Habilitado: ${binding.btnPass.isEnabled}")
        Log.d("GameActivity", "Texto: ${binding.btnPass.text}")
        Log.d("GameActivity", "Alpha: ${binding.btnPass.alpha}")
        Log.d("GameActivity", "Ancho: ${binding.btnPass.width}, Alto: ${binding.btnPass.height}")

        // Verificar si el padre está visible
        val parent = binding.btnPass.parent as? ViewGroup
        parent?.let {
            Log.d("GameActivity", "Padre visible: ${it.visibility == View.VISIBLE}")
            Log.d("GameActivity", "Padre alpha: ${it.alpha}")
        }
    }


    private fun updateGameUI(playedRow: String) {
        // Actualizar mano del jugador
        playerHandAdapter.submitList(gameEngine.gameState.player.getHandSnapshot())

        // Actualizar solo la fila donde se jugó la carta
        when (playedRow) {
            "melee" -> {
                playerMeleeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("melee"))
                Log.d("GameActivity", "Updated melee row with ${gameEngine.gameState.player.getBoardSnapshot("melee").size} cards")
            }
            "ranged" -> {
                playerRangedAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("ranged"))
                Log.d("GameActivity", "Updated ranged row with ${gameEngine.gameState.player.getBoardSnapshot("ranged").size} cards")
            }
            "siege" -> {
                playerSiegeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("siege"))
                Log.d("GameActivity", "Updated siege row with ${gameEngine.gameState.player.getBoardSnapshot("siege").size} cards")
            }
        }

        // Actualizar puntuaciones
        binding.playerScore.text = "Jugador: ${gameEngine.calculatePlayerScore()}"
        binding.aiScore.text = "IA: ${gameEngine.calculateAIScore()}"

        // Actualizar contadores de mazo
        binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
        binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()
    }



    // Asegurar que los adapters de filas se actualicen correctamente
    private fun setupBoardAdapters() {
        playerMeleeAdapter = HandAdapter.BoardRowAdapter()
        playerRangedAdapter = HandAdapter.BoardRowAdapter()
        playerSiegeAdapter = HandAdapter.BoardRowAdapter()

        // Configurar los RecyclerView de las filas
        binding.playerMeleeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerMeleeAdapter
            setHasFixedSize(true)
        }

        binding.playerRangedRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerRangedAdapter
            setHasFixedSize(true)
        }

        binding.playerSiegeRow.apply {
            layoutManager = LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerSiegeAdapter
            setHasFixedSize(true)
        }

        // Actualizar con el estado inicial
        playerMeleeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("melee"))
        playerRangedAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("ranged"))
        playerSiegeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("siege"))
    }




    private fun updateGameUIAfterCardPlay() {
        // Actualizar mano del jugador
        playerHandAdapter.submitList(gameEngine.gameState.player.getHandSnapshot())

        // Actualizar todas las filas del tablero
        playerMeleeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("melee"))
        playerRangedAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("ranged"))
        playerSiegeAdapter.submitList(gameEngine.gameState.player.getBoardSnapshot("siege"))

        // Actualizar puntuaciones
        binding.playerScore.text = "Jugador: ${gameEngine.calculatePlayerScore()}"
        binding.aiScore.text = "IA: ${gameEngine.calculateAIScore()}"

        // Actualizar contadores de mazo
        binding.playerDeckCount.text = gameEngine.gameState.player.deck.size.toString()
        binding.aiDeckCount.text = gameEngine.gameState.ai.deck.size.toString()
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

    // Asegúrate de que el método hideFullSizeCard limpie la selección
    private fun hideFullSizeCard() {
        binding.fullSizeCardImage.visibility = View.GONE
        binding.fullsizeCard.removeAllViews()
        // No limpiar la selección completamente aquí, solo la vista de carta completa
    }

    // Modificar los listeners de las filas para que también limpien la selección al jugar
    private fun setupRowClickListeners() {
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
    }


    // Modificación del método existente para pasar turno (sin lógica condicional)
    private fun setupPassButton() {
        binding.btnPass.setOnClickListener {
            handlePassTurn()
        }
        // Siempre visible y habilitado
        ensureButtonVisibility()
    }


}