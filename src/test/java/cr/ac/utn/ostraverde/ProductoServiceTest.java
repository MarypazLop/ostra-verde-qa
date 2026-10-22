package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.servicio.Sistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static cr.ac.utn.ostraverde.Fabrica.productoValido;
import static cr.ac.utn.ostraverde.Fabrica.sistemaDePrueba;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Módulo PRO — Gestión de productos agrícolas (RF-09 a RF-12)")
class ProductoServiceTest {

    @Nested
    @DisplayName("RF-09 Registrar producto")
    class RegistrarProducto {

        @Test
        @DisplayName("CP-PRO-01 · Registrar producto con datos válidos")
        void registrarProductoValido() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            Producto producto = sistema.productos().registrar(productoValido());
            // Assert
            assertEquals("PRO-01", producto.getCodigo());
            assertEquals("Cal agrícola", producto.getNombre());
            assertEquals("kg", producto.getUnidad());
        }

        @Test
        @DisplayName("CP-PRO-02 · Rechazar producto con código duplicado (RES-10)")
        void rechazarCodigoDuplicado() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.productos().registrar(productoValido());
            // Act
            DuplicadoException error = assertThrows(DuplicadoException.class,
                    () -> sistema.productos().registrar(productoValido().conNombre("Yeso")));
            // Assert
            assertEquals("Ya existe un producto con el código \"PRO-01\"", error.getMessage());
        }

        @Test
        @DisplayName("CP-PRO-03 · Rechazar producto sin unidad de medida (RES-07)")
        void rechazarSinUnidad() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            ValidacionException error = assertThrows(ValidacionException.class,
                    () -> sistema.productos().registrar(productoValido().conUnidad("")));
            // Assert
            assertEquals("El campo \"unidad\" es obligatorio", error.getMessage());
        }

        @Test
        @DisplayName("CP-PRO-04 · Aceptar producto sin descripción (RES-07, campo opcional)")
        void aceptarSinDescripcion() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            Producto producto = sistema.productos().registrar(productoValido().conDescripcion(""));
            // Assert
            assertEquals("", producto.getDescripcion());
        }
    }

    @Nested
    @DisplayName("RF-10 Modificar producto")
    class ModificarProducto {

        @Test
        @DisplayName("CP-PRO-05 · Modificar nombre y unidad de un producto existente")
        void modificarProducto() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.productos().registrar(productoValido());
            // Act
            sistema.productos().modificar("PRO-01", new Producto().conNombre("Cal dolomita").conUnidad("g"));
            // Assert
            Producto guardado = sistema.productos().consultar("PRO-01");
            assertEquals("Cal dolomita", guardado.getNombre());
            assertEquals("g", guardado.getUnidad());
        }

        @Test
        @DisplayName("CP-PRO-06 · Rechazar modificación de producto inexistente")
        void rechazarModificarInexistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.productos().modificar("PRO-99", new Producto().conNombre("X")));
            // Assert
            assertEquals("Producto con código/identificación \"PRO-99\" no existe", error.getMessage());
        }
    }

    @Nested
    @DisplayName("RF-11 Consultar productos")
    class ConsultarProductos {

        @Test
        @DisplayName("CP-PRO-07 · Consultar producto por código existente")
        void consultarExistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.productos().registrar(productoValido());
            sistema.productos().registrar(productoValido().conCodigo("PRO-02").conNombre("Peróxido"));
            // Act
            Producto producto = sistema.productos().consultar("pro-02");
            // Assert
            assertEquals("Peróxido", producto.getNombre());
            assertEquals(2, sistema.productos().listar().size());
        }

        @Test
        @DisplayName("CP-PRO-08 · Consultar código inexistente indica \"no existe\"")
        void consultarInexistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.productos().consultar("PRO-404"));
            // Assert
            assertTrue(error.getMessage().endsWith("no existe"));
        }
    }

    @Nested
    @DisplayName("RF-12 Eliminar producto no utilizado")
    class EliminarProducto {

        @Test
        @DisplayName("CP-PRO-09 · Eliminar producto que nunca se utilizó")
        void eliminarNoUtilizado() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.productos().registrar(productoValido());
            // Act
            boolean eliminado = sistema.productos().eliminar("PRO-01");
            // Assert
            assertTrue(eliminado);
            assertTrue(sistema.productos().listar().isEmpty());
        }

        @Test
        @DisplayName("CP-PRO-10 · Rechazar eliminación de producto con exactamente 1 aplicación (RN-09, límite)")
        void rechazarEliminarUtilizado() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            sistema.productos().registrar(productoValido());
            // Se precarga una aplicación directamente en el repositorio para aislar el módulo PRO
            sistema.ctx().getBd().aplicaciones().guardar("APL-01",
                    new Aplicacion().conCodigo("APL-01").conProducto("PRO-01").conCantidad(BigDecimal.ONE));
            // Act
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> sistema.productos().eliminar("PRO-01"));
            // Assert
            assertEquals("RN-09", error.getRegla());
            assertNotNull(sistema.productos().consultar("PRO-01"));
        }

        @Test
        @DisplayName("CP-PRO-11 · Rechazar eliminación de producto inexistente")
        void rechazarEliminarInexistente() {
            // Arrange
            Sistema sistema = sistemaDePrueba();
            // Act
            NoEncontradoException error = assertThrows(NoEncontradoException.class,
                    () -> sistema.productos().eliminar("PRO-77"));
            // Assert
            assertEquals("Producto con código/identificación \"PRO-77\" no existe", error.getMessage());
        }
    }
}
