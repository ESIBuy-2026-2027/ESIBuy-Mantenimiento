package es.uclm.esibuy.backend.deseos.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.deseos.application.port.in.DeseoDetallado;
import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.in.ListaDeseosDetallada;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductoDeseado;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.deseos.domain.exception.ListaDeseosNoEncontradaException;
import es.uclm.esibuy.backend.deseos.domain.exception.ProductoNoDisponibleException;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Implementa la gestión de las listas de deseos del cliente (HU14, PBI 534), hablando con el exterior solo por los
 * puertos de salida. El reloj se inyecta para fijar en los tests la fecha en que se añade un producto.
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
        comprobarDisponible(productoId);
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
        listaDeseosRepository.eliminarPorCliente(clienteEnSesion.idPorCorreo(emailCliente));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<ListaDeseosDetallada> consultarTodas(String emailCliente) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        boolean premium = clienteEnSesion.esPremium(emailCliente);
        List<ListaDeseos> listas = listaDeseosRepository.buscarTodasPorCliente(clienteId);
        if (listas.isEmpty()) {
            listas = List.of(listaDeseosRepository.guardar(ListaDeseos.nueva(clienteId, ListaDeseos.NOMBRE_POR_DEFECTO)));
        }
        return listas.stream()
                .map(lista -> detallarConRol(lista, premium))
                .toList();
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada crearLista(String emailCliente, String nombre) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        ListaDeseos guardada = listaDeseosRepository.guardar(ListaDeseos.nueva(clienteId, nombre));
        return detallarConRol(guardada, clienteEnSesion.esPremium(emailCliente));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada renombrarLista(String emailCliente, String listaId, String nombre) {
        ListaDeseos lista = listaPropia(emailCliente, listaId);
        lista.renombrar(nombre);
        return detallar(listaDeseosRepository.guardar(lista), emailCliente);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public void eliminarLista(String emailCliente, String listaId) {
        ListaDeseos lista = listaPropia(emailCliente, listaId);
        listaDeseosRepository.eliminar(lista.getId(), lista.getClienteId());
        if (listaDeseosRepository.buscarTodasPorCliente(lista.getClienteId()).isEmpty()) {
            listaDeseosRepository.guardar(ListaDeseos.nueva(lista.getClienteId(), ListaDeseos.NOMBRE_POR_DEFECTO));
        }
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada anadirALista(String emailCliente, String listaId, String productoId) {
        comprobarDisponible(productoId);
        ListaDeseos lista = listaPropia(emailCliente, listaId);
        lista.anadir(productoId, Instant.now(reloj));
        return detallar(listaDeseosRepository.guardar(lista), emailCliente);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada quitarDeLista(String emailCliente, String listaId, String productoId) {
        ListaDeseos lista = listaPropia(emailCliente, listaId);
        if (lista.quitar(productoId)) {
            lista = listaDeseosRepository.guardar(lista);
        }
        return detallar(lista, emailCliente);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public ListaDeseosDetallada actualizarCantidad(String emailCliente, String listaId, String productoId,
            int cantidad) {
        ListaDeseos lista = listaPropia(emailCliente, listaId);
        boolean premium = clienteEnSesion.esPremium(emailCliente);
        ProductoDeseado producto = productos.consultarResumen(productoId, premium)
                .filter(ProductoDeseado::disponible)
                .orElseThrow(() -> new ProductoNoDisponibleException("El producto no está disponible"));
        lista.cambiarCantidad(productoId, cantidad, producto.stock());
        return detallarConRol(listaDeseosRepository.guardar(lista), premium);
    }

    private void comprobarDisponible(String productoId) {
        if (!productos.estaDisponible(productoId)) {
            throw new ProductoNoDisponibleException("El producto no está disponible");
        }
    }

    private ListaDeseos listaPropia(String emailCliente, String listaId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return listaDeseosRepository.buscarPorIdYCliente(listaId, clienteId)
                .orElseThrow(ListaDeseosNoEncontradaException::new);
    }

    // La lista principal del cliente en sesión, o una vacía sin guardar si todavía no tiene
    private ListaDeseos listaDe(String emailCliente) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return listaDeseosRepository.buscarPorCliente(clienteId).orElseGet(() -> ListaDeseos.vacia(clienteId));
    }

    // Antes de enseñarla, quita de la lista los productos eliminados y la guarda si ha cambiado
    private ListaDeseosDetallada detallar(ListaDeseos lista, String emailCliente) {
        return detallarConRol(lista, clienteEnSesion.esPremium(emailCliente));
    }

    private ListaDeseosDetallada detallarConRol(ListaDeseos lista, boolean premium) {
        Map<String, ProductoDeseado> existentes = productosExistentes(lista, premium);
        if (lista.conservarSolo(existentes.keySet())) {
            listaDeseosRepository.guardar(lista);
        }
        return new ListaDeseosDetallada(lista.getId(), lista.getNombre(), lista.getItems().stream()
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
                producto.stock(), producto.disponible(), item.addedAt(), item.cantidad());
    }
}
