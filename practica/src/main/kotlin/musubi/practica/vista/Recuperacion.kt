package musubi.practica.vista

interface IRecuperacion : PantallaDePractica {
    fun escribir(): Boolean

    override fun recoger(): Boolean = escribir()
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
