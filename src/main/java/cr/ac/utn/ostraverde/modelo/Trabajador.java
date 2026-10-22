package cr.ac.utn.ostraverde.modelo;

/** Trabajador de la empresa (Paso 2). */
public class Trabajador implements Copiable<Trabajador> {

    private String identificacion;
    private String nombre;
    private String telefono;
    private String correo;
    private String cargo;
    private Auditoria auditoria;

    public String getIdentificacion() {
        return identificacion;
    }

    public Trabajador conIdentificacion(String identificacion) {
        this.identificacion = identificacion;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public Trabajador conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getTelefono() {
        return telefono;
    }

    public Trabajador conTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public String getCorreo() {
        return correo;
    }

    public Trabajador conCorreo(String correo) {
        this.correo = correo;
        return this;
    }

    public String getCargo() {
        return cargo;
    }

    public Trabajador conCargo(String cargo) {
        this.cargo = cargo;
        return this;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public Trabajador copia() {
        Trabajador copia = new Trabajador()
                .conIdentificacion(identificacion)
                .conNombre(nombre)
                .conTelefono(telefono)
                .conCorreo(correo)
                .conCargo(cargo);
        copia.setAuditoria(auditoria);
        return copia;
    }
}
