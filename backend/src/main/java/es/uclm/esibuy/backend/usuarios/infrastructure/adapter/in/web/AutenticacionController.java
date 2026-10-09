package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;



@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionUseCase autenticacion;

    public AutenticacionController(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @PostMapping("/login")
    public SiguientePasoResponse login(@RequestBody LoginRequest request) {
        return SiguientePasoResponse.desde(autenticacion.iniciarSesion(new IniciarSesionCommand(request.email(), request.password())).siguientePaso());
    }
    
}
