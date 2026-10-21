package musubi.practica.modelo

enum class TipoDePieza { KANA, KANJI, VOCABULARIO, GRAMATICA }

sealed class Pieza(
    val id: String,
    val nombre: String,
    val tipo: TipoDePieza,
) : Seleccion {
    override fun piezas(): List<Pieza> = listOf(this)

    override fun frases(): List<Frase> = emptyList()

    override fun equals(other: Any?): Boolean = other is Pieza && other.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id
}

enum class Silabario { HIRAGANA, KATAKANA }

class Kana(
    nombre: String,
    val silabario: Silabario,
    id: String = "kana:$nombre",
) : Pieza(id, nombre, TipoDePieza.KANA)

class Kanji(
    nombre: String,
    id: String = "kanji:$nombre",
) : Pieza(id, nombre, TipoDePieza.KANJI)

class Vocabulario(
    nombre: String,
    val kanji: List<Kanji> = emptyList(),
    id: String = "vocabulario:$nombre",
) : Pieza(id, nombre, TipoDePieza.VOCABULARIO)

enum class Registro { CORDIAL, INFORMAL, INDIFERENTE }

class Gramatica(
    nombre: String,
    val patron: List<Segmento>,
    val registro: Registro,
    id: String = "gramatica:$nombre",
) : Pieza(id, nombre, TipoDePieza.GRAMATICA) {
    fun mostrar(): String = patron.joinToString("") { it.mostrar() }

    fun encaja(texto: String): Boolean =
        Regex(patron.joinToString("") { it.forma() }).matches(texto)
}

sealed interface Segmento {
    fun mostrar(): String
    fun forma(): String
}

data class Fijo(val texto: String) : Segmento {
    override fun mostrar(): String = texto
    override fun forma(): String = Regex.escape(texto)
}

data class Hueco(val descripcion: String) : Segmento {
    override fun mostrar(): String = "[$descripcion]"
    override fun forma(): String = ".+"
}

data class Opcional(val segmentos: List<Segmento>) : Segmento {
    override fun mostrar(): String = "(${segmentos.joinToString("") { it.mostrar() }})"
    override fun forma(): String = "(?:${segmentos.joinToString("") { it.forma() }})?"
}
