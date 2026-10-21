package musubi.practica

import musubi.practica.infraestructura.AdaptadorDelAgente
import musubi.practica.infraestructura.AlmacenEnMemoria
import musubi.practica.infraestructura.ProveedorDeIA
import musubi.practica.infraestructura.RegistroEnMemoria
import musubi.practica.modelo.AlAzar
import musubi.practica.modelo.ComoEnLaSeleccion
import musubi.practica.modelo.Configuracion
import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Decision
import musubi.practica.modelo.Fijo
import musubi.practica.modelo.Frase
import musubi.practica.modelo.Gramatica
import musubi.practica.modelo.Grupo
import musubi.practica.modelo.Hueco
import musubi.practica.modelo.IdentificarAntesDeUsar
import musubi.practica.modelo.Item
import musubi.practica.modelo.Kana
import musubi.practica.modelo.Kanji
import musubi.practica.modelo.Lado
import musubi.practica.modelo.MasAltosPrimero
import musubi.practica.modelo.MasBajosPrimero
import musubi.practica.modelo.Opcional
import musubi.practica.modelo.PoliticaDePuntaje
import musubi.practica.modelo.Progreso
import musubi.practica.modelo.Puntaje
import musubi.practica.modelo.Registro
import musubi.practica.modelo.ReglaDeEfecto
import musubi.practica.modelo.Sesion
import musubi.practica.modelo.Silabario
import musubi.practica.modelo.TipoDePieza
import musubi.practica.modelo.Umbral
import musubi.practica.modelo.Vocabulario
import musubi.practica.viewmodel.CatalogoDeEjercicios
import musubi.practica.viewmodel.FabricaDeDistractores
import musubi.practica.viewmodel.FabricaDeEjercicio
import musubi.practica.viewmodel.FabricaDeEscrituraAMano
import musubi.practica.viewmodel.FabricaDeLibre
import musubi.practica.viewmodel.FabricaDePatron
import musubi.practica.viewmodel.FabricaDeTraducciones
import musubi.practica.viewmodel.SesionDePractica
import musubi.practica.vista.PantallaDePractica
import musubi.practica.vista.VistaDeDistractores
import musubi.practica.vista.VistaDeTraducciones
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class PracticaTest {
    private var avance = RegistroEnMemoria()

    private val identificar = setOf(Puntaje.IDENTIFICACION)
    private val usar = setOf(Puntaje.USO)

    @Test
    fun laFabricaAbreLosDistractoresSinQueLaSesionConstruyaLaVista() {
        val kanji = Kanji("食")
        val sesion = sesion(
            TipoDePieza.KANJI to Puntaje.IDENTIFICACION to FabricaDeDistractores(listOf("comida", "agua"), "comida", "comida"),
        )
        sesion.abrirSeleccion(Sesion(Grupo(listOf(kanji)), Configuracion(grados = identificar)))
        val vista = sesion.siguiente()
        assertIs<VistaDeDistractores>(vista)
        assertEquals("comida, agua", vista.mostrarEjercicio())
        sesion.registrarPuntaje()
        assertEquals(Progreso(identificacion = 1, uso = 0), avance.progreso(kanji))
    }

    @Test
    fun lasTraduccionesSustituyenALosDistractores() {
        val kanji = Kanji("水")
        val sesion = sesion(
            TipoDePieza.KANJI to Puntaje.IDENTIFICACION to FabricaDeTraducciones(listOf("agua", "fuego"), "agua", "fuego"),
        )
        sesion.abrirSeleccion(Sesion(Grupo(listOf(kanji)), Configuracion(grados = identificar)))
        assertIs<VistaDeTraducciones>(sesion.siguiente())
        sesion.registrarPuntaje()
        assertEquals(Progreso(), avance.progreso(kanji))
    }

    @Test
    fun unaPoliticaNuevaSubeElPuntajeSinModificarLaRegla() {
        class EfectoDeLectura : PoliticaDePuntaje {
            override fun aplicar(item: Item, frase: Frase?): List<Decision> =
                listOf(Decision(item.pieza, Puntaje.IDENTIFICACION), Decision(item.pieza, Puntaje.USO))
        }

        val lectura = Kanji("話")
        val regla = ReglaDeEfecto.porDefecto(mapOf(TipoDePieza.KANJI to EfectoDeLectura()))
        assertEquals(
            listOf(Decision(lectura, Puntaje.IDENTIFICACION), Decision(lectura, Puntaje.USO)),
            regla.puntajeRespuesta(Item(lectura, Puntaje.IDENTIFICACION), null),
        )
    }

    @Test
    fun laCopiaDeLaFraseNoAlteraLaGuardada() {
        val particula = objetoDirecto()
        val original = Frase("りんごを", mutableListOf(Vocabulario("りんご"), particula))
        val grupo = Grupo(listOf(particula), listOf(original))
        val sesion = sesion(TipoDePieza.GRAMATICA to Puntaje.USO to FabricaDePatron("りんごを"))
        sesion.abrirSeleccion(Sesion(grupo, Configuracion(grados = usar)))
        sesion.siguiente()
        val copia = sesion.frase!!
        assertNotSame(original, copia)
        copia.texto = "otra"
        copia.piezas.reverse()
        copia.incorporarGrupo(grupo)
        assertEquals("りんごを", original.texto)
        assertEquals("りんご", original.piezas.first().nombre)
        assertTrue(copia.incorporada)
        assertEquals(listOf(original, copia), grupo.frases())
    }

    @Test
    fun unGrupoDeGruposSeRecorreIgualQueLasPiezasSueltas() {
        val dia = Kanji("日")
        val libro = Kanji("本")
        val juntados = Grupo(listOf(Grupo(listOf(dia)), Grupo(listOf(libro))))
        val sueltas = Grupo(listOf(dia, libro))
        assertEquals(listOf(dia, libro), juntados.piezas())
        assertEquals(sueltas.piezas(), juntados.piezas())
    }

    @Test
    fun juntarGruposTambienJuntaSusFrases() {
        val manzana = Vocabulario("りんご")
        val comer = Vocabulario("食べる")
        val comida = Grupo(listOf(manzana), listOf(Frase("りんごです", mutableListOf(manzana))))
        val verbos = Grupo(listOf(comer), listOf(Frase("食べます", mutableListOf(comer))))
        val juntados = Grupo(listOf(comida, verbos, manzana))
        assertEquals(listOf("りんごです", "食べます"), juntados.frases().map { it.texto })
        assertEquals(emptyList(), manzana.frases())
    }

    @Test
    fun unaPiezaRepetidaEntraUnaSolaVezAlPlan() {
        val kanji = Kanji("食")
        val palabra = Vocabulario("食べる", listOf(kanji))
        val juntados = Grupo(listOf(Grupo(listOf(kanji, palabra)), Grupo(listOf(palabra))))
        assertEquals(
            listOf(Item(kanji, Puntaje.USO), Item(palabra, Puntaje.USO)),
            Sesion(juntados, Configuracion(grados = usar)).plan(avance),
        )
    }

    @Test
    fun elVocabularioSubeLaPalabraYSuKanji() {
        val kanji = Kanji("食")
        val otro = Kanji("水")
        val palabra = Vocabulario("食べる", listOf(kanji))
        val sesion = sesion(TipoDePieza.VOCABULARIO to Puntaje.USO to FabricaDeEscrituraAMano("たべる", "たべる"))
        sesion.abrirSeleccion(Sesion(Grupo(listOf(palabra)), Configuracion(grados = usar)))
        practicarTodo(sesion)
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(palabra))
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(kanji))
        assertEquals(Progreso(), avance.progreso(otro))
    }

    @Test
    fun laGramaticaTomaSuFraseDeLaSeleccionYRepartePuntaje() {
        val manzana = Vocabulario("りんご")
        val particula = objetoDirecto()
        val frase = Frase("りんごを", mutableListOf(manzana, particula))
        val sesion = sesion(TipoDePieza.GRAMATICA to Puntaje.USO to FabricaDePatron("りんごを"))
        sesion.abrirSeleccion(Sesion(Grupo(listOf(particula), listOf(frase)), Configuracion(grados = usar)))
        practicarTodo(sesion)
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(manzana))
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(particula))
        assertFalse(frase.incorporada)
    }

    @Test
    fun losKanaTienenSuPropioReparto() {
        val kana = Kana("あ", Silabario.HIRAGANA)
        val sesion = sesion(TipoDePieza.KANA to Puntaje.IDENTIFICACION to FabricaDeDistractores(listOf("a", "i"), "a", "a"))
        sesion.abrirSeleccion(Sesion(kana, Configuracion(grados = identificar)))
        practicarTodo(sesion)
        assertEquals(Progreso(identificacion = 1), avance.progreso(kana))
    }

    @Test
    fun laConfiguracionFiltraPorTipoYGrado() {
        val kana = Kana("あ", Silabario.HIRAGANA)
        val kanji = Kanji("食")
        val palabra = Vocabulario("りんご")
        val grupo = Grupo(listOf(kana, kanji, palabra, objetoDirecto()))
        val soloKanjiYVocabulario = Configuracion(tipos = setOf(TipoDePieza.KANJI, TipoDePieza.VOCABULARIO))
        assertEquals(
            listOf(
                Item(kanji, Puntaje.IDENTIFICACION),
                Item(kanji, Puntaje.USO),
                Item(palabra, Puntaje.IDENTIFICACION),
                Item(palabra, Puntaje.USO),
            ),
            Sesion(grupo, soloKanjiYVocabulario).plan(avance),
        )
        assertEquals(
            listOf(Item(kanji, Puntaje.USO)),
            Sesion(grupo, Configuracion(tipos = setOf(TipoDePieza.KANJI), grados = usar)).plan(avance),
        )
    }

    @Test
    fun elUmbralSoloDejaLosQueLoCumplen() {
        val flojo = Kanji("食")
        val fuerte = Kanji("水")
        avance = RegistroEnMemoria(mapOf(flojo to Progreso(uso = 1), fuerte to Progreso(identificacion = 4, uso = 4)))
        val grupo = Grupo(listOf(flojo, fuerte))
        val debajo = Configuracion(grados = usar, umbral = Umbral(Lado.DEBAJO_DE, 3))
        val desde = Configuracion(grados = usar, umbral = Umbral(Lado.DESDE, 3))
        assertEquals(listOf(Item(flojo, Puntaje.USO)), Sesion(grupo, debajo).plan(avance))
        assertEquals(listOf(Item(fuerte, Puntaje.USO)), Sesion(grupo, desde).plan(avance))
    }

    @Test
    fun elOrdenYElLimiteLosEligeElAprendiz() {
        val flojo = Kanji("食")
        val fuerte = Kanji("水")
        avance = RegistroEnMemoria(mapOf(flojo to Progreso(identificacion = 1), fuerte to Progreso(identificacion = 5, uso = 3)))
        val grupo = Grupo(listOf(fuerte, flojo))
        fun plan(configuracion: Configuracion) = Sesion(grupo, configuracion).plan(avance)

        assertEquals(
            listOf(
                Item(fuerte, Puntaje.IDENTIFICACION),
                Item(fuerte, Puntaje.USO),
                Item(flojo, Puntaje.IDENTIFICACION),
                Item(flojo, Puntaje.USO),
            ),
            plan(Configuracion(orden = ComoEnLaSeleccion)),
        )
        assertEquals(
            listOf(
                Item(fuerte, Puntaje.IDENTIFICACION),
                Item(flojo, Puntaje.IDENTIFICACION),
                Item(fuerte, Puntaje.USO),
                Item(flojo, Puntaje.USO),
            ),
            plan(Configuracion(orden = IdentificarAntesDeUsar)),
        )
        assertEquals(
            listOf(Item(flojo, Puntaje.USO), Item(flojo, Puntaje.IDENTIFICACION)),
            plan(Configuracion(orden = MasBajosPrimero, limite = 2)),
        )
        assertEquals(
            listOf(Item(fuerte, Puntaje.IDENTIFICACION)),
            plan(Configuracion(orden = MasAltosPrimero, limite = 1)),
        )
        assertEquals(plan(Configuracion()).toSet(), plan(Configuracion(orden = AlAzar(Random(7)))).toSet())
    }

    @Test
    fun unItemSinEjercicioNoEntraAlPlan() {
        val kana = Kana("あ", Silabario.HIRAGANA)
        val sesion = sesion(TipoDePieza.KANA to Puntaje.IDENTIFICACION to FabricaDeDistractores(listOf("a"), "a", "a"))
        sesion.abrirSeleccion(Sesion(kana))
        assertEquals(listOf(Item(kana, Puntaje.IDENTIFICACION)), sesion.plan)
    }

    @Test
    fun sinConexionLaProduccionLibreUsaElJuicio() {
        val kana = Kana("あ", Silabario.HIRAGANA)
        val bien = Kanji("食")
        val forma = objetoDirecto()
        avance = RegistroEnMemoria(mapOf(kana to Progreso(identificacion = 1, uso = 1)))
        val juicio = Correccion(enContexto = listOf(bien, kana), formaCorrecta = listOf(bien, forma, kana))
        val frase = Frase("あ食を", mutableListOf(kana, bien, forma))
        sesion().producirLibre(frase, FabricaDeLibre("あ食を", juicio))
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(kana))
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(bien))
        assertEquals(Progreso(identificacion = 1), avance.progreso(forma))
    }

    @Test
    fun elAdaptadorTraduceAlProveedorSinEnviarElAvance() {
        val bien = Kanji("食")
        val forma = objetoDirecto()
        avance = RegistroEnMemoria(mapOf(bien to Progreso(identificacion = 4, uso = 2)))
        val frase = Frase("食を", mutableListOf(bien, forma))
        val juicioContrario = Correccion(enContexto = emptyList(), formaCorrecta = emptyList())
        val proveedor = ProveedorDeIA {
            mapOf("enContexto" to listOf("食"), "forma" to listOf("食", "を"))
        }
        val adaptador = AdaptadorDelAgente(proveedor)
        sesion(evaluacion = adaptador).producirLibre(frase, FabricaDeLibre("食を", juicioContrario))
        assertEquals(mapOf("texto" to "食を"), adaptador.ultimaSolicitud)
        assertFalse(adaptador.ultimaSolicitud.containsKey("identificacion"))
        assertFalse(adaptador.ultimaSolicitud.containsKey("uso"))
        assertEquals(Progreso(identificacion = 4, uso = 3), avance.progreso(bien))
        assertEquals(Progreso(identificacion = 1), avance.progreso(forma))
    }

    @Test
    fun dosCopiasDelMismoKanjiCompartenElAvance() {
        val delCatalogo = Kanji("食")
        val dentroDeLaPalabra = Kanji("食")
        val palabra = Vocabulario("食べる", listOf(dentroDeLaPalabra))
        val sesion = sesion(TipoDePieza.VOCABULARIO to Puntaje.USO to FabricaDeEscrituraAMano("たべる", "たべる"))
        sesion.abrirSeleccion(Sesion(palabra, Configuracion(grados = usar)))
        practicarTodo(sesion)
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(delCatalogo))
    }

    @Test
    fun usarBienUnaPiezaTambienCuentaComoIdentificarla() {
        val inicial = Progreso(identificacion = 0, uso = 0)
        assertEquals(Progreso(identificacion = 1, uso = 1), inicial.subir(Puntaje.USO))
        val identificaMucho = Progreso(identificacion = 5, uso = 1)
        assertEquals(Progreso(identificacion = 5, uso = 2), identificaMucho.subir(Puntaje.USO))
        assertEquals(Progreso(identificacion = 1, uso = 0), Progreso().subir(Puntaje.IDENTIFICACION))
    }

    @Test
    fun laGramaticaEsUnaEstructuraConHuecos() {
        val condicional = Gramatica(
            "ば…ほど",
            listOf(Hueco("verbo"), Fijo("ば"), Hueco("verbo"), Fijo("ほど")),
            Registro.INDIFERENTE,
        )
        assertEquals("[verbo]ば[verbo]ほど", condicional.mostrar())
        assertTrue(condicional.encaja("食べれば食べるほど"))
        assertFalse(condicional.encaja("食べるほど食べれば"))
        assertFalse(condicional.encaja("ばほど"))
    }

    @Test
    fun laCausaDeDakaraPuedeQuedarEnLaConversacion() {
        val informal = Gramatica(
            "だから",
            listOf(Opcional(listOf(Hueco("causa"), Fijo("。"))), Fijo("だから"), Hueco("consecuencia")),
            Registro.INFORMAL,
        )
        val cordial = Gramatica(
            "ですから",
            listOf(Opcional(listOf(Hueco("causa"), Fijo("。"))), Fijo("ですから"), Hueco("consecuencia")),
            Registro.CORDIAL,
        )
        assertEquals("([causa]。)だから[consecuencia]", informal.mostrar())
        assertTrue(informal.encaja("雨が降っている。だから行かない"))
        assertTrue(informal.encaja("だから行かない"))
        assertFalse(informal.encaja("行かない"))
        assertTrue(cordial.encaja("雨が降っています。ですから行きません"))
        assertFalse(informal.encaja("ですから行きません"))
    }

    private fun objetoDirecto() = Gramatica("を", listOf(Hueco("sustantivo"), Fijo("を")), Registro.INDIFERENTE)

    private fun practicarTodo(sesion: SesionDePractica) {
        while (sesion.siguiente() != null) sesion.registrarPuntaje()
    }

    private fun sesion(
        vararg ejercicios: Pair<Pair<TipoDePieza, Puntaje>, FabricaDeEjercicio<out PantallaDePractica>>,
        evaluacion: AdaptadorDelAgente? = null,
    ) = SesionDePractica(
        AlmacenEnMemoria(),
        avance,
        ReglaDeEfecto.porDefecto(),
        CatalogoDeEjercicios(ejercicios.toMap()),
        evaluacion,
    )
}
