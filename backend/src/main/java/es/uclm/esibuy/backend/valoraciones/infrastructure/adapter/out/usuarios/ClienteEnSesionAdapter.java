package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;

/** Adaptador hacia el módulo de usuarios, sin acceder directamente a su persistencia. */
public class ClienteEnSesionAdapter implements ClienteEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public ClienteEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return autenticacion.consultarIdUsuarioEnSesion(email);
    }

    @Override
    public String nombreCompletoPorCorreo(String email) {
        DatosPersonales datos = autenticacion.consultarUsuarioEnSesion(email).getDatosPersonales();
        return datos.nombre() + " " + datos.apellidos();
    }
}
