package es.uclm.esibuy.backend.usuarios.application.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.TipoClienteUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;

/** Qué clientes son premium, para las alertas de bajada de precio (AB#529). */
public class TipoClienteService implements TipoClienteUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public TipoClienteService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Lo consulta el vendedor que rebaja su producto o el cliente que cancela un pedido (repone stock). Una consulta
    // por cliente: son los que tienen un producto en favoritos, pocos a la escala del proyecto
    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR)
    @Override
    public Set<String> premiumEntre(Collection<String> clienteIds) {
        return clienteIds.stream()
                .filter(id -> usuarioRepository.buscarPorId(id)
                        .map(usuario -> esPremium(usuario.getDatosCliente()))
                        .orElse(false))
                .collect(Collectors.toSet());
    }

    private static boolean esPremium(DatosCliente datos) {
        return datos != null && datos.tipoCliente() == TipoCliente.PREMIUM;
    }
}
