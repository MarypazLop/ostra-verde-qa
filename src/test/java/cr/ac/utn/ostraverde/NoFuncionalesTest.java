package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.AccesoDenegadoException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Auditoria;
import cr.ac.utn.ostraverde.modelo.Copiable;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Sesion;
import cr.ac.utn.ostraverde.modelo.reporte.ResumenPeriodo;
import cr.ac.utn.ostraverde.repositorio.BaseDatos;
import cr.ac.utn.ostraverde.repositorio.RepositorioMemoria;
import cr.ac.utn.ostraverde.servicio.Contexto;
import cr.ac.utn.ostraverde.servicio.Sistema;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static cr.ac.utn.ostraverde.Fabrica.RELOJ_FIJO;
import static cr.ac.utn.ostraverde.Fabrica.cosechaValida;
import static cr.ac.utn.ostraverde.Fabrica.escenarioBase;
import static cr.ac.utn.ostraverde.Fabrica.escenarioConSiembra;
import static cr.ac.utn.ostraverde.Fabrica.kg;
import static cr.ac.utn.ostraverde.Fabrica.seccionValida;
import static cr.ac.utn.ostraverde.Fabrica.siembraValida;
import static cr.ac.utn.ostraverde.Fabrica.sistemaDePrueba;
import static cr.ac.utn.ostraverde.Fabrica.sistemaSinSesion;
import static cr.ac.utn.ostraverde.Fabrica.trabajadorValido;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Requerimientos no funcionales verificables con pruebas unitarias")
class NoFuncionalesTest {

    /** Doble de prueba (espía) escrito a mano: registra cada llamada a guardar(). */
    static class RepositorioEspia<T extends Copiable<T>> extends RepositorioMemoria<T> {
        final List<String> clavesGuardadas = new ArrayList<>();

        @Override
        public T guardar(String clave, T entidad) {
            clavesGuardadas.add(clave);
            return super.guardar(clave, entidad);
        }
    }

    @Test
    @DisplayName("CP-NF-01 · RNF-01 Todos los módulos guardan en una única base de datos centralizada")
    void baseDatosCentralizada() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        sistema.cosechas().registrar(cosechaValida());
        BaseDatos bd = sistema.ctx().getBd();
        // Assert
        assertEquals("Disponible", bd.secciones().obtener("SEC-01").orElseThrow().getEstado());
        assertEquals(List.of(1, 1, 1, 1, 1), List.of(bd.secciones().contar(), bd.trabajadores().contar(),
                bd.productos().contar(), bd.siembras().contar(), bd.cosechas().contar()));
    }

    @Test
    @DisplayName("CP-NF-02 · RNF-03 El mensaje de rechazo identifica el campo y la regla incumplida")
    void mensajeIdentificaCampo() {
        // Arrange
        Sistema sistema = escenarioBase();
        // Act
        ValidacionException error = assertThrows(ValidacionException.class,
                () -> sistema.trabajadores().registrar(trabajadorValido().conIdentificacion("999").conTelefono("12")));
        // Assert
        assertEquals("telefono", error.getCampo());
        assertEquals("El campo \"telefono\" debe contener exactamente 8 dígitos", error.getMessage());
    }

    @Test
    @DisplayName("CP-NF-03 · RNF-04 Consultas responden en 2 s o menos con 5 000 secciones")
    void consultasEnDosSegundos() {
        // Arrange
        Sistema sistema = sistemaDePrueba();
        for (int i = 1; i <= 5000; i++) {
            sistema.secciones().registrar(seccionValida().conCodigo("SEC-" + i).conEstado(i % 2 == 1 ? "Disponible" : "Ocupada"));
        }
        // Act
        List<Seccion> disponibles = assertTimeout(Duration.ofSeconds(2), () -> {
            List<Seccion> resultado = sistema.secciones().consultarDisponibles();
            sistema.secciones().obtener("SEC-4999");
            return resultado;
        });
        // Assert
        assertEquals(2500, disponibles.size());
    }

    @Test
    @DisplayName("CP-NF-04 · RNF-05 Reporte de 12 meses se genera en 5 s o menos con 5 000 cosechas")
    void reporteEnCincoSegundos() {
        // Arrange
        Sistema sistema = sistemaDePrueba();
        for (int i = 1; i <= 5000; i++) {
            String mes = String.format("%02d", (i % 12) + 1);
            sistema.ctx().getBd().cosechas().guardar("COS-" + i, new Cosecha().conCodigo("COS-" + i)
                    .conFecha("15/" + mes + "/2026").conSeccion("SEC-01").conPesoKg(kg("1.25")).conBandejas(2).conResponsable("1"));
        }
        // Act
        ResumenPeriodo reporte = assertTimeout(Duration.ofSeconds(5),
                () -> sistema.reportes().produccionPorPeriodo("01/01/2026", "31/12/2026"));
        // Assert
        assertEquals(5000, reporte.cosechas());
        assertEquals(kg("6250.00"), reporte.kg());
        assertEquals(10000, reporte.bandejas());
    }

    @Test
    @DisplayName("CP-NF-05 · RNF-06 Una escritura sin sesión iniciada se rechaza y no guarda nada")
    void escrituraSinSesion() {
        // Arrange
        Sistema sistema = sistemaSinSesion();
        // Act
        assertThrows(AccesoDenegadoException.class, () -> sistema.secciones().registrar(seccionValida()));
        // Assert
        assertEquals(0, sistema.ctx().getBd().secciones().contar());
    }

    @Test
    @DisplayName("CP-NF-06 · RNF-06 Las consultas no requieren sesión")
    void consultaSinSesion() {
        // Arrange
        Sistema sistema = sistemaSinSesion();
        // Act
        List<Seccion> disponibles = assertDoesNotThrow(() -> sistema.secciones().consultarDisponibles());
        // Assert
        assertTrue(disponibles.isEmpty());
    }

    @Test
    @DisplayName("CP-NF-07 · RNF-07 Los totales en kg no acumulan error de redondeo (0.1 + 0.2 = 0.30)")
    void totalesSinErrorDeRedondeo() {
        // Arrange
        Sistema sistema = escenarioBase();
        sistema.secciones().registrar(seccionValida().conCodigo("SEC-02"));
        sistema.siembras().registrar(siembraValida().conCodigo("SIE-01").conSeccion("SEC-01"));
        sistema.siembras().registrar(siembraValida().conCodigo("SIE-02").conSeccion("SEC-02"));
        sistema.cosechas().registrar(cosechaValida().conCodigo("COS-01").conSiembra("SIE-01").conPesoKg(kg("0.1")));
        sistema.cosechas().registrar(cosechaValida().conCodigo("COS-02").conSiembra("SIE-02").conPesoKg(kg("0.2")));
        // Act
        ResumenPeriodo reporte = sistema.reportes().produccionPorPeriodo("01/10/2026", "31/10/2026");
        // Assert
        assertNotEquals(0.3, 0.1 + 0.2); // con double la suma da 0.30000000000000004
        assertEquals(kg("0.30"), reporte.kg());
    }

    @Test
    @DisplayName("CP-NF-08 · RNF-08 La lógica se prueba aislada con un doble de prueba del repositorio")
    void logicaAisladaConDoble() {
        // Arrange
        RepositorioEspia<Seccion> espia = new RepositorioEspia<>();
        BaseDatos bd = new BaseDatos(espia, new RepositorioMemoria<>(), new RepositorioMemoria<>(),
                new RepositorioMemoria<>(), new RepositorioMemoria<>(), new RepositorioMemoria<>());
        Sistema sistema = Sistema.crear(new Contexto(bd, RELOJ_FIJO, new Sesion("qa")));
        // Act
        sistema.secciones().registrar(seccionValida());
        // Assert
        assertEquals(List.of("SEC-01"), espia.clavesGuardadas);
        assertEquals("Disponible", espia.obtener("SEC-01").orElseThrow().getEstado());
    }

    @Test
    @DisplayName("CP-NF-09 · RNF-10 Cada registro guarda el usuario y la fecha de creación")
    void auditoriaAlCrear() {
        // Arrange
        Sistema sistema = sistemaDePrueba("marypaz");
        // Act
        Seccion seccion = sistema.secciones().registrar(seccionValida());
        // Assert
        assertEquals(new Auditoria("marypaz", "2026-10-15T12:00:00Z"), seccion.getAuditoria());
    }

    @Test
    @DisplayName("CP-NF-10 · RNF-10 Una modificación actualiza el usuario de la auditoría")
    void auditoriaAlModificar() {
        // Arrange
        Sistema sistema = sistemaDePrueba("marypaz");
        sistema.secciones().registrar(seccionValida());
        sistema.ctx().setSesion(new Sesion("belem"));
        // Act
        Seccion modificada = sistema.secciones().modificar("SEC-01", new Seccion().conCapacidad(700));
        // Assert
        assertEquals("belem", modificada.getAuditoria().usuario());
    }
}
