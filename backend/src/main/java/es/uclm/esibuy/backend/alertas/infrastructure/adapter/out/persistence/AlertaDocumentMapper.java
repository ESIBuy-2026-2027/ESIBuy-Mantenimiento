package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.alertas.domain.model.DetalleAlerta;
import es.uclm.esibuy.backend.alertas.domain.model.TipoAlerta;

/** Convierte entre la alerta del dominio y su documento de MongoDB (AB#529). */
public class AlertaDocumentMapper {

    public AlertaDocument aDocumento(Alerta alerta) {
        AlertaDocument documento = new AlertaDocument();
        documento.setId(alerta.getId());
        documento.setClienteId(alerta.getClienteId());
        documento.setProductoId(alerta.getProductoId());
        documento.setTipo(alerta.getTipo().name());
        documento.setFecha(alerta.getFecha());
        documento.setLeida(alerta.isLeida());
        documento.setNombreProducto(alerta.getDetalle().nombreProducto());
        documento.setImagenProducto(alerta.getDetalle().imagenProducto());
        documento.setPrecioAnterior(alerta.getDetalle().precioAnterior());
        documento.setPrecioNuevo(alerta.getDetalle().precioNuevo());
        return documento;
    }

    public Alerta aDominio(AlertaDocument documento) {
        return new Alerta(documento.getId(), documento.getClienteId(), documento.getProductoId(),
                TipoAlerta.valueOf(documento.getTipo()), documento.getFecha(), documento.isLeida(),
                new DetalleAlerta(documento.getNombreProducto(), documento.getImagenProducto(),
                        documento.getPrecioAnterior(), documento.getPrecioNuevo()));
    }
}
