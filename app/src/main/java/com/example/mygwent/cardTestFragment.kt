package com.example.mygwent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mygwent.adapter.CardAdapter
import com.example.mygwent.databinding.FragmentCardTestBinding
import kotlinx.coroutines.launch

class CardTestFragment : Fragment() {
    private var _binding: FragmentCardTestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CardAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        setupButtonListeners()
        setupObservers()
    }

    private fun setupButtonListeners() {
        binding.btnPrevPage.setOnClickListener { viewModel.prevPage() }
        binding.btnNextPage.setOnClickListener { viewModel.nextPage() }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeCurrentPageCards() }
                launch { observeCurrentPage() }
                launch { observeLoadingState() }
            }
        }
    }

    private suspend fun observeCurrentPageCards() {
        viewModel.currentPageCards.collect { cards ->
            (binding.recyclerView.adapter as? CardAdapter)?.submitList(cards)
            updatePageInfo()
        }
    }

    private suspend fun observeCurrentPage() {
        viewModel.currentPage.collect {
            updatePageInfo()
        }
    }



    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.paginationControls.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showPageLoading(isLoading: Boolean, page: Int) {
        binding.pageProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvPageInfo.text = if (isLoading) "Cargando página ${page + 1}..." else ""
    }

    private fun updatePageInfo() {
        binding.tvPageInfo.text = "Página ${viewModel.currentPage.value + 1} de ${viewModel.totalPages.value}"
        binding.btnPrevPage.isEnabled = viewModel.currentPage.value > 0
        binding.btnNextPage.isEnabled = viewModel.currentPage.value < viewModel.totalPages.value - 1
        binding.emptyView.visibility = if (viewModel.currentPageCards.value.isEmpty()) View.VISIBLE else View.GONE
    }



    private suspend fun observeLoadingState() {
        viewModel.loadingState.collect { state ->
            when (state) {
                is CardViewModel.LoadingState.LoadingCards -> {
                    // Mostrar progressBar principal para carga inicial
                    binding.progressBar.visibility = View.VISIBLE
                    binding.pageProgressBar.visibility = View.GONE
                    binding.paginationControls.visibility = View.GONE
                }
                is CardViewModel.LoadingState.LoadingPage -> {
                    // Mostrar progressBar de página específica
                    binding.progressBar.visibility = View.GONE
                    binding.pageProgressBar.visibility = View.VISIBLE
                    binding.tvPageInfo.text = "Cargando página ${state.page + 1}..."
                }
                is CardViewModel.LoadingState.Error -> {
                    // Ocultar todos los indicadores de carga
                    binding.progressBar.visibility = View.GONE
                    binding.pageProgressBar.visibility = View.GONE
                    binding.paginationControls.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Estado normal - ocultar progress bars
                    binding.progressBar.visibility = View.GONE
                    binding.pageProgressBar.visibility = View.GONE
                    binding.paginationControls.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}