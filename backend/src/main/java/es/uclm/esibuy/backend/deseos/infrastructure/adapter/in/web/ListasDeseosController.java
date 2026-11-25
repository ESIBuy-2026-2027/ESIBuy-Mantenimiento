package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;

/**
 * Adaptador de entrada web para múltiples listas de deseos del cliente (PBI 534). El cliente sale siempre de la
 * sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/wishlists")
public class ListasDeseosController {

    private final GestionListaDeseosUseCase gestionListaDeseos;

    public ListasDeseosController(GestionListaDeseosUseCase gestionListaDeseos) {
        this.gestionListaDeseos = gestionListaDeseos;
    }

    @GetMapping
    public List<ListaDeseosDetalladaResponse> consultarTodas(Authentication autenticado) {
        return gestionListaDeseos.consultarTodas(autenticado.getName()).stream()
                .map(ListaDeseosDetalladaResponse::desde)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListaDeseosDetalladaResponse crear(@Valid @RequestBody NombreListaDeseosRequest request,
            Authentication autenticado) {
        return ListaDeseosDetalladaResponse.desde(
                gestionListaDeseos.crearLista(autenticado.getName(), request.nombre()));
    }

    @PatchMapping("/{listaId}")
    public ListaDeseosDetalladaResponse renombrar(@PathVariable String listaId,
            @Valid @RequestBody NombreListaDeseosRequest request, Authentication autenticado) {
        return ListaDeseosDetalladaResponse.desde(
                gestionListaDeseos.renombrarLista(autenticado.getName(), listaId, request.nombre()));
    }

    @DeleteMapping("/{listaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String listaId, Authentication autenticado) {
        gestionListaDeseos.eliminarLista(autenticado.getName(), listaId);
    }

    @PostMapping("/{listaId}/items")
    public ListaDeseosDetalladaResponse anadirProducto(@PathVariable String listaId,
            @Valid @RequestBody AnadirDeseoRequest request, Authentication autenticado) {
        return ListaDeseosDetalladaResponse.desde(
                gestionListaDeseos.anadirALista(autenticado.getName(), listaId, request.productoId()));
    }

    @DeleteMapping("/{listaId}/items/{productoId}")
    public ListaDeseosDetalladaResponse quitarProducto(@PathVariable String listaId, @PathVariable String productoId,
            Authentication autenticado) {
        return ListaDeseosDetalladaResponse.desde(
                gestionListaDeseos.quitarDeLista(autenticado.getName(), listaId, productoId));
    }

    @PatchMapping("/{listaId}/items/{productoId}")
    public ListaDeseosDetalladaResponse actualizarCantidad(@PathVariable String listaId,
            @PathVariable String productoId, @Valid @RequestBody CantidadDeseoRequest request,
            Authentication autenticado) {
        return ListaDeseosDetalladaResponse.desde(
                gestionListaDeseos.actualizarCantidad(autenticado.getName(), listaId, productoId, request.cantidad()));
    }
}
