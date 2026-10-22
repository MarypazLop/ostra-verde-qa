package cr.ac.utn.ostraverde.modelo;

/** Producto agrícola (Paso 4). */
public class Producto implements Copiable<Producto> {

    private String codigo;
    private String nombre;
    private String tipo;
    private String descripcion;
    private String unidad;
    private Auditoria auditoria;

    public String getCodigo() {
        return codigo;
    }

    public Producto conCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public Producto conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public Producto conTipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Producto conDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public String getUnidad() {
        return unidad;
    }

    public Producto conUnidad(String unidad) {
        this.unidad = unidad;
        return this;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public Producto copia() {
        Producto copia = new Producto()
                .conCodigo(codigo)
                .conNombre(nombre)
                .conTipo(tipo)
                .conDescripcion(descripcion)
                .conUnidad(unidad);
        copia.setAuditoria(auditoria);
        return copia;
    }
}
