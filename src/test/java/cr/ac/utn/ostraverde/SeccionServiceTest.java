package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.utn.ostraverde.Fabrica.seccionValida;
import static cr.ac.utn.ostraverde.Fabrica.sistemaDePrueba;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo SEC — Gestión de secciones (RF-01, RF-02, RF-03)")
class SeccionServiceTest {

    @Nested
    @DisplayName("RF-01 Registrar sección")
    class RegistrarSeccion {

        @Test
        @DisplayName("CP-SEC-01 · Registrar sección con datos válidos")
        void registrarSeccionValida() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            Seccion datos = seccionValida();
            // Act
            Seccion resultado = sistema.secciones().registrar(datos);
            // Assert
            assertEquals("SEC-01", resultado.getCodigo());
            assertEquals(500, resultado.getCapacidad());
            assertEquals("Disponible", resultado.getEstado());
            assertEquals("Nave Norte", sistema.secciones().obtener("SEC-01").getNombre());
        }

        @Test
        @DisplayName("CP-SEC-02 · Rechazar sección con código duplicado (RN-01)")
        void rechazarCodigoDuplicado() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.secciones().registrar(seccionValida());
            // Act
            DuplicadoException error = assertThrows(DuplicadoException.class,
                    () -> sistema.secciones().registrar(seccionValida().conNombre("Otra nave")));
            // Assert
            assertEquals("Ya existe una sección con el código \"SEC-01\"", error.getMessage());
        }

        @Test
        @DisplayName("CP-SEC-03 · Rechazar código duplicado escrito con minúsculas y espacios (RN-01, límite)")
        void rechazarDuplicadoNormalizado() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-01"));
            // Act
            DuplicadoException error = assertThrows(DuplicadoException.class,
                    () -> sistema.secciones().registrar(seccionValida().conCodigo("  sec-01 ")));
            // Assert
            assertEquals("RN-01", error.getRegla());
        }

        @Test
        @DisplayName("CP-SEC-04 · Rechazar estado fuera de la lista permitida (RN-02)")
        void rechazarEstadoInvalido() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.secciones().registrar(seccionValida().conEstado("Cerrada")));
            // Assert
            assertTrue(error.getMessage().startsWith("Estado \"Cerrada\" inválido"));
        }

        @Test
        @DisplayName("CP-SEC-05 · Rechazar sección sin nombre (RES-01)")
        void rechazarSinNombre() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.secciones().registrar(seccionValida().conNombre("   ")));
            // Assert
            assertEquals("El campo \"nombre\" es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("CP-SEC-06 · Rechazar capacidad máxima igual a 0 (RES-02, límite inferior inválido)")
        void rechazarCapacidadCero() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.secciones().registrar(seccionValida().conCapacidad(0)));
            // Assert
            assertEquals("El campo \"capacidad\" debe ser un número entero mayor que cero", error.getMessage());
        }

        @Test
        @DisplayName("CP-SEC-07 · Aceptar capacidad máxima igual a 1 (RES-02, límite inferior válido)")
        void aceptarCapacidadUno() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            Seccion resultado = sistema.secciones().registrar(seccionValida().conCapacidad(1));
            // Assert
            assertEquals(1, resultado.getCapacidad());
        }
    }

    @Nested
    @DisplayName("RF-02 Modificar sección")
    class ModificarSeccion {

        @Test
        @DisplayName("CP-SEC-08 · Modificar nombre, capacidad y estado de una sección existente")
        void modificarSeccion() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.secciones().registrar(seccionValida());
            Seccion cambios = new Seccion().conNombre("Nave Norte A").conCapacidad(650).conEstado("En mantenimiento");
            // Act
            sistema.secciones().modificar("SEC-01", cambios);
            // Assert
            Seccion guardada = sistema.secciones().obtener("SEC-01");
            assertEquals("Nave Norte A", guardada.getNombre());
            assertEquals(650, guardada.getCapacidad());
            assertEquals("En mantenimiento", guardada.getEstado());
        }

        @Test
        @DisplayName("CP-SEC-09 · Rechazar modificación de una sección inexistente")
        void rechazarModificarInexistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.secciones().modificar("SEC-99", new Seccion().conNombre("X")));
            // Assert
            assertEquals("Sección con código/identificación \"SEC-99\" no existe", error.getMessage());
        }

        @Test
        @DisplayName("CP-SEC-10 · Rechazar modificación con estado inválido sin alterar los datos (RN-02)")
        void rechazarModificacionEstadoInvalido() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.secciones().registrar(seccionValida());
            // Act
            assertThrows(ReglaNegocioException.class,
                    () -> sistema.secciones().modificar("SEC-01", new Seccion().conEstado("Inactiva")));
            // Assert
            assertEquals("Disponible", sistema.secciones().obtener("SEC-01").getEstado());
        }
    }

    @Nested
    @DisplayName("RF-03 Consultar secciones disponibles")
    class ConsultarDisponibles {

        @Test
        @DisplayName("CP-SEC-11 · Consultar devuelve solo las secciones en estado Disponible")
        void consultarSoloDisponibles() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-01").conEstado("Disponible"));
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-02").conEstado("Ocupada"));
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-03").conEstado("En mantenimiento"));
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-04").conEstado("Disponible"));
            // Act
            List<Seccion> disponibles = sistema.secciones().consultarDisponibles();
            // Assert
            assertEquals(List.of("SEC-01", "SEC-04"), disponibles.stream().map(Seccion::getCodigo).toList());
        }

        @Test
        @DisplayName("CP-SEC-12 · Consultar sin secciones disponibles devuelve lista vacía (límite)")
        void consultarSinDisponibles() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-02").conEstado("Ocupada"));
            // Act
            List<Seccion> disponibles = sistema.secciones().consultarDisponibles();
            // Assert
            assertTrue(disponibles.isEmpty());
        }
    }
}
