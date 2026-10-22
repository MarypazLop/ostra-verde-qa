package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Trabajador;
import cr.ac.utn.ostraverde.repositorio.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

/** Paso 2 — Registro de personal (RF-04, RF-05, RF-06). */
public class TrabajadorService {

    private final Contexto ctx;
    private final Repositorio<Trabajador> repo;

    public TrabajadorService(Contexto ctx) {
        this.ctx = ctx;
        this.repo = ctx.getBd().trabajadores();
    }

    private void validar(Trabajador t) {
        Validaciones.requerido(t.getIdentificacion(), "identificacion");
        Validaciones.requerido(t.getNombre(), "nombre");
        Validaciones.requerido(t.getTelefono(), "telefono");
        Validaciones.requerido(t.getCorreo(), "correo");
        Validaciones.requerido(t.getCargo(), "cargo");
        Validaciones.correo(t.getCorreo());
        Validaciones.telefonoCR(t.getTelefono());
    }

    public Trabajador registrar(Trabajador datos) {
        validar(datos);
        String identificacion = datos.getIdentificacion().trim();
        if (repo.existe(identificacion)) {
            throw new DuplicadoException("RN-03", "Ya existe un trabajador con la identificación \"" + identificacion + "\"");
        }
        Trabajador nuevo = datos.copia()
                .conIdentificacion(identificacion)
                .conNombre(datos.getNombre().trim())
                .conCorreo(datos.getCorreo().trim())
                .conCargo(datos.getCargo().trim());
        return repo.guardar(identificacion, nuevo);
    }

    public Trabajador consultar(String identificacion) {
        return Validaciones.debeExistir(repo, identificacion.trim(), "Trabajador");
    }

    public List<Trabajador> listar() {
        return repo.listar();
    }

    /** Los campos nulos de "cambios" se dejan como están. */
    public Trabajador actualizar(String identificacion, Trabajador cambios) {
        Trabajador nuevo = consultar(identificacion);
        if (cambios.getIdentificacion() != null && !cambios.getIdentificacion().trim().equals(nuevo.getIdentificacion())) {
            throw new ValidacionException("identificacion", "La identificación del trabajador no puede modificarse");
        }
        if (cambios.getNombre() != null) {
            nuevo.conNombre(cambios.getNombre());
        }
        if (cambios.getTelefono() != null) {
            nuevo.conTelefono(cambios.getTelefono());
        }
        if (cambios.getCorreo() != null) {
            nuevo.conCorreo(cambios.getCorreo());
        }
        if (cambios.getCargo() != null) {
            nuevo.conCargo(cambios.getCargo());
        }
        validar(nuevo);
        return repo.guardar(nuevo.getIdentificacion(), nuevo);
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
