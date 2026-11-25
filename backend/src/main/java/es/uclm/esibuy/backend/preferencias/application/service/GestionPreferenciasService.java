package es.uclm.esibuy.backend.preferencias.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.out.CategoriasPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;
import es.uclm.esibuy.backend.preferencias.domain.exception.CategoriaNoDisponibleException;
import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Implementa la gestión de las preferencias del cliente (HU46), que son sus categorías favoritas (AB#528), hablando con
 * el exterior solo por los puertos de salida.
 */
public class GestionPreferenciasService implements GestionPreferenciasUseCase {

    private final PreferenciasRepositoryPort preferenciasRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final CategoriasPort categorias;

    public GestionPreferenciasService(PreferenciasRepositoryPort preferenciasRepository,
            ClienteEnSesionPort clienteEnSesion, CategoriasPort categorias) {
        this.preferenciasRepository = preferenciasRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.categorias = categorias;
    }

    // Una categoría que se ha desactivado o eliminado después de elegirla no sale, pero sigue guardada: si vuelve a
    // activarse, reaparece
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<String> consultar(String emailCliente) {
        return soloActivas(preferenciasDe(emailCliente), categorias.activas());
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<String> marcar(String emailCliente, String categoriaId) {
        Set<String> activas = categorias.activas();
        if (!activas.contains(categoriaId)) {
            throw new CategoriaNoDisponibleException("La categoría no está disponible");
        }
        Preferencias preferencias = preferenciasDe(emailCliente);
        if (preferencias.marcar(categoriaId)) {
            preferenciasRepository.guardar(preferencias);
        }
        return soloActivas(preferencias, activas);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<String> desmarcar(String emailCliente, String categoriaId) {
        Preferencias preferencias = preferenciasDe(emailCliente);
        if (preferencias.desmarcar(categoriaId)) {
            preferenciasRepository.guardar(preferencias);
        }
        return soloActivas(preferencias, categorias.activas());
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public void eliminar(String emailCliente) {
        preferenciasRepository.eliminarPorCliente(clienteEnSesion.idPorCorreo(emailCliente));
    }

    // Las preferencias del cliente en sesión, o unas vacías sin guardar si todavía no tiene
    private Preferencias preferenciasDe(String emailCliente) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return preferenciasRepository.buscarPorCliente(clienteId).orElseGet(() -> Preferencias.ninguna(clienteId));
    }

    private static List<String> soloActivas(Preferencias preferencias, Set<String> activas) {
        return preferencias.getCategorias().stream().filter(activas::contains).toList();
    }
}
