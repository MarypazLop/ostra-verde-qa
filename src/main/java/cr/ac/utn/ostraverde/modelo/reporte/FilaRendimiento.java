package cr.ac.utn.ostraverde.modelo.reporte;

import java.math.BigDecimal;

/** Rendimiento de una siembra cosechada: kg ÷ sustratos (RF-20, supuesto S-01). */
public record FilaRendimiento(String siembra, BigDecimal kg, int sustratos, BigDecimal kgPorSustrato) {
}
