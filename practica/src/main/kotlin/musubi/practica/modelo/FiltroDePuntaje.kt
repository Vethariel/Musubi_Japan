package musubi.practica.modelo

interface FiltroDePuntaje {
    val siguiente: FiltroDePuntaje?

    fun decidir(pieza: Pieza, correccion: Correccion): Decision?

    fun filtrar(pieza: Pieza, correccion: Correccion): Decision {
        decidir(pieza, correccion)?.let { return it }
        return siguiente?.filtrar(pieza, correccion) ?: Decision(pieza, "ninguno")
    }
}

class FiltroDeKanaDominado(
    override val siguiente: FiltroDePuntaje? = null,
) : FiltroDePuntaje {
    override fun decidir(pieza: Pieza, correccion: Correccion): Decision? {
        val kana = pieza.tipo == "hiragana" || pieza.tipo == "katakana"
        if (kana && pieza.uso >= 1) return Decision(pieza, "ninguno")
        return null
    }
}

class FiltroDeForma(
    override val siguiente: FiltroDePuntaje? = null,
) : FiltroDePuntaje {
    override fun decidir(pieza: Pieza, correccion: Correccion): Decision? {
        val enContexto = correccion.enContexto.contains(pieza)
        val forma = correccion.formaCorrecta.contains(pieza)
        if (forma && !enContexto) return Decision(pieza, "identificacion")
        return null
    }
}

class FiltroDeContexto(
    override val siguiente: FiltroDePuntaje? = null,
) : FiltroDePuntaje {
    override fun decidir(pieza: Pieza, correccion: Correccion): Decision? {
        if (correccion.enContexto.contains(pieza)) return Decision(pieza, "uso")
        return null
    }
}
