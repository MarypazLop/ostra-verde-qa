package cr.ac.utn.ostraverde.repositorio;

import java.util.List;
import java.util.Optional;

/** Operaciones de persistencia que necesitan los servicios. */
public interface Repositorio<T> {

    T guardar(String clave, T entidad);

    Optional<T> obtener(String clave);

    boolean existe(String clave);

    List<T> listar();

    boolean eliminar(String clave);

    int contar();
}
