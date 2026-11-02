package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;

/**
 * Adaptador de entrada web de las valoraciones (HU13). Recibe la petición, delega en el puerto de entrada y responde
 * con la valoración creada. El cliente sale siempre de la sesión y el producto, de la URL.
 */
@RestController
@RequestMapping("/api/products/{productoId}/valoraciones")
public class ValoracionController {

    private final ValoracionProductosUseCase valoracionProductos;

    public ValoracionController(ValoracionProductosUseCase valoracionProductos) {
        this.valoracionProductos = valoracionProductos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValoracionResponse valorar(@PathVariable String productoId, @RequestBody ValorarProductoRequest request,
            Authentication autenticado) {
        return ValoracionResponse.desde(
                valoracionProductos.valorar(request.aComando(autenticado.getName(), productoId)));
    }
}