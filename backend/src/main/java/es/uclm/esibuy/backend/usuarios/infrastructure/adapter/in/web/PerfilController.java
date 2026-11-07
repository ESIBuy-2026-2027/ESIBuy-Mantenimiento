package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;

/**
 * Adaptador de entrada web del perfil propio (HU4): {@code GET /api/perfil}. Solo lo usan un CLIENTE o un
 * VENDEDOR (SecurityConfig): sin sesión 401, el administrador 403. El usuario sale de la sesión (su email),
 * nunca de la petición, así que nadie puede pedir el perfil de otro.
 *
 * <p>El {@code DELETE /api/perfil} permite a un VENDEDOR eliminar su propia cuenta. Requiere autenticación,
 * contraseña y token CSRF; al terminar invalida la sesión y limpia el contexto de seguridad.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final GestionPerfilUseCase gestionPerfil;
    private final GestionCuentaUseCase gestionCuenta;

    public PerfilController(GestionPerfilUseCase gestionPerfil, GestionCuentaUseCase gestionCuenta) {
        this.gestionPerfil = gestionPerfil;
        this.gestionCuenta = gestionCuenta;
    }

    @GetMapping
    public PerfilResponse consultar(Authentication autenticado) {
        return PerfilResponse.desde(gestionPerfil.consultarPerfil(autenticado.getName()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCuenta(@Valid @RequestBody EliminarCuentaRequest request,
            Authentication autenticado, HttpSession sesion) {
        gestionCuenta.eliminarCuentaVendedor(autenticado.getName(), request.password());
        SecurityContextHolder.clearContext();
        sesion.invalidate();
    }
}
