package es.uclm.esibuy.backend.valoraciones.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ConsultaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Consulta las valoraciones de un producto (HU13). Cualquier cliente puede leerlas. */
public class ConsultaValoracionesService implements ConsultaValoracionesUseCase {

    private final ValoracionRepositoryPort valoracionRepository;

    public ConsultaValoracionesService(ValoracionRepositoryPort valoracionRepository) {
        this.valoracionRepository = valoracionRepository;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public List<Valoracion> consultarPorProducto(String productoId) {
        return valoracionRepository.buscarPorProducto(productoId);
    }
}
