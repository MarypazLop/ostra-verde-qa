package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

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
        return repo.guardar(siembra.getCodigo(), siembra);
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
