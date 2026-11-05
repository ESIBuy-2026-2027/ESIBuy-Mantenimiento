package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.util.ArrayList;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Convierte entre el pedido del dominio y su documento de MongoDB. Al leer se vuelve a pasar por los
 * constructores del dominio, así que un documento que no cumpla sus reglas se rechaza. El total y los subtotales
 * se guardan para consultarlos en la base de datos, pero al leer se recalculan desde las líneas.
 */
public class PedidoDocumentMapper {

    public PedidoDocument aDocumento(Pedido pedido) {
        PedidoDocument documento = new PedidoDocument();
        documento.setId(pedido.getId());
        documento.setNumeroPedido(pedido.getNumeroPedido().valor());
        documento.setClienteId(pedido.getClienteId());
        documento.setVendedorId(pedido.getVendedorId());
        documento.setNombreComercial(pedido.getNombreComercial());
        documento.setEstado(pedido.getEstado().name());
        documento.setLineas(new ArrayList<>(pedido.getLineas().stream()
                .map(linea -> aDocumento(linea, pedido.getVendedorId())).toList()));
        documento.setTotal(pedido.getTotal());
        documento.setDireccionEnvio(aDocumento(pedido.getDireccionEnvio()));
        documento.setPaymentMethodId(pedido.getPaymentMethodId());
        documento.setCreatedAt(pedido.getCreatedAt());
        return documento;
    }

    public Pedido aDominio(PedidoDocument documento) {
        return new Pedido(documento.getId(), new NumeroPedido(documento.getNumeroPedido()), documento.getClienteId(),
                documento.getVendedorId(), documento.getNombreComercial(), aEstado(documento.getEstado()),
                documento.getLineas().stream()
                        .map(linea -> new LineaPedido(linea.getProductoId(), linea.getNombreProducto(),
                                linea.getCantidad(), linea.getPrecioUnitario()))
                        .toList(),
                aDominio(documento.getDireccionEnvio()), documento.getPaymentMethodId(), documento.getCreatedAt());
    }

    private static LineaPedidoDocument aDocumento(LineaPedido linea, String vendedorId) {
        LineaPedidoDocument documento = new LineaPedidoDocument();
        documento.setProductoId(linea.productoId());
        documento.setNombreProducto(linea.nombreProducto());
        documento.setVendedorId(vendedorId);
        documento.setCantidad(linea.cantidad());
        documento.setPrecioUnitario(linea.precioUnitario());
        documento.setSubtotal(linea.subtotal());
        return documento;
    }

    private static DireccionEnvioDocument aDocumento(DireccionEnvioPedido direccion) {
        DireccionEnvioDocument documento = new DireccionEnvioDocument();
        documento.setDestinatario(direccion.destinatario());
        documento.setCalle(direccion.calle());
        documento.setDetalles(direccion.detalles());
        documento.setCodigoPostal(direccion.codigoPostal());
        documento.setCiudad(direccion.ciudad());
        documento.setProvincia(direccion.provincia());
        documento.setTelefono(direccion.telefono());
        return documento;
    }

    private static DireccionEnvioPedido aDominio(DireccionEnvioDocument documento) {
        if (documento == null) {
            return null; // el constructor de Pedido lo rechaza como dirección obligatoria
        }
        return new DireccionEnvioPedido(documento.getDestinatario(), documento.getCalle(), documento.getDetalles(),
                documento.getCodigoPostal(), documento.getCiudad(), documento.getProvincia(), documento.getTelefono());
    }

    // Un estado que no existe en el dominio es un documento inválido
    private static EstadoPedido aEstado(String estado) {
        try {
            return estado == null ? null : EstadoPedido.valueOf(estado);
        } catch (IllegalArgumentException e) {
            throw new PedidoInvalidoException("El estado del pedido guardado no es válido: " + estado);
        }
    }
}
