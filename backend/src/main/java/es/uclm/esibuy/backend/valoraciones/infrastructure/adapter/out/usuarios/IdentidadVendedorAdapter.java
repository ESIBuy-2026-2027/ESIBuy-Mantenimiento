package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.IdentidadVendedorPort;

public class IdentidadVendedorAdapter implements IdentidadVendedorPort {

    private final AutenticacionUseCase autenticacion;

    public IdentidadVendedorAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String emailVendedor) {
        return autenticacion.consultarIdUsuarioEnSesion(emailVendedor);
    }
}
