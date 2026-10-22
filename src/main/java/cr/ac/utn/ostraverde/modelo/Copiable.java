package cr.ac.utn.ostraverde.modelo;

/** Permite que el repositorio guarde y devuelva copias, igual que una base de datos real. */
public interface Copiable<T> {

    T copia();
}
