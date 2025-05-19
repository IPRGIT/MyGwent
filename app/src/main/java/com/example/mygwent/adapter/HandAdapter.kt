
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
        holder.itemView.setOnClickListener { onClick(card) }

        // Ajustar el ancho de la carta para que todas quepan en el contenedor
        val totalCards = currentList.size
        val displayMetrics = holder.itemView.context.resources.displayMetrics
        val containerWidth = (displayMetrics.widthPixels * 0.9).toInt() // 90% del ancho

        val cardWidth = if (totalCards > 0) containerWidth / totalCards else ViewGroup.LayoutParams.WRAP_CONTENT

        holder.itemView.layoutParams.width = cardWidth
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





    }

}








