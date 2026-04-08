package com.andikas.pantaubumi.vo

enum class HazardType(val label: String) {
    ALL("Semua"),
    FLOOD("Banjir"),
    LANDSLIDE("Longsor"),
    EARTHQUAKE("Gempa");

    companion object {
        fun nonFilter(): List<HazardType> {
            return listOf(FLOOD, LANDSLIDE, EARTHQUAKE)
        }
    }
}