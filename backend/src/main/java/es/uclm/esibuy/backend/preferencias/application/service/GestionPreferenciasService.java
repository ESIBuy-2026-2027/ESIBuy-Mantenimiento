package es.uclm.esibuy.backend.preferencias.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.out.CategoriasPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;
import es.uclm.esibuy.backend.preferencias.domain.exception.PreferenciasInvalidasException;
import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Implementa la gestión de las preferencias del cliente (HU46), hablando con el exterior solo por los puertos de
 * salida.
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
        Set<String> activas = categorias.activas();
        return preferenciasRepository.buscarPorCliente(clienteEnSesion.idPorCorreo(emailCliente))
                .map(preferencias -> preferencias.getCategorias().stream().filter(activas::contains).toList())
                .orElse(List.of());
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<String> guardar(String emailCliente, List<String> elegidas) {
        if (elegidas == null) {
            throw new PreferenciasInvalidasException("Las categorías son obligatorias");
        }
        if (!categorias.activas().containsAll(elegidas)) {
            throw new PreferenciasInvalidasException("Alguna categoría no existe o no está activa");
        }
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        Preferencias preferencias = preferenciasRepository.buscarPorCliente(clienteId)
                .orElseGet(() -> Preferencias.ninguna(clienteId));
        preferencias.elegirCategorias(elegidas);
        return preferenciasRepository.guardar(preferencias).getCategorias();
    }
}
