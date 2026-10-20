package musubi.practica.modelo

class Sesion(
    var seleccion: Seleccion,
    val tipo: String,
    val ejercicio: String,
) {
    fun guardarSeleccion(seleccion: Seleccion) {
        this.seleccion = seleccion
    }
}
