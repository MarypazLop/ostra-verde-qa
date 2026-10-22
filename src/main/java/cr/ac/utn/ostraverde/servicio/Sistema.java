package cr.ac.utn.ostraverde.servicio;

/**
 * Punto de entrada del sistema de Ostra Verde S.A.
 * Todos los servicios comparten un único contexto y base de datos (RNF-01).
 */
public record Sistema(Contexto ctx,
                      SeccionService secciones,
                      TrabajadorService trabajadores,
                      SiembraService siembras,
                      ProductoService productos,
                      AplicacionService aplicaciones,
                      CosechaService cosechas,
                      ReporteService reportes) {

    public static Sistema crear(Contexto ctx) {
        SeccionService secciones = new SeccionService(ctx);
        SiembraService siembras = new SiembraService(ctx, secciones);
        return new Sistema(ctx, secciones, new TrabajadorService(ctx), siembras, new ProductoService(ctx),
                new AplicacionService(ctx), new CosechaService(ctx, siembras, secciones), new ReporteService(ctx));
    }
}
