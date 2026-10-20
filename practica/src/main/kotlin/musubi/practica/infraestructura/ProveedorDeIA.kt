package musubi.practica.infraestructura

fun interface ProveedorDeIA {
    fun responder(solicitud: Map<String, String>): Map<String, List<String>>
}
