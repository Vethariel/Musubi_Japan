package musubi.practica.modelo

interface PoliticaDePuntaje {
    fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?)
}

fun puntajeDe(sesion: Sesion): String =
    if (sesion.ejercicio == "identificar") "identificacion" else "uso"

class EfectoDeKanji : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?) {
        val cual = puntajeDe(sesion)
        sesion.seleccion.piezas().filter { it.tipo == "kanji" }.forEach { it.subirPuntaje(cual) }
    }
}

class EfectoDeVocabulario : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?) {
        val cual = puntajeDe(sesion)
        sesion.seleccion.piezas().filter { it.tipo == "vocabulario" }.forEach { palabra ->
            palabra.subirPuntaje(cual)
            palabra.kanji?.subirPuntaje(cual)
        }
    }
}

class EfectoDeFrase : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?) {
        val cual = puntajeDe(sesion)
        frase?.piezas?.forEach { it.subirPuntaje(cual) }
    }
}

class EfectoDeContexto : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?) {
        decisiones.orEmpty().forEach { decision ->
            if (decision.puntaje == "identificacion" || decision.puntaje == "uso") {
                decision.pieza.subirPuntaje(decision.puntaje)
            }
        }
    }
}
