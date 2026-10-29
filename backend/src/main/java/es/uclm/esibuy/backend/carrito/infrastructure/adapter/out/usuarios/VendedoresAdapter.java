package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.carrito.application.port.out.VendedoresPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;

/** Adaptador de salida que pide al módulo de usuarios el nombre comercial de cada proveedor del carrito. */
public class VendedoresAdapter implements VendedoresPort {

    private final AdministracionUsuariosUseCase administracionUsuarios;

    public VendedoresAdapter(AdministracionUsuariosUseCase administracionUsuarios) {
        this.administracionUsuarios = administracionUsuarios;
    }

    @Override
    public String nombreComercial(String vendedorId) {
        return administracionUsuarios.consultarNombreComercial(vendedorId);
    }
}
