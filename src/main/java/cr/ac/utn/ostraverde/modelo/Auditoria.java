package cr.ac.utn.ostraverde.modelo;

/** Usuario y fecha (ISO-8601) de la última creación o modificación de un registro (RNF-10). */
public record Auditoria(String usuario, String fecha) {
}
