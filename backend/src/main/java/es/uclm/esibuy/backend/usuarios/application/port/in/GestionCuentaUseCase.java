package es.uclm.esibuy.backend.usuarios.application.port.in;

/**
 * Casos de uso del cliente o vendedor sobre su propia cuenta.
 */
public interface GestionCuentaUseCase {

    /**
     * Elimina la cuenta del vendedor tras comprobar la contraseña actual.
     */
    void eliminarCuentaVendedor(String email, String password);
}
