package musubi.practica.infraestructura

import musubi.practica.modelo.IRegistroAvance
import musubi.practica.modelo.Pieza

class RegistroEnMemoria : IRegistroAvance {
    private val guardados = mutableListOf<Pieza>()

    override fun registrarPuntaje(piezas: List<Pieza>) {
        guardados.clear()
        guardados.addAll(piezas)
    }

    fun puntajes(): List<Pieza> = guardados.toList()
}
