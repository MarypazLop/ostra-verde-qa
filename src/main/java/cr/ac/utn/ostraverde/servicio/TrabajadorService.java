package cr.ac.utn.ostraverde.servicio;

import cr.ac.utn.ostraverde.excepciones.DuplicadoException;
import cr.ac.utn.ostraverde.excepciones.ValidacionException;
import cr.ac.utn.ostraverde.modelo.Trabajador;
import cr.ac.utn.ostraverde.repositorio.Repositorio;
import cr.ac.utn.ostraverde.util.Validaciones;

import java.util.List;

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
}
