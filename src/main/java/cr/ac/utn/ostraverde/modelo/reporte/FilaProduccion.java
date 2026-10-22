package cr.ac.utn.ostraverde.modelo.reporte;

import java.math.BigDecimal;

/** Producción agrupada por sección o por trabajador (RF-17, RF-18). */
public record FilaProduccion(String clave, BigDecimal kg, int bandejas, int cosechas) {
}
