package musubi.practica.modelo

class Frase(
    var texto: String,
    val piezas: MutableList<Pieza>,
    incorporada: Boolean = false,
) {
    var incorporada: Boolean = incorporada
        private set

    fun clonar(): Frase = Frase(texto, piezas.toMutableList())

    fun incorporarGrupo(grupo: Grupo) {
        grupo.agregarFrase(this)
        incorporada = true
    }
}
