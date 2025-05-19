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

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
        holder.itemView.setOnClickListener {
            selectedCard = card
            onClick(card)
        }

        // Calculate card width to fit all 10 cards
        val totalCards = 10 // Fixed number of cards
        val displayMetrics = holder.itemView.context.resources.displayMetrics
        val containerWidth = (displayMetrics.widthPixels * 0.67).toInt() // Match gameContainer width
        val cardWidth = containerWidth / totalCards

        holder.itemView.layoutParams.width = cardWidth
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    inner class CardViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card) {
            binding.cardName.text = card.name
            binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"

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
            }
            return CardViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val card = getItem(position)
            holder.bind(card)

            // Adjust card size to fit within row
            val displayMetrics = holder.itemView.context.resources.displayMetrics
            val containerWidth = (displayMetrics.widthPixels * 0.67 * 0.94).toInt() // Match attack container
            val cardWidth = containerWidth / 5 // Assume max 5 cards per row

            holder.itemView.layoutParams.width = cardWidth
            holder.itemView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        inner class CardViewHolder(private val binding: ItemCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(card: Card) {
                binding.cardName.text = card.name
                binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"

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