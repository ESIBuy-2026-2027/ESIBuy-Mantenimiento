package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.ArrayList;

import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import es.uclm.esibuy.backend.deseos.domain.model.TipoListaDeseos;

/** Convierte entre la lista de deseos del dominio y su documento de MongoDB, donde el cliente es usuarioId. */
public class ListaDeseosDocumentMapper {

    public ListaDeseosDocument aDocumento(ListaDeseos lista) {
        ListaDeseosDocument documento = new ListaDeseosDocument();
        documento.setId(lista.getId());
        documento.setUsuarioId(lista.getClienteId());
        documento.setTipo(lista.getTipo().name());
        documento.setNombre(lista.getNombre());
        documento.setItems(new ArrayList<>(lista.getItems().stream().map(ListaDeseosDocumentMapper::aItem).toList()));
        return documento;
    }

    public ListaDeseos aDominio(ListaDeseosDocument documento) {
        return new ListaDeseos(documento.getId(), documento.getUsuarioId(),
                TipoListaDeseos.valueOf(documento.getTipo()), documento.getNombre(),
                documento.getItems().stream()
                        .map(item -> new ItemDeseo(item.getProductoId(), item.getAddedAt(), item.getCantidad()))
                        .toList());
    }

    private static ItemDeseoDocument aItem(ItemDeseo itemDeseo) {
        ItemDeseoDocument item = new ItemDeseoDocument();
        item.setProductoId(itemDeseo.productoId());
        item.setAddedAt(itemDeseo.addedAt());
        item.setCantidad(itemDeseo.cantidad());
        return item;
    }
}
