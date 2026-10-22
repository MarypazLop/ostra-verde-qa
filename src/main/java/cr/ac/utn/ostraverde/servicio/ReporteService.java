package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.modelo.Trabajador;
import cr.ac.utn.ostraverde.modelo.reporte.FilaHistorial;
import cr.ac.utn.ostraverde.modelo.reporte.FilaProduccion;
import cr.ac.utn.ostraverde.modelo.reporte.FilaRendimiento;
import cr.ac.utn.ostraverde.modelo.reporte.ResumenPeriodo;
import cr.ac.utn.ostraverde.repositorio.BaseDatos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Paso 7 — Reportes gerenciales (RF-16 a RF-20). — esqueleto: se implementa con TDD. */
public class ReporteService {

    public ReporteService(Contexto ctx) {
    }

    public ResumenPeriodo produccionPorPeriodo(String desde, String hasta) {
        throw new UnsupportedOperationException("ReporteService.produccionPorPeriodo: No implementado");
    }

    public List<FilaProduccion> produccionPorSeccion() {
        throw new UnsupportedOperationException("ReporteService.produccionPorSeccion: No implementado");
    }

    public List<FilaProduccion> produccionPorTrabajador() {
        throw new UnsupportedOperationException("ReporteService.produccionPorTrabajador: No implementado");
    }

    public List<FilaHistorial> historialAplicaciones() {
        throw new UnsupportedOperationException("ReporteService.historialAplicaciones: No implementado");
    }

    public List<FilaRendimiento> rendimientoCultivos() {
        throw new UnsupportedOperationException("ReporteService.rendimientoCultivos: No implementado");
    }
}
