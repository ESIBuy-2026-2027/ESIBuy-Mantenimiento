package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Convierte entre el modelo de dominio y el documento MongoDB de una valoración. */
public class ValoracionDocumentMapper {

    public ValoracionDocument aDocumento(Valoracion valoracion) {
        ValoracionDocument documento = new ValoracionDocument();
        documento.setId(valoracion.id());
        documento.setProductoId(valoracion.productoId());
        documento.setUsuarioId(valoracion.clienteId());
        documento.setNombreCliente(valoracion.nombreCliente());
        documento.setPuntuacion(valoracion.puntuacion());
        documento.setComentario(valoracion.comentario());
        documento.setFechaCreacion(valoracion.fechaCreacion());
        return documento;
    }

    public Valoracion aDominio(ValoracionDocument documento) {
        return new Valoracion(documento.getId(), documento.getProductoId(), documento.getUsuarioId(),
                documento.getNombreCliente(), documento.getPuntuacion(), documento.getComentario(),
                documento.getFechaCreacion());
    }
}
