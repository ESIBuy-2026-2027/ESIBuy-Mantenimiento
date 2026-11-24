package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.metodospago;

import java.util.Optional;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodoPagoPedido;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;

/**
 * Adaptador de salida que pide al módulo de métodos de pago (HU6), por su puerto de entrada, el tipo y la máscara del
 * método elegido. Nunca recibe los datos de pago.
 */
public class MetodosPagoAdapter implements MetodosPagoPort {

    private final GestionMetodosPagoUseCase gestionMetodosPago;

    public MetodosPagoAdapter(GestionMetodosPagoUseCase gestionMetodosPago) {
        this.gestionMetodosPago = gestionMetodosPago;
    }

    @Override
    public Optional<MetodoPagoPedido> consultar(String emailCliente, String metodoId) {
        return gestionMetodosPago.consultarParaPedido(emailCliente, metodoId)
                .map(metodo -> new MetodoPagoPedido(metodo.tipo(), metodo.mascara()));
    }
}
