package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import cr.ac.utn.ostraverde.util.Validaciones;

import java.util.List;

/** Paso 4 — Gestión de productos agrícolas (RF-09 a RF-12). */
public class ProductoService {

    private final Contexto ctx;
    private final Repositorio<Producto> repo;

    public ProductoService(Contexto ctx) {
        this.ctx = ctx;
        this.repo = ctx.getBd().productos();
    }

    private void validar(Producto p) {
        Validaciones.requerido(p.getNombre(), "nombre");
        Validaciones.requerido(p.getTipo(), "tipo");
        Validaciones.requerido(p.getUnidad(), "unidad");
    }

    public Producto registrar(Producto datos) {
        ctx.verificarSesion();
        Validaciones.requerido(datos.getCodigo(), "codigo");
        validar(datos);
        String codigo = Validaciones.normalizarCodigo(datos.getCodigo());
        if (repo.existe(codigo)) {
            throw new DuplicadoException("RES-10", "Ya existe un producto con el código \"" + codigo + "\"");
        }
        Producto nuevo = datos.copia()
                .conCodigo(codigo)
                .conNombre(datos.getNombre().trim())
                .conDescripcion(datos.getDescripcion() == null ? "" : datos.getDescripcion().trim());
        nuevo.setAuditoria(new cr.ac.utn.ostraverde.modelo.Auditoria(ctx.getSesion().usuario(), ctx.ahora().toString()));
        return repo.guardar(codigo, nuevo);
    }

    /** Los campos nulos de "cambios" se dejan como están. */
    public Producto modificar(String codigoBuscado, Producto cambios) {
        ctx.verificarSesion();
        Producto nuevo = consultar(codigoBuscado);
        if (cambios.getCodigo() != null && !Validaciones.normalizarCodigo(cambios.getCodigo()).equals(nuevo.getCodigo())) {
            throw new ValidacionException("codigo", "El código del producto no puede modificarse");
        }
        if (cambios.getNombre() != null) {
            nuevo.conNombre(cambios.getNombre());
        }
        if (cambios.getTipo() != null) {
            nuevo.conTipo(cambios.getTipo());
        }
        if (cambios.getDescripcion() != null) {
            nuevo.conDescripcion(cambios.getDescripcion());
        }
        if (cambios.getUnidad() != null) {
            nuevo.conUnidad(cambios.getUnidad());
        }
        validar(nuevo);
        nuevo.setAuditoria(new cr.ac.utn.ostraverde.modelo.Auditoria(ctx.getSesion().usuario(), ctx.ahora().toString()));
        return repo.guardar(nuevo.getCodigo(), nuevo);
    }

    public Producto consultar(String codigo) {
        return Validaciones.debeExistir(repo, Validaciones.normalizarCodigo(codigo), "Producto");
    }

    public List<Producto> listar() {
        return repo.listar();
    }

    public boolean eliminar(String codigoBuscado) {
        ctx.verificarSesion();
        Producto producto = consultar(codigoBuscado);
        boolean usado = ctx.getBd().aplicaciones().listar().stream()
                .anyMatch(a -> producto.getCodigo().equals(a.getProducto()));
        if (usado) {
            throw new ReglaNegocioException("RN-09",
                    "El producto \"" + producto.getCodigo() + "\" ya fue utilizado en aplicaciones y no puede eliminarse");
        }
        return repo.eliminar(producto.getCodigo());
    }
}
