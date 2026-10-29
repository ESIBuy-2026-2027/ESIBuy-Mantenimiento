package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.direcciones.domain.model.CodigoPostal;
import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import es.uclm.esibuy.backend.direcciones.domain.model.TelefonoContacto;

/**
 * Convierte entre la entidad de dominio y el documento de MongoDB. Al leer se vuelve a pasar por los
 * constructores del dominio, así que un documento que no cumpla sus reglas se rechaza.
 */
public class DireccionDocumentMapper {

    public DireccionDocument aDocumento(DireccionEnvio direccion) {
        DatosDireccion datos = direccion.getDatos();
        DireccionDocument documento = new DireccionDocument();
        documento.setId(direccion.getId());
        documento.setClienteId(direccion.getClienteId());
        documento.setDestinatario(datos.destinatario());
        documento.setCalle(datos.calle());
        documento.setDetalles(datos.detalles());
        documento.setCodigoPostal(datos.codigoPostal().valor());
        documento.setCiudad(datos.ciudad());
        documento.setProvincia(datos.provincia());
        documento.setTelefono(datos.telefono().valor());
        documento.setPrincipal(direccion.isPrincipal());
        return documento;
    }

    public DireccionEnvio aDominio(DireccionDocument documento) {
        DatosDireccion datos = new DatosDireccion(documento.getDestinatario(), documento.getCalle(),
                documento.getDetalles(), new CodigoPostal(documento.getCodigoPostal()), documento.getCiudad(),
                documento.getProvincia(), new TelefonoContacto(documento.getTelefono()));
        return new DireccionEnvio(documento.getId(), documento.getClienteId(), datos, documento.isPrincipal());
    }
}
