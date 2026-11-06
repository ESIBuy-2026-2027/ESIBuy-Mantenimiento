package es.uclm.esibuy.backend.incidencias.application.service;

import java.time.Clock;

import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/** Esqueleto del ciclo rojo: todavía no crea nada ni comprueba permisos. */
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

    @Override
    public Incidencia crear(CrearIncidenciaCommand comando) {
        return null;
    }
}