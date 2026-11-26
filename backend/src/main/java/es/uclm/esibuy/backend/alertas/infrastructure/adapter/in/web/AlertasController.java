package es.uclm.esibuy.backend.alertas.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;

/**
 * Adaptador de entrada web de las alertas del cliente (AB#529): {@code GET /api/customer/alerts} devuelve las alertas
 * de su cuenta. Recibe la petición y delega en el puerto de entrada; el cliente sale siempre de la sesión.
 * /api/customer/** solo lo usa un CLIENTE (SecurityConfig): sin sesión 401 y con otro rol 403 ACCESS_DENIED.
 */
@RestController
@RequestMapping("/api/customer/alerts")
public class AlertasController {

    private final ConsultaAlertasUseCase consultaAlertas;

    public AlertasController(ConsultaAlertasUseCase consultaAlertas) {
        this.consultaAlertas = consultaAlertas;
    }

    @GetMapping
    public AlertasResponse consultar(Authentication autenticado) {
        return AlertasResponse.de(consultaAlertas.consultar(autenticado.getName()));
    }
}
