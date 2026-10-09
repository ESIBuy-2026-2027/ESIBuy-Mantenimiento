package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;




@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    private static final String CORREO_PENDIENTE = "login.correoPendiente";
    private final AutenticacionUseCase autenticacion;

    public AutenticacionController(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @PostMapping("/login")
    public SiguientePasoResponse login(@RequestBody LoginRequest request, HttpServletRequest peticion) {
        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand(request.email(), request.password()));
        recordarCorreo(peticion, resultado.email());
        return SiguientePasoResponse.desde(resultado.siguientePaso());
    }

    @PostMapping ("/verify-email-code")
    public SiguientePasoResponse verificarCodigoEmail(@RequestBody VerificarCodigoRequest request, @SessionAttribute(name = CORREO_PENDIENTE, required = false) String email) {
        if (email == null) {
            throw new CodigoInvalidoException("Código Inválido");
        }
        return SiguientePasoResponse.desde(autenticacion.verificarCodigoEmail(new VerificarCodigoEmailCommand(email, request.code())).siguientePaso());
    }

    private static void recordarCorreo(HttpServletRequest peticion, String email) {
        peticion.getSession().setAttribute(CORREO_PENDIENTE, email);
        peticion.changeSessionId();
    }
    
}
