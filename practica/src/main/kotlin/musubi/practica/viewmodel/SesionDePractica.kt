package musubi.practica.viewmodel

import musubi.practica.modelo.Frase
import musubi.practica.modelo.Gramatica
import musubi.practica.modelo.IAlmacen
import musubi.practica.modelo.IEvaluacion
import musubi.practica.modelo.IRegistroAvance
import musubi.practica.modelo.Item
import musubi.practica.modelo.ReglaDeEfecto
import musubi.practica.modelo.Sesion
import musubi.practica.vista.PantallaDePractica

class SesionDePractica(
    private val almacen: IAlmacen,
    private val avance: IRegistroAvance,
    private val regla: ReglaDeEfecto,
    private val ejercicios: CatalogoDeEjercicios,
    private val evaluacion: IEvaluacion? = null,
) {
    lateinit var modelo: Sesion
        private set
    var plan: List<Item> = emptyList()
        private set
    var vista: PantallaDePractica? = null
        private set
    var frase: Frase? = null
        private set
    private var actual = 0

    fun abrirSeleccion(sesion: Sesion) {
        modelo = sesion
        almacen.leerSeleccion(sesion.seleccion)
        plan = sesion.plan(avance).filter { ejercicios.tiene(it) }
        actual = 0
    }

    fun siguiente(): PantallaDePractica? {
        val item = plan.getOrNull(actual) ?: return null
        frase = prepararFrase(item)
        vista = ejercicios.crear(item)
        return vista
    }

    fun registrarPuntaje() {
        val abierta = vista ?: error("No hay un ejercicio abierto: llama a siguiente() antes de registrar.")
        val item = plan[actual]
        val decisiones = if (abierta.recoger()) regla.puntajeRespuesta(item, frase) else emptyList()
        avance.registrarPuntaje(decisiones)
        vista = null
        frase = null
        actual += 1
    }

    fun producirLibre(frase: Frase, fabrica: FabricaDeLibre) {
        val libre = fabrica.crear()
        val correccion = evaluacion?.corregirTexto(libre.producir(), frase.piezas) ?: libre.juicio()
        avance.registrarPuntaje(regla.puntajeLibre(frase, correccion, avance))
    }

    private fun prepararFrase(item: Item): Frase? {
        if (item.pieza !is Gramatica) return null
        return modelo.seleccion.frases()
            .filter { item.pieza in it.piezas }
            .minByOrNull { candidata -> candidata.piezas.sumOf { avance.progreso(it).en(item.grado) } }
            ?.clonar()
    }
}
