package cr.ac.utn.ostraverde.modelo;

import java.math.BigDecimal;

/** Aplicación de un producto sobre una siembra (Paso 5). */
public class Aplicacion implements Copiable<Aplicacion> {

    private String codigo;
    private String fecha;
    private String siembra;
    private String producto;
    private BigDecimal cantidad;
    private String unidad;
    private String responsable;
    private String observaciones;
    private Auditoria auditoria;

    public String getCodigo() {
        return codigo;
    }

    public Aplicacion conCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public String getFecha() {
        return fecha;
    }

    public Aplicacion conFecha(String fecha) {
        this.fecha = fecha;
        return this;
    }

    public String getSiembra() {
        return siembra;
    }

    public Aplicacion conSiembra(String siembra) {
        this.siembra = siembra;
        return this;
    }

    public String getProducto() {
        return producto;
    }

    public Aplicacion conProducto(String producto) {
        this.producto = producto;
        return this;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public Aplicacion conCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        return this;
    }

    public String getUnidad() {
        return unidad;
    }

    public Aplicacion conUnidad(String unidad) {
        this.unidad = unidad;
        return this;
    }

    public String getResponsable() {
        return responsable;
    }

    public Aplicacion conResponsable(String responsable) {
        this.responsable = responsable;
        return this;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Aplicacion conObservaciones(String observaciones) {
        this.observaciones = observaciones;
        return this;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public Aplicacion copia() {
        Aplicacion copia = new Aplicacion()
                .conCodigo(codigo)
                .conFecha(fecha)
                .conSiembra(siembra)
                .conProducto(producto)
                .conCantidad(cantidad)
                .conUnidad(unidad)
                .conResponsable(responsable)
                .conObservaciones(observaciones);
        copia.setAuditoria(auditoria);
        return copia;
    }
}
