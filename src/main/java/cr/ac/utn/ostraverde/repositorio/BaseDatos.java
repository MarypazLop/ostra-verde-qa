package cr.ac.utn.ostraverde.repositorio;

import cr.ac.utn.ostraverde.modelo.Aplicacion;
import cr.ac.utn.ostraverde.modelo.Cosecha;
import cr.ac.utn.ostraverde.modelo.Producto;
import cr.ac.utn.ostraverde.modelo.Seccion;
import cr.ac.utn.ostraverde.modelo.Siembra;
import cr.ac.utn.ostraverde.modelo.Trabajador;

/** Único repositorio de datos del sistema: todos los módulos comparten esta instancia (RNF-01). */
public class BaseDatos {

    private final Repositorio<Seccion> secciones;
    private final Repositorio<Trabajador> trabajadores;
    private final Repositorio<Siembra> siembras;
    private final Repositorio<Producto> productos;
    private final Repositorio<Aplicacion> aplicaciones;
    private final Repositorio<Cosecha> cosechas;

    public BaseDatos() {
        this(new RepositorioMemoria<>(), new RepositorioMemoria<>(), new RepositorioMemoria<>(),
                new RepositorioMemoria<>(), new RepositorioMemoria<>(), new RepositorioMemoria<>());
    }

    public BaseDatos(Repositorio<Seccion> secciones, Repositorio<Trabajador> trabajadores,
                     Repositorio<Siembra> siembras, Repositorio<Producto> productos,
                     Repositorio<Aplicacion> aplicaciones, Repositorio<Cosecha> cosechas) {
        this.secciones = secciones;
        this.trabajadores = trabajadores;
        this.siembras = siembras;
        this.productos = productos;
        this.aplicaciones = aplicaciones;
        this.cosechas = cosechas;
    }

    public Repositorio<Seccion> secciones() {
        return secciones;
    }

    public Repositorio<Trabajador> trabajadores() {
        return trabajadores;
    }

    public Repositorio<Siembra> siembras() {
        return siembras;
    }

    public Repositorio<Producto> productos() {
        return productos;
    }

    public Repositorio<Aplicacion> aplicaciones() {
        return aplicaciones;
    }

    public Repositorio<Cosecha> cosechas() {
        return cosechas;
    }
}
