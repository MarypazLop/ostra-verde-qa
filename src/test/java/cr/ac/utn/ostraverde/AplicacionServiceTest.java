package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cr.ac.utn.ostraverde.Fabrica.aplicacionValida;
import static cr.ac.utn.ostraverde.Fabrica.escenarioConSiembra;
import static cr.ac.utn.ostraverde.Fabrica.kg;
import static cr.ac.utn.ostraverde.Fabrica.trabajadorValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo APL — Registro de aplicaciones (RF-13)")
class AplicacionServiceTest {

    @Test
    @DisplayName("CP-APL-01 · Registrar aplicación válida sobre una siembra activa")
    void registrarAplicacionValida() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        Aplicacion aplicacion = sistema.aplicaciones().registrar(aplicacionValida());
        // Assert
        assertEquals("APL-01", aplicacion.getCodigo());
        assertEquals("SIE-01", aplicacion.getSiembra());
        assertEquals("PRO-01", aplicacion.getProducto());
        assertEquals(kg("2.5"), aplicacion.getCantidad());
        assertEquals("kg", aplicacion.getUnidad());
    }

    @Test
    @DisplayName("CP-APL-02 · Rechazar aplicación sobre siembra inexistente (RN-10)")
    void rechazarSiembraInexistente() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conSiembra("SIE-99")));
        // Assert
        assertEquals("La siembra \"SIE-99\" no existe", error.getMessage());
    }

    @Test
    @DisplayName("CP-APL-03 · Rechazar aplicación con producto inexistente (RN-11)")
    void rechazarProductoInexistente() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conProducto("PRO-99")));
        // Assert
        assertEquals("El producto \"PRO-99\" no existe", error.getMessage());
    }

    @Test
    @DisplayName("CP-APL-04 · Rechazar aplicación con un producto que fue eliminado (RN-11, límite)")
    void rechazarProductoEliminado() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        sistema.productos().eliminar("PRO-01");
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conProducto("PRO-01")));
        // Assert
        assertEquals("El producto \"PRO-01\" no existe", error.getMessage());
    }

    @Test
    @DisplayName("CP-APL-05 · Rechazar cantidad aplicada igual a 0 (RN-12, límite inválido)")
    void rechazarCantidadCero() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conCantidad(kg("0"))));
        // Assert
        assertEquals("La cantidad aplicada debe ser mayor que cero", error.getMessage());
    }

    @Test
    @DisplayName("CP-APL-06 · Aceptar cantidad aplicada igual a 0.01 (RN-12, límite válido)")
    void aceptarCantidadMinima() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        Aplicacion aplicacion = sistema.aplicaciones().registrar(aplicacionValida().conCantidad(kg("0.01")));
        // Assert
        assertEquals(kg("0.01"), aplicacion.getCantidad());
    }

    @Test
    @DisplayName("CP-APL-07 · Rechazar cantidad aplicada negativa (RN-12)")
    void rechazarCantidadNegativa() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conCantidad(kg("-1"))));
        // Assert
        assertEquals("RN-12", error.getRegla());
    }

    @Test
    @DisplayName("CP-APL-08 · Rechazar aplicación sobre un cultivo ya cosechado (RN-13)")
    void rechazarCultivoCosechado() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        sistema.siembras().marcarCosechada("SIE-01");
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conFecha("10/10/2026")));
        // Assert
        assertEquals("La siembra \"SIE-01\" ya fue cosechada; no admite aplicaciones", error.getMessage());
    }

    @Test
    @DisplayName("CP-APL-09 · Rechazar aplicación el mismo día de la cosecha, ya cosechado (RN-13, límite)")
    void rechazarMismoDiaCosecha() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        sistema.siembras().marcarCosechada("SIE-01"); // cosechada el 14/10/2026
        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conFecha("14/10/2026")));
        // Assert
        assertEquals("RN-13", error.getRegla());
        assertTrue(sistema.aplicaciones().listar().isEmpty());
    }

    @Test
    @DisplayName("CP-APL-10 · Rechazar aplicación con responsable inexistente (RES-06)")
    void rechazarResponsableInexistente() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        // Act
        NoEncontradoException error = assertThrows(NoEncontradoException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conResponsable("100000000")));
        // Assert
        assertEquals("Trabajador con código/identificación \"100000000\" no existe", error.getMessage());
    }

    @Test
    @DisplayName("CP-APL-11 · Rechazar código de aplicación duplicado (RES-10)")
    void rechazarCodigoDuplicado() {
        // Arrange
        Sistema sistema = escenarioConSiembra();
        sistema.trabajadores().registrar(trabajadorValido().conIdentificacion("301230456"));
        sistema.aplicaciones().registrar(aplicacionValida());
        // Act
        DuplicadoException error = assertThrows(DuplicadoException.class,
                () -> sistema.aplicaciones().registrar(aplicacionValida().conResponsable("301230456")));
        // Assert
        assertEquals("Ya existe una aplicación con el código \"APL-01\"", error.getMessage());
    }
}
