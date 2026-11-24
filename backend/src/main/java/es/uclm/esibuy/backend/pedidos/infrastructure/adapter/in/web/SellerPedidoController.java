package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.pedidos.application.port.in.GestionPedidosVendedorUseCase;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerPedidoController {

    private final GestionPedidosVendedorUseCase gestionPedidos;

    public SellerPedidoController(GestionPedidosVendedorUseCase gestionPedidos) {
        this.gestionPedidos = gestionPedidos;
    }

    @GetMapping
    public List<PedidoResponse> listar(Authentication autenticado) {
        return gestionPedidos.listarPedidos(autenticado.getName()).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse consultar(@PathVariable String id, Authentication autenticado) {
        return PedidoResponse.desde(gestionPedidos.consultarPedido(autenticado.getName(), id));
    }

    @PostMapping("/{id}/advance")
    public PedidoResponse avanzar(@PathVariable String id, Authentication autenticado) {
        return PedidoResponse.desde(gestionPedidos.avanzarEstado(autenticado.getName(), id));
    }

    @PostMapping("/{id}/revert")
    public PedidoResponse retroceder(@PathVariable String id, Authentication autenticado) {
        return PedidoResponse.desde(gestionPedidos.retrocederEstado(autenticado.getName(), id));
    }

    @PostMapping("/{id}/cancel")
    public PedidoResponse cancelar(@PathVariable String id, Authentication autenticado) {
        return PedidoResponse.desde(gestionPedidos.cancelarPedido(autenticado.getName(), id));
    }
}