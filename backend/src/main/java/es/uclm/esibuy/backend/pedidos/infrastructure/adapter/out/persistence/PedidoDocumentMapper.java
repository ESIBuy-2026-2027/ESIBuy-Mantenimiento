package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import es.uclm.esibuy.backend.pedidos.domain.model.CambioEstado;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Convierte entre el pedido del dominio y su documento de MongoDB. Al leer se
 * vuelve a pasar por los
 * constructores del dominio, así que un documento que no cumpla sus reglas se
 * rechaza. El total y los subtotales
 * se guardan para consultarlos en la base de datos, pero al leer se recalculan
 * desde las líneas.
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
                .map(linea -> aDocumento(linea)).toList()));
        documento.setTotal(pedido.getTotal());
        documento.setDireccionEnvio(aDocumento(pedido.getDireccionEnvio()));
        documento.setPaymentMethodId(pedido.getPaymentMethodId());
        documento.setCreatedAt(pedido.getCreatedAt());
        documento.setHistorial(new ArrayList<>(pedido.getHistorial().stream()
                .map(cambio -> aDocumento(cambio)).toList()));
        documento.setFechaEstimadaEntrega(pedido.getFechaEstimadaEntrega());
        documento.setFechaEntrega(pedido.getFechaEntrega());
        return documento;
    }

    public Pedido aDominio(PedidoDocument documento) {
        DatosPedido datosPedido = new DatosPedido(
                documento.getId(),
                new NumeroPedido(documento.getNumeroPedido()),
                documento.getClienteId(),
                documento.getVendedorId(),
                documento.getNombreComercial(),
                aEstado(documento.getEstado()),
                documento.getLineas().stream()
                        .map(linea -> new LineaPedido(
                                linea.getProductoId(),
                                linea.getVendedorId(),
                                linea.getNombreProducto(),
                                linea.getCantidad(),
                                linea.getPrecioUnitario(),
                                linea.getImagen()))
                        .toList(),
                aDominio(documento.getDireccionEnvio()),
                documento.getPaymentMethodId(),
                documento.getCreatedAt(),
                aHistorial(documento),
                documento.getFechaEstimadaEntrega(),
                documento.getFechaEntrega());

        return new Pedido(datosPedido);
    }

    private static CambioEstadoDocument aDocumento(CambioEstado cambio) {
        CambioEstadoDocument documento = new CambioEstadoDocument();
        documento.setEstado(cambio.estado().name());
        documento.setFecha(cambio.fecha());
        return documento;
    }

    // Un pedido anterior al seguimiento no tiene histórico guardado
    private static List<CambioEstado> aHistorial(PedidoDocument documento) {
        if (documento.getHistorial() == null) {
            return List.of();
        }
        return documento.getHistorial().stream()
                .map(cambio -> new CambioEstado(aEstado(cambio.getEstado()), cambio.getFecha()))
                .toList();
    }

    private static LineaPedidoDocument aDocumento(LineaPedido linea) {
        LineaPedidoDocument documento = new LineaPedidoDocument();
        documento.setProductoId(linea.productoId());
        documento.setNombreProducto(linea.nombreProducto());
        documento.setVendedorId(vendedorId);
        documento.setImagen(linea.imagen());
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