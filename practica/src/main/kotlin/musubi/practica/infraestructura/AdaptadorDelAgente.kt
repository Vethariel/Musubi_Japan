package musubi.practica.infraestructura

import musubi.practica.modelo.Correccion
import musubi.practica.modelo.IEvaluacion
import musubi.practica.modelo.Pieza

class AdaptadorDelAgente(
    private val proveedor: ProveedorDeIA,
) : IEvaluacion {
    var ultimaSolicitud: Map<String, String> = emptyMap()
        private set

    override fun corregirTexto(texto: String, piezas: List<Pieza>): Correccion {
        val solicitud = mapOf("texto" to texto)
        ultimaSolicitud = solicitud
        val crudo = proveedor.responder(solicitud)
        val porNombre = piezas.associateBy { it.nombre }
        return Correccion(
            enContexto = crudo["enContexto"].orEmpty().mapNotNull { porNombre[it] },
            formaCorrecta = crudo["forma"].orEmpty().mapNotNull { porNombre[it] },
        )
    }
}
