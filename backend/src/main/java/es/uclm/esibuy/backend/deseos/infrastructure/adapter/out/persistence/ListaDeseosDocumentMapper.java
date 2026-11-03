package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.List;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Convierte entre la lista de deseos del dominio y su documento de MongoDB. */
public class ListaDeseosDocumentMapper {

    public ListaDeseosDocument aDocumento(ListaDeseos lista) {
        return new ListaDeseosDocument();
    }

    public ListaDeseos aDominio(ListaDeseosDocument documento) {
        return new ListaDeseos(null, null, List.of());
    }
}
