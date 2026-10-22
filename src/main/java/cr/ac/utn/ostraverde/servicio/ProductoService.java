package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.repositorio.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

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
        return repo.guardar(codigo, nuevo);
    }

    /** Los campos nulos de "cambios" se dejan como están. */
    public Producto modificar(String codigoBuscado, Producto cambios) {
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
        return repo.guardar(nuevo.getCodigo(), nuevo);
    }

    public Producto consultar(String codigo) {
        return Validaciones.debeExistir(repo, Validaciones.normalizarCodigo(codigo), "Producto");
    }

    public List<Producto> listar() {
        return repo.listar();
    }

    public boolean eliminar(String codigoBuscado) {
        Producto producto = consultar(codigoBuscado);
        boolean usado = ctx.getBd().aplicaciones().listar().stream()
                .anyMatch(a -> producto.getCodigo().equals(a.getProducto()));
        if (usado) {
            throw new ReglaNegocioException("RN-09",
                    "El producto \"" + producto.getCodigo() + "\" ya fue utilizado en aplicaciones y no puede eliminarse");
        }
        return repo.eliminar(producto.getCodigo());
    }

    // Validaciones locales del módulo (duplicadas en cada servicio; se extraen en el REFACTOR)
    private static final class Validaciones {

        private static final DateTimeFormatter FORMATO_FECHA =
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        private static final Pattern PATRON_FECHA = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");
        private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{8}$");

        private Validaciones() {
        }

        public static boolean esVacio(Object valor) {
            return valor == null || (valor instanceof String texto && texto.isBlank());
        }

        public static void requerido(Object valor, String campo) {
            if (esVacio(valor)) {
                throw new ValidacionException(campo, "El campo \"" + campo + "\" es obligatorio");
            }
        }

        /** Normaliza códigos: sin espacios externos y en mayúscula ("sec-01 " -> "SEC-01"). */
        public static String normalizarCodigo(String codigo) {
            return codigo.trim().toUpperCase();
        }

        public static void enteroPositivo(Integer valor, String campo) {
            if (valor == null || valor <= 0) {
                throw new ValidacionException(campo, "El campo \"" + campo + "\" debe ser un número entero mayor que cero");
            }
        }

        public static void enteroNoNegativo(Integer valor, String campo) {
            if (valor == null || valor < 0) {
                throw new ValidacionException(campo, "El campo \"" + campo + "\" debe ser un número entero mayor o igual a cero");
            }
        }

        public static void maxDecimales(BigDecimal valor, int decimales, String campo) {
            if (valor.stripTrailingZeros().scale() > decimales) {
                throw new ValidacionException(campo, "El campo \"" + campo + "\" admite como máximo " + decimales + " decimales");
            }
        }

        public static void correo(String valor) {
            if (!PATRON_CORREO.matcher(valor).matches()) {
                throw new ValidacionException("correo", "El campo \"correo\" debe tener el formato usuario@dominio");
            }
        }

        public static void telefonoCR(String valor) {
            if (!PATRON_TELEFONO.matcher(valor).matches()) {
                throw new ValidacionException("telefono", "El campo \"telefono\" debe contener exactamente 8 dígitos");
            }
        }

        /** Convierte "dd/mm/aaaa" en LocalDate. Rechaza fechas que no existen en el calendario. */
        public static LocalDate parsearFecha(String texto, String campo) {
            if (texto == null || !PATRON_FECHA.matcher(texto).matches()) {
                throw new ValidacionException(campo, "El campo \"" + campo + "\" debe tener el formato dd/mm/aaaa");
            }
            try {
                return LocalDate.parse(texto, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                throw new ValidacionException(campo, "El campo \"" + campo + "\" no es una fecha válida del calendario");
            }
        }

        public static <T> T debeExistir(Repositorio<T> repositorio, String clave, String entidad) {
            return repositorio.obtener(clave).orElseThrow(() -> new NoEncontradoException(entidad, clave));
        }
    }
}
