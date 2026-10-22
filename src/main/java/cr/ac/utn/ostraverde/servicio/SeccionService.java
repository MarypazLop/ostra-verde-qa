package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.repositorio.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

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
        Validaciones.requerido(datos.getCodigo(), "codigo");
        validar(datos);
        String codigo = Validaciones.normalizarCodigo(datos.getCodigo());
        if (repo.existe(codigo)) {
            throw new DuplicadoException("RN-01", "Ya existe una sección con el código \"" + codigo + "\"");
        }
        Seccion nueva = datos.copia().conCodigo(codigo).conNombre(datos.getNombre().trim());
        return repo.guardar(codigo, nueva);
    }

    /** Los campos nulos de "cambios" se dejan como están. */
    public Seccion modificar(String codigoBuscado, Seccion cambios) {
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
        return repo.guardar(seccion.getCodigo(), seccion);
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
