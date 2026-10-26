package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence;

import java.util.List;

import es.uclm.esibuy.backend.metodospago.application.port.out.MetodoPagoRepositoryPort;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;

/** Adaptador de salida que implementa el repositorio de métodos de pago con Spring Data MongoDB. */
public class MetodoPagoMongoAdapter implements MetodoPagoRepositoryPort {

    private final MetodoPagoMongoRepository repository;
    private final MetodoPagoDocumentMapper mapper;

    public MetodoPagoMongoAdapter(MetodoPagoMongoRepository repository, MetodoPagoDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<MetodoPago> buscarPorCliente(String clienteId) {
        return repository.findByClienteId(clienteId).stream().map(mapper::aDominio).toList();
    }

    @Override
    public MetodoPago guardar(MetodoPago metodo) {
        MetodoPagoDocument documento = mapper.aDocumento(metodo);
        if (metodo.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(metodo.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }

    @Override
    public void eliminar(String metodoId) {
        repository.deleteById(metodoId);
    }
}
