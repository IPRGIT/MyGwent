package com.example.mygwent

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mygwent.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPlay.setOnClickListener {
            // Navegar a la pantalla de juego
        }

        binding.btnOptions.setOnClickListener {
            // Navegar a la pantalla de opciones
        }

        binding.btnExit.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Salir")
            .setMessage("¿Estás seguro de que quieres salir?")
            .setPositiveButton("Sí") { _, _ ->
                activity?.finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}