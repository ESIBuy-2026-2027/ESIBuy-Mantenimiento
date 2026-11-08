package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Adaptador de entrada web del perfil propio: {@code GET /api/perfil} (HU4), {@code PUT /api/perfil} (HU45) y
 * {@code DELETE /api/perfil}, que elimina la cuenta propia del cliente (HU25) o del vendedor (HU47). Solo lo usan
 * un CLIENTE o un VENDEDOR (SecurityConfig y @PreAuthorize de los servicios): sin sesión 401, el administrador 403.
 * El usuario sale de la sesión (su email), nunca de la petición, así que nadie puede consultar, editar ni eliminar
 * la cuenta de otro.
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

    // Editar el perfil propio (HU45): devuelve el perfil ya actualizado, el mismo JSON que el GET
    @PutMapping
    public PerfilResponse editar(@Valid @RequestBody EditarPerfilRequest request, Authentication autenticado) {
        return PerfilResponse.desde(gestionPerfil.editarPerfil(autenticado.getName(), request.aComando()));
    }
    
    // Paso a Premium del cliente en sesión (HU45, T423). El pago es simulado en el frontend: aquí no se cobra nada.
    @PostMapping("/premium")
    public PerfilResponse pasarAPremium(Authentication autenticado) {
        return PerfilResponse.desde(gestionPerfil.pasarAPremium(autenticado.getName()));
    }

    // Eliminar la cuenta propia: el servicio distingue el rol. 204 sin cuerpo y la sesión cerrada, igual que el
    // logout de SecurityConfig; si el caso de uso falla (p. ej. contraseña incorrecta, 400), la sesión sigue abierta.
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCuenta(@Valid @RequestBody EliminarCuentaRequest request, Authentication autenticado,
            HttpServletRequest peticion, HttpServletResponse respuesta) {
        gestionCuenta.eliminarCuentaPropia(autenticado.getName(), request.password());
        new SecurityContextLogoutHandler().logout(peticion, respuesta, autenticado);
        new CookieClearingLogoutHandler("JSESSIONID").logout(peticion, respuesta, autenticado);
    }
}
