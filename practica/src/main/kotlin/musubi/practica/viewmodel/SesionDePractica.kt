package musubi.practica.viewmodel

import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Frase
import musubi.practica.modelo.IAlmacen
import musubi.practica.modelo.IEvaluacion
import musubi.practica.modelo.IRegistroAvance
import musubi.practica.modelo.ReglaDeEfecto
import musubi.practica.modelo.Seleccion
import musubi.practica.modelo.Sesion
import musubi.practica.vista.IIdentificacion
import musubi.practica.vista.IProduccion
import musubi.practica.vista.IRecuperacion
import musubi.practica.vista.ProduccionLibre

class SesionDePractica(
    private val almacen: IAlmacen,
    private val avance: IRegistroAvance,
    private val regla: ReglaDeEfecto,
    private val evaluacion: IEvaluacion? = null,
) {
    lateinit var modelo: Sesion
        private set
    var vista: Any? = null
        private set

    fun abrirSeleccion(
        seleccion: Seleccion,
        tipo: String,
        ejercicio: String,
        fabrica: FabricaDeEjercicio<*>,
    ) {
        modelo = Sesion(seleccion, tipo, ejercicio)
        modelo.guardarSeleccion(seleccion)
        almacen.leerSeleccion(seleccion)
        vista = fabrica.crear()
    }

    fun prepararFrase(frase: Frase): Frase = frase.clonar()

    fun recoger(): Boolean {
        val actual = vista
        return when (modelo.ejercicio) {
            "identificar" -> (actual as IIdentificacion).elegir()
            "interiorizar" ->
                if (modelo.tipo == "gramatica") (actual as IProduccion).producirPatron()
                else (actual as IRecuperacion).escribir()
            else -> true
        }
    }

    fun registrarPuntaje(frase: Frase? = null) {
        if (modelo.ejercicio == "libre") {
            val libre = vista as ProduccionLibre
            val texto = libre.producir()
            val correccion = correccionLibre(texto, frase, libre)
            regla.puntajeRespuesta(modelo, frase, correccion)
        } else if (recoger()) {
            regla.puntajeRespuesta(modelo, frase, null)
        }
        avance.registrarPuntaje(modelo.seleccion.piezas())
    }

    private fun correccionLibre(texto: String, frase: Frase?, libre: ProduccionLibre): Correccion {
        val agente = evaluacion
        if (agente == null) return libre.juicio()
        return agente.corregirTexto(texto, frase?.piezas.orEmpty())
    }
}
