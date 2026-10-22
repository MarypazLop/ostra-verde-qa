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
import cr.ac.utn.ostraverde.util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Paso 7 — Reportes gerenciales (RF-16 a RF-20). */
public class ReporteService {

    private final BaseDatos bd;

    public ReporteService(Contexto ctx) {
        this.bd = ctx.getBd();
    }

    /** Suma exacta con BigDecimal y dos decimales (RNF-07). */
    private static BigDecimal sumarKg(List<Cosecha> cosechas) {
        return cosechas.stream().map(Cosecha::getPesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static int sumarBandejas(List<Cosecha> cosechas) {
        return cosechas.stream().mapToInt(Cosecha::getBandejas).sum();
    }

    /** RF-16: producción total en un período [desde, hasta], ambos inclusive. */
    public ResumenPeriodo produccionPorPeriodo(String desde, String hasta) {
        LocalDate inicio = Validaciones.parsearFecha(desde, "desde");
        LocalDate fin = Validaciones.parsearFecha(hasta, "hasta");
        if (inicio.isAfter(fin)) {
            throw new ValidacionException("desde", "La fecha inicial del período no puede ser posterior a la fecha final");
        }
        List<Cosecha> enPeriodo = bd.cosechas().listar().stream().filter(c -> {
            LocalDate fecha = Validaciones.parsearFecha(c.getFecha(), "fecha");
            return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
        }).toList();
        return new ResumenPeriodo(desde, hasta, sumarKg(enPeriodo), sumarBandejas(enPeriodo), enPeriodo.size());
    }

    private List<FilaProduccion> agruparPor(List<String> claves, Function<Cosecha, String> obtenerClave) {
        List<Cosecha> cosechas = bd.cosechas().listar();
        return claves.stream()
                .map(clave -> {
                    List<Cosecha> grupo = cosechas.stream().filter(c -> clave.equals(obtenerClave.apply(c))).toList();
                    return new FilaProduccion(clave, sumarKg(grupo), sumarBandejas(grupo), grupo.size());
                })
                .sorted(Comparator.comparing(FilaProduccion::kg).reversed().thenComparing(FilaProduccion::clave))
                .toList();
    }

    /** RF-17: producción por sección (incluye secciones sin cosechas, en 0). */
    public List<FilaProduccion> produccionPorSeccion() {
        return agruparPor(bd.secciones().listar().stream().map(Seccion::getCodigo).toList(), Cosecha::getSeccion);
    }

    /** RF-18: producción por trabajador responsable de la cosecha (supuesto S-07). */
    public List<FilaProduccion> produccionPorTrabajador() {
        return agruparPor(bd.trabajadores().listar().stream().map(Trabajador::getIdentificacion).toList(),
                Cosecha::getResponsable);
    }

    /** RF-19: historial de aplicaciones ordenado por fecha y luego por código. */
    public List<FilaHistorial> historialAplicaciones() {
        return bd.aplicaciones().listar().stream()
                .sorted(Comparator.comparing((Aplicacion a) ->
                                Validaciones.parsearFecha(a.getFecha(), "fecha"))
                        .thenComparing(Aplicacion::getCodigo))
                .map(a -> new FilaHistorial(a.getCodigo(), a.getFecha(), a.getSiembra(), a.getProducto(),
                        a.getCantidad(), a.getUnidad(), a.getResponsable()))
                .toList();
    }

    /** RF-20: rendimiento = kg cosechados / sustratos sembrados (supuesto S-01). */
    public List<FilaRendimiento> rendimientoCultivos() {
        Map<String, Siembra> siembras = bd.siembras().listar().stream()
                .collect(Collectors.toMap(Siembra::getCodigo, Function.identity()));
        return bd.cosechas().listar().stream().map(c -> {
            Siembra siembra = siembras.get(c.getSiembra());
            BigDecimal kgPorSustrato = c.getPesoKg()
                    .divide(BigDecimal.valueOf(siembra.getSustratos()), 2, RoundingMode.HALF_UP);
            return new FilaRendimiento(c.getSiembra(), c.getPesoKg(), siembra.getSustratos(), kgPorSustrato);
        }).toList();
    }
}
