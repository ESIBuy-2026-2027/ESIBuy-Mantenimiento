package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Invalida la sesión de un usuario cuya cuenta ha sido desactivada (HU23, T206) o cuyo rol ha cambiado
 * (HU22, T43).
 *
 * <p>En cada petición con un usuario en sesión comprueba que su cuenta sigue activa y con el mismo rol; si no
 * (o el usuario ya no existe), invalida la sesión y responde 401 sin llegar al endpoint. Al volver a iniciar
 * sesión, el login le da el permiso de su rol nuevo.
 * El usuario en sesión se identifica por su email, como lo guarda el login.
 */
public class CuentaActivaFilter extends OncePerRequestFilter {

    private final AutenticacionUseCase autenticacion;

    public CuentaActivaFilter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication autenticado = SecurityContextHolder.getContext().getAuthentication();
        if (autenticado == null || autenticado instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }
        if (sesionVigente(autenticado)) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    // La sesión sigue valiendo si la cuenta está activa (HU23) y conserva el rol con el que inició sesión (HU22):
    // si un administrador le ha cambiado el rol, el permiso ROLE_<rol> de la sesión ya no es el suyo.
    private boolean sesionVigente(Authentication autenticado) {
        try {
            Usuario usuario = autenticacion.consultarUsuarioEnSesion(autenticado.getName());
            String permisoActual = "ROLE_" + usuario.getRol().name();
            return usuario.estaActivo() && autenticado.getAuthorities().stream()
                    .anyMatch(permiso -> permisoActual.equals(permiso.getAuthority()));
        } catch (CredencialesInvalidasException e) {
            return false;
        }
    }
}
