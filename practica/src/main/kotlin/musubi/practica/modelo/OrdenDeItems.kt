package musubi.practica.modelo

import kotlin.random.Random

interface OrdenDeItems {
    fun ordenar(items: List<Item>, avance: IConsultaAvance): List<Item>
}

object ComoEnLaSeleccion : OrdenDeItems {
    override fun ordenar(items: List<Item>, avance: IConsultaAvance): List<Item> = items
}

object IdentificarAntesDeUsar : OrdenDeItems {
    override fun ordenar(items: List<Item>, avance: IConsultaAvance): List<Item> =
        items.sortedBy { it.grado.ordinal }
}

object MasBajosPrimero : OrdenDeItems {
    override fun ordenar(items: List<Item>, avance: IConsultaAvance): List<Item> =
        items.sortedBy { avance.progreso(it.pieza).en(it.grado) }
}

object MasAltosPrimero : OrdenDeItems {
    override fun ordenar(items: List<Item>, avance: IConsultaAvance): List<Item> =
        items.sortedByDescending { avance.progreso(it.pieza).en(it.grado) }
}

class AlAzar(
    private val azar: Random = Random.Default,
) : OrdenDeItems {
    override fun ordenar(items: List<Item>, avance: IConsultaAvance): List<Item> = items.shuffled(azar)
}
