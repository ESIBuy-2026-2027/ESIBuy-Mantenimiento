package es.uclm.esibuy.backend.alertas.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto;
import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto.Situacion;
import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.FavoritosPort;
import es.uclm.esibuy.backend.alertas.application.port.out.TiposClientePort;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Genera las alertas de los productos favoritos (AB#529) en el momento en que cambia el producto: una por cada cliente
 * que lo tiene en favoritos. La bajada de precio se mira con el precio que paga cada cliente (estándar o premium).
 */
public class GeneracionAlertasService implements GeneracionAlertasUseCase {

    private final AlertaRepositoryPort alertaRepository;
    private final FavoritosPort favoritos;
    private final TiposClientePort tiposCliente;
    private final Clock reloj;

    public GeneracionAlertasService(AlertaRepositoryPort alertaRepository, FavoritosPort favoritos,
            TiposClientePort tiposCliente, Clock reloj) {
        this.alertaRepository = alertaRepository;
        this.favoritos = favoritos;
        this.tiposCliente = tiposCliente;
        this.reloj = reloj;
    }

    // Lo genera el vendedor al modificar o activar su producto, o el cliente al cancelar un pedido (repone stock)
    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR)
    @Override
    public void notificarCambio(CambioProducto cambio) {
        Situacion antes = cambio.antes();
        Situacion despues = cambio.despues();
        boolean bajaEstandar = Alerta.esBajadaDePrecio(antes.precioEstandar(), despues.precioEstandar());
        boolean bajaPremium = Alerta.esBajadaDePrecio(antes.precioPremium(), despues.precioPremium());
        boolean vuelve = Alerta.vuelveAEstarDisponible(antes.comprable(), despues.comprable());
        if (!bajaEstandar && !bajaPremium && !vuelve) {
            return;
        }
        List<String> clientes = favoritos.clientesConFavorito(cambio.productoId());
        if (clientes.isEmpty()) {
            return;
        }
        // Solo hace falta saber quién es premium si ha bajado algún precio
        Set<String> premium = bajaEstandar || bajaPremium ? tiposCliente.premiumEntre(clientes) : Set.of();
        Instant ahora = Instant.now(reloj);
        List<Alerta> alertas = new ArrayList<>();
        for (String clienteId : clientes) {
            alertas.addAll(alertasDelCliente(clienteId, premium.contains(clienteId), vuelve, cambio, ahora));
        }
        if (!alertas.isEmpty()) {
            alertaRepository.guardarTodas(alertas);
        }
    }

    private static List<Alerta> alertasDelCliente(String clienteId, boolean esPremium, boolean vuelve,
            CambioProducto cambio, Instant ahora) {
        List<Alerta> alertas = new ArrayList<>();
        if (vuelve) {
            alertas.add(Alerta.disponibilidad(clienteId, cambio.productoId(), cambio.nombreProducto(),
                    cambio.imagenProducto(), ahora));
        }
        BigDecimal anterior = esPremium ? cambio.antes().precioPremium() : cambio.antes().precioEstandar();
        BigDecimal nuevo = esPremium ? cambio.despues().precioPremium() : cambio.despues().precioEstandar();
        if (Alerta.esBajadaDePrecio(anterior, nuevo)) {
            alertas.add(Alerta.bajadaDePrecio(clienteId, cambio.productoId(), cambio.nombreProducto(),
                    cambio.imagenProducto(), anterior, nuevo, ahora));
        }
        return alertas;
    }
}
