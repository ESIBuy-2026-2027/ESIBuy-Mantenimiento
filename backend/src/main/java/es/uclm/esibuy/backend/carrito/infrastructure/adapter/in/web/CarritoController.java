package es.uclm.esibuy.backend.carrito.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;

/**
 * Adaptador de entrada web del carrito (HU9). Recibe la petición, delega en el puerto de entrada y responde
 * siempre con el carrito completo. El cliente sale siempre de la sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/cart")
public class CarritoController {

    private final GestionCarritoUseCase gestionCarrito;

    public CarritoController(GestionCarritoUseCase gestionCarrito) {
        this.gestionCarrito = gestionCarrito;
    }

    @GetMapping
    public CarritoResponse consultar(Authentication autenticado) {
        return CarritoResponse.desde(gestionCarrito.consultar(autenticado.getName()));
    }

    // Vaciar el carrito de un clic
    @DeleteMapping
    public CarritoResponse vaciar(Authentication autenticado) {
        return CarritoResponse.desde(gestionCarrito.vaciar(autenticado.getName()));
    }

    // Suma unidades a las que ya hubiera de ese producto
    @PostMapping("/items")
    public CarritoResponse anadir(@Valid @RequestBody AnadirAlCarritoRequest request, Authentication autenticado) {
        return CarritoResponse.desde(
                gestionCarrito.anadir(autenticado.getName(), request.productoId(), request.cantidad()));
    }

    // Sustituye la cantidad de un producto que ya está en el carrito
    @PutMapping("/items/{productoId}")
    public CarritoResponse fijarCantidad(@PathVariable String productoId, @Valid @RequestBody CantidadRequest request,
            Authentication autenticado) {
        return CarritoResponse.desde(
                gestionCarrito.fijarCantidad(autenticado.getName(), productoId, request.cantidad()));
    }

    @DeleteMapping("/items/{productoId}")
    public CarritoResponse quitar(@PathVariable String productoId, Authentication autenticado) {
        return CarritoResponse.desde(gestionCarrito.quitar(autenticado.getName(), productoId));
    }
}
