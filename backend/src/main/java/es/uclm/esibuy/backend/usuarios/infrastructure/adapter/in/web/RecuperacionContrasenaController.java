package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.RecuperacionContrasenaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RestablecerContrasenaCommand;
import jakarta.validation.Valid;

/**
 * Recuperación de contraseña (HU3). Se usa sin sesión, pero pide el token CSRF como el resto.
 *
 * <p>Sin try/catch: el 400 (validación, contraseña inválida) y el 401 (código inválido) los da
 * {@code ManejadorGlobalErrores}.
 */
@RestController
@RequestMapping("/api/auth/password-recovery")
public class RecuperacionContrasenaController {

    private final RecuperacionContrasenaUseCase recuperacion;

    public RecuperacionContrasenaController(RecuperacionContrasenaUseCase recuperacion) {
        this.recuperacion = recuperacion;
    }

    // Siempre 204, exista o no la cuenta (HU3-CP02 y CP03)
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void solicitar(@Valid @RequestBody SolicitarRecuperacionRequest request) {
        recuperacion.solicitarRecuperacion(request.email());
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecer(@Valid @RequestBody RestablecerContrasenaRequest request) {
        recuperacion.restablecerContrasena(
                new RestablecerContrasenaCommand(request.email(), request.code(), request.password()));
    }
}
