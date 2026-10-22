package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.AccesoDenegadoException;
import cr.ac.utn.ostraverde.modelo.Sesion;
import cr.ac.utn.ostraverde.repositorio.BaseDatos;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Contexto compartido por todos los servicios:
 * la base de datos centralizada (RNF-01), el reloj del sistema (inyectable para pruebas
 * deterministas) y la sesión del usuario.
 */
public class Contexto {

    private final BaseDatos bd;
    private final Clock reloj;
    private Sesion sesion;

    public Contexto(BaseDatos bd, Clock reloj, Sesion sesion) {
        this.bd = bd;
        this.reloj = reloj;
        this.sesion = sesion;
    }

    public BaseDatos getBd() {
        return bd;
    }

    public Sesion getSesion() {
        return sesion;
    }

    public void setSesion(Sesion sesion) {
        this.sesion = sesion;
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    public Instant ahora() {
        return Instant.now(reloj);
    }

    /** RNF-06: solo usuarios con sesión pueden registrar, modificar o eliminar. */
    public void verificarSesion() {
        if (sesion == null || sesion.usuario() == null || sesion.usuario().isBlank()) {
            throw new AccesoDenegadoException();
        }
    }
}
