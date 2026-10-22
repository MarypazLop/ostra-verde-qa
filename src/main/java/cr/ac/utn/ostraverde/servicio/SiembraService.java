package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import cr.ac.utn.ostraverde.util.Validaciones;

import java.time.LocalDate;
import java.util.List;

/** Paso 3 — Registro de siembras (RF-07, RF-08). */
public class SiembraService {

    private final Contexto ctx;
    private final Repositorio<Siembra> repo;
    private final SeccionService secciones;

    public SiembraService(Contexto ctx, SeccionService secciones) {
        this.ctx = ctx;
        this.repo = ctx.getBd().siembras();
        this.secciones = secciones;
    }

    public Siembra registrar(Siembra datos) {
        ctx.verificarSesion();
        // RES-05: todo es obligatorio excepto observaciones
        Validaciones.requerido(datos.getCodigo(), "codigo");
        Validaciones.requerido(datos.getFecha(), "fecha");
        Validaciones.requerido(datos.getSeccion(), "seccion");
        Validaciones.requerido(datos.getSustratos(), "sustratos");
        Validaciones.requerido(datos.getResponsable(), "responsable");
        LocalDate fecha = Validaciones.parsearFecha(datos.getFecha(), "fecha");

        if (datos.getSustratos() <= 0) {
            throw new ReglaNegocioException("RN-06", "La cantidad de sustratos debe ser mayor que cero");
        }
        if (fecha.isAfter(ctx.hoy())) {
            throw new ReglaNegocioException("RN-05", "La fecha de siembra no puede ser futura");
        }

        String codigo = Validaciones.normalizarCodigo(datos.getCodigo());
        if (repo.existe(codigo)) {
            throw new DuplicadoException("RES-10", "Ya existe una siembra con el código \"" + codigo + "\"");
        }

        // RES-06: la sección y el responsable deben existir
        Seccion seccion = secciones.obtener(datos.getSeccion());
        String responsable = datos.getResponsable().trim();
        Validaciones.debeExistir(ctx.getBd().trabajadores(), responsable, "Trabajador");

        if ("Ocupada".equals(seccion.getEstado())) {
            throw new ReglaNegocioException("RN-07", "La sección \"" + seccion.getCodigo()
                    + "\" está ocupada y no puede recibir otra siembra simultánea");
        }
        if (!"Disponible".equals(seccion.getEstado())) {
            throw new ReglaNegocioException("RN-04", "Solo pueden utilizarse secciones disponibles; \""
                    + seccion.getCodigo() + "\" está " + seccion.getEstado());
        }

        // Todas las validaciones pasaron: se escriben ambos cambios juntos (RNF-02)
        Siembra nueva = datos.copia()
                .conCodigo(codigo)
                .conSeccion(seccion.getCodigo())
                .conResponsable(responsable)
                .conObservaciones(datos.getObservaciones() == null ? "" : datos.getObservaciones().trim())
                .conEstado("Activa");
        nueva.setAuditoria(ctx.sello());
        Siembra guardada = repo.guardar(codigo, nueva);
        secciones.cambiarEstado(seccion.getCodigo(), "Ocupada"); // RF-08 / RN-08
        return guardada;
    }

    public Siembra obtener(String codigo) {
        return Validaciones.debeExistir(repo, Validaciones.normalizarCodigo(codigo), "Siembra");
    }

    public List<Siembra> listar() {
        return repo.listar();
    }

    /** Uso interno de cosechas. */
    public Siembra marcarCosechada(String codigo) {
        Siembra siembra = obtener(codigo).conEstado("Cosechada");
        siembra.setAuditoria(ctx.sello());
        return repo.guardar(siembra.getCodigo(), siembra);
    }
}
