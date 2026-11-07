package es.uclm.esibuy.backend.usuarios.application.service;

import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CarritoClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.DireccionesClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ListaDeseosClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.MetodosPagoClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.PedidosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.PreferenciasClientePort;
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
    private final ListaDeseosClientePort listaDeseos;
    private final CarritoClientePort carrito;
    private final PreferenciasClientePort preferencias;
    private final MetodosPagoClientePort metodosPago;
    private final DireccionesClientePort direcciones;

    public GestionCuentaService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            ProductosVendedorPort productos, PedidosVendedorPort productosConPedidos,
            ValoracionesProductoPort valoraciones, FactorAutenticacionRepositoryPort factores,
            CodigoVerificacionRepositoryPort codigosVerificacion,
            CodigoRecuperacionRepositoryPort codigosRecuperacion, ListaDeseosClientePort listaDeseos,
            CarritoClientePort carrito, PreferenciasClientePort preferencias, MetodosPagoClientePort metodosPago,
            DireccionesClientePort direcciones) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
        this.productos = productos;
        this.consultaProductosConPedidos = productosConPedidos;
        this.valoraciones = valoraciones;
        this.factores = factores;
        this.codigosVerificacion = codigosVerificacion;
        this.codigosRecuperacion = codigosRecuperacion;
        this.listaDeseos = listaDeseos;
        this.carrito = carrito;
        this.preferencias = preferencias;
        this.metodosPago = metodosPago;
        this.direcciones = direcciones;
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
        } else {
            eliminarDatosDelCliente(email);
        }
        // El usuario se borra el último: si algo falla antes, se puede repetir la eliminación
        factores.eliminarPorUsuario(usuario.getId());
        codigosVerificacion.eliminarPorUsuario(usuario.getId());
        codigosRecuperacion.eliminarPorUsuario(usuario.getId());
        usuarioRepository.eliminarPorId(usuario.getId());
    }

    // Lo que depende del cliente (correo del profesor, 06/11/2026). Se conservan sus pedidos y valoraciones para las
    // estadísticas y los históricos. Los otros módulos sacan el id del correo, así que va antes de borrar al usuario.
    private void eliminarDatosDelCliente(String email) {
        listaDeseos.eliminar(email);
        carrito.eliminar(email);
        preferencias.eliminar(email);
        metodosPago.eliminarTodos(email);
        direcciones.eliminarTodas(email);
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
