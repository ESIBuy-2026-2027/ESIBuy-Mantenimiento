package es.uclm.esibuy.backend.valoraciones.domain.model;

import java.util.List;

public record ResumenValoracionesVendedor(Double media, int total, List<ProductoValoradoEnPeriodo> productos,
        List<FrecuenciaPuntuacion> distribucion) {
}
