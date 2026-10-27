package es.uclm.esibuy.backend.metodospago.application.port.in;

import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import java.util.List;

/**
 * Gestión de sus propios métodos de pago por parte del cliente (HU6). Recibe el correo del cliente en
 * sesión: solo puede actuar sobre sus métodos.
 */
public interface GestionMetodosPagoUseCase {

    /** Devuelve los métodos de pago del cliente en sesión; nunca los de otro cliente. */
    List<MetodoPago> listar(String emailCliente);

    /**
     * Da de alta un método de pago del cliente en sesión y lo devuelve ya guardado.
     *
     * @throws es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException si los datos no son válidos
     */
    MetodoPago anadir(AnadirMetodoPagoCommand comando);

    /**
     * Marca como predeterminado un método del cliente en sesión; el que lo era deja de serlo.
     *
     * @throws es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException si no es suyo
     */
    void marcarPredeterminado(String emailCliente, String metodoId);

    /**
     * Elimina un método del cliente en sesión. Si es el predeterminado y le quedan otros, el cliente elige
     * cuál pasa a serlo.
     *
     * @param nuevoPredeterminadoId id del sustituto; puede ser nulo si no hace falta elegir
     * @throws es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException si no es suyo
     * @throws es.uclm.esibuy.backend.metodospago.domain.exception.NuevoPredeterminadoObligatorioException si
     *         falta elegir el sustituto
     */
    void eliminar(String emailCliente, String metodoId, String nuevoPredeterminadoId);
}
