package es.uclm.esibuy.backend.deseos.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.deseos.application.port.in.DeseoDetallado;
import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.in.ListaDeseosDetallada;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductoDeseado;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.deseos.domain.exception.ProductoNoDisponibleException;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

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

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseos anadir(String emailCliente, String productoId) {
        if (!productos.estaDisponible(productoId)) {
            throw new ProductoNoDisponibleException("El producto no está disponible");
        }
        ListaDeseos lista = listaDe(emailCliente);
        lista.anadir(productoId, Instant.now(reloj));
        return listaDeseosRepository.guardar(lista);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada consultar(String emailCliente) {
        return detallar(listaDe(emailCliente), emailCliente);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada quitar(String emailCliente, String productoId) {
        ListaDeseos lista = listaDe(emailCliente);
        if (lista.quitar(productoId)) {
            listaDeseosRepository.guardar(lista);
        }
        return detallar(lista, emailCliente);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public void eliminar(String emailCliente) {
    }

    // La lista del cliente en sesión, o una vacía sin guardar si todavía no tiene
    private ListaDeseos listaDe(String emailCliente) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return listaDeseosRepository.buscarPorCliente(clienteId).orElseGet(() -> ListaDeseos.vacia(clienteId));
    }

    // Antes de enseñarla, quita de la lista los productos eliminados y la guarda si ha cambiado
    private ListaDeseosDetallada detallar(ListaDeseos lista, String emailCliente) {
        Map<String, ProductoDeseado> existentes = productosExistentes(lista, clienteEnSesion.esPremium(emailCliente));
        if (lista.conservarSolo(existentes.keySet())) {
            listaDeseosRepository.guardar(lista);
        }
        return new ListaDeseosDetallada(lista.getItems().stream()
                .map(item -> detallar(item, existentes.get(item.productoId())))
                .toList());
    }

    // Los datos actuales de los productos de la lista que siguen existiendo, por su id
    private Map<String, ProductoDeseado> productosExistentes(ListaDeseos lista, boolean premium) {
        Map<String, ProductoDeseado> existentes = new HashMap<>();
        for (ItemDeseo item : lista.getItems()) {
            productos.consultarResumen(item.productoId(), premium)
                    .ifPresent(producto -> existentes.put(producto.id(), producto));
        }
        return existentes;
    }

    private static DeseoDetallado detallar(ItemDeseo item, ProductoDeseado producto) {
        return new DeseoDetallado(producto.id(), producto.nombre(), producto.imagen(), producto.precioFinal(),
                producto.stock(), producto.disponible(), item.addedAt());
    }
}
