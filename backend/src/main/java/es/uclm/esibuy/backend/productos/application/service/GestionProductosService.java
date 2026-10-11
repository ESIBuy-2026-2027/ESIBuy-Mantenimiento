package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

public class GestionProductosService implements GestionProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public GestionProductosService(ProductoRepositoryPort productoRepository, UsuarioEnSesionPort usuarioEnSesion) {
        this.productoRepository = productoRepository;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @Override
    public Producto crear(CrearProductoCommand comando) {
        throw new UnsupportedOperationException("pendiente");
    }
}