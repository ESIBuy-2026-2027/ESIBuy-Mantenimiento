package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;

import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionDuplicadaException;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import es.uclm.esibuy.backend.valoraciones.domain.model.PuntuacionAgregadaProducto;

/** Adaptador de salida que implementa el repositorio de valoraciones con Spring Data MongoDB. */
public class ValoracionMongoAdapter implements ValoracionRepositoryPort {

    private final ValoracionMongoRepository repository;
    private final ValoracionDocumentMapper mapper;

    public ValoracionMongoAdapter(ValoracionMongoRepository repository, ValoracionDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Valoracion guardar(Valoracion valoracion) {
        try {
            return mapper.aDominio(repository.save(mapper.aDocumento(valoracion)));
        } catch (DuplicateKeyException e) {
            throw new ValoracionDuplicadaException();
        }
    }

    @Override
    public boolean existePorClienteYProducto(String clienteId, String productoId) {
        return repository.existsByUsuarioIdAndProductoId(clienteId, productoId);
    }

    @Override
    public List<Valoracion> buscarPorProducto(String productoId) {
        return repository.findByProductoId(productoId).stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<PuntuacionAgregadaProducto> resumirPorProductos(List<String> productoIds, Instant desde,
            Instant hasta) {
        List<ValoracionDocument> docs = repository
                .findByProductoIdInAndFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(productoIds, desde, hasta);
        return agrupar(docs);
    }

    @Override
    public List<PuntuacionAgregadaProducto> resumirPorProductos(List<String> productoIds) {
        return repository.resumirPorProductos(productoIds).stream()
                .map(ValoracionMongoAdapter::aDominio)
                .toList();
    }

    @Override
    public void eliminarPorProducto(String productoId) {
        repository.deleteByProductoId(productoId);
    }

    /** Agrupa documentos individuales por (productoId, puntuacion) para sustituir la aggregation de fecha. */
    private static List<PuntuacionAgregadaProducto> agrupar(List<ValoracionDocument> docs) {
        Map<String, Integer> contadores = new HashMap<>();
        for (ValoracionDocument doc : docs) {
            String clave = doc.getProductoId() + ":" + doc.getPuntuacion();
            contadores.merge(clave, 1, Integer::sum);
        }
        return contadores.entrySet().stream().map(entrada -> {
            String[] partes = entrada.getKey().split(":", 2);
            return new PuntuacionAgregadaProducto(partes[0], Integer.parseInt(partes[1]), entrada.getValue());
        }).toList();
    }

    private static PuntuacionAgregadaProducto aDominio(ValoracionMongoRepository.ValoracionAgrupada agregada) {
        return new PuntuacionAgregadaProducto(agregada.getProductoId(), agregada.getPuntuacion(),
                Math.toIntExact(agregada.getNumero()));
    }
}

