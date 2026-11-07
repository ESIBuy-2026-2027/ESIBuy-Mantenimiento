package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.deseos;

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.ListaDeseosClientePort;

/** Adaptador de salida hacia el módulo deseos, para borrar lo del cliente al eliminar su cuenta (HU25). */
public class ListaDeseosClienteAdapter implements ListaDeseosClientePort {

    private final GestionListaDeseosUseCase listaDeseos;

    public ListaDeseosClienteAdapter(GestionListaDeseosUseCase listaDeseos) {
        this.listaDeseos = listaDeseos;
    }

    @Override
    public void eliminar(String emailCliente) {
        listaDeseos.eliminar(emailCliente);
    }
}
