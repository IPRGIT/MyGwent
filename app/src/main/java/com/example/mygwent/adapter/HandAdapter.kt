package com.example.mygwent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.mygwent.R
import com.example.mygwent.data.Card
import com.example.mygwent.databinding.ItemCardBinding

class HandAdapter(private val onClick: (Card) -> Unit) :
    ListAdapter<Card, HandAdapter.CardViewHolder>(CardDiffCallback()) {

    private var selectedCard: Card? = null
    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {
            root.setBackgroundResource(android.R.color.transparent)
            val (cardWidth, cardHeight) = CardSizeHelper.calculateCardSize(parent.context, isHand = true)
            root.layoutParams = ViewGroup.LayoutParams(cardWidth, cardHeight)
        }
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        val isSelected = selectedPosition == position
        holder.bind(card, isSelected)

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = if (selectedPosition == position) -1 else position
            selectedCard = if (selectedPosition == -1) null else card

            // Notificar cambios para actualizar UI
            if (previousSelected != -1) notifyItemChanged(previousSelected)
            if (selectedPosition != -1) notifyItemChanged(selectedPosition)

            onClick(card)
        }
    }

    fun getSelectedCard(): Card? = selectedCard

    fun clearSelection() {
        val previousSelected = selectedPosition
        selectedPosition = -1
        selectedCard = null
        if (previousSelected != -1) notifyItemChanged(previousSelected)
    }


    inner class CardViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card, isSelected: Boolean) {
            val (cardWidth, cardHeight) = CardSizeHelper.calculateCardSize(
                binding.root.context,
                isHand = !isSelected
            )
            binding.root.layoutParams = ViewGroup.LayoutParams(cardWidth, cardHeight)

            binding.cardName.text = card.name
            binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"
            binding.cardStrength.text = card.power?.toString() ?: ""
            binding.cardStrength.visibility = if (card.power != null) View.VISIBLE else View.GONE
            binding.cardBorder.visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE

            if (card.type == "Unit" && card.attributes.reach != null) {
                val reachIconRes = when (card.attributes.reach) {
                    0 -> R.drawable.card_reach0
                    1 -> R.drawable.card_reach1
                    2 -> R.drawable.card_reach2
                    else -> null
                }
                if (reachIconRes != null) {
                    binding.cardReachIcon.setImageResource(reachIconRes)
                    binding.cardReachIcon.visibility = View.VISIBLE
                } else {
                    binding.cardReachIcon.visibility = View.GONE
                }
            } else {
                binding.cardReachIcon.visibility = View.GONE
            }

            Glide.with(binding.root)
                .load(card.art)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(16)))
                .placeholder(R.drawable.card_placeholder)
                .error(R.drawable.card_error)
                .into(binding.cardImage)

            binding.root.alpha = if (isSelected) 1.0f else 0.7f
            binding.root.setBackgroundResource(if (isSelected) R.drawable.border_gold else android.R.color.transparent)
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

    class BoardRowAdapter : ListAdapter<Card, BoardRowAdapter.CardViewHolder>(CardDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                root.setBackgroundResource(android.R.color.transparent)
                val (cardWidth, cardHeight) = CardSizeHelper.calculateCardSize(parent.context, isHand = false)
                root.layoutParams = ViewGroup.LayoutParams(cardWidth, cardHeight)
            }
            return CardViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val card = getItem(position)
            holder.bind(card)
        }

        inner class CardViewHolder(private val binding: ItemCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(card: Card) {
                val (cardWidth, cardHeight) = CardSizeHelper.calculateCardSize(
                    binding.root.context,
                    isHand = false
                )
                binding.root.layoutParams = ViewGroup.LayoutParams(cardWidth, cardHeight)
                binding.root.requestLayout()

                binding.cardName.text = card.name
                binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                binding.cardStrength.text = card.power?.toString() ?: ""
                binding.cardStrength.visibility = if (card.power != null) View.VISIBLE else View.GONE
                binding.cardBorder.visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE

                if (card.type == "Unit" && card.attributes.reach != null) {
                    val reachIconRes = when (card.attributes.reach) {
                        0 -> R.drawable.card_reach0
                        1 -> R.drawable.card_reach1
                        2 -> R.drawable.card_reach2
                        else -> null
                    }
                    if (reachIconRes != null) {
                        binding.cardReachIcon.setImageResource(reachIconRes)
                        binding.cardReachIcon.visibility = View.VISIBLE
                    } else {
                        binding.cardReachIcon.visibility = View.GONE
                    }
                } else {
                    binding.cardReachIcon.visibility = View.GONE
                }

                Glide.with(binding.root)
                    .load(card.art)
                    .override(cardWidth, cardHeight)
                    .transform(MultiTransformation(CenterCrop(), RoundedCorners(16)))
                    .placeholder(R.drawable.card_placeholder)
                    .error(R.drawable.card_error)
                    .into(binding.cardImage)
            }
        }
    }

    object CardSizeHelper {
        fun calculateCardSize(context: Context, isHand: Boolean): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            return if (isHand) {
                val width = (displayMetrics.widthPixels * 0.12f).toInt()
                val height = (width * 1.4f).toInt()
                width to height
            } else {
                val width = (displayMetrics.widthPixels * 0.15f).toInt()
                val height = (width * 1.4f).toInt()
                width to height
            }
        }
    }







}