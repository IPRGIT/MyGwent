package com.example.mygwent

import android.content.Context
import android.util.Log
import com.example.mygwent.api.APIConfig
import com.example.mygwent.data.Card
import com.example.mygwent.data.CardAttributes
import com.example.mygwent.data.CardId
import com.example.mygwent.data.CardResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

class CardRepository(private val context: Context? = null) {

    private var allCardsCache: List<Card>? = null


        suspend fun getCards(): List<Card> {
            return try {
                // Primero intentar cargar desde caché
                context?.let {
                    val cachedCards = readCardsFromFile(it)
                    if (cachedCards.isNotEmpty()) {
                        Log.d("CardRepository", "Loaded ${cachedCards.size} cards from cache")
                        allCardsCache = cachedCards
                        return cachedCards
                    }
                }

                // Si no hay caché, cargar todas las páginas desde la API
                val allCards = mutableListOf<Card>()
                var currentPage = 0
                var hasMore = true

                while (hasMore) {
                    val pageCards = getCardsByPage(currentPage)
                    if (pageCards.isNotEmpty()) {
                        allCards.addAll(pageCards)
                        currentPage++
                        Log.d("CardRepository", "Loaded page $currentPage with ${pageCards.size} cards")
                    } else {
                        hasMore = false
                    }

                    // Limitar a 10 páginas como máximo para evitar bucles infinitos
                    if (currentPage >= 10) {
                        Log.w("CardRepository", "Reached maximum page limit (10)")
                        hasMore = false
                    }
                }

                Log.d("CardRepository", "Total cards loaded from API: ${allCards.size}")

                // Guardar en caché
                context?.let { saveCardsToFile(it, allCards) }
                allCardsCache = allCards

                allCards
            } catch (e: Exception) {
                Log.e("CardRepository", "Error fetching cards: ${e.message}", e)
                emptyList()
            }
        }

        suspend fun getCardsByPage(page: Int): List<Card> {
            return try {
                val response = APIConfig.GwentApi.retrofitService.getCardsByPage(
                    key = "data",
                    response = "json",
                    version = "latest",
                    page = page,
                    size = 50
                )

                if (response.isSuccessful) {
                    response.body()?.response?.values?.map { it.toCard() } ?: emptyList()
                } else {
                    Log.e("CardRepository", "Error fetching page $page: ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("CardRepository", "Error fetching page $page", e)
                emptyList()
            }
        }

        private fun saveCardsToFile(context: Context, cards: List<Card>) {
            try {
                val file = File(context.filesDir, "gwent_cards.json")
                file.writeText(Gson().toJson(cards))
                Log.d("CardRepository", "Saved ${cards.size} cards to cache")
            } catch (e: Exception) {
                Log.e("CardRepository", "Error saving cards to file: ${e.message}", e)
            }
        }

        private fun readCardsFromFile(context: Context): List<Card> {
            return try {
                val file = File(context.filesDir, "gwent_cards.json")
                if (file.exists()) {
                    val json = file.readText()
                    Gson().fromJson(json, Array<Card>::class.java).toList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("CardRepository", "Error reading cards from file: ${e.message}", e)
                emptyList()
            }
        }



        suspend fun getAllCards(): List<Card> {
            return if (allCardsCache != null) {
                allCardsCache!!
            } else {
                getCards() // Carga las cartas si no están en caché
            }
        }

        // Método sincrónico para usar cuando no se puede esperar
        fun getAllCardsSync(): List<Card> {
            return allCardsCache ?: run {
                // Intenta cargar desde caché local si está disponible
                context?.let { readCardsFromFile(it) } ?: emptyList()
            }
        }


}