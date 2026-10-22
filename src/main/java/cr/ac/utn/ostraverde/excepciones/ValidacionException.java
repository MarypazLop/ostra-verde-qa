package cr.ac.utn.ostraverde.excepciones;

/** Dato con formato inválido o campo obligatorio ausente (restricciones RES-xx). */
public class ValidacionException extends RuntimeException {

    private final String campo;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
