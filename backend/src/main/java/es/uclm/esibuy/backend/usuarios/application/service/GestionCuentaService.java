package es.uclm.esibuy.backend.usuarios.application.service;

import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.ProductosConPedidosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosVendedorUseCase;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.application.port.in.LimpiezaValoracionesUseCase;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public class GestionCuentaService implements GestionCuentaUseCase {

    private static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final GestionProductosVendedorUseCase productos;
    private final ProductosConPedidosUseCase consultaProductosConPedidos;
    private final LimpiezaValoracionesUseCase valoraciones;
    private final FactorAutenticacionRepositoryPort factores;
    private final CodigoVerificacionRepositoryPort codigosVerificacion;
    private final CodigoRecuperacionRepositoryPort codigosRecuperacion;

    public GestionCuentaService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            GestionProductosVendedorUseCase productos, ProductosConPedidosUseCase productosConPedidos,
            LimpiezaValoracionesUseCase valoraciones, FactorAutenticacionRepositoryPort factores,
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

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public void eliminarCuentaVendedor(String email, String password) {
        Usuario vendedor = usuarioRepository.buscarPorCorreo(email)
                .filter(usuario -> usuario.getRol() == Rol.VENDEDOR)
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));
        if (!hashContrasena.coincide(password, vendedor.getPasswordHash())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        Set<String> productosDelVendedor = Set.copyOf(productos.desactivarTodos(vendedor.getId()));
        Set<String> productosConPedidos = consultaProductosConPedidos.buscarDeVendedor(vendedor.getId());
        productosDelVendedor.stream()
                .filter(productoId -> !productosConPedidos.contains(productoId))
                .forEach(productoId -> {
                    // Primero se limpian las valoraciones: así un fallo posterior permite reintentar sin huérfanos.
                    valoraciones.eliminarDeProducto(productoId);
                    productos.eliminar(productoId);
                });

        factores.eliminarPorUsuario(vendedor.getId());
        codigosVerificacion.eliminarPorUsuario(vendedor.getId());
        codigosRecuperacion.eliminarPorUsuario(vendedor.getId());
        usuarioRepository.eliminarPorId(vendedor.getId());
    }
}
