package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import cr.ac.utn.ostraverde.util.Validaciones;

import java.math.BigDecimal;
import java.util.List;

/** Paso 5 — Registro de aplicaciones de productos (RF-13). */
public class AplicacionService {

    private final Contexto ctx;
    private final Repositorio<Aplicacion> repo;

    public AplicacionService(Contexto ctx) {
        this.ctx = ctx;
        this.repo = ctx.getBd().aplicaciones();
    }

    public Aplicacion registrar(Aplicacion datos) {
        ctx.verificarSesion();
        // RES-08: todo es obligatorio excepto observaciones
        Validaciones.requerido(datos.getCodigo(), "codigo");
        Validaciones.requerido(datos.getFecha(), "fecha");
        Validaciones.requerido(datos.getSiembra(), "siembra");
        Validaciones.requerido(datos.getProducto(), "producto");
        Validaciones.requerido(datos.getCantidad(), "cantidad");
        Validaciones.requerido(datos.getResponsable(), "responsable");
        Validaciones.parsearFecha(datos.getFecha(), "fecha");

        if (datos.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("RN-12", "La cantidad aplicada debe ser mayor que cero");
        }

        String codigo = Validaciones.normalizarCodigo(datos.getCodigo());
        if (repo.existe(codigo)) {
            throw new DuplicadoException("RES-10", "Ya existe una aplicación con el código \"" + codigo + "\"");
        }

        Siembra siembra = ctx.getBd().siembras().obtener(Validaciones.normalizarCodigo(datos.getSiembra()))
                .orElseThrow(() -> new ReglaNegocioException("RN-10", "La siembra \"" + datos.getSiembra() + "\" no existe"));
        Producto producto = ctx.getBd().productos().obtener(Validaciones.normalizarCodigo(datos.getProducto()))
                .orElseThrow(() -> new ReglaNegocioException("RN-11", "El producto \"" + datos.getProducto() + "\" no existe"));
        String responsable = datos.getResponsable().trim();
        Validaciones.debeExistir(ctx.getBd().trabajadores(), responsable, "Trabajador"); // RES-06

        if ("Cosechada".equals(siembra.getEstado())) {
            throw new ReglaNegocioException("RN-13", "La siembra \"" + siembra.getCodigo()
                    + "\" ya fue cosechada; no admite aplicaciones");
        }

        Aplicacion nueva = datos.copia()
                .conCodigo(codigo)
                .conSiembra(siembra.getCodigo())
                .conProducto(producto.getCodigo())
                .conUnidad(producto.getUnidad())
                .conResponsable(responsable)
                .conObservaciones(datos.getObservaciones() == null ? "" : datos.getObservaciones().trim());
        nueva.setAuditoria(new cr.ac.utn.ostraverde.modelo.Auditoria(ctx.getSesion().usuario(), ctx.ahora().toString()));
        return repo.guardar(codigo, nueva);
    }

    public List<Aplicacion> listar() {
        return repo.listar();
    }
}
