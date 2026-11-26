package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/** Adaptador de salida que implementa el repositorio de alertas con Spring Data MongoDB (AB#529). */
public class AlertaMongoAdapter implements AlertaRepositoryPort {

    private final AlertaMongoRepository repository;
    private final AlertaDocumentMapper mapper;

    public AlertaMongoAdapter(AlertaMongoRepository repository, AlertaDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void guardarTodas(List<Alerta> alertas) {
        repository.saveAll(alertas.stream().map(mapper::aDocumento).toList());
    }

    @Override
    public List<Alerta> buscarPorCliente(String clienteId) {
        return repository.findByClienteIdOrderByFechaDesc(clienteId).stream().map(mapper::aDominio).toList();
    }

    @Override
    public Optional<Alerta> buscarPorIdYCliente(String alertaId, String clienteId) {
        return repository.findByIdAndClienteId(alertaId, clienteId).map(mapper::aDominio);
    }

    @Override
    public Alerta guardar(Alerta alerta) {
        AlertaDocument documento = mapper.aDocumento(alerta);
        if (alerta.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(alerta.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }
}
