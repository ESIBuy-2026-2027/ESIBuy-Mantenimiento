package es.uclm.esibuy.backend.alertas.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.in.GestionAlertasUseCase;

/**
 * Adaptador de entrada web de las alertas del cliente: {@code GET /api/customer/alerts} devuelve las alertas de su
 * cuenta (AB#529) y {@code PATCH /api/customer/alerts/{id}/read} marca una como leída (AB#531). Recibe la petición y
 * delega en los puertos de entrada; el cliente sale siempre de la sesión. /api/customer/** solo lo usa un CLIENTE
 * (SecurityConfig): sin sesión 401 y con otro rol 403 ACCESS_DENIED. Una alerta ajena o inexistente, 404
 * ALERT_NOT_FOUND.
 */
@RestController
@RequestMapping("/api/customer/alerts")
public class AlertasController {

    private final ConsultaAlertasUseCase consultaAlertas;
    private final GestionAlertasUseCase gestionAlertas;

    public AlertasController(ConsultaAlertasUseCase consultaAlertas, GestionAlertasUseCase gestionAlertas) {
        this.consultaAlertas = consultaAlertas;
        this.gestionAlertas = gestionAlertas;
    }

    @GetMapping
    public AlertasResponse consultar(Authentication autenticado) {
        return AlertasResponse.de(consultaAlertas.consultar(autenticado.getName()));
    }

    @PatchMapping("/{id}/read")
    public AlertaLeidaResponse marcarComoLeida(@PathVariable String id, Authentication autenticado) {
        return AlertaLeidaResponse.de(gestionAlertas.marcarComoLeida(autenticado.getName(), id));
    }
}
