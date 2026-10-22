package cr.ac.utn.ostraverde.excepciones;

/** Incumplimiento de una regla de negocio (RN-xx). */
public class ReglaNegocioException extends RuntimeException {

    private final String regla;

    public ReglaNegocioException(String regla, String mensaje) {
        super(mensaje);
        this.regla = regla;
    }

    public String getRegla() {
        return regla;
    }
}
