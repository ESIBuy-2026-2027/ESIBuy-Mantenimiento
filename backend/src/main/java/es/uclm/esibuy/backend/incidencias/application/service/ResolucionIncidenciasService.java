package es.uclm.esibuy.backend.incidencias.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.incidencias.application.port.in.ResolucionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaResueltaException;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Resolución de las incidencias por el administrador: abrirlas con su respuesta y cerrarlas (HU50) y marcarlas como
 * resueltas (HU192). La fecha de resolución sale del reloj para que se pueda probar.
 */
public class ResolucionIncidenciasService implements ResolucionIncidenciasUseCase {

    private final IncidenciaRepositoryPort incidenciaRepository;

    private final Clock reloj;

    public ResolucionIncidenciasService(IncidenciaRepositoryPort incidenciaRepository, Clock reloj) {
        this.incidenciaRepository = incidenciaRepository;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public Incidencia abrir(String id, String respuesta) {
        return incidenciaRepository.guardar(buscar(id).abrir(respuesta));
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public Incidencia resolver(String id) {
        Incidencia resuelta = buscar(id).resolver(Instant.now(reloj));
        if (!incidenciaRepository.registrarResolucion(resuelta)) {
            // Otro administrador la ha cerrado (ya no existe) o la ha resuelto mientras tanto
            buscar(id);
            throw new IncidenciaYaResueltaException();
        }
        return resuelta;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public void cerrar(String id) {
        if (incidenciaRepository.buscarPorId(id).isEmpty()) {
            throw new IncidenciaNoEncontradaException();
        }
        incidenciaRepository.eliminar(id);
    }

    private Incidencia buscar(String id) {
        return incidenciaRepository.buscarPorId(id).orElseThrow(IncidenciaNoEncontradaException::new);
    }
}
