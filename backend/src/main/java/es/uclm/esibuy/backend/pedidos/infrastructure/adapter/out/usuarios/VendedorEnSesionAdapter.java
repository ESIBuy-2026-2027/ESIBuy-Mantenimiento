package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.pedidos.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

public class VendedorEnSesionAdapter implements VendedorEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public VendedorEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return autenticacion.consultarIdUsuarioEnSesion(email);
    }
}
