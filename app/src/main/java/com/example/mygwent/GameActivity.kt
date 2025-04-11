

package com.example.mygwent

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mygwent.data.Deck
import com.example.mygwent.databinding.ActivityGameBinding



class GameActivity : AppCompatActivity()

/*
{


   private lateinit var binding: ActivityGameBinding
   private val viewModel: GameViewModel by viewModels()

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       binding = ActivityGameBinding.inflate(layoutInflater)
       setContentView(binding.root)

       // Configurar ViewModel con los mazos seleccionados
       val player1Deck = intent.getSerializableExtra("PLAYER1_DECK") as Deck
       val player2Deck = intent.getSerializableExtra("PLAYER2_DECK") as Deck
       val isVsAI = intent.getBooleanExtra("VS_AI", true)

       viewModel.initializeGame(player1Deck, player2Deck, isVsAI)

       setupObservers()
       setupUI()
   }

   private fun setupObservers() {
       viewModel.gameState.observe(this) { state ->
           updateUI(state)
       }
   }

   private fun setupUI() {
       binding.btnOptions.setOnClickListener {
           startActivity(Intent(this, InGameMenuActivity::class.java))
       }
   }
}


    */