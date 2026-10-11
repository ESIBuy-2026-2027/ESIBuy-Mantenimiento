package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoAuthenticatorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Adaptador web del login en tres pasos (HU2): contraseña (T230), código del correo (T230) y
 * Microsoft Authenticator (T20), más {@code /me}. El logout lo hace Spring Security (SecurityConfig).
 *
 * <p>Entre pasos, la sesión del servidor recuerda el correo y si se superó el código del correo; el
 * navegador solo lleva la cookie de sesión. Al superar el paso 3 se abre la sesión de Spring Security.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private static final String CORREO_PENDIENTE = "login.correoPendiente";
    // La sesión recuerda que se superó el código del correo: el paso 3 lo exige (T20, D8)
    private static final String CODIGO_EMAIL_VERIFICADO = "login.codigoEmailVerificado";
    private static final String CODIGO_INVALIDO = "Código inválido";

    private final AutenticacionUseCase autenticacion;
    private final AuthenticatorUseCase authenticator;
    // Guarda el usuario autenticado en la sesión HTTP; es lo que leen /me y las reglas de SecurityConfig
    private final SecurityContextRepository repositorioContexto = new HttpSessionSecurityContextRepository();

    public AutenticacionController(AutenticacionUseCase autenticacion, AuthenticatorUseCase authenticator) {
        this.autenticacion = autenticacion;
        this.authenticator = authenticator;
    }

    @PostMapping("/login")
    public SiguientePasoResponse login(@RequestBody LoginRequest request, HttpServletRequest peticion) {
        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(
                new IniciarSesionCommand(request.email(), request.password()));
        recordarCorreo(peticion, resultado.email());
        return SiguientePasoResponse.desde(resultado.siguientePaso());
    }

    @PostMapping("/verify-email-code")
    public SiguientePasoResponse verificarCodigoEmail(@RequestBody VerificarCodigoRequest request,
            @SessionAttribute(name = CORREO_PENDIENTE, required = false) String email,
            HttpServletRequest peticion) {
        if (email == null) {
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }
        ResultadoInicioSesion resultado = autenticacion.verificarCodigoEmail(
                new VerificarCodigoEmailCommand(email, request.code()));
        peticion.getSession().setAttribute(CODIGO_EMAIL_VERIFICADO, true);
        peticion.changeSessionId();
        return SiguientePasoResponse.desde(resultado.siguientePaso());
    }

    @PostMapping("/enroll-authenticator")
    public AltaAuthenticatorResponse altaAuthenticator(
            @SessionAttribute(name = CORREO_PENDIENTE, required = false) String email,
            @SessionAttribute(name = CODIGO_EMAIL_VERIFICADO, required = false) Boolean pasoDosSuperado) {
        exigirPasoDosSuperado(email, pasoDosSuperado);
        return new AltaAuthenticatorResponse(authenticator.prepararAlta(email).qr());
    }

    @PostMapping("/verify-authenticator-code")
    public UsuarioSesionResponse verificarCodigoAuthenticator(@RequestBody VerificarCodigoRequest request,
            @SessionAttribute(name = CORREO_PENDIENTE, required = false) String email,
            @SessionAttribute(name = CODIGO_EMAIL_VERIFICADO, required = false) Boolean pasoDosSuperado,
            HttpServletRequest peticion, HttpServletResponse respuesta) {
        exigirPasoDosSuperado(email, pasoDosSuperado);
        Usuario usuario = authenticator.verificarCodigo(
                new VerificarCodigoAuthenticatorCommand(email, request.code()));
        abrirSesion(usuario, peticion, respuesta);
        return UsuarioSesionResponse.desde(usuario);
    }

    @GetMapping("/me")
    public UsuarioSesionResponse me(Authentication autenticado) {
        return UsuarioSesionResponse.desde(autenticacion.consultarUsuarioEnSesion(autenticado.getName()));
    }

    private static void recordarCorreo(HttpServletRequest peticion, String email) {
        peticion.getSession().setAttribute(CORREO_PENDIENTE, email);
        peticion.changeSessionId();
    }

    // Sin los pasos 1 y 2 superados en esta sesión: el mismo 401 que un código incorrecto
    private static void exigirPasoDosSuperado(String email, Boolean pasoDosSuperado) {
        if (email == null || !Boolean.TRUE.equals(pasoDosSuperado)) {
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }
    }

    // Paso 3 superado: el usuario queda autenticado en la sesión con su rol (T20, D9)
    private void abrirSesion(Usuario usuario, HttpServletRequest peticion, HttpServletResponse respuesta) {
        // El login a medias ya no hace falta
        HttpSession sesion = peticion.getSession();
        sesion.removeAttribute(CORREO_PENDIENTE);
        sesion.removeAttribute(CODIGO_EMAIL_VERIFICADO);
        peticion.changeSessionId();   // id nuevo al autenticarse (session fixation)

        // Su correo como nombre y ROLE_<rol> como permiso, que es lo que comprueba /api/admin/**
        Authentication credenciales = UsernamePasswordAuthenticationToken.authenticated(
                usuario.getEmail().valor(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(credenciales);
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, peticion, respuesta);
    }
}
