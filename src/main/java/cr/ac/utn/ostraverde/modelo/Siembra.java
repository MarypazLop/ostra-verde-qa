package cr.ac.utn.ostraverde.modelo;

/** Siembra de hongos en una sección (Paso 3). Fecha en formato dd/mm/aaaa. */
public class Siembra implements Copiable<Siembra> {

    private String codigo;
    private String fecha;
    private String seccion;
    private Integer sustratos;
    private String responsable;
    private String observaciones;
    private String estado;
    private Auditoria auditoria;

    public String getCodigo() {
        return codigo;
    }

    public Siembra conCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public String getFecha() {
        return fecha;
    }

    public Siembra conFecha(String fecha) {
        this.fecha = fecha;
        return this;
    }

    public String getSeccion() {
        return seccion;
    }

    public Siembra conSeccion(String seccion) {
        this.seccion = seccion;
        return this;
    }

    public Integer getSustratos() {
        return sustratos;
    }

    public Siembra conSustratos(Integer sustratos) {
        this.sustratos = sustratos;
        return this;
    }

    public String getResponsable() {
        return responsable;
    }

    public Siembra conResponsable(String responsable) {
        this.responsable = responsable;
        return this;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Siembra conObservaciones(String observaciones) {
        this.observaciones = observaciones;
        return this;
    }

    public String getEstado() {
        return estado;
    }

    public Siembra conEstado(String estado) {
        this.estado = estado;
        return this;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public Siembra copia() {
        Siembra copia = new Siembra()
                .conCodigo(codigo)
                .conFecha(fecha)
                .conSeccion(seccion)
                .conSustratos(sustratos)
                .conResponsable(responsable)
                .conObservaciones(observaciones)
                .conEstado(estado);
        copia.setAuditoria(auditoria);
        return copia;
    }
}
