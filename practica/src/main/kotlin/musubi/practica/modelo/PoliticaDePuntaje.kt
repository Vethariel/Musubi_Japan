package musubi.practica.modelo

interface PoliticaDePuntaje {
    fun aplicar(item: Item, frase: Frase?): List<Decision>
}

class EfectoDeKana : PoliticaDePuntaje {
    override fun aplicar(item: Item, frase: Frase?): List<Decision> = listOf(Decision(item.pieza, item.grado))
}

class EfectoDeKanji : PoliticaDePuntaje {
    override fun aplicar(item: Item, frase: Frase?): List<Decision> = listOf(Decision(item.pieza, item.grado))
}

class EfectoDeVocabulario : PoliticaDePuntaje {
    override fun aplicar(item: Item, frase: Frase?): List<Decision> {
        val kanji = (item.pieza as? Vocabulario)?.kanji.orEmpty()
        return (listOf(item.pieza) + kanji).map { Decision(it, item.grado) }
    }
}

class EfectoDeFrase : PoliticaDePuntaje {
    override fun aplicar(item: Item, frase: Frase?): List<Decision> =
        (frase?.piezas ?: listOf(item.pieza)).map { Decision(it, item.grado) }
}
