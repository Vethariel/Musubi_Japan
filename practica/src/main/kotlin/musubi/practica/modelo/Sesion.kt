package musubi.practica.modelo

data class Item(
    val pieza: Pieza,
    val grado: Puntaje,
)

enum class Lado { DEBAJO_DE, DESDE }

data class Umbral(
    val lado: Lado,
    val valor: Int,
) {
    fun admite(progreso: Int): Boolean = when (lado) {
        Lado.DEBAJO_DE -> progreso < valor
        Lado.DESDE -> progreso >= valor
    }
}

data class Configuracion(
    val tipos: Set<TipoDePieza> = TipoDePieza.entries.toSet(),
    val grados: Set<Puntaje> = Puntaje.entries.toSet(),
    val umbral: Umbral? = null,
    val orden: OrdenDeItems = ComoEnLaSeleccion,
    val limite: Int? = null,
)

class Sesion(
    val seleccion: Seleccion,
    val configuracion: Configuracion = Configuracion(),
) {
    fun plan(avance: IConsultaAvance): List<Item> {
        val grados = Puntaje.entries.filter { it in configuracion.grados }
        val items = seleccion.piezas()
            .filter { it.tipo in configuracion.tipos }
            .flatMap { pieza -> grados.map { Item(pieza, it) } }
            .filter { item ->
                configuracion.umbral?.admite(avance.progreso(item.pieza).en(item.grado)) ?: true
            }
        val ordenados = configuracion.orden.ordenar(items, avance)
        return configuracion.limite?.let { ordenados.take(it) } ?: ordenados
    }
}
