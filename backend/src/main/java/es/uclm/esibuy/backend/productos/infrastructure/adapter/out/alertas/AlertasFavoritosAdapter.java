package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.alertas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto;
import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.AlertasFavoritosPort;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

/**
 * Adaptador de salida que avisa al módulo de alertas por su puerto de entrada (AB#529), sin pasarle el dominio de
 * productos: le envía el precio que paga un cliente estándar y uno premium, y si se puede comprar.
 *
 * <p>El cambio del producto ya está guardado cuando se llama: si generar las alertas falla, se deja en el log y no se
 * propaga, para que el vendedor (o el cliente que cancela un pedido) no reciba un error por algo que sí ha hecho.
 */
public class AlertasFavoritosAdapter implements AlertasFavoritosPort {

    private static final Logger LOG = LoggerFactory.getLogger(AlertasFavoritosAdapter.class);

    private final GeneracionAlertasUseCase generacionAlertas;

    public AlertasFavoritosAdapter(GeneracionAlertasUseCase generacionAlertas) {
        this.generacionAlertas = generacionAlertas;
    }

    @Override
    public void notificarCambio(Producto antes, Producto despues) {
        try {
            generacionAlertas.notificarCambio(new CambioProducto(despues.getId(), despues.getNombre(),
                    situacion(antes), situacion(despues)));
        } catch (RuntimeException e) {
            LOG.warn("No se han podido generar las alertas del producto {}", despues.getId(), e);
        }
    }

    private static CambioProducto.Situacion situacion(Producto producto) {
        return new CambioProducto.Situacion(producto.precioFinal(false), producto.precioFinal(true),
                producto.esComprable());
    }
}
