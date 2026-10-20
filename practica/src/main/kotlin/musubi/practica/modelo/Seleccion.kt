package musubi.practica.modelo

interface Seleccion {
    fun piezas(): List<Pieza>
}

class Grupo(
    private val hijos: List<Seleccion>,
    val frases: MutableList<Frase> = mutableListOf(),
) : Seleccion {
    override fun piezas(): List<Pieza> = hijos.flatMap { it.piezas() }
}
