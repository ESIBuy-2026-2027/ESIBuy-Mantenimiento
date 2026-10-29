package es.uclm.esibuy.backend.direcciones.domain.model;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.LimiteDireccionesException;
import es.uclm.esibuy.backend.direcciones.domain.exception.NuevaPrincipalObligatoriaException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Direcciones de envío de un cliente. Reúne las reglas que afectan al conjunto y no a una dirección suelta,
 * con las mismas reglas que los métodos de pago (HU6): como máximo 5 y siempre una principal si hay alguna.
 */
public class DireccionesCliente {

    private static final int MAXIMO_DIRECCIONES = 5;

    private final List<DireccionEnvio> direcciones;

    public DireccionesCliente(List<DireccionEnvio> direcciones) {
        this.direcciones = new ArrayList<>(direcciones);
    }

    public void anadir(DireccionEnvio direccion) {
        if (direcciones.size() >= MAXIMO_DIRECCIONES) {
            throw new LimiteDireccionesException(
                    "No se pueden tener más de " + MAXIMO_DIRECCIONES + " direcciones de envío");
        }
        // La primera queda como principal: así el checkout siempre tiene una si el cliente tiene alguna
        if (direcciones.isEmpty()) {
            direccion.marcarPrincipal();
        }
        direcciones.add(direccion);
    }

    public DireccionEnvio editar(String direccionId, DatosDireccion nuevosDatos) {
        DireccionEnvio direccion = buscar(direccionId);
        direccion.editar(nuevosDatos);
        return direccion;
    }

    // Solo puede haber una principal: marcar una desmarca el resto
    public void marcarPrincipal(String direccionId) {
        // Se comprueba antes de tocar nada para no dejar al cliente sin principal
        buscar(direccionId);
        for (DireccionEnvio direccion : direcciones) {
            if (direccion.getId().equals(direccionId)) {
                direccion.marcarPrincipal();
            } else {
                direccion.desmarcarPrincipal();
            }
        }
    }

    public void eliminar(String direccionId, String nuevaPrincipalId) {
        DireccionEnvio eliminada = buscar(direccionId);
        // Sin elección si no es la principal o si no queda ninguna otra
        if (!eliminada.isPrincipal() || direcciones.size() == 1) {
            direcciones.remove(eliminada);
            return;
        }
        // Si se elimina la principal, el cliente elige cuál la sustituye; elegir la misma no vale
        if (nuevaPrincipalId == null || nuevaPrincipalId.equals(direccionId)) {
            throw new NuevaPrincipalObligatoriaException("Hay que elegir qué dirección pasa a ser la principal");
        }
        DireccionEnvio sustituta = buscar(nuevaPrincipalId);
        direcciones.remove(eliminada);
        sustituta.marcarPrincipal();
    }

    // Solo entre las direcciones de este cliente: un id de otro cliente no se encuentra
    private DireccionEnvio buscar(String direccionId) {
        return direcciones.stream()
                .filter(direccion -> direccion.getId().equals(direccionId))
                .findFirst()
                .orElseThrow(() -> new DireccionNoEncontradaException("La dirección de envío no existe"));
    }

    public List<DireccionEnvio> getDirecciones() {
        return Collections.unmodifiableList(direcciones);
    }
}
