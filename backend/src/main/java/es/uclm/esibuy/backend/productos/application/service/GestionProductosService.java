package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ModificarProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class GestionProductosService implements GestionProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public GestionProductosService(ProductoRepositoryPort productoRepository, UsuarioEnSesionPort usuarioEnSesion) {
        this.productoRepository = productoRepository;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO_EN_COMANDO)
    @Override
    public Producto crear(CrearProductoCommand comando) {
        String vendedorId = usuarioEnSesion.idPorCorreo(comando.emailVendedor());

        // Las reglas del producto se validan en el dominio: si algo está mal, el constructor de Producto lanza
        // la excepción y no se guarda nada. No se comprueba que el vendedor exista: el correo viene de la sesión.
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
        // Comprobación previa para dar el 409 en el caso normal; el índice único de MongoDB cubre la carrera.
        if (productoRepository.existePorVendedorYNombre(vendedorId, producto.getNombre())) {
            throw new ProductoDuplicadoException();
        }
        return productoRepository.guardar(producto);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public Producto consultarMiProducto(String emailVendedor, String productoId) {
        String vendedorId = usuarioEnSesion.idPorCorreo(emailVendedor);
        // Un producto de otro vendedor se trata igual que uno inexistente, para no revelar que existe.
        return productoRepository.buscarPorId(productoId)
                .filter(producto -> producto.getVendedorId().equals(vendedorId))
                .orElseThrow(ProductoNoEncontradoException::new);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO_EN_COMANDO)
    @Override
    public Producto modificarMiProducto(ModificarProductoCommand comando) {
        Producto actual = consultarMiProducto(comando.emailVendedor(), comando.productoId());
        // El id y el vendedor se conservan: así se actualiza el mismo documento y sigue siendo del mismo vendedor.
        Producto modificado = new Producto(
                actual.getId(),
                actual.getVendedorId(),
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
        // Solo se comprueba si cambia el nombre: si no, el repositorio encontraría el propio producto. Como el índice
        // único, no se distinguen mayúsculas; la carrera con otra modificación la sigue cubriendo ese índice.
        boolean cambiaElNombre = !modificado.getNombre().equalsIgnoreCase(actual.getNombre());
        if (cambiaElNombre
                && productoRepository.existePorVendedorYNombre(modificado.getVendedorId(), modificado.getNombre())) {
            throw new ProductoDuplicadoException();
        }
        return productoRepository.guardar(modificado);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public void eliminarMiProducto(String emailVendedor, String productoId) {
        // Si el producto no existe o es de otro vendedor, consultarMiProducto lanza la excepción y no se borra nada.
        Producto propio = consultarMiProducto(emailVendedor, productoId);
        productoRepository.eliminar(propio.getId());
    }
}
