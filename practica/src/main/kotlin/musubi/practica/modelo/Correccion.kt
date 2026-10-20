package musubi.practica.modelo

data class Correccion(
    val enContexto: List<Pieza>,
    val formaCorrecta: List<Pieza>,
)

data class Decision(
    val pieza: Pieza,
    val puntaje: String,
)
