package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.reporte.FilaHistorial;
import cr.ac.utn.ostraverde.modelo.reporte.FilaProduccion;
import cr.ac.utn.ostraverde.modelo.reporte.FilaRendimiento;
import cr.ac.utn.ostraverde.modelo.reporte.ResumenPeriodo;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.utn.ostraverde.Fabrica.aplicacionValida;
import static cr.ac.utn.ostraverde.Fabrica.cosechaValida;
import static cr.ac.utn.ostraverde.Fabrica.kg;
import static cr.ac.utn.ostraverde.Fabrica.productoValido;
import static cr.ac.utn.ostraverde.Fabrica.seccionValida;
import static cr.ac.utn.ostraverde.Fabrica.siembraValida;
import static cr.ac.utn.ostraverde.Fabrica.sistemaDePrueba;
import static cr.ac.utn.ostraverde.Fabrica.trabajadorValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo REP — Reportes gerenciales (RF-16 a RF-20)")
class ReporteServiceTest {

    private static final String ANA = "205550111";
    private static final String LUIS = "301230456";

    /**
     * Escenario de producción de setiembre-octubre 2026:
     * COS-01 20/09 SIE-01 (SEC-01) 45.75 kg / 90 bandejas — Ana
     * COS-02 30/09 SIE-02 (SEC-02) 20.00 kg / 40 bandejas — Luis
     * COS-03 01/10 SIE-03 (SEC-01) 30.10 kg / 60 bandejas — Luis
     * SIE-04 (SEC-02) sembrada el 02/10, todavía sin cosechar
     */
    private static Sistema escenarioProduccion() {
        Sistema s = sistemaDePrueba();
        for (String codigo : List.of("SEC-01", "SEC-02", "SEC-03")) {
            s.secciones().registrar(seccionValida().conCodigo(codigo));
        }
        s.trabajadores().registrar(trabajadorValido().conIdentificacion(ANA));
        s.trabajadores().registrar(trabajadorValido().conIdentificacion(LUIS).conNombre("Luis Vargas Solís"));
        s.productos().registrar(productoValido().conCodigo("PRO-01"));
        s.productos().registrar(productoValido().conCodigo("PRO-02").conNombre("Peróxido de hidrógeno").conUnidad("L"));

        s.siembras().registrar(siembraValida().conCodigo("SIE-01").conSeccion("SEC-01").conFecha("01/09/2026").conSustratos(120).conResponsable(ANA));
        s.siembras().registrar(siembraValida().conCodigo("SIE-02").conSeccion("SEC-02").conFecha("05/09/2026").conSustratos(80).conResponsable(LUIS));
        s.aplicaciones().registrar(aplicacionValida().conCodigo("APL-02").conFecha("10/09/2026").conSiembra("SIE-01").conProducto("PRO-01").conCantidad(kg("2")));
        s.aplicaciones().registrar(aplicacionValida().conCodigo("APL-03").conFecha("10/09/2026").conSiembra("SIE-02").conProducto("PRO-02").conCantidad(kg("1.5")));
        s.aplicaciones().registrar(aplicacionValida().conCodigo("APL-01").conFecha("15/09/2026").conSiembra("SIE-02").conProducto("PRO-01").conCantidad(kg("3")));

        s.cosechas().registrar(cosechaValida().conCodigo("COS-01").conFecha("20/09/2026").conSiembra("SIE-01").conPesoKg(kg("45.75")).conBandejas(90).conResponsable(ANA));
        s.siembras().registrar(siembraValida().conCodigo("SIE-03").conSeccion("SEC-01").conFecha("21/09/2026").conSustratos(100).conResponsable(ANA));
        s.cosechas().registrar(cosechaValida().conCodigo("COS-02").conFecha("30/09/2026").conSiembra("SIE-02").conPesoKg(kg("20.00")).conBandejas(40).conResponsable(LUIS));
        s.cosechas().registrar(cosechaValida().conCodigo("COS-03").conFecha("01/10/2026").conSiembra("SIE-03").conPesoKg(kg("30.10")).conBandejas(60).conResponsable(LUIS));
        s.siembras().registrar(siembraValida().conCodigo("SIE-04").conSeccion("SEC-02").conFecha("02/10/2026").conSustratos(50).conResponsable(ANA));
        return s;
    }

    @Nested
    @DisplayName("RF-16 Producción total por período")
    class ProduccionPorPeriodo {

        @Test
        @DisplayName("CP-REP-01 · Sumar todas las cosechas dentro del período")
        void sumarCosechasDelPeriodo() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            ResumenPeriodo reporte = sistema.reportes().produccionPorPeriodo("01/09/2026", "31/10/2026");
            // Assert
            assertEquals(kg("95.85"), reporte.kg());
            assertEquals(190, reporte.bandejas());
            assertEquals(3, reporte.cosechas());
        }

        @Test
        @DisplayName("CP-REP-02 · Incluir cosechas en las fechas límite del período y excluir las de fuera (límite)")
        void incluirFechasLimite() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            ResumenPeriodo reporte = sistema.reportes().produccionPorPeriodo("20/09/2026", "30/09/2026");
            // Assert
            assertEquals(kg("65.75"), reporte.kg());
            assertEquals(130, reporte.bandejas());
            assertEquals(2, reporte.cosechas());
        }

        @Test
        @DisplayName("CP-REP-03 · Rechazar período con fecha inicial posterior a la final")
        void rechazarPeriodoInvertido() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.reportes().produccionPorPeriodo("31/10/2026", "01/10/2026"));
            // Assert
            assertEquals("La fecha inicial del período no puede ser posterior a la fecha final", error.getMessage());
        }

        @Test
        @DisplayName("CP-REP-04 · Período sin cosechas devuelve totales en cero (límite)")
        void periodoSinCosechas() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            ResumenPeriodo reporte = sistema.reportes().produccionPorPeriodo("01/01/2026", "31/01/2026");
            // Assert
            assertEquals(kg("0.00"), reporte.kg());
            assertEquals(0, reporte.bandejas());
            assertEquals(0, reporte.cosechas());
        }
    }

    @Nested
    @DisplayName("RF-17 Producción por sección")
    class ProduccionPorSeccion {

        @Test
        @DisplayName("CP-REP-05 · Totalizar kg y bandejas por sección")
        void totalizarPorSeccion() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            List<FilaProduccion> reporte = sistema.reportes().produccionPorSeccion();
            // Assert
            assertEquals(new FilaProduccion("SEC-01", kg("75.85"), 150, 2), reporte.get(0));
            assertEquals(new FilaProduccion("SEC-02", kg("20.00"), 40, 1), reporte.get(1));
        }

        @Test
        @DisplayName("CP-REP-06 · Sección sin cosechas aparece con producción 0 (límite)")
        void seccionSinCosechas() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            List<FilaProduccion> reporte = sistema.reportes().produccionPorSeccion();
            // Assert
            assertEquals(new FilaProduccion("SEC-03", kg("0.00"), 0, 0), reporte.get(2));
        }
    }

    @Nested
    @DisplayName("RF-18 Producción por trabajador")
    class ProduccionPorTrabajador {

        @Test
        @DisplayName("CP-REP-07 · Totalizar producción por responsable de la cosecha")
        void totalizarPorTrabajador() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            List<FilaProduccion> reporte = sistema.reportes().produccionPorTrabajador();
            // Assert
            assertEquals(List.of(
                    new FilaProduccion(LUIS, kg("50.10"), 100, 2),
                    new FilaProduccion(ANA, kg("45.75"), 90, 1)), reporte);
        }

        @Test
        @DisplayName("CP-REP-08 · Trabajador sin cosechas aparece con producción 0 (límite)")
        void trabajadorSinCosechas() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            sistema.trabajadores().registrar(trabajadorValido().conIdentificacion("402220333").conNombre("Sofía Ulate"));
            // Act
            List<FilaProduccion> reporte = sistema.reportes().produccionPorTrabajador();
            // Assert
            assertTrue(reporte.contains(new FilaProduccion("402220333", kg("0.00"), 0, 0)));
        }
    }

    @Nested
    @DisplayName("RF-19 Historial de aplicaciones")
    class HistorialAplicaciones {

        @Test
        @DisplayName("CP-REP-09 · Listar aplicaciones ordenadas por fecha y luego por código")
        void listarOrdenado() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            List<FilaHistorial> historial = sistema.reportes().historialAplicaciones();
            // Assert
            assertEquals(List.of("APL-02", "APL-03", "APL-01"), historial.stream().map(FilaHistorial::codigo).toList());
            FilaHistorial segunda = historial.get(1);
            assertEquals("SIE-02", segunda.siembra());
            assertEquals("PRO-02", segunda.producto());
            assertEquals(kg("1.5"), segunda.cantidad());
            assertEquals("L", segunda.unidad());
        }

        @Test
        @DisplayName("CP-REP-10 · Historial sin aplicaciones devuelve lista vacía (límite)")
        void historialVacio() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            List<FilaHistorial> historial = sistema.reportes().historialAplicaciones();
            // Assert
            assertTrue(historial.isEmpty());
        }
    }

    @Nested
    @DisplayName("RF-20 Rendimiento de cultivos")
    class RendimientoCultivos {

        @Test
        @DisplayName("CP-REP-11 · Calcular kg por sustrato de cada siembra cosechada (S-01)")
        void calcularRendimiento() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            List<FilaRendimiento> rendimiento = sistema.reportes().rendimientoCultivos();
            // Assert
            assertEquals(new FilaRendimiento("SIE-01", kg("45.75"), 120, kg("0.38")), rendimiento.get(0));
            assertEquals(kg("0.25"), rendimiento.get(1).kgPorSustrato());
        }

        @Test
        @DisplayName("CP-REP-12 · Excluir del rendimiento las siembras no cosechadas (límite)")
        void excluirNoCosechadas() {
            // Arrange
            Sistema sistema = escenarioProduccion();
            // Act
            List<FilaRendimiento> rendimiento = sistema.reportes().rendimientoCultivos();
            // Assert
            assertEquals(List.of("SIE-01", "SIE-02", "SIE-03"),
                    rendimiento.stream().map(FilaRendimiento::siembra).sorted().toList());
        }
    }
}
