package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import java.util.List;

/** Paso 4 — Gestión de productos agrícolas (RF-09 a RF-12). — esqueleto: se implementa con TDD. */
public class ProductoService {

    public ProductoService(Contexto ctx) {
    }

    public Producto registrar(Producto datos) {
        throw new UnsupportedOperationException("ProductoService.registrar: No implementado");
    }

    public Producto modificar(String codigoBuscado, Producto cambios) {
        throw new UnsupportedOperationException("ProductoService.modificar: No implementado");
    }

    public Producto consultar(String codigo) {
        throw new UnsupportedOperationException("ProductoService.consultar: No implementado");
    }

    public List<Producto> listar() {
        throw new UnsupportedOperationException("ProductoService.listar: No implementado");
    }

    public boolean eliminar(String codigoBuscado) {
        throw new UnsupportedOperationException("ProductoService.eliminar: No implementado");
    }
}
