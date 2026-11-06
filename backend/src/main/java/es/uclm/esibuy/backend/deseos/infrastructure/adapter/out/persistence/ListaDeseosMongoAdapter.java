package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.Optional;

import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Adaptador de salida que implementa el repositorio de listas de deseos con Spring Data MongoDB. */
public class ListaDeseosMongoAdapter implements ListaDeseosRepositoryPort {

    private final ListaDeseosMongoRepository repository;
    private final ListaDeseosDocumentMapper mapper;

    public ListaDeseosMongoAdapter(ListaDeseosMongoRepository repository, ListaDeseosDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ListaDeseos> buscarPorCliente(String clienteId) {
        return repository.findByUsuarioId(clienteId).map(mapper::aDominio);
    }

    @Override
    public ListaDeseos guardar(ListaDeseos lista) {
        ListaDeseosDocument documento = mapper.aDocumento(lista);
        if (lista.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(lista.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }

    @Override
    public void eliminarPorCliente(String clienteId) {
        repository.deleteByUsuarioId(clienteId);
    }
}
