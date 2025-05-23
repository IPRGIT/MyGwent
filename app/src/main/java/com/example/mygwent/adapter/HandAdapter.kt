package com.example.mygwent.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mygwent.R
import com.example.mygwent.data.Card
import com.example.mygwent.databinding.ItemCardBinding

class HandAdapter(private val onClick: (Card) -> Unit) :
    ListAdapter<Card, HandAdapter.CardViewHolder>(CardDiffCallback()) {

    private var selectedCard: Card? = null

    fun getSelectedCard(): Card? = selectedCard

    fun clearSelection() {
        selectedCard = null
    }



    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
        holder.itemView.setOnClickListener {
            selectedCard = card
            onClick(card)
        }

        // Ajustar tamaño de las cartas en la mano para que se vean completas
        val displayMetrics = holder.itemView.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels * 0.67f // 67% del ancho de pantalla
        val cardWidth = (screenWidth * 0.67f * 0.5f / 5).toInt() // Mitad del ancho disponible para 5 cartas
        val cardHeight = (cardWidth * 1.4f).toInt()

        holder.itemView.layoutParams.width = cardWidth
        holder.itemView.layoutParams.height = cardHeight
        holder.itemView.requestLayout()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {
            root.setBackgroundResource(android.R.color.transparent)
        }
        return CardViewHolder(binding)
    }



    inner class CardViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card) {
            binding.cardName.text = card.name
            binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"

            // Make card background transparent when on board
            binding.root.background = null

            Glide.with(binding.root)
                .load(card.art)
                .placeholder(R.drawable.card_placeholder)
                .error(R.drawable.card_error)
                .into(binding.cardImage)

            card.power?.let {
                binding.cardStrength.text = it.toString()
                binding.cardStrength.visibility = View.VISIBLE
            } ?: run {
                binding.cardStrength.visibility = View.GONE
            }

            binding.root.alpha = if (card.type in listOf("Unit", "Special", "Weather")) 1f else 0.5f
        }
    }



    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }


    class BoardRowAdapter :
        ListAdapter<Card, BoardRowAdapter.CardViewHolder>(CardDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                root.setBackgroundResource(android.R.color.transparent)
                // Asegurar que la carta ocupe todo el espacio disponible en la fila
                root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            return CardViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val card = getItem(position)
            holder.bind(card)

            // Ajustar tamaño de las cartas en las filas de ataque
            val displayMetrics = holder.itemView.context.resources.displayMetrics
            val cardWidth = (displayMetrics.widthPixels * 0.67 * 0.94 / 5).toInt() // Espacio para 5 cartas
            val cardHeight = (cardWidth * 1.4).toInt()

            holder.itemView.layoutParams.width = cardWidth
            holder.itemView.layoutParams.height = cardHeight
        }

        inner class CardViewHolder(private val binding: ItemCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(card: Card) {
                binding.cardName.text = card.name
                binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"

                // Hacer fondo transparente y ajustar márgenes
                binding.root.background = null
                binding.root.setPadding(4, 4, 4, 4)

                Glide.with(binding.root)
                    .load(card.art)
                    .placeholder(R.drawable.card_placeholder)
                    .error(R.drawable.card_error)
                    .into(binding.cardImage)

                card.power?.let {
                    binding.cardStrength.text = it.toString()
                    binding.cardStrength.visibility = View.VISIBLE
                } ?: run {
                    binding.cardStrength.visibility = View.GONE
                }
            }
        }
    }










}