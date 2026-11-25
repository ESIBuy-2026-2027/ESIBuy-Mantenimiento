package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

public record BuscarCatalogoQuery(
    String busqueda,
    String categoriaId,
    Integer valoracionMinima,
    boolean soloStock,
    boolean soloOfertas,
    boolean clientePremium,
    BigDecimal precioMinimo,
    BigDecimal precioMaximo,
    CriterioOrdenCatalogo criterio,
    boolean descendente,
    int pagina,
    int tamano
) {

}
