package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;

public class GestionProductosService implements GestionProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public GestionProductosService(ProductoRepositoryPort productoRepository, UsuarioEnSesionPort usuarioEnSesion) {
        this.productoRepository = productoRepository;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @Override
    public Producto crear(CrearProductoCommand comando) {
        String vendedorId = usuarioEnSesion.idPorCorreo(comando.emailVendedor());

        // Las relgas del producto se validan en dominio, si huubiese algo mal, saltaría la excepción desde el constructor de Producto. No se valida que el vendedor exista, porque se asume que el correo de la sesión es válido.
        Producto producto = new Producto(
                null,
                vendedorId,
                comando.nombre(),
                comando.descripcion(),
                comando.precio(),
                comando.stock(),
                comando.categoriaId(),
                comando.imagen(),
                comando.disponible(),
                comando.descuento(),
                comando.descuentoPremium()
        );
        if (productoRepository.existePorVendedorYNombre(vendedorId, producto.getNombre())) {
            throw new ProductoDuplicadoException();
        }
        return productoRepository.guardar(producto);
    }
}