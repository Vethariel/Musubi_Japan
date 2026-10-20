package musubi.practica.vista

interface IRecuperacion {
    fun mostrarEjercicio(): String
    fun escribir(): Boolean
}

class EscrituraAMano(
    val trazo: String,
    private val esperado: String,
) : IRecuperacion {
    override fun mostrarEjercicio(): String = esperado
    override fun escribir(): Boolean = trazo == esperado
}

class EscrituraEnCelular(
    val texto: String,
    private val esperado: String,
) : IRecuperacion {
    override fun mostrarEjercicio(): String = esperado
    override fun escribir(): Boolean = texto == esperado
}
