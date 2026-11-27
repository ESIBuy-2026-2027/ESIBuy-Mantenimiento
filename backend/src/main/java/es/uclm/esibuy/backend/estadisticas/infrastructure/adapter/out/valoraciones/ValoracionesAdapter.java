package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.valoraciones;

import java.time.Instant;
import java.util.Optional;

import es.uclm.esibuy.backend.estadisticas.application.port.out.ValoracionesPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.PuntuacionValoracion;
import es.uclm.esibuy.backend.estadisticas.domain.model.ResumenValoracionesPeriodo;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProductoPeriodo;
import es.uclm.esibuy.backend.valoraciones.application.port.in.EstadisticasValoracionesVendedorUseCase;

public class ValoracionesAdapter implements ValoracionesPort {

    private final EstadisticasValoracionesVendedorUseCase estadisticasValoraciones;

    public ValoracionesAdapter(EstadisticasValoracionesVendedorUseCase estadisticasValoraciones) {
        this.estadisticasValoraciones = estadisticasValoraciones;
    }

    @Override
    public ResumenValoracionesPeriodo resumenDe(String emailVendedor, Optional<Instant> desde, Instant hasta) {
        var resumen = estadisticasValoraciones.consultar(emailVendedor, desde, hasta);
        return new ResumenValoracionesPeriodo(resumen.media(), resumen.total(),
                resumen.productos().stream()
                        .map(producto -> new ValoracionProductoPeriodo(producto.productoId(),
                                producto.media(), producto.numero()))
                        .toList(),
                resumen.distribucion().stream()
                        .map(puntuacion -> new PuntuacionValoracion(puntuacion.puntuacion(), puntuacion.numero(), 0))
                        .toList());
    }
}
