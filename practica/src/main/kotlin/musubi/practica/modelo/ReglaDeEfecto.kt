package musubi.practica.modelo

class ReglaDeEfecto(
    private val politicas: Map<String, PoliticaDePuntaje>,
    private val cadena: FiltroDePuntaje,
) {
    fun puntajeRespuesta(sesion: Sesion, frase: Frase?, correccion: Correccion?) {
        if (sesion.ejercicio == "libre") {
            val decisiones = frase?.piezas.orEmpty().map { cadena.filtrar(it, correccion!!) }
            politicas.getValue("libre").aplicar(sesion, frase, decisiones)
            return
        }
        politicas.getValue(sesion.tipo).aplicar(sesion, frase, null)
    }

    companion object {
        fun porDefecto(extra: Map<String, PoliticaDePuntaje> = emptyMap()): ReglaDeEfecto {
            val politicas = mapOf(
                "kanji" to EfectoDeKanji(),
                "vocabulario" to EfectoDeVocabulario(),
                "gramatica" to EfectoDeFrase(),
                "libre" to EfectoDeContexto(),
            ) + extra
            val cadena = FiltroDeKanaDominado(FiltroDeForma(FiltroDeContexto()))
            return ReglaDeEfecto(politicas, cadena)
        }
    }
}
