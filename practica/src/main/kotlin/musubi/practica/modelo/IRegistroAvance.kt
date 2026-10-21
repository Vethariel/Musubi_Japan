package musubi.practica.modelo

interface IRegistroAvance : IConsultaAvance {
    fun registrarPuntaje(decisiones: List<Decision>)
}
