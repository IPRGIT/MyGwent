package com.example.mygwent.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.mygwent.R
import com.example.mygwent.data.Card
import com.example.mygwent.databinding.ItemCardBinding

class CardAdapter : ListAdapter<Card, CardAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    inner class CardViewHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Card) {
            binding.cardName.text = card.name
            binding.cardFaction.text = card.faction.replaceFirstChar { it.uppercase() }
            binding.cardStrength.text = card.power?.toString() ?: ""
            binding.cardStrength.visibility = if (card.power != null) View.VISIBLE else View.GONE
            binding.cardBorder.visibility = if (card.isGoldCard()) View.VISIBLE else View.GONE

            // Configuración del ícono de alcance
            if (card.type == "Unit" && card.attributes.reach != null) {
                val reachIconRes = when (card.attributes.reach) {
                    0 -> R.drawable.card_reach0
                    1 -> R.drawable.card_reach1
                    2 -> R.drawable.card_reach2
                    else -> null
                }

                if (reachIconRes != null) {
                    binding.cardReachIcon.setImageDrawable(ContextCompat.getDrawable(binding.root.context, reachIconRes))
                    binding.cardReachIcon.visibility = View.VISIBLE
                } else {
                    binding.cardReachIcon.visibility = View.GONE
                }

            } else {
                binding.cardReachIcon.visibility = View.GONE
            }

            // Carga de imagen con Glide
            Glide.with(binding.root)
                .load(card.art)
                .apply(RequestOptions().transform(RoundedCorners(16)))
                .placeholder(R.drawable.card_placeholder)
                .error(R.drawable.card_error)
                .fallback(R.drawable.card_error)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: com.bumptech.glide.load.engine.GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("CardAdapter", "Image load failed for ${card.name}: ${card.art}, error: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("CardAdapter", "Image loaded for ${card.name}: ${card.art}")
                        return false
                    }
                })
                .into(binding.cardImage)

            binding.root.alpha = if (card.isPlayable()) 1f else 0.5f
        }
    }

    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id.card == newItem.id.card
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }
}