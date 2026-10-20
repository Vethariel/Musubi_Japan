package musubi.practica.vista

interface IIdentificacion {
    fun mostrarEjercicio(): String
    fun elegir(): Boolean
}

class VistaDeDistractores(
    val opciones: List<String>,
    private val correcta: String,
    private val eleccion: String,
) : IIdentificacion {
    override fun mostrarEjercicio(): String = opciones.joinToString()
    override fun elegir(): Boolean = eleccion == correcta
}

class VistaDeTraducciones(
    val traducciones: List<String>,
    private val correcta: String,
    private val eleccion: String,
) : IIdentificacion {
    override fun mostrarEjercicio(): String = traducciones.joinToString()
    override fun elegir(): Boolean = eleccion == correcta
}
