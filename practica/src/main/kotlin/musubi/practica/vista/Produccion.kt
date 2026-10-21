package musubi.practica.vista

import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Gramatica

interface IProduccion : PantallaDePractica {
    fun producirPatron(): Boolean

    override fun recoger(): Boolean = producirPatron()
}

class ProduccionDePatron(
    val gramatica: Gramatica,
    private val producido: String,
) : IProduccion {
    override fun mostrarEjercicio(): String = gramatica.mostrar()
    override fun producirPatron(): Boolean = gramatica.encaja(producido)
}

class ProduccionLibre(
    val texto: String,
    private val correccion: Correccion,
) {
    fun mostrarEjercicio(): String = texto
    fun producir(): String = texto
    fun juicio(): Correccion = correccion
}
