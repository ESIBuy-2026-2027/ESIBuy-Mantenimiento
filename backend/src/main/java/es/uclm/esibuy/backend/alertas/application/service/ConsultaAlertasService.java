package es.uclm.esibuy.backend.alertas.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Consulta de sus alertas por parte del cliente en sesión (AB#529). */
public class ConsultaAlertasService implements ConsultaAlertasUseCase {

    private final AlertaRepositoryPort alertaRepository;
    private final ClienteEnSesionPort clienteEnSesion;

    public ConsultaAlertasService(AlertaRepositoryPort alertaRepository, ClienteEnSesionPort clienteEnSesion) {
        this.alertaRepository = alertaRepository;
        this.clienteEnSesion = clienteEnSesion;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<Alerta> consultar(String emailCliente) {
        return alertaRepository.buscarPorCliente(clienteEnSesion.idPorCorreo(emailCliente));
    }
}
