package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.ArrayList;

import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Convierte entre la lista de deseos del dominio y su documento de MongoDB, donde el cliente es usuarioId. */
public class ListaDeseosDocumentMapper {

    public ListaDeseosDocument aDocumento(ListaDeseos lista) {
        ListaDeseosDocument documento = new ListaDeseosDocument();
        documento.setId(lista.getId());
        documento.setUsuarioId(lista.getClienteId());
        documento.setItems(new ArrayList<>(lista.getItems().stream().map(ListaDeseosDocumentMapper::aItem).toList()));
        return documento;
    }

    public ListaDeseos aDominio(ListaDeseosDocument documento) {
        return new ListaDeseos(documento.getId(), documento.getUsuarioId(), documento.getItems().stream()
                .map(item -> new ItemDeseo(item.getProductoId(), item.getAddedAt()))
                .toList());
    }

    private static ItemDeseoDocument aItem(ItemDeseo itemDeseo) {
        ItemDeseoDocument item = new ItemDeseoDocument();
        item.setProductoId(itemDeseo.productoId());
        item.setAddedAt(itemDeseo.addedAt());
        return item;
    }
}
