package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;

/**
 * Adaptador de entrada web de la lista de deseos (HU14). Recibe la petición y delega en el puerto de entrada. El
 * cliente sale siempre de la sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/wishlist")
public class ListaDeseosController {

    private final GestionListaDeseosUseCase gestionListaDeseos;

    public ListaDeseosController(GestionListaDeseosUseCase gestionListaDeseos) {
        this.gestionListaDeseos = gestionListaDeseos;
    }

    @PostMapping("/items")
    public ListaDeseosResponse anadir(@Valid @RequestBody AnadirDeseoRequest request, Authentication autenticado) {
        return ListaDeseosResponse.desde(gestionListaDeseos.anadir(autenticado.getName(), request.productoId()));
    }
}
