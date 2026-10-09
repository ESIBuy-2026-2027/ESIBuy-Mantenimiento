package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Adaptador de entrada web del módulo de usuarios.
 *
 * <p>Recibe la petición HTTP, la traduce a un comando y delega en el puerto de entrada.
 * No contiene lógica de negocio: las reglas del registro viven en {@code RegistroUsuarioService}.
 */
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final RegistroUsuarioUseCase registroUsuario;
    private final ListadoUsuariosUseCase listadoUsuarios;

    public UsuarioController(RegistroUsuarioUseCase registroUsuario, ListadoUsuariosUseCase listadoUsuarios) {
        this.registroUsuario = registroUsuario;
        this.listadoUsuarios = listadoUsuarios;
    }

    @PostMapping("/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@Valid @RequestBody RegistroRequest request) {
        registroUsuario.registrar(new RegistrarUsuarioCommand(request.email(), request.password()));
    }

    @GetMapping("/admin/usuarios")
    public UsuarioListadoPageResponse listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Rol role,
            @RequestParam(required = false) String status) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("La página y el tamaño deben ser positivos");
        }
        return UsuarioListadoPageResponse.desde(
                listadoUsuarios.listar(new ListarUsuariosQuery(search, role, status, page - 1, size))
        );
    }
}
