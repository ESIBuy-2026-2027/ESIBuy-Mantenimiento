package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.dao.DuplicateKeyException;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import es.uclm.esibuy.backend.deseos.domain.model.TipoListaDeseos;

/** Adaptador de salida que implementa el repositorio de listas de deseos con Spring Data MongoDB. */
public class ListaDeseosMongoAdapter implements ListaDeseosRepositoryPort {

    private static final String FAVORITOS = TipoListaDeseos.FAVORITOS.name();
    private static final String LISTA = TipoListaDeseos.LISTA.name();
    private static final String PREFIJO_POR_DEFECTO = "defecto-";

    private final ListaDeseosMongoRepository repository;
    private final ListaDeseosDocumentMapper mapper;

    public ListaDeseosMongoAdapter(ListaDeseosMongoRepository repository, ListaDeseosDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ListaDeseos> buscarFavoritos(String clienteId) {
        return todas(clienteId).filter(ListaDeseos::esFavoritos).findFirst();
    }

    @Override
    public List<ListaDeseos> buscarListas(String clienteId) {
        return todas(clienteId).filter(lista -> !lista.esFavoritos()).toList();
    }

    @Override
    public Optional<ListaDeseos> buscarLista(String id, String clienteId) {
        return buscarListas(clienteId).stream().filter(lista -> lista.getId().equals(id)).findFirst();
    }

    private Stream<ListaDeseos> todas(String clienteId) {
        List<ListaDeseosDocument> documentos = repository.findAllByUsuarioIdOrderByCreatedAtAsc(clienteId);
        ponerTipoALosAntiguos(documentos);
        return documentos.stream().map(mapper::aDominio);
    }

    // Lo guardado antes de separar los favoritos de las listas no tiene tipo: si el cliente aún no tiene favoritos, lo
    // son la lista única que había al principio (sin nombre) o la primera con el nombre por defecto de entonces. Las
    // demás, también las que renombró, son listas. El tipo queda guardado la próxima vez que se guarde cada una.
    private static void ponerTipoALosAntiguos(List<ListaDeseosDocument> documentos) {
        boolean conFavoritos = documentos.stream().anyMatch(d -> FAVORITOS.equals(d.getTipo()));
        for (ListaDeseosDocument documento : documentos) {
            if (documento.getTipo() == null) {
                boolean eranFavoritos = !conFavoritos && (documento.getNombre() == null
                        || ListaDeseos.NOMBRE_FAVORITOS.equals(documento.getNombre()));
                documento.setTipo(eranFavoritos ? FAVORITOS : LISTA);
                conFavoritos = conFavoritos || eranFavoritos;
            }
        }
    }

    // Con un id fijo por cliente, dos creaciones a la vez no la duplican. Al llevar id, Spring no pone createdAt
    @Override
    public ListaDeseos crearListaPorDefecto(String clienteId) {
        ListaDeseosDocument documento = mapper.aDocumento(
                ListaDeseos.nueva(clienteId, ListaDeseos.NOMBRE_POR_DEFECTO));
        documento.setId(PREFIJO_POR_DEFECTO + clienteId);
        documento.setCreatedAt(Instant.now());
        try {
            return mapper.aDominio(repository.insert(documento));
        } catch (DuplicateKeyException yaExiste) {
            return mapper.aDominio(repository.findById(documento.getId()).orElseThrow(() -> yaExiste));
        }
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

    // Solo los favoritos con tipo: los guardados antes de separarlos lo reciben la próxima vez que se guardan
    @Override
    public List<String> clientesConFavorito(String productoId) {
        return repository.findByTipoAndItemsProductoId(FAVORITOS, productoId).stream()
                .map(ListaDeseosDocument::getUsuarioId)
                .distinct()
                .toList();
    }
}
