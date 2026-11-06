package es.uclm.esibuy.backend.incidencias.application.service;

import es.uclm.esibuy.backend.incidencias.application.port.in.ConsultaIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

/** Esqueleto del ciclo rojo: todavía no consulta nada ni comprueba permisos. */
public class ConsultaIncidenciasService implements ConsultaIncidenciasUseCase {

    private final IncidenciaRepositoryPort incidenciaRepository;
    private final AutorEnSesionPort autorEnSesion;

    public ConsultaIncidenciasService(IncidenciaRepositoryPort incidenciaRepository, AutorEnSesionPort autorEnSesion) {
        this.incidenciaRepository = incidenciaRepository;
        this.autorEnSesion = autorEnSesion;
    }

    @Override
    public IncidenciaPage consultarPropias(String email, int pagina, int tamano) {
        return null;
    }
}