package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.in.web;

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

import es.uclm.esibuy.backend.valoraciones.application.port.in.ConsultaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;

/**
 * Adaptador de entrada web de las valoraciones (HU13). Recibe la petición, delega en el puerto de entrada y responde
 * con la valoración creada o con las valoraciones del producto. El cliente sale siempre de la sesión y el producto,
 * de la URL.
 */
@RestController
@RequestMapping("/api/products/{productoId}/valoraciones")
public class ValoracionController {

    private final ValoracionProductosUseCase valoracionProductos;
    private final ConsultaValoracionesUseCase consultaValoraciones;

    public ValoracionController(ValoracionProductosUseCase valoracionProductos,
            ConsultaValoracionesUseCase consultaValoraciones) {
        this.valoracionProductos = valoracionProductos;
        this.consultaValoraciones = consultaValoraciones;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValoracionResponse valorar(@PathVariable String productoId, @RequestBody ValorarProductoRequest request,
            Authentication autenticado) {
        return ValoracionResponse.desde(
                valoracionProductos.valorar(request.aComando(autenticado.getName(), productoId)));
    }

    @GetMapping
    public List<ValoracionResponse> consultar(@PathVariable String productoId) {
        return consultaValoraciones.consultarPorProducto(productoId).stream().map(ValoracionResponse::desde).toList();
    }
}
