# Ostra Verde S.A. — Pruebas unitarias (ISW-622)

Sistema de Gestión para la Producción de Hongos Ostra. Repositorio de la **Fase 4** del proyecto de
ISW-622 Pruebas de Software (UTN), facilitador Víctor Iván Zúñiga Zúñiga.

Integrantes: Marypaz López Sánchez y Belem.

Tecnología: Java 17, Spring Boot 3.3, Maven y JUnit 5 (Jupiter). Se abre y ejecuta en **Spring Tools (STS)**.

## Cómo ejecutar en Spring Tools

1. **File → Import → Maven → Existing Maven Projects** y seleccionar la carpeta del proyecto.
2. Esperar a que Maven descargue las dependencias (barra inferior derecha).
3. Clic derecho sobre `src/test/java` → **Run As → JUnit Test**. Deben aprobar las 97 pruebas.
4. Cobertura: clic derecho sobre `src/test/java` → **Coverage As → JUnit Test**.

Desde terminal (opcional): `mvn test` ejecuta las pruebas y genera el reporte de cobertura en
`target/site/jacoco/index.html`.

## Estructura

```
src/main/java/cr/ac/utn/ostraverde/
  OstraVerdeApplication.java     Arranque de Spring Boot
  excepciones/                   Validacion, ReglaNegocio, Duplicado, NoEncontrado, AccesoDenegado
  modelo/                        Seccion, Trabajador, Producto, Siembra, Aplicacion, Cosecha, Auditoria
  modelo/reporte/                Records de los reportes gerenciales
  repositorio/                   Repositorio, RepositorioMemoria (doble de prueba de la BD), BaseDatos
  servicio/                      Un servicio por paso del enunciado + Contexto + Sistema
  util/Validaciones.java         Validaciones compartidas (extraídas en el primer REFACTOR)
src/test/java/cr/ac/utn/ostraverde/
  Fabrica.java                   Datos válidos base y escenarios (Arrange reutilizable)
  *ServiceTest.java              Una clase de prueba por módulo
  NoFuncionalesTest.java         Requerimientos no funcionales
src/test/resources/junit-platform.properties   Ordena las pruebas por su nombre (CP-XXX-01, 02, ...)
evidencias/                      Salidas reales de cada fase RED, GREEN y REFACTOR
```

## Convenciones

- Cada prueba tiene `@DisplayName` igual a su caso del catálogo: `CP-SEC-01 · Registrar sección con datos válidos`.
- Cada prueba tiene las secciones `// Arrange`, `// Act` y `// Assert` (patrón AAA).
- Las pruebas se agrupan por requerimiento con `@Nested` (por ejemplo, "RF-01 Registrar sección").
- El "hoy" del sistema está fijo en 15/10/2026 con un `Clock` inyectado, para que los límites de fecha (RN-05) sean reproducibles.
- Pesos y cantidades con `BigDecimal` para evitar errores de redondeo (RNF-07).

## Ciclo TDD aplicado

| Módulo | Requerimientos | Casos | RED | GREEN | REFACTOR |
| --- | --- | --- | --- | --- | --- |
| SEC | RF-01 a RF-03 | 12 | 12 fallan | 12 aprueban | Extraer `util/Validaciones.java` |
| PER | RF-04 a RF-06 | 12 | 12 fallan | 12 aprueban | Usar validaciones compartidas |
| SIE | RF-07, RF-08 | 15 | 15 fallan | 15 aprueban | Usar validaciones compartidas |
| PRO | RF-09 a RF-12 | 11 | 11 fallan | 11 aprueban | Usar validaciones compartidas |
| APL | RF-13 | 11 | 11 fallan | 11 aprueban | Usar validaciones compartidas |
| COS | RF-14, RF-15 | 14 | 14 fallan | 14 aprueban | Usar validaciones compartidas |
| REP | RF-16 a RF-20 | 12 | 12 fallan | 12 aprueban | Usar validaciones compartidas |
| NF | RNF-01, 03 a 08, 10 | 10 | 3 fallan (sesión y auditoría) | 10 aprueban | Centralizar `ctx.sello()` |

En el RED de cada módulo los servicios existen como esqueletos cuyos métodos lanzan
`UnsupportedOperationException("No implementado")`, por eso las pruebas compilan pero fallan.
En el ciclo NF, 7 pruebas aprueban desde RED porque verifican características que ya existían;
las 3 que fallan guiaron la implementación de sesión (RNF-06) y auditoría (RNF-10).
