package musubi.practica.modelo

interface PoliticaDePuntaje {
    fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?): List<Decision>
}

fun puntajeDe(sesion: Sesion): Puntaje =
    if (sesion.ejercicio == "identificar") Puntaje.IDENTIFICACION else Puntaje.USO

class EfectoDeKanji : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?): List<Decision> {
        val cual = puntajeDe(sesion)
        return sesion.seleccion.piezas().filterIsInstance<Kanji>().map { Decision(it, cual) }
    }
}

class EfectoDeVocabulario : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?): List<Decision> {
        val cual = puntajeDe(sesion)
        return sesion.seleccion.piezas().filterIsInstance<Vocabulario>().flatMap { palabra ->
            listOf(Decision(palabra, cual)) + palabra.kanji.map { Decision(it, cual) }
        }
    }
}

class EfectoDeFrase : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?): List<Decision> {
        val cual = puntajeDe(sesion)
        return frase?.piezas.orEmpty().map { Decision(it, cual) }
    }
}

class EfectoDeContexto : PoliticaDePuntaje {
    override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?): List<Decision> =
        decisiones.orEmpty().filter { it.puntaje != null }
}
