package cr.ac.utn.ostraverde.modelo.reporte;

import java.math.BigDecimal;

/** Producción total de un período (RF-16). */
public record ResumenPeriodo(String desde, String hasta, BigDecimal kg, int bandejas, int cosechas) {
}
