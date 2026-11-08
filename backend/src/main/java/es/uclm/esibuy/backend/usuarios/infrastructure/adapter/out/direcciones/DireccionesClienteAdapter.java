package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.direcciones;

import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.DireccionesClientePort;

/** Adaptador de salida hacia el módulo direcciones, para borrar lo del cliente al eliminar su cuenta (HU25). */
public class DireccionesClienteAdapter implements DireccionesClientePort {

    private final GestionDireccionesUseCase direcciones;

    public DireccionesClienteAdapter(GestionDireccionesUseCase direcciones) {
        this.direcciones = direcciones;
    }

    @Override
    public void eliminarTodas(String emailCliente) {
        direcciones.eliminarTodas(emailCliente);
    }
}
