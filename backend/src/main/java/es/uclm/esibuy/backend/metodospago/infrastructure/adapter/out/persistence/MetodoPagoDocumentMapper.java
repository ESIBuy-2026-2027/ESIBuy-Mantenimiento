package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;

/**
 * Convierte entre la entidad de dominio y el documento de MongoDB. Al leer se vuelve a pasar por el
 * constructor de MetodoPago, así que un documento que no cumpla las reglas del dominio se rechaza.
 */
public class MetodoPagoDocumentMapper {

    public MetodoPagoDocument aDocumento(MetodoPago metodo) {
        MetodoPagoDocument documento = new MetodoPagoDocument();
        documento.setId(metodo.getId());
        documento.setClienteId(metodo.getClienteId());
        documento.setTipo(metodo.getTipo().name());
        documento.setMascara(metodo.getMascara());
        documento.setDatosCifrados(metodo.getDatosCifrados());
        documento.setPredeterminado(metodo.isPredeterminado());
        return documento;
    }

    public MetodoPago aDominio(MetodoPagoDocument documento) {
        return new MetodoPago(documento.getId(), documento.getClienteId(), aTipo(documento.getTipo()),
                documento.getMascara(), documento.getDatosCifrados(), documento.isPredeterminado());
    }

    // Un tipo que no existe en el dominio es un documento inválido, igual que un campo obligatorio vacío
    private static TipoMetodoPago aTipo(String tipo) {
        if (tipo == null) {
            return null; // el constructor de MetodoPago lo rechaza como tipo obligatorio
        }
        try {
            return TipoMetodoPago.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            throw new MetodoPagoInvalidoException("El tipo de método de pago guardado no es válido: " + tipo);
        }
    }
}
