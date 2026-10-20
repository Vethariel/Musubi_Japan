package musubi.practica.vista

import musubi.practica.modelo.Correccion

interface IProduccion {
    fun mostrarEjercicio(): String
    fun producirPatron(): Boolean
}

class ProduccionDePatron(
    val patron: String,
    private val producido: String,
) : IProduccion {
    override fun mostrarEjercicio(): String = patron
    override fun producirPatron(): Boolean = producido.contains(patron)
}

class ProduccionLibre(
    val texto: String,
    private val correccion: Correccion,
) {
    fun mostrarEjercicio(): String = texto
    fun producir(): String = texto
    fun juicio(): Correccion = correccion
}
