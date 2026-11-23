package es.uclm.esibuy.backend.valoraciones.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ConsultaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.EstadoValoracion;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Consulta valoraciones públicas para clientes y el estado particular del cliente autenticado. */
public class ConsultaValoracionesService implements ConsultaValoracionesUseCase {

    private final ValoracionRepositoryPort valoracionRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final ComprasPort compras;

    public ConsultaValoracionesService(ValoracionRepositoryPort valoracionRepository,
            ClienteEnSesionPort clienteEnSesion, ComprasPort compras) {
        this.valoracionRepository = valoracionRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.compras = compras;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public List<Valoracion> consultarPorProducto(String productoId) {
        return valoracionRepository.buscarPorProducto(productoId);
    }

    @PreAuthorize("hasRole('CLIENTE') and #emailCliente == authentication.name")
    @Override
    public EstadoValoracion consultarEstado(String emailCliente, String productoId) {
        boolean haComprado = compras.haComprado(emailCliente, productoId);
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return valoracionRepository.buscarPorProducto(productoId).stream()
                .filter(valoracion -> valoracion.clienteId().equals(clienteId))
                .findFirst()
                .map(valoracion -> new EstadoValoracion(haComprado, true, valoracion.id()))
                .orElseGet(() -> new EstadoValoracion(haComprado, false, null));
    }
}
