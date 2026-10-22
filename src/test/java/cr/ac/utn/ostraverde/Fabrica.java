package cr.ac.utn.ostraverde;

import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Sesion;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.modelo.Trabajador;
import cr.ac.utn.ostraverde.repositorio.BaseDatos;
import cr.ac.utn.ostraverde.servicio.Contexto;
import cr.ac.utn.ostraverde.servicio.Sistema;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Datos válidos base y escenarios reutilizables: la parte común del Arrange de las pruebas. */
public final class Fabrica {

    /** Fecha fija del "hoy" en todas las pruebas: 15/10/2026. */
    public static final String HOY = "15/10/2026";
    public static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);

    private Fabrica() {
    }

    public static Sistema sistemaDePrueba() {
        return sistemaDePrueba("qa.tester");
    }

    public static Sistema sistemaDePrueba(String usuario) {
        return Sistema.crear(new Contexto(new BaseDatos(), RELOJ_FIJO, new Sesion(usuario)));
    }

    public static Sistema sistemaSinSesion() {
        return Sistema.crear(new Contexto(new BaseDatos(), RELOJ_FIJO, null));
    }

    // ---- Datos válidos base (cada prueba modifica solo lo que necesita) ----

    public static Seccion seccionValida() {
        return new Seccion().conCodigo("SEC-01").conNombre("Nave Norte").conCapacidad(500).conEstado("Disponible");
    }

    public static Trabajador trabajadorValido() {
        return new Trabajador().conIdentificacion("205550111").conNombre("Ana Rojas Mora").conTelefono("88887777")
                .conCorreo("ana.rojas@ostraverde.cr").conCargo("Operaria de cultivo");
    }

    public static Producto productoValido() {
        return new Producto().conCodigo("PRO-01").conNombre("Cal agrícola").conTipo("Enmienda")
                .conDescripcion("Regula el pH del sustrato").conUnidad("kg");
    }

    public static Siembra siembraValida() {
        return new Siembra().conCodigo("SIE-01").conFecha("01/10/2026").conSeccion("SEC-01").conSustratos(120)
                .conResponsable("205550111").conObservaciones("Lote de paja pasteurizada");
    }

    public static Aplicacion aplicacionValida() {
        return new Aplicacion().conCodigo("APL-01").conFecha("05/10/2026").conSiembra("SIE-01").conProducto("PRO-01")
                .conCantidad(new BigDecimal("2.5")).conResponsable("205550111").conObservaciones("");
    }

    public static Cosecha cosechaValida() {
        return new Cosecha().conCodigo("COS-01").conFecha("14/10/2026").conSiembra("SIE-01")
                .conPesoKg(new BigDecimal("45.75")).conBandejas(90).conResponsable("205550111");
    }

    /** Escenario: sección SEC-01 disponible + trabajadora 205550111 registrada. */
    public static Sistema escenarioBase() {
        Sistema s = sistemaDePrueba();
        s.secciones().registrar(seccionValida());
        s.trabajadores().registrar(trabajadorValido());
        return s;
    }

    /** Escenario: base + producto PRO-01 + siembra SIE-01 activa en SEC-01. */
    public static Sistema escenarioConSiembra() {
        Sistema s = escenarioBase();
        s.productos().registrar(productoValido());
        s.siembras().registrar(siembraValida());
        return s;
    }

    public static BigDecimal kg(String valor) {
        return new BigDecimal(valor);
    }
}
