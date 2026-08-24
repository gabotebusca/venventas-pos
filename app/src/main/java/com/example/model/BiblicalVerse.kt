package com.example.model

data class BiblicalVerse(
    val quote: String,
    val reference: String,
    val topic: String
) {
    companion object {
        val DAILY_VERSES = listOf(
            BiblicalVerse(
                quote = "Pon en manos del Señor todas tus obras, y tus proyectos se cumplirán.",
                reference = "Proverbios 16:3",
                topic = "Prosperidad y Éxito"
            ),
            BiblicalVerse(
                quote = "Todo lo puedo en Cristo que me fortalece.",
                reference = "Filipenses 4:13",
                topic = "Fortaleza Diaria"
            ),
            BiblicalVerse(
                quote = "El Señor es mi pastor; nada me faltará.",
                reference = "Salmos 23:1",
                topic = "Provisión Divina"
            ),
            BiblicalVerse(
                quote = "Mira que te mando que te esfuerces y seas valiente; no temas ni desmayes, porque el Señor tu Dios estará contigo en dondequiera que vayas.",
                reference = "Josué 1:9",
                topic = "Valentía y Esfuerzo"
            ),
            BiblicalVerse(
                quote = "El Señor enviará su bendición sobre tus graneros y sobre todo aquello en que pongas tu mano.",
                reference = "Deuteronomio 28:8",
                topic = "Bendición en tus Negocios"
            ),
            BiblicalVerse(
                quote = "Sea la luz del Señor nuestro Dios sobre nosotros, y confirma sobre nosotros la obra de nuestras manos; sí, la obra de nuestras manos confirma.",
                reference = "Salmos 90:17",
                topic = "Éxito en el Trabajo"
            ),
            BiblicalVerse(
                quote = "Porque yo sé los pensamientos que tengo acerca de vosotros, dice el Señor, pensamientos de paz, y no de mal, para daros el fin que esperáis.",
                reference = "Jeremías 29:11",
                topic = "Esperanza y Futuro"
            ),
            BiblicalVerse(
                quote = "Amado, deseo que seas prosperado en todas las cosas, y que tengas salud, así como prospera tu alma.",
                reference = "3 Juan 1:2",
                topic = "Prosperidad Integral"
            )
        )

        fun getRandomVerse(): BiblicalVerse {
            return DAILY_VERSES.random()
        }
    }
}
