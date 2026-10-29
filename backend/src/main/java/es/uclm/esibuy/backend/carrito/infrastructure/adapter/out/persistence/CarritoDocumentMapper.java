package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import java.util.ArrayList;

import es.uclm.esibuy.backend.carrito.domain.model.Carrito;
import es.uclm.esibuy.backend.carrito.domain.model.LineaCarrito;

/**
 * Convierte entre el carrito del dominio y su documento de MongoDB. Al leer se vuelve a pasar por el
 * constructor de cada línea, así que una cantidad que no cumpla las reglas del dominio se rechaza.
 */
public class CarritoDocumentMapper {

    public CarritoDocument aDocumento(Carrito carrito) {
        CarritoDocument documento = new CarritoDocument();
        documento.setId(carrito.getId());
        documento.setClienteId(carrito.getClienteId());
        documento.setItems(new ArrayList<>(carrito.getLineas().stream().map(CarritoDocumentMapper::aItem).toList()));
        return documento;
    }

    public Carrito aDominio(CarritoDocument documento) {
        return new Carrito(documento.getId(), documento.getClienteId(), documento.getItems().stream()
                .map(item -> new LineaCarrito(item.getProductoId(), item.getCantidad(), item.getAddedAt(),
                        item.getUpdatedAt()))
                .toList());
    }

    private static ItemCarritoDocument aItem(LineaCarrito linea) {
        ItemCarritoDocument item = new ItemCarritoDocument();
        item.setProductoId(linea.productoId());
        item.setCantidad(linea.cantidad());
        item.setAddedAt(linea.addedAt());
        item.setUpdatedAt(linea.updatedAt());
        return item;
    }
}
