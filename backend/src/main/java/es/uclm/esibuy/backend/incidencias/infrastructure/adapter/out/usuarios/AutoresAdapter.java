package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios;

import java.util.Optional;

import es.uclm.esibuy.backend.incidencias.application.port.out.AutoresPort;
import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;

/**
 * Busca el correo y el rol del autor de una incidencia (HU49). Pregunta al módulo de usuarios por su puerto de
 * entrada, sin usar su dominio.
 */
public class AutoresAdapter implements AutoresPort {

    private final AdministracionUsuariosUseCase administracionUsuarios;

    public AutoresAdapter(AdministracionUsuariosUseCase administracionUsuarios) {
        this.administracionUsuarios = administracionUsuarios;
    }

    @Override
    public Optional<AutorIncidencia> buscarAutor(String autorId) {
        return administracionUsuarios.consultarContacto(autorId)
                .map(contacto -> new AutorIncidencia(contacto.email(), contacto.rol()));
    }
}
