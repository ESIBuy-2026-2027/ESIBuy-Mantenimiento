package es.uclm.esibuy.backend.incidencias.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.incidencias.application.port.in.ResolucionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Resolución de las incidencias por el administrador (HU50): abrirlas con su respuesta y cerrarlas. */
public class ResolucionIncidenciasService implements ResolucionIncidenciasUseCase {

    private final IncidenciaRepositoryPort incidenciaRepository;

    public ResolucionIncidenciasService(IncidenciaRepositoryPort incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public Incidencia abrir(String id, String respuesta) {
        Incidencia incidencia = incidenciaRepository.buscarPorId(id).orElseThrow(IncidenciaNoEncontradaException::new);
        return incidenciaRepository.guardar(incidencia.abrir(respuesta));
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public void cerrar(String id) {
        throw new UnsupportedOperationException("Pendiente de implementar (HU50)");
    }
}
