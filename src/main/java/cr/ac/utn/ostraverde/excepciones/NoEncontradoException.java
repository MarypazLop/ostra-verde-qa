package cr.ac.utn.ostraverde.excepciones;

/** Entidad referenciada que no existe. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String entidad, String clave) {
        super(entidad + " con código/identificación \"" + clave + "\" no existe");
    }
}
