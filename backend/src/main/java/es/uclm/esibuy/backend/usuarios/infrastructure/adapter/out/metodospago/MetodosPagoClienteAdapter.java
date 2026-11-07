package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.metodospago;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.MetodosPagoClientePort;

/** Adaptador de salida hacia el módulo metodospago, para borrar lo del cliente al eliminar su cuenta (HU25). */
public class MetodosPagoClienteAdapter implements MetodosPagoClientePort {

    private final GestionMetodosPagoUseCase metodosPago;

    public MetodosPagoClienteAdapter(GestionMetodosPagoUseCase metodosPago) {
        this.metodosPago = metodosPago;
    }

    @Override
    public void eliminarTodos(String emailCliente) {
        metodosPago.eliminarTodos(emailCliente);
    }
}
