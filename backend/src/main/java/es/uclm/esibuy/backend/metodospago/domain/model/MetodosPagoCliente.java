package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.LimiteMetodosPagoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.NuevoPredeterminadoObligatorioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Métodos de pago de un cliente. Reúne las reglas que afectan al conjunto y no a un método suelto.
 */
public class MetodosPagoCliente {

    private static final int MAXIMO_METODOS = 5;

    private final List<MetodoPago> metodos;

    public MetodosPagoCliente(List<MetodoPago> metodos) {
        this.metodos = new ArrayList<>(metodos);
    }

    public void anadir(MetodoPago metodo) {
        if (metodos.size() >= MAXIMO_METODOS) {
            throw new LimiteMetodosPagoException(
                    "No se pueden tener más de " + MAXIMO_METODOS + " métodos de pago");
        }
        // El primero queda como predeterminado: así el cliente siempre tiene uno si tiene alguno
        if (metodos.isEmpty()) {
            metodo.marcarPredeterminado();
        }
        metodos.add(metodo);
    }

    // Solo puede haber uno predeterminado: marcar uno desmarca el resto
    public void marcarPredeterminado(String metodoId) {
        // Se comprueba antes de tocar nada para no dejar al cliente sin predeterminado
        buscar(metodoId);
        for (MetodoPago metodo : metodos) {
            if (metodo.getId().equals(metodoId)) {
                metodo.marcarPredeterminado();
            } else {
                metodo.desmarcarPredeterminado();
            }
        }
    }

    public void eliminar(String metodoId, String nuevoPredeterminadoId) {
        MetodoPago eliminado = buscar(metodoId);
        // Sin elección si no es el predeterminado o si no queda ningún otro
        if (!eliminado.isPredeterminado() || metodos.size() == 1) {
            metodos.remove(eliminado);
            return;
        }
        // Si se elimina el predeterminado, el cliente elige cuál lo sustituye; elegir el mismo no vale
        if (nuevoPredeterminadoId == null || nuevoPredeterminadoId.equals(metodoId)) {
            throw new NuevoPredeterminadoObligatorioException(
                    "Hay que elegir qué método pasa a ser el predeterminado");
        }
        MetodoPago sustituto = buscar(nuevoPredeterminadoId);
        metodos.remove(eliminado);
        sustituto.marcarPredeterminado();
    }

    // Solo entre los métodos de este cliente: un id de otro cliente no se encuentra
    private MetodoPago buscar(String metodoId) {
        return metodos.stream()
                .filter(metodo -> metodo.getId().equals(metodoId))
                .findFirst()
                .orElseThrow(() -> new MetodoPagoNoEncontradoException("El método de pago no existe"));
    }

    public List<MetodoPago> getMetodos() {
        return Collections.unmodifiableList(metodos);
    }
}
