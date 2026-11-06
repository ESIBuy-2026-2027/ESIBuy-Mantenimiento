package es.uclm.esibuy.backend.incidencias.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Crea una incidencia en nombre del cliente o del vendedor en sesión (HU44). Solo habla con el exterior a través de
 * los puertos de salida. La fecha sale del reloj para que se pueda probar.
 */
public class CreacionIncidenciasService implements CreacionIncidenciasUseCase {

    private final IncidenciaRepositoryPort incidenciaRepository;
    private final AutorEnSesionPort autorEnSesion;
    private final Clock reloj;

    public CreacionIncidenciasService(IncidenciaRepositoryPort incidenciaRepository, AutorEnSesionPort autorEnSesion,
            Clock reloj) {
        this.incidenciaRepository = incidenciaRepository;
        this.autorEnSesion = autorEnSesion;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO_EN_COMANDO)
    @Override
    public Incidencia crear(CrearIncidenciaCommand comando) {
        String autorId = autorEnSesion.idPorCorreo(comando.email());
        Incidencia incidencia = Incidencia.nueva(autorId, comando.asunto(), comando.descripcion(),
                Instant.now(reloj));
        return incidenciaRepository.guardar(incidencia);
    }
}