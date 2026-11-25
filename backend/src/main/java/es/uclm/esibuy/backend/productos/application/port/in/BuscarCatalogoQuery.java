package es.uclm.esibuy.backend.productos.application.port.in;

public record BuscarCatalogoQuery(
    String busqueda,
    String categoriaId,
    Integer valoracionMinima,
    boolean soloStock,
    boolean soloOfertas,
    boolean clientePremium,
    Integer precioMinimo,
    Integer precioMaximo,
    CriterioOrdenCatalogo criterio,
    boolean descendente,
    int pagina,
    int tamano
) {

}
