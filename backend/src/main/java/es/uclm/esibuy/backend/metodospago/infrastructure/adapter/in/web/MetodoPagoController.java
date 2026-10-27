package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;

/**
 * Adaptador de entrada web del módulo de métodos de pago (HU6). Recibe la petición, la traduce a un comando
 * y delega en el puerto de entrada. El cliente sale siempre de la sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/payment-methods")
public class MetodoPagoController {

    private final GestionMetodosPagoUseCase gestionMetodosPago;

    public MetodoPagoController(GestionMetodosPagoUseCase gestionMetodosPago) {
        this.gestionMetodosPago = gestionMetodosPago;
    }

    @GetMapping
    public List<MetodoPagoResponse> listar(Authentication autenticado) {
        return gestionMetodosPago.listar(autenticado.getName()).stream().map(MetodoPagoResponse::desde).toList();
    }

    // Alta de un método de pago: un clienteId en el cuerpo se ignora
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoPagoResponse anadir(@Valid @RequestBody MetodoPagoRequest request, Authentication autenticado) {
        return MetodoPagoResponse.desde(gestionMetodosPago.anadir(request.aComando(autenticado.getName())));
    }

    // Marca como predeterminado un método propio; uno ajeno o inexistente da 404
    @PutMapping("/{id}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarPredeterminado(@PathVariable String id, Authentication autenticado) {
        gestionMetodosPago.marcarPredeterminado(autenticado.getName(), id);
    }

    // Elimina un método propio. Si es el predeterminado y quedan otros, el cliente elige el sustituto
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id, @RequestParam(required = false) String nuevoPredeterminado,
            Authentication autenticado) {
        gestionMetodosPago.eliminar(autenticado.getName(), id, nuevoPredeterminado);
    }
}
