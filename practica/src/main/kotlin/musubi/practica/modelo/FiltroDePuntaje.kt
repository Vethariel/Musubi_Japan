package musubi.practica.modelo

interface FiltroDePuntaje {
    val siguiente: FiltroDePuntaje?

    fun decidir(pieza: Pieza, correccion: Correccion, avance: IConsultaAvance): Decision?

    fun filtrar(pieza: Pieza, correccion: Correccion, avance: IConsultaAvance): Decision {
        decidir(pieza, correccion, avance)?.let { return it }
        return siguiente?.filtrar(pieza, correccion, avance) ?: Decision(pieza, null)
    }
}

class FiltroDeKanaDominado(
    override val siguiente: FiltroDePuntaje? = null,
) : FiltroDePuntaje {
    override fun decidir(pieza: Pieza, correccion: Correccion, avance: IConsultaAvance): Decision? {
        if (pieza is Kana && avance.progreso(pieza).uso >= 1) return Decision(pieza, null)
        return null
    }
}

class FiltroDeForma(
    override val siguiente: FiltroDePuntaje? = null,
) : FiltroDePuntaje {
    override fun decidir(pieza: Pieza, correccion: Correccion, avance: IConsultaAvance): Decision? {
        val enContexto = correccion.enContexto.contains(pieza)
        val forma = correccion.formaCorrecta.contains(pieza)
        if (forma && !enContexto) return Decision(pieza, Puntaje.IDENTIFICACION)
        return null
    }
}

class FiltroDeContexto(
    override val siguiente: FiltroDePuntaje? = null,
) : FiltroDePuntaje {
    override fun decidir(pieza: Pieza, correccion: Correccion, avance: IConsultaAvance): Decision? {
        if (correccion.enContexto.contains(pieza)) return Decision(pieza, Puntaje.USO)
        return null
    }
}
