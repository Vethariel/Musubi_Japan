package musubi.practica.modelo

class ReglaDeEfecto(
    private val politicas: Map<TipoDePieza, PoliticaDePuntaje>,
    private val cadena: FiltroDePuntaje,
) {
    fun puntajeRespuesta(item: Item, frase: Frase?): List<Decision> =
        politicas.getValue(item.pieza.tipo).aplicar(item, frase).distinct()

    fun puntajeLibre(frase: Frase, correccion: Correccion, avance: IConsultaAvance): List<Decision> =
        frase.piezas.map { cadena.filtrar(it, correccion, avance) }.filter { it.puntaje != null }.distinct()

    companion object {
        fun porDefecto(extra: Map<TipoDePieza, PoliticaDePuntaje> = emptyMap()): ReglaDeEfecto {
            val politicas = mapOf(
                TipoDePieza.KANA to EfectoDeKana(),
                TipoDePieza.KANJI to EfectoDeKanji(),
                TipoDePieza.VOCABULARIO to EfectoDeVocabulario(),
                TipoDePieza.GRAMATICA to EfectoDeFrase(),
            ) + extra
            val cadena = FiltroDeKanaDominado(FiltroDeForma(FiltroDeContexto()))
            return ReglaDeEfecto(politicas, cadena)
        }
    }
}
