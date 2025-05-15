package com.example.mygwent.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.mygwent.R
import com.example.mygwent.data.Card
import com.example.mygwent.databinding.ItemCardBinding



// Versión corregida de HandAdapter
  class HandAdapter(private val onClick: (Card) -> Unit) :

    ListAdapter<Card, HandAdapter.CardViewHolder>(BoardRowAdapter.CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
        holder.itemView.setOnClickListener { onClick(card) }
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

            // Highlight if card is playable (necesitarás implementar isPlayable() en la clase Card)
            binding.root.alpha = if (card.type?.let { it == "Unit" || it == "Special" || it == "Weather" } == true) 1f else 0.5f
        }
    }
}







class BoardRowAdapter(private val onClick: (Card) -> Unit = {}) :
    ListAdapter<Card, BoardRowAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
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

    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }
}
























