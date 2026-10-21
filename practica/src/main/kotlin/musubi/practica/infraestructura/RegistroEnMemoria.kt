package musubi.practica.infraestructura

import musubi.practica.modelo.Decision
import musubi.practica.modelo.IRegistroAvance
import musubi.practica.modelo.Pieza
import musubi.practica.modelo.Progreso

class RegistroEnMemoria(
    inicial: Map<Pieza, Progreso> = emptyMap(),
) : IRegistroAvance {
    private val progresos = inicial.mapKeys { it.key.id }.toMutableMap()

    override fun progreso(pieza: Pieza): Progreso = progresos[pieza.id] ?: Progreso()

    override fun registrarPuntaje(decisiones: List<Decision>) {
        decisiones.forEach { decision ->
            val cual = decision.puntaje ?: return@forEach
            progresos[decision.pieza.id] = progreso(decision.pieza).subir(cual)
        }
    }
}
