package es.uclm.esibuy.backend.direcciones.application.port.in;

import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import java.util.List;
import java.util.Optional;

/**
 * Gestión de sus propias direcciones de envío por parte del cliente (HU5). Recibe el correo del cliente en
 * sesión: solo puede actuar sobre sus direcciones.
 */
public interface GestionDireccionesUseCase {

    /** Devuelve las direcciones del cliente en sesión, con la marca de la principal; nunca las de otro. */
    List<DireccionEnvio> listar(String emailCliente);

    /**
     * Da de alta una dirección del cliente en sesión y la devuelve ya guardada. La primera queda como principal.
     *
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException si los datos no son válidos
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.LimiteDireccionesException si ya tiene el máximo
     */
    DireccionEnvio anadir(String emailCliente, DireccionCommand direccion);

    /**
     * Cambia los datos de una dirección del cliente en sesión y la devuelve; no cambia cuál es la principal.
     *
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException si los datos no son válidos
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException si no es suya
     */
    DireccionEnvio editar(String emailCliente, String direccionId, DireccionCommand direccion);

    /**
     * Marca como principal una dirección del cliente en sesión; la que lo era deja de serlo.
     *
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException si no es suya
     */
    void marcarPrincipal(String emailCliente, String direccionId);

    /**
     * Elimina una dirección del cliente en sesión. Si es la principal y le quedan otras, el cliente elige cuál
     * pasa a serlo.
     *
     * @param nuevaPrincipalId id de la sustituta; puede ser nulo si no hace falta elegir
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException si no es suya
     * @throws es.uclm.esibuy.backend.direcciones.domain.exception.NuevaPrincipalObligatoriaException si falta
     *         elegir la sustituta
     */
    void eliminar(String emailCliente, String direccionId, String nuevaPrincipalId);

    /**
     * Los datos de una dirección del cliente en sesión para el pedido (HU10).
     *
     * @return vacío si la dirección no existe o es de otro cliente
     */
    Optional<DireccionParaPedido> consultarParaPedido(String emailCliente, String direccionId);
}
