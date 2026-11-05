package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/**
 * Convierte entre la valoración del dominio y su documento de MongoDB: el clienteId del dominio es el usuarioId
 * del documento. Al leer se vuelve a pasar por el constructor de Valoracion, así que un documento que no cumpla
 * las reglas del dominio se rechaza.
 */
public class ValoracionDocumentMapper {

    public ValoracionDocument aDocumento(Valoracion valoracion) {
        ValoracionDocument documento = new ValoracionDocument();
        documento.setId(valoracion.id());
        documento.setProductoId(valoracion.productoId());
        documento.setUsuarioId(valoracion.clienteId());
        documento.setPuntuacion(valoracion.puntuacion());
        documento.setComentario(valoracion.comentario());
        return documento;
    }

    public Valoracion aDominio(ValoracionDocument documento) {
        return new Valoracion(documento.getId(), documento.getProductoId(), documento.getUsuarioId(),
                documento.getPuntuacion(), documento.getComentario());
    }
}
