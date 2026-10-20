package musubi.practica.modelo

interface IAlmacen {
    fun leerSeleccion(seleccion: Seleccion): List<Pieza>
}
