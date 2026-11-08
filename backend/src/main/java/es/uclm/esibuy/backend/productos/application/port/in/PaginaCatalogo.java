package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Página del catálogo del cliente (HU7) y si el cliente en sesión es Premium, para dar a cada producto el precio que
 * le corresponde (HU9).
 */
public record PaginaCatalogo(ProductoPage pagina, boolean clientePremium) {
}