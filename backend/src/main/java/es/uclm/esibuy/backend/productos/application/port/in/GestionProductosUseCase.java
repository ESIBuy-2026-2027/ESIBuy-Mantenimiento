package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/** Gestión de los productos propios por parte del vendedor (HU31 alta; HU34 y HU35 añadirán más). */
public interface GestionProductosUseCase {

    Producto crear(CrearProductoCommand comando);
}