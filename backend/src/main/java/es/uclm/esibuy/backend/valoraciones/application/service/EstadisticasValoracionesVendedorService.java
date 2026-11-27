package es.uclm.esibuy.backend.valoraciones.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.valoraciones.application.port.in.EstadisticasValoracionesVendedorUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.IdentidadVendedorPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosDelVendedorPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.FrecuenciaPuntuacion;
import es.uclm.esibuy.backend.valoraciones.domain.model.ProductoValoradoEnPeriodo;
import es.uclm.esibuy.backend.valoraciones.domain.model.PuntuacionAgregadaProducto;
import es.uclm.esibuy.backend.valoraciones.domain.model.ResumenValoracionesVendedor;

public class EstadisticasValoracionesVendedorService implements EstadisticasValoracionesVendedorUseCase {

    private final ValoracionRepositoryPort valoraciones;
    private final IdentidadVendedorPort identidadVendedor;
    private final ProductosDelVendedorPort productosDelVendedor;

    public EstadisticasValoracionesVendedorService(ValoracionRepositoryPort valoraciones,
            IdentidadVendedorPort identidadVendedor, ProductosDelVendedorPort productosDelVendedor) {
        this.valoraciones = valoraciones;
        this.identidadVendedor = identidadVendedor;
        this.productosDelVendedor = productosDelVendedor;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public ResumenValoracionesVendedor consultar(String emailVendedor, Optional<Instant> desde, Instant hasta) {
        List<String> productoIds = productosDelVendedor.idsDe(identidadVendedor.idPorCorreo(emailVendedor));
        if (productoIds.isEmpty()) {
            return new ResumenValoracionesVendedor(null, 0, List.of(), distribucionVacia());
        }
        Set<String> productosPropios = Set.copyOf(productoIds);
        List<PuntuacionAgregadaProducto> agregados = desde
                .map(inicio -> valoraciones.resumirPorProductos(productoIds, inicio, hasta))
                .orElseGet(() -> valoraciones.resumirPorProductos(productoIds));
        return resumir(agregados.stream()
                .filter(agregado -> productosPropios.contains(agregado.productoId()))
                .toList());
    }

    private static ResumenValoracionesVendedor resumir(List<PuntuacionAgregadaProducto> agregados) {
        Map<String, AcumuladoValoraciones> porProducto = new HashMap<>();
        int[] porPuntuacion = new int[6];
        int total = 0;
        long sumaPuntuaciones = 0;
        for (PuntuacionAgregadaProducto agregado : agregados) {
            if (agregado.puntuacion() < 1 || agregado.puntuacion() > 5 || agregado.numero() <= 0) {
                throw new IllegalStateException("El repositorio devolvió una puntuación agregada inválida");
            }
            porProducto.computeIfAbsent(agregado.productoId(), ignorado -> new AcumuladoValoraciones())
                    .agregar(agregado);
            porPuntuacion[agregado.puntuacion()] += agregado.numero();
            total += agregado.numero();
            sumaPuntuaciones += (long) agregado.puntuacion() * agregado.numero();
        }
        List<ProductoValoradoEnPeriodo> productos = porProducto.entrySet().stream()
                .map(entrada -> new ProductoValoradoEnPeriodo(entrada.getKey(),
                        entrada.getValue().suma / entrada.getValue().numero, entrada.getValue().numero))
                .toList();
        return new ResumenValoracionesVendedor(total == 0 ? null : (double) sumaPuntuaciones / total, total,
                productos, distribucion(porPuntuacion));
    }

    private static List<FrecuenciaPuntuacion> distribucion(int[] porPuntuacion) {
        List<FrecuenciaPuntuacion> resultado = new ArrayList<>();
        for (int estrellas = 1; estrellas <= 5; estrellas++) {
            resultado.add(new FrecuenciaPuntuacion(estrellas, porPuntuacion[estrellas]));
        }
        return List.copyOf(resultado);
    }

    private static List<FrecuenciaPuntuacion> distribucionVacia() {
        return distribucion(new int[6]);
    }

    private static final class AcumuladoValoraciones {
        private int numero;
        private long suma;

        private void agregar(PuntuacionAgregadaProducto agregado) {
            numero += agregado.numero();
            suma += (long) agregado.puntuacion() * agregado.numero();
        }
    }
}
