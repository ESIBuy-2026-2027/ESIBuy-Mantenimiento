package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence;

import java.util.List;

import es.uclm.esibuy.backend.direcciones.application.port.out.DireccionRepositoryPort;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;

/** Adaptador de salida que implementa el repositorio de direcciones de envío con Spring Data MongoDB. */
public class DireccionMongoAdapter implements DireccionRepositoryPort {

    private final DireccionMongoRepository repository;
    private final DireccionDocumentMapper mapper;

    public DireccionMongoAdapter(DireccionMongoRepository repository, DireccionDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<DireccionEnvio> buscarPorCliente(String clienteId) {
        return repository.findByClienteId(clienteId).stream().map(mapper::aDominio).toList();
    }

    @Override
    public DireccionEnvio guardar(DireccionEnvio direccion) {
        DireccionDocument documento = mapper.aDocumento(direccion);
        if (direccion.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(direccion.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }

    @Override
    public void eliminar(String direccionId) {
        repository.deleteById(direccionId);
    }

    @Override
    public void eliminarPorCliente(String clienteId) {
        repository.deleteByClienteId(clienteId);
    }
}
