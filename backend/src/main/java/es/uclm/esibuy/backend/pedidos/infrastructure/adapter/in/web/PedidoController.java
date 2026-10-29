package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;

/**
 * Adaptador de entrada web de los pedidos (HU10). Recibe la petición, delega en el puerto de entrada y responde con
 * el pedido creado. El cliente sale siempre de la sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/orders")
public class PedidoController {

    private final RealizacionPedidoUseCase realizacionPedido;

    public PedidoController(RealizacionPedidoUseCase realizacionPedido) {
        this.realizacionPedido = realizacionPedido;
    }

    // Un pedido por proveedor: el frontend pide uno por cada proveedor del carrito
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse realizar(@Valid @RequestBody RealizarPedidoRequest request, Authentication autenticado) {
        return PedidoResponse.desde(realizacionPedido.realizar(request.aComando(autenticado.getName())));
    }
}
