package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/** Esqueleto del ciclo rojo: todavía no pregunta al módulo de usuarios. */
public class AutorEnSesionAdapter implements AutorEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public AutorEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return null;
    }
}