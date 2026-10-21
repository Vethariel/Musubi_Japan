package musubi.practica.vista

import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Gramatica

interface IProduccion {
    fun mostrarEjercicio(): String
    fun producirPatron(): Boolean
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
