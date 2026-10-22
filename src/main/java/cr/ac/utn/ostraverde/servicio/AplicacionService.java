package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import java.math.BigDecimal;
import java.util.List;

/** Paso 5 — Registro de aplicaciones de productos (RF-13). — esqueleto: se implementa con TDD. */
public class AplicacionService {

    public AplicacionService(Contexto ctx) {
    }

    public Aplicacion registrar(Aplicacion datos) {
        throw new UnsupportedOperationException("AplicacionService.registrar: No implementado");
    }

    public List<Aplicacion> listar() {
        throw new UnsupportedOperationException("AplicacionService.listar: No implementado");
    }
}
