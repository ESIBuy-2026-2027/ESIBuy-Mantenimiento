package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Adaptador de entrada web del perfil propio: {@code GET /api/perfil} (HU4) y {@code DELETE /api/perfil} (HU25).
 * Solo lo usan un CLIENTE o un VENDEDOR (SecurityConfig): sin sesión 401, el administrador 403; eliminar la
 * cuenta es además solo del cliente (@PreAuthorize del servicio). El usuario sale de la sesión (su email), nunca
 * de la petición, así que nadie puede consultar ni eliminar la cuenta de otro.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final GestionPerfilUseCase gestionPerfil;

    public PerfilController(GestionPerfilUseCase gestionPerfil) {
        this.gestionPerfil = gestionPerfil;
    }

    @GetMapping
    public PerfilResponse consultar(Authentication autenticado) {
        return PerfilResponse.desde(gestionPerfil.consultarPerfil(autenticado.getName()));
    }

    // Eliminar la cuenta propia (HU25). 204 sin cuerpo y la sesión cerrada, igual que el logout de
    // SecurityConfig; si el caso de uso falla (p. ej. contraseña incorrecta, 400), la sesión sigue abierta.
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCuenta(@Valid @RequestBody EliminarCuentaRequest request, Authentication autenticado,
            HttpServletRequest peticion, HttpServletResponse respuesta) {
        gestionPerfil.eliminarCuenta(autenticado.getName(), request.password());
        new SecurityContextLogoutHandler().logout(peticion, respuesta, autenticado);
        new CookieClearingLogoutHandler("JSESSIONID").logout(peticion, respuesta, autenticado);
    }
}
