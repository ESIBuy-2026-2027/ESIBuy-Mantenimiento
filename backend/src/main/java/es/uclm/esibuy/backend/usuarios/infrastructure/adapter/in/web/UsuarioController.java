package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.util.Map;
import java.util.stream.Collectors;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarClienteCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarVendedorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorCommand;
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
import org.springframework.web.bind.annotation.PutMapping;

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
    private final CrearAdministradorUseCase crearAdministrador;
    // Módulo de categorías, por su puerto de entrada: da el nombre de la categoría principal de un vendedor.
    private final ConsultaCategoriasUseCase consultaCategorias;

    public UsuarioController(RegistroUsuarioUseCase registroUsuario, ListadoUsuariosUseCase listadoUsuarios,
            AdministracionUsuariosUseCase administracionUsuarios, CrearAdministradorUseCase crearAdministrador,
            ConsultaCategoriasUseCase consultaCategorias) {
        this.registroUsuario = registroUsuario;
        this.listadoUsuarios = listadoUsuarios;
        this.administracionUsuarios = administracionUsuarios;
        this.crearAdministrador = crearAdministrador;
        this.consultaCategorias = consultaCategorias;
    }

    @PostMapping("/auth/register/cliente")   // contrato acordado con el frontend (T229)
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarCliente(@Valid @RequestBody RegistroRequest request) {
        registroUsuario.registrarCliente(new RegistrarClienteCommand(
                request.nombre(), request.apellidos(), request.fechaNacimiento(), request.dni(), request.email(),
                request.telefono(), request.password(), request.repeticionPassword(), request.tipoCliente(),
                request.avatar(), request.aceptaPrivacidad()));
    }

    @PostMapping("/auth/register/vendedor")   // contrato del frontend (T241)
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarVendedor(@Valid @RequestBody RegistroVendedorRequest request) {
        registroUsuario.registrarVendedor(new RegistrarVendedorCommand(
                request.nombre(), request.apellidos(), request.nombreComercial(), request.categoriaPrincipalId(),
                request.dni(), request.email(), request.telefono(), request.password(), request.repeticionPassword(),
                request.avatar(), request.aceptaPrivacidad()));
    }

    @GetMapping("/admin/usuarios")
    public UsuarioListadoPageResponse listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Rol role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("La página y el tamaño deben ser positivos");
        }
        // sort y order como en /admin/sellers (fallos de la demo del Sprint 1: la pantalla de clientes ya los enviaba)
        return UsuarioListadoPageResponse.desde(listadoUsuarios.listar(new ListarUsuariosQuery(
                search, role, status, page - 1, size, orden(sort), "desc".equals(order))));
    }

    // Ficha de un usuario (HU20). 404 si no existe (UsuarioNoEncontradoException, en ManejadorGlobalErrores).
    @GetMapping("/admin/usuarios/{id}")
    public UsuarioDetalleResponse consultar(@PathVariable String id) {
        return UsuarioDetalleResponse.desde(administracionUsuarios.consultar(id));
    }

    // Editar los datos de un usuario (HU22). 400 si los datos no son válidos, 404 si no existe y 409 si el
    // DNI es de otra cuenta (ManejadorGlobalErrores). Devuelve la ficha actualizada, como el GET de la HU20.
    @PutMapping("/admin/usuarios/{id}")
    public UsuarioDetalleResponse editar(@PathVariable String id, @Valid @RequestBody EditarUsuarioRequest request) {
        return UsuarioDetalleResponse.desde(administracionUsuarios.editar(id, request.aComando()));
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

    // Crear un administrador (HU21).  
    @PostMapping("/admin/admins")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearAdministrador(@Valid @RequestBody CrearAdministradorRequest request) {
        crearAdministrador.crear(new CrearAdministradorCommand(
                request.nombre(), request.apellidos(), request.email(), request.avatar(), request.password()));
    }

    // Supervisión de vendedores, solo lectura (HU26): el listado de la HU19 con el rol VENDEDOR fijo.
    @GetMapping("/admin/sellers")
    public VendedorPageResponse listarVendedores(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("La página y el tamaño deben ser positivos");
        }
        // category: id de la categoría principal del vendedor (fallos de la demo del Sprint 1: antes se ignoraba)
        return VendedorPageResponse.desde(listadoUsuarios.listar(new ListarUsuariosQuery(
                search, Rol.VENDEDOR, status, category, page - 1, size, ordenacion(sort, order))),
                nombresDeCategorias());
    }

    private static ListarUsuariosQuery.Ordenacion ordenacion(String sort, String order) {
        ListarUsuariosQuery.Orden campo = orden(sort);
        return campo == null ? null : new ListarUsuariosQuery.Ordenacion(campo, "desc".equals(order));
    }

    // Ficha de un vendedor (HU26). 404 si no existe o no es vendedor.
    // PENDIENTE: /admin/sellers/{id}/products y /orders, cuando existan los productos y los pedidos.
    @GetMapping("/admin/sellers/{id}")
    public VendedorResponse consultarVendedor(@PathVariable String id) {
        return VendedorResponse.desde(administracionUsuarios.consultarVendedor(id), nombresDeCategorias());
    }

    // Activar o desactivar un vendedor desde su supervisión (HU21). Misma validación que el PATCH de usuarios
    // (CambiarEstadoRequest). 404 si no existe o no es vendedor. Devuelve la ficha, como el GET de la HU26.
    @PatchMapping("/admin/sellers/{id}/status")
    public VendedorResponse cambiarEstadoVendedor(@PathVariable String id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        Usuario vendedor = "DESACTIVADO".equals(request.estado())
                ? administracionUsuarios.desactivarVendedor(id)
                : administracionUsuarios.activarVendedor(id);
        return VendedorResponse.desde(vendedor, nombresDeCategorias());
    }

    // Nombre de cada categoría por su identificador, para traducir la categoría principal de los vendedores.
    private Map<String, String> nombresDeCategorias() {
        return consultaCategorias.listar().stream().collect(Collectors.toMap(Categoria::id, Categoria::nombre));
    }

    private static ListarUsuariosQuery.Orden orden(String sort) {
        if ("name".equals(sort)) {
            return ListarUsuariosQuery.Orden.NOMBRE;
        }
        if ("status".equals(sort)) {
            return ListarUsuariosQuery.Orden.ESTADO;
        }
        if ("category".equals(sort)) {
            return ListarUsuariosQuery.Orden.CATEGORIA;
        }
        return null;
    }
}
