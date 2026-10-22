package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Trabajador;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import java.util.List;

/** Paso 2 — Registro de personal (RF-04, RF-05, RF-06). — esqueleto: se implementa con TDD. */
public class TrabajadorService {

    public TrabajadorService(Contexto ctx) {
    }

    public Trabajador registrar(Trabajador datos) {
        throw new UnsupportedOperationException("TrabajadorService.registrar: No implementado");
    }

    public Trabajador consultar(String identificacion) {
        throw new UnsupportedOperationException("TrabajadorService.consultar: No implementado");
    }

    public List<Trabajador> listar() {
        throw new UnsupportedOperationException("TrabajadorService.listar: No implementado");
    }

    public Trabajador actualizar(String identificacion, Trabajador cambios) {
        throw new UnsupportedOperationException("TrabajadorService.actualizar: No implementado");
    }
}
