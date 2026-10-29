package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.carrito;

import es.uclm.esibuy.backend.carrito.application.port.in.CarritoDetallado;
import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.carrito.application.port.in.GrupoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.in.LineaCarritoDetallada;
import es.uclm.esibuy.backend.pedidos.application.port.out.CompraProveedor;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Adaptador de pedidos hacia el carrito (HU10). El puerto de entrada del carrito es un doble de prueba (Mockito)
@ExtendWith(MockitoExtension.class)
class CarritoAdapterTest {

    private static final String EMAIL = "cliente@esibuy.test";

    @Mock
    private GestionCarritoUseCase gestionCarrito;

    private static final CarritoDetallado CARRITO = new CarritoDetallado(List.of(
            new GrupoCarrito("u-2", "TecnoVera", new BigDecimal("107.82"), List.of(new LineaCarritoDetallada("p-1",
                    "Auriculares", "a.png", 2, new BigDecimal("53.91"), new BigDecimal("107.82"), 34))),
            new GrupoCarrito("u-99", "Otero", new BigDecimal("129.00"), List.of(new LineaCarritoDetallada("p-24",
                    "Smartwatch", "s.png", 1, new BigDecimal("129.00"), new BigDecimal("129.00"), 10)))),
            3, new BigDecimal("236.82"));

    @Test
    @DisplayName("HU10-CP14: la compra de un proveedor sale de su grupo del carrito, con sus precios")
    void laCompraDeUnProveedorSaleDeSuGrupo() {
        when(gestionCarrito.consultar(EMAIL)).thenReturn(CARRITO);

        CompraProveedor compra = new CarritoAdapter(gestionCarrito).compraDelProveedor(EMAIL, "u-2").orElseThrow();

        assertEquals(new CompraProveedor("u-2", "TecnoVera",
                List.of(new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("53.91")))), compra);
    }

    @Test
    @DisplayName("HU10-CP17: sin productos de ese proveedor en el carrito no hay compra")
    void sinProductosDelProveedorNoHayCompra() {
        when(gestionCarrito.consultar(EMAIL)).thenReturn(CARRITO);

        assertEquals(Optional.empty(), new CarritoAdapter(gestionCarrito).compraDelProveedor(EMAIL, "u-5"));
    }

    @Test
    @DisplayName("HU10-CP14: quitar los productos del pedido los quita uno a uno del carrito")
    void quitarLosProductosDelPedidoLosQuitaDelCarrito() {
        new CarritoAdapter(gestionCarrito).quitar(EMAIL, List.of("p-1", "p-2"));

        verify(gestionCarrito).quitar(EMAIL, "p-1");
        verify(gestionCarrito).quitar(EMAIL, "p-2");
    }
}
