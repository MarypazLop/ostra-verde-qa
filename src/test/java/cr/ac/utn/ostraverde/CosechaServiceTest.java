package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static cr.ac.utn.ostraverde.Fabrica.cosechaValida;
import static cr.ac.utn.ostraverde.Fabrica.escenarioConSiembra;
import static cr.ac.utn.ostraverde.Fabrica.kg;
import static cr.ac.utn.ostraverde.Fabrica.siembraValida;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo COS — Registro de cosechas (RF-14, RF-15)")
class CosechaServiceTest {

    @Nested
    @DisplayName("RF-14 Registrar cosecha")
    class RegistrarCosecha {

        @Test
        @DisplayName("CP-COS-01 · Registrar cosecha válida")
        void registrarCosechaValida() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            Cosecha cosecha = sistema.cosechas().registrar(cosechaValida());
            // Assert
            assertEquals("COS-01", cosecha.getCodigo());
            assertEquals("SEC-01", cosecha.getSeccion());
            assertEquals(kg("45.75"), cosecha.getPesoKg());
            assertEquals(90, cosecha.getBandejas());
            assertEquals("Cosechada", sistema.siembras().obtener("SIE-01").getEstado());
        }

        @Test
        @DisplayName("CP-COS-02 · Rechazar segunda cosecha de la misma siembra (RN-14)")
        void rechazarSegundaCosecha() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            sistema.cosechas().registrar(cosechaValida().conCodigo("COS-01"));
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conCodigo("COS-02").conFecha("15/10/2026")));
            // Assert
            assertEquals("La siembra \"SIE-01\" ya fue cosechada; solo puede cosecharse una vez", error.getMessage());
        }

        @Test
        @DisplayName("CP-COS-03 · Rechazar peso cosechado igual a 0 kg (RN-15, límite inválido)")
        void rechazarPesoCero() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conPesoKg(kg("0"))));
            // Assert
            assertEquals("El peso cosechado debe ser mayor que cero", error.getMessage());
        }

        @Test
        @DisplayName("CP-COS-04 · Aceptar peso cosechado de 0.01 kg (RN-15, límite válido)")
        void aceptarPesoMinimo() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            Cosecha cosecha = sistema.cosechas().registrar(cosechaValida().conPesoKg(kg("0.01")));
            // Assert
            assertEquals(kg("0.01"), cosecha.getPesoKg());
        }

        @Test
        @DisplayName("CP-COS-05 · Rechazar peso con 3 decimales (RES-09)")
        void rechazarTresDecimales() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conPesoKg(kg("10.125"))));
            // Assert
            assertEquals("El campo \"pesoKg\" admite como máximo 2 decimales", error.getMessage());
        }

        @Test
        @DisplayName("CP-COS-06 · Rechazar cosecha con la misma fecha de la siembra (RN-16, límite inválido)")
        void rechazarMismaFechaSiembra() {
            // Arrange
            Sistema sistema = escenarioConSiembra(); // siembra del 01/10/2026
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conFecha("01/10/2026")));
            // Assert
            assertEquals("La fecha de cosecha debe ser posterior a la fecha de siembra", error.getMessage());
        }

        @Test
        @DisplayName("CP-COS-07 · Aceptar cosecha el día siguiente a la siembra (RN-16, límite válido)")
        void aceptarDiaSiguiente() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            Cosecha cosecha = sistema.cosechas().registrar(cosechaValida().conFecha("02/10/2026"));
            // Assert
            assertEquals("02/10/2026", cosecha.getFecha());
        }

        @Test
        @DisplayName("CP-COS-08 · Rechazar cosecha con fecha anterior a la siembra (RN-16)")
        void rechazarFechaAnterior() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conFecha("20/09/2026")));
            // Assert
            assertEquals("RN-16", error.getRegla());
        }

        @Test
        @DisplayName("CP-COS-09 · Aceptar cosecha con 0 bandejas (RES-09, límite válido)")
        void aceptarCeroBandejas() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            Cosecha cosecha = sistema.cosechas().registrar(cosechaValida().conBandejas(0));
            // Assert
            assertEquals(0, cosecha.getBandejas());
        }

        @Test
        @DisplayName("CP-COS-10 · Rechazar cantidad de bandejas negativa (RES-09)")
        void rechazarBandejasNegativas() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conBandejas(-1)));
            // Assert
            assertEquals("El campo \"bandejas\" debe ser un número entero mayor o igual a cero", error.getMessage());
        }

        @Test
        @DisplayName("CP-COS-11 · Rechazar cosecha de una siembra inexistente")
        void rechazarSiembraInexistente() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conSiembra("SIE-404")));
            // Assert
            assertEquals("Siembra con código/identificación \"SIE-404\" no existe", error.getMessage());
        }

        @Test
        @DisplayName("CP-COS-12 · Rechazar cosecha con responsable inexistente (RES-06)")
        void rechazarResponsableInexistente() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conResponsable("100000000")));
            // Assert
            assertEquals("Trabajador con código/identificación \"100000000\" no existe", error.getMessage());
        }
    }

    @Nested
    @DisplayName("RF-15 La sección vuelve a Disponible al cosechar")
    class SeccionDisponible {

        @Test
        @DisplayName("CP-COS-13 · Tras cosechar, la sección queda Disponible y acepta una nueva siembra (RN-17)")
        void seccionVuelveADisponible() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            sistema.cosechas().registrar(cosechaValida());
            // Assert
            assertEquals("Disponible", sistema.secciones().obtener("SEC-01").getEstado());
            Siembra nueva = sistema.siembras().registrar(siembraValida().conCodigo("SIE-02").conFecha("15/10/2026"));
            assertEquals("SEC-01", nueva.getSeccion());
        }

        @Test
        @DisplayName("CP-COS-14 · Una cosecha rechazada deja la sección Ocupada y la siembra Activa (RNF-02)")
        void cosechaRechazadaNoCambiaEstados() {
            // Arrange
            Sistema sistema = escenarioConSiembra();
            // Act
            assertThrows(NoEncontradoException.class,
                    () -> sistema.cosechas().registrar(cosechaValida().conResponsable("100000000")));
            // Assert
            assertEquals("Ocupada", sistema.secciones().obtener("SEC-01").getEstado());
            assertEquals("Activa", sistema.siembras().obtener("SIE-01").getEstado());
            assertTrue(sistema.cosechas().listar().isEmpty());
        }
    }
}
