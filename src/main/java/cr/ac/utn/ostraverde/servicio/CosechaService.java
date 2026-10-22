package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import cr.ac.utn.ostraverde.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Paso 6 — Registro de cosechas (RF-14, RF-15). */
public class CosechaService {

    private final Contexto ctx;
    private final Repositorio<Cosecha> repo;
    private final SiembraService siembras;
    private final SeccionService secciones;

    public CosechaService(Contexto ctx, SiembraService siembras, SeccionService secciones) {
        this.ctx = ctx;
        this.repo = ctx.getBd().cosechas();
        this.siembras = siembras;
        this.secciones = secciones;
    }

    public Cosecha registrar(Cosecha datos) {
        ctx.verificarSesion();
        Validaciones.requerido(datos.getCodigo(), "codigo");
        Validaciones.requerido(datos.getFecha(), "fecha");
        Validaciones.requerido(datos.getSiembra(), "siembra");
        Validaciones.requerido(datos.getPesoKg(), "pesoKg");
        Validaciones.requerido(datos.getBandejas(), "bandejas");
        Validaciones.requerido(datos.getResponsable(), "responsable");
        LocalDate fecha = Validaciones.parsearFecha(datos.getFecha(), "fecha");

        if (datos.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("RN-15", "El peso cosechado debe ser mayor que cero");
        }
        Validaciones.maxDecimales(datos.getPesoKg(), 2, "pesoKg"); // RES-09
        Validaciones.enteroNoNegativo(datos.getBandejas(), "bandejas"); // RES-09

        String codigo = Validaciones.normalizarCodigo(datos.getCodigo());
        if (repo.existe(codigo)) {
            throw new DuplicadoException("RES-10", "Ya existe una cosecha con el código \"" + codigo + "\"");
        }

        Siembra siembra = siembras.obtener(datos.getSiembra());
        if ("Cosechada".equals(siembra.getEstado())) {
            throw new ReglaNegocioException("RN-14", "La siembra \"" + siembra.getCodigo()
                    + "\" ya fue cosechada; solo puede cosecharse una vez");
        }
        if (!fecha.isAfter(Validaciones.parsearFecha(siembra.getFecha(), "fecha"))) {
            throw new ReglaNegocioException("RN-16", "La fecha de cosecha debe ser posterior a la fecha de siembra");
        }
        String responsable = datos.getResponsable().trim();
        Validaciones.debeExistir(ctx.getBd().trabajadores(), responsable, "Trabajador"); // RES-06

        // Todas las validaciones pasaron: se escriben los tres cambios juntos (RNF-02)
        Cosecha nueva = datos.copia()
                .conCodigo(codigo)
                .conSiembra(siembra.getCodigo())
                .conSeccion(siembra.getSeccion())
                .conResponsable(responsable);
        nueva.setAuditoria(ctx.sello());
        Cosecha guardada = repo.guardar(codigo, nueva);
        siembras.marcarCosechada(siembra.getCodigo());
        secciones.cambiarEstado(siembra.getSeccion(), "Disponible"); // RF-15 / RN-17
        return guardada;
    }

    public List<Cosecha> listar() {
        return repo.listar();
    }
}
