package cr.ac.utn.ostraverde.excepciones;

/** Operación de escritura sin sesión iniciada (RNF-06). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException() {
        super("Debe iniciar sesión para registrar, modificar o eliminar información");
    }
}
