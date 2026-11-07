package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.carrito;

import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CarritoClientePort;

/** Adaptador de salida hacia el módulo carrito, para borrar lo del cliente al eliminar su cuenta (HU25). */
public class CarritoClienteAdapter implements CarritoClientePort {

    private final GestionCarritoUseCase carrito;

    public CarritoClienteAdapter(GestionCarritoUseCase carrito) {
        this.carrito = carrito;
    }

    @Override
    public void eliminar(String emailCliente) {
        carrito.eliminar(emailCliente);
    }
}
