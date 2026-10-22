package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.NoEncontradoException;
import cr.ac.utn.ostraverde.excepciones.ReglaNegocioException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.repositorio.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

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
        Cosecha guardada = repo.guardar(codigo, nueva);
        siembras.marcarCosechada(siembra.getCodigo());
        secciones.cambiarEstado(siembra.getSeccion(), "Disponible"); // RF-15 / RN-17
        return guardada;
    }

    public List<Cosecha> listar() {
        return repo.listar();
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
