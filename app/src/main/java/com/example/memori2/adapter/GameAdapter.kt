package com.example.memori2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memori2.model.CardItem
import com.example.memori2.model.CardType
import com.example.memori2.R

class GameAdapter(
    private val context: Context,
    private val cards: List<CardItem>,
    private val listener: (Int) -> Unit
) : RecyclerView.Adapter<GameAdapter.CardViewHolder>() {

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.cardImage)
        val txt: TextView = view.findViewById(R.id.cardText)

        init {
            itemView.setOnClickListener {
                listener(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]

        if (card.isFlipped || card.isMatched) {

            if (card.type == CardType.IMAGE) {
                val resId = context.resources.getIdentifier(card.content, "drawable", context.packageName)
                holder.img.setImageResource(resId)
                holder.img.visibility = View.VISIBLE
                holder.txt.visibility = View.GONE
            } else {
                holder.txt.text = card.content
                holder.txt.visibility = View.VISIBLE
                holder.img.visibility = View.GONE
            }

        } else {
            holder.img.setImageResource(R.drawable.card_background)
            holder.img.visibility = View.VISIBLE
            holder.txt.visibility = View.GONE
        }
    }

    override fun getItemCount() = cards.size
}