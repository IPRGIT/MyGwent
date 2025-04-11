package com.example.mygwent.adapter

/*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.Card
import androidx.recyclerview.widget.RecyclerView
import com.example.mygwent.R
import com.example.mygwent.data.Card


class CardAdapter(



    private val cards: List<Card.Card>,
    private val onCardClick: (Card) -> Unit

    ) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_card, parent, false)
            return CardViewHolder(view)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            holder.bind(cards[position])
        }

        override fun getItemCount() = cards.size

        inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(card: Card.Card) {
                itemView.apply {
                    findViewById<TextView>(R.id.tvCardName).text = card.name
                    // Configurar el resto de la UI según el tipo de carta
                    setOnClickListener { onCardClick(card) }
                }
            }
        }


    }

     */


