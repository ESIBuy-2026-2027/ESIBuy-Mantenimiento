package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionSeguridadUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoAuthenticatorCommand;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seguridad")
public class SeguridadController {

    private final GestionSeguridadUseCase gestionSeguridad;
    private final AuthenticatorUseCase authenticator;

    public SeguridadController(
            GestionSeguridadUseCase gestionSeguridad,
            AuthenticatorUseCase authenticator) {
        this.gestionSeguridad = gestionSeguridad;
        this.authenticator = authenticator;
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

    /**
     * Inicia el alta desde la configuración de la cuenta. Esta ruta está bajo
     * /api/seguridad/** y, por tanto, requiere una sesión autenticada.
     */
    @PostMapping("/factores/authenticator/enroll")
    public AltaAuthenticatorResponse iniciarAltaAuthenticator(Authentication autenticado) {
        return new AltaAuthenticatorResponse(
                authenticator.prepararAlta(autenticado.getName()).qr());
    }

    /**
     * Confirma el alta con el primer código TOTP. No abre una sesión nueva:
     * el usuario ya está autenticado en el área de configuración.
     */
    @PostMapping("/factores/authenticator/verify")
    public ResponseEntity<Void> verificarAltaAuthenticator(
            @Valid @RequestBody VerificarCodigoRequest request,
            Authentication autenticado) {
        authenticator.verificarCodigo(
                new VerificarCodigoAuthenticatorCommand(autenticado.getName(), request.code()));
        return ResponseEntity.noContent().build();
    }
}
