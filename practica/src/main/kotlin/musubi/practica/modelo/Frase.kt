package musubi.practica.modelo

class Frase(
    var texto: String,
    val piezas: MutableList<Pieza>,
    var incorporada: Boolean = false,
) {
    fun clonar(): Frase = Frase(texto, piezas.toMutableList())

    fun incorporarGrupo(grupo: Grupo) {
        grupo.frases.add(this)
        incorporada = true
    }
}
