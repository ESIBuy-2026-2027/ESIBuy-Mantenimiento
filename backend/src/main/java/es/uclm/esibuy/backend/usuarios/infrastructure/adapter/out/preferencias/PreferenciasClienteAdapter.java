package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.preferencias;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.PreferenciasClientePort;

/** Adaptador de salida hacia el módulo preferencias, para borrar lo del cliente al eliminar su cuenta (HU25). */
public class PreferenciasClienteAdapter implements PreferenciasClientePort {

    private final GestionPreferenciasUseCase preferencias;

    public PreferenciasClienteAdapter(GestionPreferenciasUseCase preferencias) {
        this.preferencias = preferencias;
    }

    @Override
    public void eliminar(String emailCliente) {
        preferencias.eliminar(emailCliente);
    }
}
