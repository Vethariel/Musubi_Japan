package musubi.practica.modelo

interface Seleccion {
    fun piezas(): List<Pieza>
    fun frases(): List<Frase>
}

class Grupo(
    private val hijos: List<Seleccion>,
    frases: List<Frase> = emptyList(),
) : Seleccion {
    private val propias = frases.toMutableList()

    override fun piezas(): List<Pieza> = hijos.flatMap { it.piezas() }.distinct()

    override fun frases(): List<Frase> = (propias + hijos.flatMap { it.frases() }).distinct()

    fun agregarFrase(frase: Frase) {
        propias.add(frase)
    }
}
