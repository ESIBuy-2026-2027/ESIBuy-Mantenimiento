package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    private final AdministracionUsuariosUseCase administracionUsuarios;

    public UsuarioController(RegistroUsuarioUseCase registroUsuario, ListadoUsuariosUseCase listadoUsuarios,
            AdministracionUsuariosUseCase administracionUsuarios) {
        this.registroUsuario = registroUsuario;
        this.listadoUsuarios = listadoUsuarios;
        this.administracionUsuarios = administracionUsuarios;
    }

    @PostMapping("/auth/register/cliente")   // contrato acordado con el frontend (T229)
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@Valid @RequestBody RegistroRequest request) {
        registroUsuario.registrar(new RegistrarUsuarioCommand(
                request.nombre(), request.apellidos(), request.fechaNacimiento(), request.dni(), request.email(),
                request.telefono(), request.password(), request.repeticionPassword(), request.tipoCliente(),
                request.imagenPerfil(), request.aceptaPrivacidad()));
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

    // Activar (HU24) o desactivar (HU23) una cuenta. La validación de CambiarEstadoRequest ya garantiza
    // que el estado pedido es ACTIVO o DESACTIVADO. Al desactivar se pasa el email del administrador en sesión.
    @PatchMapping("/admin/usuarios/{id}/status")
    public UsuarioListadoResponse cambiarEstado(@PathVariable String id,
            @Valid @RequestBody CambiarEstadoRequest request, Authentication autenticado) {
        if ("DESACTIVADO".equals(request.estado())) {
            return UsuarioListadoResponse.desde(administracionUsuarios.desactivar(id, autenticado.getName()));
        }
        return UsuarioListadoResponse.desde(administracionUsuarios.activar(id));
    }
}
