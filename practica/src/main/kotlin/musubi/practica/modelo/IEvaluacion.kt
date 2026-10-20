package musubi.practica.modelo

interface IEvaluacion {
    fun corregirTexto(texto: String, piezas: List<Pieza>): Correccion
}
