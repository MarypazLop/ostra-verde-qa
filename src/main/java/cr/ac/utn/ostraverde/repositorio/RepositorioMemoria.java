package cr.ac.utn.ostraverde.repositorio;

import cr.ac.utn.ostraverde.modelo.Copiable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio en memoria. Funciona como doble de prueba (fake) de la base de datos real,
 * lo que permite probar la lógica de negocio de forma aislada (RNF-08).
 * Guarda y devuelve copias para que un objeto modificado fuera del repositorio no altere lo guardado.
 */
public class RepositorioMemoria<T extends Copiable<T>> implements Repositorio<T> {

    private final Map<String, T> datos = new LinkedHashMap<>();

    @Override
    public T guardar(String clave, T entidad) {
        datos.put(clave, entidad.copia());
        return entidad.copia();
    }

    @Override
    public Optional<T> obtener(String clave) {
        return Optional.ofNullable(datos.get(clave)).map(Copiable::copia);
    }

    @Override
    public boolean existe(String clave) {
        return datos.containsKey(clave);
    }

    @Override
    public List<T> listar() {
        return datos.values().stream().map(Copiable::copia).toList();
    }

    @Override
    public boolean eliminar(String clave) {
        return datos.remove(clave) != null;
    }

    @Override
    public int contar() {
        return datos.size();
    }
}
