package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Paso 6 — Registro de cosechas (RF-14, RF-15). — esqueleto: se implementa con TDD. */
public class CosechaService {

    public CosechaService(Contexto ctx, SiembraService siembras, SeccionService secciones) {
    }

    public Cosecha registrar(Cosecha datos) {
        throw new UnsupportedOperationException("CosechaService.registrar: No implementado");
    }

    public List<Cosecha> listar() {
        throw new UnsupportedOperationException("CosechaService.listar: No implementado");
    }
}
