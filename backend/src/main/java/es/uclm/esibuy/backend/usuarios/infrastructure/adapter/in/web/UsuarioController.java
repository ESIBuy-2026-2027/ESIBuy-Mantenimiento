package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada web del módulo de usuarios.
 *
 * <p>Recibe la petición HTTP, la traduce a un comando y delega en el puerto de entrada.
 * No contiene lógica de negocio: las reglas del registro viven en {@code RegistroUsuarioService}.
 */
@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final RegistroUsuarioUseCase registroUsuario;

    public UsuarioController(RegistroUsuarioUseCase registroUsuario) {
        this.registroUsuario = registroUsuario;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@Valid @RequestBody RegistroRequest request) {
        registroUsuario.registrar(new RegistrarUsuarioCommand(request.email(), request.password()));
    }
}
