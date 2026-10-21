package musubi.practica.viewmodel

import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Gramatica
import musubi.practica.vista.EscrituraAMano
import musubi.practica.vista.EscrituraEnCelular
import musubi.practica.vista.IIdentificacion
import musubi.practica.vista.IProduccion
import musubi.practica.vista.IRecuperacion
import musubi.practica.vista.ProduccionDePatron
import musubi.practica.vista.ProduccionLibre
import musubi.practica.vista.VistaDeDistractores
import musubi.practica.vista.VistaDeTraducciones

abstract class FabricaDeEjercicio<T> {
    abstract fun crear(): T
}

class FabricaDeDistractores(
    private val opciones: List<String>,
    private val correcta: String,
    private val eleccion: String,
) : FabricaDeEjercicio<IIdentificacion>() {
    override fun crear(): IIdentificacion = VistaDeDistractores(opciones, correcta, eleccion)
}

class FabricaDeTraducciones(
    private val traducciones: List<String>,
    private val correcta: String,
    private val eleccion: String,
) : FabricaDeEjercicio<IIdentificacion>() {
    override fun crear(): IIdentificacion = VistaDeTraducciones(traducciones, correcta, eleccion)
}

class FabricaDeEscrituraAMano(
    private val trazo: String,
    private val esperado: String,
) : FabricaDeEjercicio<IRecuperacion>() {
    override fun crear(): IRecuperacion = EscrituraAMano(trazo, esperado)
}

class FabricaDeEscrituraEnCelular(
    private val texto: String,
    private val esperado: String,
) : FabricaDeEjercicio<IRecuperacion>() {
    override fun crear(): IRecuperacion = EscrituraEnCelular(texto, esperado)
}

class FabricaDePatron(
    private val gramatica: Gramatica,
    private val producido: String,
) : FabricaDeEjercicio<IProduccion>() {
    override fun crear(): IProduccion = ProduccionDePatron(gramatica, producido)
}

class FabricaDeLibre(
    private val texto: String,
    private val correccion: Correccion,
) : FabricaDeEjercicio<ProduccionLibre>() {
    override fun crear(): ProduccionLibre = ProduccionLibre(texto, correccion)
}
