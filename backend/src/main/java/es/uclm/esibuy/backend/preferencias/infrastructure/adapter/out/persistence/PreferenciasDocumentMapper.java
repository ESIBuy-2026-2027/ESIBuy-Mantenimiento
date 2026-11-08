package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence;

import java.util.ArrayList;

import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;

/** Convierte entre las preferencias del dominio y su documento de MongoDB. */
public class PreferenciasDocumentMapper {

    public PreferenciasDocument aDocumento(Preferencias preferencias) {
        PreferenciasDocument documento = new PreferenciasDocument();
        documento.setId(preferencias.getId());
        documento.setClienteId(preferencias.getClienteId());
        documento.setCategorias(new ArrayList<>(preferencias.getCategorias()));
        return documento;
    }

    public Preferencias aDominio(PreferenciasDocument documento) {
        return new Preferencias(documento.getId(), documento.getClienteId(), documento.getCategorias());
    }
}
