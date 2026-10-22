package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import java.util.List;

/** Paso 1 — Gestión de secciones de producción (RF-01, RF-02, RF-03). — esqueleto: se implementa con TDD. */
public class SeccionService {

    public SeccionService(Contexto ctx) {
    }

    public Seccion registrar(Seccion datos) {
        throw new UnsupportedOperationException("SeccionService.registrar: No implementado");
    }

    public Seccion modificar(String codigoBuscado, Seccion cambios) {
        throw new UnsupportedOperationException("SeccionService.modificar: No implementado");
    }

    public Seccion obtener(String codigo) {
        throw new UnsupportedOperationException("SeccionService.obtener: No implementado");
    }

    public List<Seccion> consultarDisponibles() {
        throw new UnsupportedOperationException("SeccionService.consultarDisponibles: No implementado");
    }

    public Seccion cambiarEstado(String codigo, String estado) {
        throw new UnsupportedOperationException("SeccionService.cambiarEstado: No implementado");
    }
}
