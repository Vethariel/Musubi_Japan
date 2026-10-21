package musubi.practica.viewmodel

import musubi.practica.modelo.Item
import musubi.practica.modelo.Puntaje
import musubi.practica.modelo.TipoDePieza
import musubi.practica.vista.PantallaDePractica

class CatalogoDeEjercicios(
    private val fabricas: Map<Pair<TipoDePieza, Puntaje>, FabricaDeEjercicio<out PantallaDePractica>>,
) {
    fun tiene(item: Item): Boolean = (item.pieza.tipo to item.grado) in fabricas

    fun crear(item: Item): PantallaDePractica = fabricas.getValue(item.pieza.tipo to item.grado).crear(item)
}
