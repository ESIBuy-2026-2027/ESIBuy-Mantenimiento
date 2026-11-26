package es.uclm.esibuy.backend.alertas.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.alertas.application.port.in.GestionAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.domain.exception.AlertaNoEncontradaException;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Gestión de sus alertas por parte del cliente en sesión (AB#531). */
public class GestionAlertasService implements GestionAlertasUseCase {

    private final AlertaRepositoryPort alertaRepository;
    private final ClienteEnSesionPort clienteEnSesion;

    public GestionAlertasService(AlertaRepositoryPort alertaRepository, ClienteEnSesionPort clienteEnSesion) {
        this.alertaRepository = alertaRepository;
        this.clienteEnSesion = clienteEnSesion;
    }

    // Se busca por id y cliente a la vez: una alerta ajena es, para este cliente, una que no existe
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Alerta marcarComoLeida(String emailCliente, String alertaId) {
        Alerta alerta = alertaRepository.buscarPorIdYCliente(alertaId, clienteEnSesion.idPorCorreo(emailCliente))
                .orElseThrow(() -> new AlertaNoEncontradaException("La alerta no existe"));
        if (alerta.isLeida()) {
            return alerta;
        }
        return alertaRepository.guardar(alerta.marcarComoLeida());
    }
}
