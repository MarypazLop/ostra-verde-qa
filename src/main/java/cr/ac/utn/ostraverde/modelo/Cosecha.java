package cr.ac.utn.ostraverde.modelo;

import java.math.BigDecimal;

/** Cosecha de una siembra (Paso 6). Peso en kilogramos. */
public class Cosecha implements Copiable<Cosecha> {

    private String codigo;
    private String fecha;
    private String siembra;
    private String seccion;
    private BigDecimal pesoKg;
    private Integer bandejas;
    private String responsable;
    private Auditoria auditoria;

    public String getCodigo() {
        return codigo;
    }

    public Cosecha conCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public String getFecha() {
        return fecha;
    }

    public Cosecha conFecha(String fecha) {
        this.fecha = fecha;
        return this;
    }

    public String getSiembra() {
        return siembra;
    }

    public Cosecha conSiembra(String siembra) {
        this.siembra = siembra;
        return this;
    }

    public String getSeccion() {
        return seccion;
    }

    public Cosecha conSeccion(String seccion) {
        this.seccion = seccion;
        return this;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public Cosecha conPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
        return this;
    }

    public Integer getBandejas() {
        return bandejas;
    }

    public Cosecha conBandejas(Integer bandejas) {
        this.bandejas = bandejas;
        return this;
    }

    public String getResponsable() {
        return responsable;
    }

    public Cosecha conResponsable(String responsable) {
        this.responsable = responsable;
        return this;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public Cosecha copia() {
        Cosecha copia = new Cosecha()
                .conCodigo(codigo)
                .conFecha(fecha)
                .conSiembra(siembra)
                .conSeccion(seccion)
                .conPesoKg(pesoKg)
                .conBandejas(bandejas)
                .conResponsable(responsable);
        copia.setAuditoria(auditoria);
        return copia;
    }
}
