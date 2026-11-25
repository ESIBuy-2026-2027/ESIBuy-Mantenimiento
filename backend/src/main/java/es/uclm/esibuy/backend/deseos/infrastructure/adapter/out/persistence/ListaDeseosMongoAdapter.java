package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.List;
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
        return buscarTodasPorCliente(clienteId).stream().findFirst();
    }

    @Override
    public List<ListaDeseos> buscarTodasPorCliente(String clienteId) {
        return repository.findAllByUsuarioIdOrderByCreatedAtAsc(clienteId).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public Optional<ListaDeseos> buscarPorIdYCliente(String id, String clienteId) {
        return repository.findByIdAndUsuarioId(id, clienteId).map(mapper::aDominio);
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
    public void eliminar(String id, String clienteId) {
        repository.deleteByIdAndUsuarioId(id, clienteId);
    }

    @Override
    public void eliminarPorCliente(String clienteId) {
        repository.deleteByUsuarioId(clienteId);
    }
}
