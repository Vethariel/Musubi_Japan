# Musubi Japan

Musubi Japan (結び, nudo) es una aplicación móvil para aprender japonés. Reúne en un solo lugar el kanji, el vocabulario, la gramática y las lecturas, y los ata a partir de frases.

La aplicación provee los contenidos. Un aprendiz novato puede seguir la ruta de aprendizaje estándar. Quien ya va aprendiendo incorpora sus frases, en el orden que define, para no olvidarlas y poder usarlas después. Cada frase se desglosa en piezas: vocabulario, kanji y gramática.

## Avance

Cada pieza registra dos grados: el reconocimiento, cuando ya se identifica, y la interiorización, cuando ya se usa bien.

Practicar una pieza aislada sirve para entender su significado y en qué otras frases puede aparecer. Practicar una frase hace avanzar las piezas que la componen, y así se dedica menos tiempo a cada una por separado. El avance general muestra cuánto se ha recorrido.

## Estudio y mascota

La aplicación funciona sin conexión. En ese modo las frases y el avance permanecen en el teléfono, sin anuncios y sin mascota.

La mascota es otra capa. Propone ejercicios nuevos e información de uso correcto para fortalecer el japonés. Se ofrece mediante anuncios o pago, para sostener su servicio. Al usarla, al proveedor solo se envía la solicitud, sin información personal.

## Licencia

MIT. El texto está en [LICENSE](LICENSE).

## Código del componente Práctica

El componente que corre en el celular está en [practica](practica), escrito en Kotlin. Los paquetes `vista`, `viewmodel` y `modelo` siguen el MVVM. El almacén, el registro de avance y el proveedor de la mascota están en memoria, dentro de `infraestructura`.

El proyecto trae el wrapper de Gradle 9.7.1 y compila para Java 21, aunque Gradle corra con un JDK más nuevo. Las pruebas se corren así:

```bash
cd practica
./gradlew test
```

| Patrón | Archivo | Clases |
| --- | --- | --- |
| Factory Method | `viewmodel/FabricaDeEjercicio.kt` | `FabricaDeEjercicio`, `FabricaDeDistractores` |
| Prototype | `modelo/Frase.kt` | `Frase` |
| Composite | `modelo/Seleccion.kt`, `modelo/Pieza.kt` | `Seleccion`, `Grupo`, `Pieza` |
| Adapter | `infraestructura/AdaptadorDelAgente.kt` | `AdaptadorDelAgente`, `IEvaluacion` |
| Strategy | `modelo/PoliticaDePuntaje.kt` | `ReglaDeEfecto`, `PoliticaDePuntaje`, `EfectoDeKanji`, `EfectoDeVocabulario`, `EfectoDeFrase`, `EfectoDeContexto` |
| Chain of Responsibility | `modelo/FiltroDePuntaje.kt` | `FiltroDePuntaje`, `FiltroDeKanaDominado`, `FiltroDeForma`, `FiltroDeContexto` |

Las rutas de la tabla cuelgan de `practica/src/main/kotlin/musubi/practica/`. Las pruebas están en `practica/src/test/kotlin/musubi/practica/PracticaTest.kt`.
