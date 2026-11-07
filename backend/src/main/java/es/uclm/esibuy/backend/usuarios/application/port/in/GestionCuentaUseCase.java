package es.uclm.esibuy.backend.usuarios.application.port.in;

/**
 * Casos de uso del cliente o vendedor sobre su propia cuenta.
 */
public interface GestionCuentaUseCase {

    /**
     * Elimina la cuenta del cliente (HU25) o del vendedor (HU47) en sesión, tras confirmar su contraseña actual. El
     * borrado es físico. Del cliente se borran su lista de deseos, su carrito, sus preferencias, sus métodos de pago
     * y sus direcciones, y se conservan sus pedidos y valoraciones. Del vendedor se conservan como no disponibles los
     * productos con pedidos y se borran los demás con sus valoraciones. En los dos roles se borran sus factores de
     * autenticación, sus códigos y, el último, el usuario.
     *
     * @param email    correo de la sesión: solo se puede eliminar la cuenta propia
     * @param password contraseña actual
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException si la
     *         contraseña no coincide; no se borra nada
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException si el usuario de
     *         la sesión ya no existe
     */
    void eliminarCuentaPropia(String email, String password);
}
