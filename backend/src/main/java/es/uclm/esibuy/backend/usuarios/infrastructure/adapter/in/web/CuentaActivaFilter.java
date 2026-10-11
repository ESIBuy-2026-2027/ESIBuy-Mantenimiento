package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Invalida la sesión de un usuario cuya cuenta ha sido desactivada (HU23, T206).
 *
 * <p>En cada petición con un usuario en sesión comprueba que su cuenta sigue activa; si no lo está
 * (o el usuario ya no existe), invalida la sesión y responde 401 sin llegar al endpoint.
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
        if (cuentaActiva(autenticado.getName())) {
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

    private boolean cuentaActiva(String email) {
        try {
            return autenticacion.consultarUsuarioEnSesion(email).estaActivo();
        } catch (CredencialesInvalidasException e) {
            return false;
        }
    }
}
