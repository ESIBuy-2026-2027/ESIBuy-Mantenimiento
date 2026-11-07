package es.uclm.esibuy.backend.valoraciones.application.service;

import es.uclm.esibuy.backend.valoraciones.application.port.in.LimpiezaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class LimpiezaValoracionesService implements LimpiezaValoracionesUseCase {

    private final ValoracionRepositoryPort valoracionRepository;

    public LimpiezaValoracionesService(ValoracionRepositoryPort valoracionRepository) {
        this.valoracionRepository = valoracionRepository;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR)
    @Override
    public void eliminarDeProducto(String productoId) {
        valoracionRepository.eliminarPorProducto(productoId);
    }
}
