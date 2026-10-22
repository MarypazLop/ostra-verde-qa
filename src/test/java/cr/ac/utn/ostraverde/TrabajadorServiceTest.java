package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Trabajador;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.utn.ostraverde.Fabrica.sistemaDePrueba;
import static cr.ac.utn.ostraverde.Fabrica.trabajadorValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo PER — Registro de personal (RF-04, RF-05, RF-06)")
class TrabajadorServiceTest {

    @Nested
    @DisplayName("RF-04 Registrar trabajador")
    class RegistrarTrabajador {

        @Test
        @DisplayName("CP-PER-01 · Registrar trabajador con datos válidos")
        void registrarTrabajadorValido() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            Trabajador resultado = sistema.trabajadores().registrar(trabajadorValido());
            // Assert
            assertEquals("205550111", resultado.getIdentificacion());
            assertEquals("Operaria de cultivo", resultado.getCargo());
            assertEquals(1, sistema.trabajadores().listar().size());
        }

        @Test
        @DisplayName("CP-PER-02 · Rechazar identificación duplicada (RN-03)")
        void rechazarIdentificacionDuplicada() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.trabajadores().registrar(trabajadorValido());
            // Act
            DuplicadoException error = assertThrows(DuplicadoException.class,
                    () -> sistema.trabajadores().registrar(trabajadorValido().conNombre("Luis Vargas")));
            // Assert
            assertEquals("Ya existe un trabajador con la identificación \"205550111\"", error.getMessage());
        }

        @Test
        @DisplayName("CP-PER-03 · Rechazar correo con formato inválido (RES-03)")
        void rechazarCorreoInvalido() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.trabajadores().registrar(trabajadorValido().conCorreo("ana.rojas.ostraverde.cr")));
            // Assert
            assertEquals("El campo \"correo\" debe tener el formato usuario@dominio", error.getMessage());
        }

        @Test
        @DisplayName("CP-PER-04 · Rechazar teléfono de 7 dígitos (RES-04, límite inválido)")
        void rechazarTelefonoSieteDigitos() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.trabajadores().registrar(trabajadorValido().conTelefono("8888777")));
            // Assert
            assertEquals("El campo \"telefono\" debe contener exactamente 8 dígitos", error.getMessage());
        }

        @Test
        @DisplayName("CP-PER-05 · Aceptar teléfono de exactamente 8 dígitos (RES-04, límite válido)")
        void aceptarTelefonoOchoDigitos() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            Trabajador resultado = sistema.trabajadores().registrar(trabajadorValido().conTelefono("24601234"));
            // Assert
            assertEquals("24601234", resultado.getTelefono());
        }

        @Test
        @DisplayName("CP-PER-06 · Rechazar trabajador sin cargo (RES-03)")
        void rechazarSinCargo() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.trabajadores().registrar(trabajadorValido().conCargo("")));
            // Assert
            assertEquals("El campo \"cargo\" es obligatorio", error.getMessage());
        }
    }

    @Nested
    @DisplayName("RF-05 Consultar trabajadores")
    class ConsultarTrabajadores {

        @Test
        @DisplayName("CP-PER-07 · Consultar trabajador por identificación existente")
        void consultarExistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.trabajadores().registrar(trabajadorValido());
            // Act
            Trabajador trabajador = sistema.trabajadores().consultar("205550111");
            // Assert
            assertEquals("ana.rojas@ostraverde.cr", trabajador.getCorreo());
        }

        @Test
        @DisplayName("CP-PER-08 · Consultar identificación inexistente indica \"no existe\"")
        void consultarInexistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.trabajadores().consultar("999999999"));
            // Assert
            assertTrue(error.getMessage().endsWith("no existe"));
        }

        @Test
        @DisplayName("CP-PER-09 · Listar sin trabajadores registrados devuelve lista vacía (límite)")
        void listarVacio() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            List<Trabajador> lista = sistema.trabajadores().listar();
            // Assert
            assertTrue(lista.isEmpty());
        }
    }

    @Nested
    @DisplayName("RF-06 Actualizar trabajador")
    class ActualizarTrabajador {

        @Test
        @DisplayName("CP-PER-10 · Actualizar teléfono y cargo de un trabajador existente")
        void actualizarTelefonoYCargo() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.trabajadores().registrar(trabajadorValido());
            // Act
            sistema.trabajadores().actualizar("205550111", new Trabajador().conTelefono("70001234").conCargo("Supervisora"));
            // Assert
            Trabajador guardado = sistema.trabajadores().consultar("205550111");
            assertEquals("70001234", guardado.getTelefono());
            assertEquals("Supervisora", guardado.getCargo());
        }

        @Test
        @DisplayName("CP-PER-11 · Rechazar actualización con correo inválido sin modificar los datos")
        void rechazarActualizacionCorreoInvalido() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.trabajadores().registrar(trabajadorValido());
            // Act
            assertThrows(ValidacionException.class,
                    () -> sistema.trabajadores().actualizar("205550111", new Trabajador().conCorreo("sin-arroba")));
            // Assert
            assertEquals("ana.rojas@ostraverde.cr", sistema.trabajadores().consultar("205550111").getCorreo());
        }

        @Test
        @DisplayName("CP-PER-12 · Rechazar cambio de identificación al actualizar (RN-03)")
        void rechazarCambioIdentificacion() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.trabajadores().registrar(trabajadorValido());
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.trabajadores().actualizar("205550111", new Trabajador().conIdentificacion("111111111")));
            // Assert
            assertEquals("La identificación del trabajador no puede modificarse", error.getMessage());
        }
    }
}
