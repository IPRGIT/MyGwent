package com.example.mygwent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
    private var highlightedRows: List<String> = emptyList()

    fun getSelectedCard(): Card? = selectedCard
    fun clearSelection() {
        selectedCard = null
        notifyDataSetChanged()
    }


    fun setHighlightedRows(rows: List<String>) {
        highlightedRows = rows
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {
            root.setBackgroundResource(android.R.color.transparent)

            // Reducir tamaño de las cartas a la mitad (9% del ancho de pantalla)
            val displayMetrics = parent.context.resources.displayMetrics
            val cardWidth = (displayMetrics.widthPixels * 0.15f).toInt()
            val cardHeight = (cardWidth * 1f).toInt()

            root.layoutParams = ViewGroup.LayoutParams(cardWidth, cardHeight)
        }
        return CardViewHolder(binding)
    }



    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }

    // Clase para mostrar cartas en el tablero (BoardRowAdapter)
    class BoardRowAdapter : ListAdapter<Card, BoardRowAdapter.CardViewHolder>(CardDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                root.setBackgroundResource(android.R.color.transparent)
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
        }

        inner class CardViewHolder(private val binding: ItemCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(card: Card) {
                // Calcular tamaño para el tablero
                val (cardWidth, cardHeight) = CardSizeHelper.calculateCardSize(
                    binding.root.context,
                    isHand = false
                )

                // Configurar layout
                binding.root.layoutParams = ViewGroup.LayoutParams(cardWidth, cardHeight)
                binding.root.requestLayout()

                // Configurar elementos de la carta
                binding.cardName.text = card.name
                binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"

                Glide.with(binding.root)
                    .load(card.art)
                    .override(cardWidth, cardHeight)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(16)
                        )
                    )
                    .placeholder(R.drawable.card_placeholder)
                    .error(R.drawable.card_error)
                    .into(binding.cardImage)

                binding.cardBorder.visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE
                binding.cardBorder.layoutParams = FrameLayout.LayoutParams(cardWidth, cardHeight)

                card.power?.let {
                    binding.cardStrength.text = it.toString()
                    binding.cardStrength.visibility = View.VISIBLE
                    binding.cardStrength.textSize = cardWidth * 0.1f
                } ?: run {
                    binding.cardStrength.visibility = View.GONE
                }
            }
        }
    }

    object CardSizeHelper {
        fun calculateCardSize(context: Context, isHand: Boolean): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            return if (isHand) {
                // Tamaño para la mano (18% del ancho de pantalla)
                val width = (displayMetrics.widthPixels * 0.18f).toInt()
                val height = (width * 1.4f).toInt()
                width to height
            } else {
                // Tamaño para el tablero (15% del ancho de pantalla)
                val width = (displayMetrics.widthPixels * 0.15f).toInt()
                val height = (width * 1.4f).toInt()
                width to height
            }
        }
    }



        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val card = getItem(position)
            holder.bind(card, card == selectedCard)

            holder.itemView.setOnClickListener {
                selectedCard = card
                notifyDataSetChanged()
                onClick(card)
            }
        }

        inner class CardViewHolder(private val binding: ItemCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(card: Card, isSelected: Boolean) {
                // Configurar elementos de la carta
                binding.cardName.text = card.name
                binding.cardFaction.text = card.faction?.replaceFirstChar { it.uppercase() } ?: "Unknown"

                // Configurar imagen con Glide
                Glide.with(binding.root)
                    .load(card.art)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(16)
                        )
                    )
                    .placeholder(R.drawable.card_placeholder)
                    .error(R.drawable.card_error)
                    .into(binding.cardImage)

                // Resaltar si está seleccionada
                if (isSelected) {
                    binding.root.alpha = 1.0f
                    binding.cardBorder.visibility = View.VISIBLE
                } else {
                    binding.root.alpha = 0.7f
                    binding.cardBorder.visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE
                }
            }
        }





















}
