package musubi.practica.infraestructura

import musubi.practica.modelo.IAlmacen
import musubi.practica.modelo.Pieza
import musubi.practica.modelo.Seleccion

class AlmacenEnMemoria : IAlmacen {
    override fun leerSeleccion(seleccion: Seleccion): List<Pieza> = seleccion.piezas()
}
