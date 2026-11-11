
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionSeguridadUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seguridad")
public class SeguridadController {

    private final GestionSeguridadUseCase gestionSeguridad;

    public SeguridadController(GestionSeguridadUseCase gestionSeguridad) {
        this.gestionSeguridad = gestionSeguridad;
    }

    @GetMapping("/factores")
    public FactoresSeguridadResponse consultar(Authentication autenticado) {
        return FactoresSeguridadResponse.desde(
                gestionSeguridad.consultar(autenticado.getName()));
    }

    @PatchMapping("/factores")
    public FactoresSeguridadResponse actualizar(
            @Valid @RequestBody ActualizarFactoresRequest request,
            Authentication autenticado) {

        return FactoresSeguridadResponse.desde(
                gestionSeguridad.actualizar(
                        autenticado.getName(),
                        request.aComando()));
    }
}
