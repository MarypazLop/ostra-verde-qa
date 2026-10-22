package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static cr.ac.utn.ostraverde.Fabrica.HOY;
import static cr.ac.utn.ostraverde.Fabrica.escenarioBase;
import static cr.ac.utn.ostraverde.Fabrica.seccionValida;
import static cr.ac.utn.ostraverde.Fabrica.siembraValida;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo SIE — Registro de siembras (RF-07, RF-08)")
class SiembraServiceTest {

    @Nested
    @DisplayName("RF-07 Registrar siembra")
    class RegistrarSiembra {

        @Test
        @DisplayName("CP-SIE-01 · Registrar siembra válida en sección disponible")
        void registrarSiembraValida() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            Siembra siembra = sistema.siembras().registrar(siembraValida());
            // Assert
            assertEquals("SIE-01", siembra.getCodigo());
            assertEquals("SEC-01", siembra.getSeccion());
            assertEquals(120, siembra.getSustratos());
            assertEquals("Activa", siembra.getEstado());
        }

        @Test
        @DisplayName("CP-SIE-02 · Rechazar siembra en sección Ocupada (RN-07)")
        void rechazarSeccionOcupada() {
            // Arrange
            Sistema sistema = escenarioBase();
            sistema.siembras().registrar(siembraValida().conCodigo("SIE-01"));
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.siembras().registrar(siembraValida().conCodigo("SIE-02")));
            // Assert
            assertEquals("RN-07", error.getRegla());
            assertTrue(error.getMessage().contains("ocupada y no puede recibir otra siembra"));
        }

        @Test
        @DisplayName("CP-SIE-03 · Rechazar siembra en sección En mantenimiento (RN-04)")
        void rechazarSeccionEnMantenimiento() {
            // Arrange
            Sistema sistema = escenarioBase();
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-02").conEstado("En mantenimiento"));
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.siembras().registrar(siembraValida().conSeccion("SEC-02")));
            // Assert
            assertEquals("RN-04", error.getRegla());
        }

        @Test
        @DisplayName("CP-SIE-04 · Aceptar siembra con fecha igual a hoy (RN-05, límite válido)")
        void aceptarFechaHoy() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            Siembra siembra = sistema.siembras().registrar(siembraValida().conFecha(HOY));
            // Assert
            assertEquals("15/10/2026", siembra.getFecha());
        }

        @Test
        @DisplayName("CP-SIE-05 · Rechazar siembra con fecha de mañana (RN-05, límite inválido)")
        void rechazarFechaManana() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.siembras().registrar(siembraValida().conFecha("16/10/2026")));
            // Assert
            assertEquals("La fecha de siembra no puede ser futura", error.getMessage());
        }

        @Test
        @DisplayName("CP-SIE-06 · Rechazar cantidad de sustratos igual a 0 (RN-06, límite inválido)")
        void rechazarSustratosCero() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.siembras().registrar(siembraValida().conSustratos(0)));
            // Assert
            assertEquals("La cantidad de sustratos debe ser mayor que cero", error.getMessage());
        }

        @Test
        @DisplayName("CP-SIE-07 · Aceptar cantidad de sustratos igual a 1 (RN-06, límite válido)")
        void aceptarSustratosUno() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            Siembra siembra = sistema.siembras().registrar(siembraValida().conSustratos(1));
            // Assert
            assertEquals(1, siembra.getSustratos());
        }

        @Test
        @DisplayName("CP-SIE-08 · Rechazar cantidad de sustratos negativa (RN-06)")
        void rechazarSustratosNegativos() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.siembras().registrar(siembraValida().conSustratos(-5)));
            // Assert
            assertEquals("RN-06", error.getRegla());
        }

        @Test
        @DisplayName("CP-SIE-09 · Rechazar siembra en sección inexistente (RES-06)")
        void rechazarSeccionInexistente() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.siembras().registrar(siembraValida().conSeccion("SEC-99")));
            // Assert
            assertEquals("Sección con código/identificación \"SEC-99\" no existe", error.getMessage());
        }

        @Test
        @DisplayName("CP-SIE-10 · Rechazar siembra con responsable inexistente (RES-06)")
        void rechazarResponsableInexistente() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.siembras().registrar(siembraValida().conResponsable("100000000")));
            // Assert
            assertEquals("Trabajador con código/identificación \"100000000\" no existe", error.getMessage());
        }

        @Test
        @DisplayName("CP-SIE-11 · Rechazar fecha inexistente en el calendario 31/02/2026 (RES-11)")
        void rechazarFechaInexistente() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.siembras().registrar(siembraValida().conFecha("31/02/2026")));
            // Assert
            assertEquals("El campo \"fecha\" no es una fecha válida del calendario", error.getMessage());
        }

        @Test
        @DisplayName("CP-SIE-12 · Rechazar código de siembra duplicado (RES-10)")
        void rechazarCodigoDuplicado() {
            // Arrange
            Sistema sistema = escenarioBase();
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-02"));
            sistema.siembras().registrar(siembraValida().conCodigo("SIE-01").conSeccion("SEC-01"));
            // Act
            DuplicadoException error = assertThrows(DuplicadoException.class,
                    () -> sistema.siembras().registrar(siembraValida().conCodigo("SIE-01").conSeccion("SEC-02")));
            // Assert
            assertEquals("RES-10", error.getRegla());
        }

        @Test
        @DisplayName("CP-SIE-13 · Aceptar siembra sin observaciones (RES-05, campo opcional)")
        void aceptarSinObservaciones() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            Siembra siembra = sistema.siembras().registrar(siembraValida().conObservaciones(null));
            // Assert
            assertEquals("", siembra.getObservaciones());
        }
    }

    @Nested
    @DisplayName("RF-08 La sección pasa a Ocupada al sembrar")
    class SeccionOcupada {

        @Test
        @DisplayName("CP-SIE-14 · Tras registrar la siembra la sección queda Ocupada y sale de disponibles (RN-08)")
        void seccionQuedaOcupada() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            sistema.siembras().registrar(siembraValida());
            // Assert
            assertEquals("Ocupada", sistema.secciones().obtener("SEC-01").getEstado());
            assertTrue(sistema.secciones().consultarDisponibles().isEmpty());
        }

        @Test
        @DisplayName("CP-SIE-15 · Una siembra rechazada no cambia el estado de la sección (RNF-02)")
        void siembraRechazadaNoCambiaSeccion() {
            // Arrange
            Sistema sistema = escenarioBase();
            // Act
            assertThrows(NoEncontradoException.class,
                    () -> sistema.siembras().registrar(siembraValida().conResponsable("100000000")));
            // Assert
            assertEquals("Disponible", sistema.secciones().obtener("SEC-01").getEstado());
            assertTrue(sistema.siembras().listar().isEmpty());
        }
    }
}
