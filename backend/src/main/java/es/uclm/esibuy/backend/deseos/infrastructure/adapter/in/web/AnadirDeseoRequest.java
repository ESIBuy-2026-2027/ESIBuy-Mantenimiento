package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

/** Cuerpo de POST /api/customer/wishlist/items. Un clienteId en el cuerpo se ignora: el cliente sale de la sesión. */
public record AnadirDeseoRequest(String productoId) {
}
