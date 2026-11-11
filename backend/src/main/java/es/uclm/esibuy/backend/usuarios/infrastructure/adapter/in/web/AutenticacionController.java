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
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoAuthenticatorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private static final String CORREO_PENDIENTE = "login.correoPendiente";
    private static final String CODIGO_EMAIL_VERIFICADO = "login.codigoEmailVerificado";
    private static final String AUTHENTICATOR_DIRECTO_PENDIENTE = "login.authenticatorDirectoPendiente";
    private static final String CODIGO_INVALIDO = "Código inválido";

    private final AutenticacionUseCase autenticacion;
    private final AuthenticatorUseCase authenticator;
    private final SecurityContextRepository repositorioContexto = new HttpSessionSecurityContextRepository();

    public AutenticacionController(AutenticacionUseCase autenticacion, AuthenticatorUseCase authenticator) {
        this.autenticacion = autenticacion;
        this.authenticator = authenticator;
    }

    @PostMapping("/login")
    public SiguientePasoResponse login(
            @RequestBody LoginRequest request,
            HttpServletRequest peticion,
            HttpServletResponse respuesta) {

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(
                new IniciarSesionCommand(request.email(), request.password()));

        if (resultado.siguientePaso() == PasoLogin.ACCESO_DIRECTO) {
            abrirSesion(resultado.usuario(), peticion, respuesta);
            return SiguientePasoResponse.desde(resultado.siguientePaso());
        }

        recordarCorreo(peticion, resultado.email());
        if (resultado.siguientePaso() == PasoLogin.AUTHENTICATOR) {
            peticion.getSession().setAttribute(AUTHENTICATOR_DIRECTO_PENDIENTE, true);
        }
        return SiguientePasoResponse.desde(resultado.siguientePaso());
    }

    @PostMapping("/verify-email-code")
    public SiguientePasoResponse verificarCodigoEmail(@RequestBody VerificarCodigoRequest request,
            @SessionAttribute(name = CORREO_PENDIENTE, required = false) String email,
            HttpServletRequest peticion,
            HttpServletResponse respuesta) {
        if (email == null) {
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }

        ResultadoInicioSesion resultado = autenticacion.verificarCodigoEmail(
                new VerificarCodigoEmailCommand(email, request.code()));

        if (resultado.siguientePaso() == PasoLogin.ACCESO_DIRECTO) {
            abrirSesion(resultado.usuario(), peticion, respuesta);
            return SiguientePasoResponse.desde(resultado.siguientePaso());
        }

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
            @SessionAttribute(name = AUTHENTICATOR_DIRECTO_PENDIENTE, required = false) Boolean authenticatorDirecto,
            HttpServletRequest peticion, HttpServletResponse respuesta) {
        exigirPasoAuthenticator(email, pasoDosSuperado, authenticatorDirecto);
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

    private static void exigirPasoDosSuperado(String email, Boolean pasoDosSuperado) {
        if (email == null || !Boolean.TRUE.equals(pasoDosSuperado)) {
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }
    }

    private static void exigirPasoAuthenticator(
            String email, Boolean pasoDosSuperado, Boolean authenticatorDirecto) {
        boolean correoVerificado = Boolean.TRUE.equals(pasoDosSuperado);
        boolean authenticatorSinCorreo = Boolean.TRUE.equals(authenticatorDirecto);
        if (email == null || (!correoVerificado && !authenticatorSinCorreo)) {
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }
    }

    private void abrirSesion(Usuario usuario, HttpServletRequest peticion, HttpServletResponse respuesta) {
        HttpSession sesion = peticion.getSession();
        sesion.removeAttribute(CORREO_PENDIENTE);
        sesion.removeAttribute(CODIGO_EMAIL_VERIFICADO);
        sesion.removeAttribute(AUTHENTICATOR_DIRECTO_PENDIENTE);
        peticion.changeSessionId();

        Authentication credenciales = UsernamePasswordAuthenticationToken.authenticated(
                usuario.getEmail().valor(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(credenciales);
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, peticion, respuesta);
    }
}
