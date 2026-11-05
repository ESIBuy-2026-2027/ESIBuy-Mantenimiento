package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.metodospago;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;

/** Adaptador de salida que pregunta al módulo de métodos de pago (HU6) por su puerto de entrada. */
public class MetodosPagoAdapter implements MetodosPagoPort {

    private final GestionMetodosPagoUseCase gestionMetodosPago;

    public MetodosPagoAdapter(GestionMetodosPagoUseCase gestionMetodosPago) {
        this.gestionMetodosPago = gestionMetodosPago;
    }

    @Override
    public boolean esDelCliente(String emailCliente, String metodoId) {
        return gestionMetodosPago.esDelCliente(emailCliente, metodoId);
    }
}
