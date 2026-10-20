package musubi.practica

import musubi.practica.infraestructura.AdaptadorDelAgente
import musubi.practica.infraestructura.AlmacenEnMemoria
import musubi.practica.infraestructura.ProveedorDeIA
import musubi.practica.infraestructura.RegistroEnMemoria
import musubi.practica.modelo.Correccion
import musubi.practica.modelo.Decision
import musubi.practica.modelo.Frase
import musubi.practica.modelo.Grupo
import musubi.practica.modelo.Pieza
import musubi.practica.modelo.PoliticaDePuntaje
import musubi.practica.modelo.ReglaDeEfecto
import musubi.practica.modelo.Sesion
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
    @Test
    fun laFabricaAbreLosDistractoresSinQueLaSesionConstruyaLaVista() {
        val kanji = Pieza("食", "kanji")
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
        assertEquals(1, kanji.identificacion)
        assertEquals(0, kanji.uso)
    }

    @Test
    fun lasTraduccionesSustituyenALosDistractores() {
        val kanji = Pieza("水", "kanji")
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(kanji)),
            "kanji",
            "identificar",
            FabricaDeTraducciones(listOf("agua", "fuego"), "agua", "fuego"),
        )
        assertIs<VistaDeTraducciones>(sesion.vista)
        sesion.registrarPuntaje()
        assertEquals(0, kanji.identificacion)
    }

    @Test
    fun unaPoliticaNuevaSubeElPuntajeSinModificarLaRegla() {
        class EfectoDeLectura : PoliticaDePuntaje {
            override fun aplicar(sesion: Sesion, frase: Frase?, decisiones: List<Decision>?) {
                sesion.seleccion.piezas().forEach {
                    it.subirPuntaje("identificacion")
                    it.subirPuntaje("uso")
                }
            }
        }

        val lectura = Pieza("話", "kanji")
        val regla = ReglaDeEfecto.porDefecto(mapOf("lectura" to EfectoDeLectura()))
        val modelo = Sesion(Grupo(listOf(lectura)), "lectura", "identificar")
        regla.puntajeRespuesta(modelo, null, null)
        assertEquals(1, lectura.identificacion)
        assertEquals(1, lectura.uso)
    }

    @Test
    fun laCopiaDeLaFraseNoAlteraLaGuardada() {
        val piezas = mutableListOf(Pieza("りんご", "vocabulario"), Pieza("を", "gramatica"))
        val original = Frase("りんごを", piezas)
        val grupo = Grupo(listOf(piezas[1]))
        val sesion = sesion()
        sesion.abrirSeleccion(grupo, "gramatica", "interiorizar", FabricaDePatron("を", "を"))
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
        val dia = Pieza("日", "kanji")
        val libro = Pieza("本", "kanji")
        val juntados = Grupo(listOf(Grupo(listOf(dia)), Grupo(listOf(libro))))
        val sueltas = Grupo(listOf(dia, libro))
        assertEquals(listOf(dia, libro), juntados.piezas())
        assertEquals(sueltas.piezas(), juntados.piezas())
    }

    @Test
    fun elVocabularioSubeLaPalabraYSuKanji() {
        val kanji = Pieza("食", "kanji")
        val otro = Pieza("水", "kanji")
        val palabra = Pieza("食べる", "vocabulario", kanji = kanji)
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(palabra)),
            "vocabulario",
            "interiorizar",
            FabricaDeEscrituraAMano("たべる", "たべる"),
        )
        sesion.registrarPuntaje()
        assertEquals(1, palabra.uso)
        assertEquals(1, kanji.uso)
        assertEquals(0, otro.uso)
        assertEquals(0, palabra.identificacion)
    }

    @Test
    fun laFraseReparteElPuntajeEntreSusPiezas() {
        val manzana = Pieza("りんご", "vocabulario")
        val particula = Pieza("を", "gramatica")
        val frase = Frase("りんごを", mutableListOf(manzana, particula))
        val sesion = sesion()
        sesion.abrirSeleccion(
            Grupo(listOf(particula)),
            "gramatica",
            "interiorizar",
            FabricaDePatron("を", "りんごを"),
        )
        val copia = sesion.prepararFrase(frase)
        sesion.registrarPuntaje(copia)
        assertEquals(1, manzana.uso)
        assertEquals(1, particula.uso)
        assertFalse(frase.incorporada)
    }

    @Test
    fun sinConexionLaProduccionLibreUsaElJuicio() {
        val kana = Pieza("あ", "hiragana", uso = 1)
        val bien = Pieza("食", "kanji")
        val forma = Pieza("を", "gramatica")
        val juicio = Correccion(enContexto = listOf(bien, kana), formaCorrecta = listOf(bien, forma, kana))
        val frase = Frase("あ食を", mutableListOf(kana, bien, forma))
        val sesion = sesion()
        sesion.abrirSeleccion(Grupo(listOf(bien)), "kanji", "libre", FabricaDeLibre("あ食を", juicio))
        sesion.registrarPuntaje(frase)
        assertEquals(1, kana.uso)
        assertEquals(0, kana.identificacion)
        assertEquals(1, bien.uso)
        assertEquals(1, forma.identificacion)
        assertEquals(0, forma.uso)
    }

    @Test
    fun elAdaptadorTraduceAlProveedorSinEnviarElAvance() {
        val bien = Pieza("食", "kanji", identificacion = 4, uso = 2)
        val forma = Pieza("を", "gramatica")
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
        assertEquals(3, bien.uso)
        assertEquals(1, forma.identificacion)
        assertEquals(4, bien.identificacion)
    }

    private fun sesion(evaluacion: AdaptadorDelAgente? = null) = SesionDePractica(
        AlmacenEnMemoria(),
        RegistroEnMemoria(),
        ReglaDeEfecto.porDefecto(),
        evaluacion,
    )
}
