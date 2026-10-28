package es.uclm.esibuy.backend.productos.application.port.in;

public record BuscarCatalogoQuery(
    String busqueda,
    String categoriaId,
    Integer valoracionMinima,
    CriterioOrdenCatalogo criterio,
    boolean descendente,
    int pagina,
    int tamano
) {

}
