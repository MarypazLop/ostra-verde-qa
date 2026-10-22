package cr.ac.utn.ostraverde.modelo.reporte;

import java.math.BigDecimal;

/** Una aplicación del historial (RF-19). */
public record FilaHistorial(String codigo, String fecha, String siembra, String producto, BigDecimal cantidad, String unidad, String responsable) {
}
