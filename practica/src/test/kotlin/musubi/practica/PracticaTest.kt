package musubi.practica

import musubi.practica.infraestructura.AdaptadorDelAgente
import musubi.practica.infraestructura.AlmacenEnMemoria
import musubi.practica.infraestructura.ProveedorDeIA
import musubi.practica.infraestructura.RegistroEnMemoria
import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Decision
import musubi.practica.modelo.Fijo
import musubi.practica.modelo.Frase
import musubi.practica.modelo.Gramatica
import musubi.practica.modelo.Grupo
import musubi.practica.modelo.Hueco
import musubi.practica.modelo.Kana
import musubi.practica.modelo.Kanji
import musubi.practica.modelo.Opcional
import musubi.practica.modelo.PoliticaDePuntaje
import musubi.practica.modelo.Progreso
import musubi.practica.modelo.Puntaje
import musubi.practica.modelo.Registro
import musubi.practica.modelo.ReglaDeEfecto
import musubi.practica.modelo.Sesion
import musubi.practica.modelo.Silabario
import musubi.practica.modelo.Vocabulario
import musubi.practica.viewmodel.FabricaDeDistractores
import musubi.practica.viewmodel.FabricaDeEscrituraAMano
import musubi.practica.viewmodel.FabricaDeLibre
import musubi.practica.viewmodel.FabricaDePatron
import musubi.practica.viewmodel.FabricaDeTraducciones
import musubi.practica.viewmodel.SesionDePractica
import musubi.practica.vista.VistaDeDistractores
import musubi.practica.vista.VistaDeTraducciones
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PracticaTest {
    private var avance = RegistroEnMemoria()

    @Test
    fun laFabricaAbreLosDistractoresSinQueLaSesionConstruyaLaVista() {
        val kanji = Kanji("食")
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(kanji)),
            "kanji",
            "identificar",
            FabricaDeDistractores(listOf("comida", "agua"), "comida", "comida"),
        )
        assertIs<VistaDeDistractores>(sesion.vista)
        assertEquals("comida, agua", (sesion.vista as VistaDeDistractores).mostrarEjercicio())
        sesion.registrarPuntaje()
        assertEquals(Progreso(identificacion = 1, uso = 0), avance.progreso(kanji))
    }

    @Test
    fun lasTraduccionesSustituyenALosDistractores() {
        val kanji = Kanji("水")
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(kanji)),
            "kanji",
            "identificar",
            FabricaDeTraducciones(listOf("agua", "fuego"), "agua", "fuego"),
        )
        assertIs<VistaDeTraducciones>(sesion.vista)
        sesion.registrarPuntaje()
        assertEquals(Progreso(), avance.progreso(kanji))
    }

    @Test
    fun unaPoliticaNuevaSubeElPuntajeSinModificarLaRegla() {
        class EfectoDeLectura : PoliticaDePuntaje {
            override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?): List<Decision> =
                sesion.seleccion.piezas().flatMap {
                    listOf(Decision(it, Puntaje.IDENTIFICACION), Decision(it, Puntaje.USO))
                }
        }

        val lectura = Kanji("話")
        val regla = ReglaDeEfecto.porDefecto(mapOf("lectura" to EfectoDeLectura()))
        val modelo = Sesion(Grupo(listOf(lectura)), "lectura", "identificar")
        val decisiones = regla.puntajeRespuesta(modelo, null, null, avance)
        assertEquals(
            listOf(Decision(lectura, Puntaje.IDENTIFICACION), Decision(lectura, Puntaje.USO)),
            decisiones,
        )
    }

    @Test
    fun laCopiaDeLaFraseNoAlteraLaGuardada() {
        val particula = objetoDirecto()
        val piezas = mutableListOf(Vocabulario("りんご"), particula)
        val original = Frase("りんごを", piezas)
        val grupo = Grupo(listOf(particula))
        val sesion = sesion()
        sesion.abrirSeleccion(grupo, "gramatica", "interiorizar", FabricaDePatron(particula, "りんごを"))
        val copia = sesion.prepararFrase(original)
        copia.texto = "otra"
        copia.piezas.reverse()
        copia.incorporarGrupo(grupo)
        assertEquals("りんごを", original.texto)
        assertEquals("りんご", original.piezas.first().nombre)
        assertFalse(original.incorporada)
        assertTrue(copia.incorporada)
        assertEquals(listOf(copia), grupo.frases)
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
    fun elVocabularioSubeLaPalabraYSuKanji() {
        val kanji = Kanji("食")
        val otro = Kanji("水")
        val palabra = Vocabulario("食べる", listOf(kanji))
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(palabra)),
            "vocabulario",
            "interiorizar",
            FabricaDeEscrituraAMano("たべる", "たべる"),
        )
        sesion.registrarPuntaje()
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(palabra))
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(kanji))
        assertEquals(Progreso(), avance.progreso(otro))
    }

    @Test
    fun laFraseReparteElPuntajeEntreSusPiezas() {
        val manzana = Vocabulario("りんご")
        val particula = objetoDirecto()
        val frase = Frase("りんごを", mutableListOf(manzana, particula))
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(particula)),
            "gramatica",
            "interiorizar",
            FabricaDePatron(particula, "りんごを"),
        )
        val copia = sesion.prepararFrase(frase)
        sesion.registrarPuntaje(copia)
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(manzana))
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(particula))
        assertFalse(frase.incorporada)
    }

    @Test
    fun sinConexionLaProduccionLibreUsaElJuicio() {
        val kana = Kana("あ", Silabario.HIRAGANA)
        val bien = Kanji("食")
        avance = RegistroEnMemoria(mapOf(kana to Progreso(identificacion = 1, uso = 1)))
        val forma = objetoDirecto()
        val juicio = Correccion(enContexto = listOf(bien, kana), formaCorrecta = listOf(bien, forma, kana))
        val frase = Frase("あ食を", mutableListOf(kana, bien, forma))
        val sesion = sesion()
        sesion.abrirSeleccion(Grupo(listOf(bien)), "kanji", "libre", FabricaDeLibre("あ食を", juicio))
        sesion.registrarPuntaje(frase)
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
        val sesion = sesion(adaptador)
        sesion.abrirSeleccion(
            Grupo(listOf(bien)),
            "kanji",
            "libre",
            FabricaDeLibre("食を", juicioContrario),
        )
        sesion.registrarPuntaje(frase)
        assertEquals(mapOf("texto" to "食を"), adaptador.ultimaSolicitud)
        assertFalse(adaptador.ultimaSolicitud.containsKey("identificacion"))
        assertFalse(adaptador.ultimaSolicitud.containsKey("uso"))
        assertEquals(Progreso(identificacion = 4, uso = 3), avance.progreso(bien))
        assertEquals(Progreso(identificacion = 1), avance.progreso(forma))
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

    @Test
    fun elAvanceRecibeLasPiezasDeLaFraseAunqueNoEstenEnLaSeleccion() {
        val manzana = Vocabulario("りんご")
        val particula = objetoDirecto()
        val frase = Frase("りんごを", mutableListOf(manzana, particula))
        val sesion = sesion()
        sesion.abrirSeleccion(Grupo(listOf(particula)), "gramatica", "interiorizar", FabricaDePatron(particula, "りんごを"))
        sesion.registrarPuntaje(frase)
        assertEquals(Progreso(identificacion = 1, uso = 1), avance.progreso(manzana))
    }

    @Test
    fun dosCopiasDelMismoKanjiCompartenElAvance() {
        val delCatalogo = Kanji("食")
        val dentroDeLaPalabra = Kanji("食")
        val palabra = Vocabulario("食べる", listOf(dentroDeLaPalabra))
        val sesion = sesion()
        sesion.abrirSeleccion(Grupo(listOf(palabra)), "vocabulario", "interiorizar", FabricaDeEscrituraAMano("たべる", "たべる"))
        sesion.registrarPuntaje()
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

    private fun objetoDirecto() = Gramatica("を", listOf(Hueco("sustantivo"), Fijo("を")), Registro.INDIFERENTE)

    private fun sesion(evaluacion: AdaptadorDelAgente? = null) = SesionDePractica(
        AlmacenEnMemoria(),
        avance,
        ReglaDeEfecto.porDefecto(),
        evaluacion,
    )
}
