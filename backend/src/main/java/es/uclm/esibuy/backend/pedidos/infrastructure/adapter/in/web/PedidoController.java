package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;

/**
 * Adaptador de entrada web de los pedidos. Recibe la petición y delega en el puerto de entrada: crea el pedido
 * (HU10) y sirve el historial de pedidos del cliente y el detalle de cada uno (HU11). El cliente sale siempre de la
 * sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/orders")
public class PedidoController {

    private final RealizacionPedidoUseCase realizacionPedido;
    private final ConsultaComprasUseCase consultaCompras;

    public PedidoController(RealizacionPedidoUseCase realizacionPedido, ConsultaComprasUseCase consultaCompras) {
        this.realizacionPedido = realizacionPedido;
        this.consultaCompras = consultaCompras;
    }

    // Un pedido por proveedor: el frontend pide uno por cada proveedor del carrito
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse realizar(@Valid @RequestBody RealizarPedidoRequest request, Authentication autenticado) {
        return PedidoResponse.desde(realizacionPedido.realizar(request.aComando(autenticado.getName())));
    }

    // Historial del cliente (HU11), del más reciente al más antiguo
    @GetMapping
    public List<PedidoResponse> listar(Authentication autenticado) {
        return consultaCompras.listarPedidos(autenticado.getName()).stream().map(PedidoResponse::desde).toList();
    }

    // Detalle de un pedido (HU11): uno ajeno se responde igual que uno inexistente
    @GetMapping("/{id}")
    public PedidoResponse consultar(@PathVariable String id, Authentication autenticado) {
        return PedidoResponse.desde(consultaCompras.consultarPedido(autenticado.getName(), id));
    }
}
