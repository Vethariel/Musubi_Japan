package musubi.practica.infraestructura

import musubi.practica.modelo.Frase
import musubi.practica.modelo.Grupo
import musubi.practica.modelo.IAlmacen

class AlmacenEnMemoria : IAlmacen {
    private val guardadas = mutableMapOf<Grupo, MutableList<Frase>>()

    override fun guardarFrase(grupo: Grupo, frase: Frase) {
        guardadas.getOrPut(grupo) { mutableListOf() }.add(frase)
    }

    fun frasesGuardadas(grupo: Grupo): List<Frase> = guardadas[grupo].orEmpty()
}
