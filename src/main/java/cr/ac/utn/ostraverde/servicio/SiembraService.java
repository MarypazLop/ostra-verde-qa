package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import java.time.LocalDate;
import java.util.List;

/** Paso 3 — Registro de siembras (RF-07, RF-08). — esqueleto: se implementa con TDD. */
public class SiembraService {

    public SiembraService(Contexto ctx, SeccionService secciones) {
    }

    public Siembra registrar(Siembra datos) {
        throw new UnsupportedOperationException("SiembraService.registrar: No implementado");
    }

    public Siembra obtener(String codigo) {
        throw new UnsupportedOperationException("SiembraService.obtener: No implementado");
    }

    public List<Siembra> listar() {
        throw new UnsupportedOperationException("SiembraService.listar: No implementado");
    }

    public Siembra marcarCosechada(String codigo) {
        throw new UnsupportedOperationException("SiembraService.marcarCosechada: No implementado");
    }
}
