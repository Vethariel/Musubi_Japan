package musubi.practica.modelo

class Pieza(
    val nombre: String,
    val tipo: String,
    var identificacion: Int = 0,
    var uso: Int = 0,
    val kanji: Pieza? = null,
) : Seleccion {
    override fun piezas(): List<Pieza> = listOf(this)

    fun subirPuntaje(cual: String) {
        when (cual) {
            "identificacion" -> identificacion += 1
            "uso" -> uso += 1
        }
    }
}
