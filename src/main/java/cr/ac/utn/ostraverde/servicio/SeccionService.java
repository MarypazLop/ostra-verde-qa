package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import cr.ac.utn.ostraverde.util.Validaciones;

import java.util.List;

/** Paso 1 — Gestión de secciones de producción (RF-01, RF-02, RF-03). */
public class SeccionService {

    public static final List<String> ESTADOS = List.of("Disponible", "Ocupada", "En mantenimiento");

    private final Contexto ctx;
    private final Repositorio<Seccion> repo;

    public SeccionService(Contexto ctx) {
        this.ctx = ctx;
        this.repo = ctx.getBd().secciones();
    }

    private void validarEstado(String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new ReglaNegocioException("RN-02",
                    "Estado \"" + estado + "\" inválido. Valores permitidos: " + String.join(", ", ESTADOS));
        }
    }

    private void validar(Seccion seccion) {
        Validaciones.requerido(seccion.getNombre(), "nombre");
        Validaciones.requerido(seccion.getCapacidad(), "capacidad");
        Validaciones.requerido(seccion.getEstado(), "estado");
        Validaciones.enteroPositivo(seccion.getCapacidad(), "capacidad");
        validarEstado(seccion.getEstado());
    }

    public Seccion registrar(Seccion datos) {
        ctx.verificarSesion();
        Validaciones.requerido(datos.getCodigo(), "codigo");
        validar(datos);
        String codigo = Validaciones.normalizarCodigo(datos.getCodigo());
        if (repo.existe(codigo)) {
            throw new DuplicadoException("RN-01", "Ya existe una sección con el código \"" + codigo + "\"");
        }
        Seccion nueva = datos.copia().conCodigo(codigo).conNombre(datos.getNombre().trim());
        nueva.setAuditoria(new cr.ac.utn.ostraverde.modelo.Auditoria(ctx.getSesion().usuario(), ctx.ahora().toString()));
        return repo.guardar(codigo, nueva);
    }

    /** Los campos nulos de "cambios" se dejan como están. */
    public Seccion modificar(String codigoBuscado, Seccion cambios) {
        ctx.verificarSesion();
        String codigo = Validaciones.normalizarCodigo(codigoBuscado);
        Seccion nueva = Validaciones.debeExistir(repo, codigo, "Sección");
        if (cambios.getCodigo() != null && !Validaciones.normalizarCodigo(cambios.getCodigo()).equals(codigo)) {
            throw new ValidacionException("codigo", "El código de la sección no puede modificarse");
        }
        if (cambios.getNombre() != null) {
            nueva.conNombre(cambios.getNombre());
        }
        if (cambios.getCapacidad() != null) {
            nueva.conCapacidad(cambios.getCapacidad());
        }
        if (cambios.getEstado() != null) {
            nueva.conEstado(cambios.getEstado());
        }
        validar(nueva);
        nueva.conNombre(nueva.getNombre().trim());
        nueva.setAuditoria(new cr.ac.utn.ostraverde.modelo.Auditoria(ctx.getSesion().usuario(), ctx.ahora().toString()));
        return repo.guardar(codigo, nueva);
    }

    public Seccion obtener(String codigo) {
        return Validaciones.debeExistir(repo, Validaciones.normalizarCodigo(codigo), "Sección");
    }

    public List<Seccion> consultarDisponibles() {
        return repo.listar().stream().filter(s -> "Disponible".equals(s.getEstado())).toList();
    }

    /** Uso interno de siembras y cosechas (RF-08, RF-15). */
    public Seccion cambiarEstado(String codigo, String estado) {
        validarEstado(estado);
        Seccion seccion = obtener(codigo).conEstado(estado);
        seccion.setAuditoria(new cr.ac.utn.ostraverde.modelo.Auditoria(ctx.getSesion().usuario(), ctx.ahora().toString()));
        return repo.guardar(seccion.getCodigo(), seccion);
    }
}
