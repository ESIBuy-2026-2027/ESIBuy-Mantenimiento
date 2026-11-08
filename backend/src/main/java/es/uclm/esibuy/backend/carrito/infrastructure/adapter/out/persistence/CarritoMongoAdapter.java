package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import java.util.Optional;

import es.uclm.esibuy.backend.carrito.application.port.out.CarritoRepositoryPort;
import es.uclm.esibuy.backend.carrito.domain.model.Carrito;

/** Adaptador de salida que implementa el repositorio de carritos con Spring Data MongoDB. */
public class CarritoMongoAdapter implements CarritoRepositoryPort {

    private final CarritoMongoRepository repository;
    private final CarritoDocumentMapper mapper;

    public CarritoMongoAdapter(CarritoMongoRepository repository, CarritoDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Carrito> buscarPorCliente(String clienteId) {
        return repository.findByClienteId(clienteId).map(mapper::aDominio);
    }

    @Override
    public Carrito guardar(Carrito carrito) {
        CarritoDocument documento = mapper.aDocumento(carrito);
        if (carrito.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(carrito.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }

    @Override
    public void eliminarPorCliente(String clienteId) {
        repository.deleteByClienteId(clienteId);
    }
}
