package es.uclm.esibuy.backend.carrito.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;

import es.uclm.esibuy.backend.carrito.application.port.in.CarritoDetallado;
import es.uclm.esibuy.backend.carrito.application.port.in.GrupoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.in.LineaCarritoDetallada;

/**
 * Lo que la API devuelve del carrito, con el contrato que ya usa el frontend (frontend/src/api/cart.js):
 * {@code { grupos: [{ vendedor: { id, nombreComercial }, total, lineas: [...] }], totalProductos, total }}.
 */
public record CarritoResponse(List<Grupo> grupos, int totalProductos, BigDecimal total) {

    public record Grupo(Vendedor vendedor, BigDecimal total, List<Linea> lineas) {
    }

    public record Vendedor(String id, String nombreComercial) {
    }

    public record Linea(String productoId, String nombre, String imagen, int cantidad, BigDecimal precioUnitario,
            BigDecimal subtotal, int stock) {
    }

    public static CarritoResponse desde(CarritoDetallado carrito) {
        return new CarritoResponse(carrito.grupos().stream().map(CarritoResponse::aGrupo).toList(),
                carrito.totalProductos(), carrito.total());
    }

    private static Grupo aGrupo(GrupoCarrito grupo) {
        return new Grupo(new Vendedor(grupo.vendedorId(), grupo.nombreComercial()), grupo.total(),
                grupo.lineas().stream().map(CarritoResponse::aLinea).toList());
    }

    private static Linea aLinea(LineaCarritoDetallada linea) {
        return new Linea(linea.productoId(), linea.nombre(), linea.imagen(), linea.cantidad(),
                linea.precioUnitario(), linea.subtotal(), linea.stock());
    }
}
