package com.example.mygwent

import android.view.View
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mygwent.adapter.CardAdapter
import com.example.mygwent.databinding.ActivityCardTestBinding
import kotlinx.coroutines.launch

class CardTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardTestBinding
    private val viewModel: CardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        if (viewModel.allCards.value.isEmpty()) {
            viewModel.loadAllCards()
        }
    }

    private fun setupRecyclerView() {
        val adapter = CardAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPageCards.collect { cards ->
                    adapter.submitList(cards)
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collect { state ->
                    when (state) {
                        is CardViewModel.LoadingState.LoadingCards -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CardViewModel.LoadingState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                this@CardTestActivity,
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}