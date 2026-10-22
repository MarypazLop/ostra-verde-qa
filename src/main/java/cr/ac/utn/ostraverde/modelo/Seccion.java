package cr.ac.utn.ostraverde.modelo;

/** Sección de producción (Paso 1). */
public class Seccion implements Copiable<Seccion> {

    private String codigo;
    private String nombre;
    private Integer capacidad;
    private String estado;
    private Auditoria auditoria;

    public String getCodigo() {
        return codigo;
    }

    public Seccion conCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public Seccion conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public Seccion conCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
        return this;
    }

    public String getEstado() {
        return estado;
    }

    public Seccion conEstado(String estado) {
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
    public Seccion copia() {
        Seccion copia = new Seccion()
                .conCodigo(codigo)
                .conNombre(nombre)
                .conCapacidad(capacidad)
                .conEstado(estado);
        copia.setAuditoria(auditoria);
        return copia;
    }
}
