package cr.ac.utn.ostraverde.excepciones;

/** Registro duplicado por código o identificación. */
public class DuplicadoException extends ReglaNegocioException {

    public DuplicadoException(String regla, String mensaje) {
        super(regla, mensaje);
    }
}
