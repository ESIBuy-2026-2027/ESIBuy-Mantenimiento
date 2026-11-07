package es.uclm.esibuy.backend.usuarios.application.service;

import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.PedidosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ProductosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ValoracionesProductoPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public class GestionCuentaService implements GestionCuentaUseCase {

    private static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final ProductosVendedorPort productos;
    private final PedidosVendedorPort consultaProductosConPedidos;
    private final ValoracionesProductoPort valoraciones;
    private final FactorAutenticacionRepositoryPort factores;
    private final CodigoVerificacionRepositoryPort codigosVerificacion;
    private final CodigoRecuperacionRepositoryPort codigosRecuperacion;

    public GestionCuentaService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            ProductosVendedorPort productos, PedidosVendedorPort productosConPedidos,
            ValoracionesProductoPort valoraciones, FactorAutenticacionRepositoryPort factores,
            CodigoVerificacionRepositoryPort codigosVerificacion,
            CodigoRecuperacionRepositoryPort codigosRecuperacion) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
        this.productos = productos;
        this.consultaProductosConPedidos = productosConPedidos;
        this.valoraciones = valoraciones;
        this.factores = factores;
        this.codigosVerificacion = codigosVerificacion;
        this.codigosRecuperacion = codigosRecuperacion;
    }

    // Un único caso de uso para el cliente (HU25) y el vendedor (HU47), detrás de un único DELETE /api/perfil.
    // El permiso deja pasar solo a esos dos roles y con su propio correo.
    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO)
    @Override
    public void eliminarCuentaPropia(String email, String password) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));
        // 400 WRONG_PASSWORD y no 401: la sesión sigue abierta, así que no es una sesión caducada
        if (!hashContrasena.coincide(password, usuario.getPasswordHash())) {
            throw new ContrasenaActualIncorrectaException("La contraseña actual no es correcta");
        }

        if (usuario.getRol() == Rol.VENDEDOR) {
            eliminarProductosDelVendedor(usuario.getId());
        }
        // El usuario se borra el último: si algo falla antes, se puede repetir la eliminación
        factores.eliminarPorUsuario(usuario.getId());
        codigosVerificacion.eliminarPorUsuario(usuario.getId());
        codigosRecuperacion.eliminarPorUsuario(usuario.getId());
        usuarioRepository.eliminarPorId(usuario.getId());
    }

    // Se conservan como no disponibles los productos con pedidos; los demás se borran con sus valoraciones
    private void eliminarProductosDelVendedor(String vendedorId) {
        Set<String> productosDelVendedor = Set.copyOf(productos.desactivarTodos(vendedorId));
        Set<String> productosConPedidos = consultaProductosConPedidos.buscarDeVendedor(vendedorId);
        productosDelVendedor.stream()
                .filter(productoId -> !productosConPedidos.contains(productoId))
                .forEach(productoId -> {
                    // Primero se limpian las valoraciones: así un fallo posterior permite reintentar sin huérfanos.
                    valoraciones.eliminarDeProducto(productoId);
                    productos.eliminar(productoId);
                });
    }
}
