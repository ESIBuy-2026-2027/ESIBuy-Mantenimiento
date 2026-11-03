package es.uclm.esibuy.backend.deseos.application.service;

import java.time.Clock;

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/**
 * Implementa la gestión de la lista de deseos del cliente (HU14), hablando con el exterior solo por los puertos
 * de salida. El reloj se inyecta para fijar en los tests la fecha en que se añade un producto.
 */
public class GestionListaDeseosService implements GestionListaDeseosUseCase {

    private final ListaDeseosRepositoryPort listaDeseosRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final ProductosPort productos;
    private final Clock reloj;

    public GestionListaDeseosService(ListaDeseosRepositoryPort listaDeseosRepository,
            ClienteEnSesionPort clienteEnSesion, ProductosPort productos, Clock reloj) {
        this.listaDeseosRepository = listaDeseosRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.productos = productos;
        this.reloj = reloj;
    }

    @Override
    public ListaDeseos anadir(String emailCliente, String productoId) {
        return null;
    }
}
