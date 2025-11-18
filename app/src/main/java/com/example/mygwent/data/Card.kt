package com.example.mygwent.data

import com.google.gson.annotations.SerializedName

data class CardResponse(
    @SerializedName("request") val request: RequestInfo,
    @SerializedName("response") val response: Map<String, CardData>
)

data class RequestInfo(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("REQUEST") val requestDetails: RequestDetails
)

data class RequestDetails(
    @SerializedName("language") val language: String,
    @SerializedName("response") val response: String,
    @SerializedName("version") val version: String
)

data class CardData(
    @SerializedName("id") val id: CardId,
    @SerializedName("attributes") val attributes: CardAttributes,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String?,
    @SerializedName("ability") val ability: String?,
    @SerializedName("ability_html") val abilityHtml: String?,
    @SerializedName("keyword_html") val keywordHtml: String?,
    @SerializedName("flavor") val flavor: String?
) {
    fun toCard(): Card {
        return Card(
            id = id,
            attributes = attributes,
            name = name,
            category = category,
            ability = ability,
            abilityHtml = abilityHtml,
            keywordHtml = keywordHtml,
            flavor = flavor
        )
    }
}

data class CardId(
    @SerializedName("art") val art: Int,
    @SerializedName("card") val card: Int,
    @SerializedName("audio") val audio: Int
)

data class CardAttributes(
    @SerializedName("set") val set: String,
    @SerializedName("type") val type: String,
    @SerializedName("armor") val armor: Int?,
    @SerializedName("color") val color: String,
    @SerializedName("power") val power: Int?,
    @SerializedName("reach") val reach: Int?,
    @SerializedName("artist") val artist: String?,
    @SerializedName("rarity") val rarity: String,
    @SerializedName("faction") val faction: String,
    @SerializedName("related") val related: String?,
    @SerializedName("provision") val provision: Int,
    @SerializedName("factionSecondary") val factionSecondary: String?
)

data class Card(
    val id: CardId,
    val attributes: CardAttributes,
    val name: String,
    val category: String?,
    val ability: String?,
    val abilityHtml: String?,
    val keywordHtml: String?,
    val flavor: String?
) {
    val type: String get() = attributes.type
    val power: Int? get() = attributes.power
    val faction: String get() = attributes.faction

    // URL correcta para el arte de la carta usando el ID de arte
    val art: String get() = "https://gwent.one/image/gwent/assets/card/art/medium/${id.art}.jpg"

    // URL alternativa por si falla la primera
    val artFallback: String get() = "https://api.gwent.one/image/art/${id.art}"

    // URL de la carta en el sitio web (para compartir o ver detalles)
    val cardUrl: String get() = "https://gwent.one/es/card/${id.card}"

    val combatRow: String? = when (attributes.type) {
        "Unit" -> when {
            attributes.reach == 1 -> "melee"
            attributes.reach == 2 -> "ranged"
            attributes.reach == 3 -> "siege"
            else -> null
        }
        else -> null
    }

    val effects: List<String> = parseEffects(ability)

    fun isSpecialCard(): Boolean = type == "Special"
    fun isWeatherCard(): Boolean = type == "Weather"
    fun isUnitCard(): Boolean = type == "Unit"
    fun isPlayable(): Boolean = type in listOf("Unit", "Special", "Weather")
    fun isGoldCard(): Boolean = attributes.color.equals("gold", ignoreCase = true)
    fun isLeader(): Boolean = category?.equals("Leader", ignoreCase = true) ?: false

    companion object {


        fun empty(): Card {
            return Card(
                id = CardId(art = -1, card = -1, audio = -1),
                attributes = CardAttributes(
                    set = "",
                    type = "",
                    armor = null,
                    color = "",
                    power = null,
                    reach = null,
                    artist = null,
                    rarity = "",
                    faction = "",
                    related = null,
                    provision = 0,
                    factionSecondary = null
                ),
                name = "",
                category = null,
                ability = null,
                abilityHtml = null,
                keywordHtml = null,
                flavor = null
            )
        }

        private fun parseEffects(ability: String?): List<String> {
            if (ability.isNullOrEmpty()) return emptyList()
            val effects = mutableListOf<String>()
            when {
                ability.contains("Médico", ignoreCase = true) -> effects.add("medic")
                ability.contains("Espía", ignoreCase = true) -> effects.add("spy")
                ability.contains("Asamblea", ignoreCase = true) -> effects.add("assembly")
                ability.contains("Vínculo Estrecho", ignoreCase = true) -> effects.add("tight_bond")
                ability.contains("Cuerno", ignoreCase = true) -> effects.add("horn")
                ability.contains("Abrasión", ignoreCase = true) -> effects.add("scorch")
            }
            return effects
        }
    }



    fun hasZeroPower(): Boolean {
        return this.isUnitCard() && this.power != null && this.power == 0
    }


}