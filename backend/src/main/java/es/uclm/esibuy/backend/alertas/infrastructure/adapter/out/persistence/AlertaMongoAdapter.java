package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import java.util.List;

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
}
