package musubi.practica.modelo

enum class Puntaje { IDENTIFICACION, USO }

data class Progreso(
    val identificacion: Int = 0,
    val uso: Int = 0,
) {
    fun subir(cual: Puntaje): Progreso = when (cual) {
        Puntaje.IDENTIFICACION -> copy(identificacion = identificacion + 1)
        Puntaje.USO -> copy(uso = uso + 1, identificacion = maxOf(identificacion, uso + 1))
    }
}
