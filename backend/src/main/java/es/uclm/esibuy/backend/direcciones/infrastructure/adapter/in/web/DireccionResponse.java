package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;

/**
 * Lo que la API devuelve de una dirección de envío: sus datos y si es la principal. Nunca el cliente, que ya
 * es el de la sesión. Sin id ni principal, es el objeto direccionEnvio que copia el pedido (HU10).
 */
public record DireccionResponse(
        String id,
        String destinatario,
        String calle,
        String detalles,
        String codigoPostal,
        String ciudad,
        String provincia,
        String telefono,
        boolean principal) {

    public static DireccionResponse desde(DireccionEnvio direccion) {
        DatosDireccion datos = direccion.getDatos();
        return new DireccionResponse(direccion.getId(), datos.destinatario(), datos.calle(), datos.detalles(),
                datos.codigoPostal().valor(), datos.ciudad(), datos.provincia(), datos.telefono().valor(),
                direccion.isPrincipal());
    }
}
