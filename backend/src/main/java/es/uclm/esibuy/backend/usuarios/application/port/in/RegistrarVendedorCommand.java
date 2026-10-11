package es.uclm.esibuy.backend.usuarios.application.port.in;

/**
 * Datos de entrada del caso de uso de registro de un vendedor (T241).
 *
 * <p>Como el comando del cliente, lleva texto plano y es el servicio quien construye los value
 * objects del dominio. A diferencia del cliente, el vendedor no tiene fecha de nacimiento ni tipo
 * de cliente, y aporta su nombre comercial y una única categoría principal (decisión del equipo).
 *
 * <p>El {@code toString} oculta las dos contraseñas para que no acaben en un log.
 */
public record RegistrarVendedorCommand(
        String nombre,
        String apellidos,
        String nombreComercial,
        String categoriaPrincipalId,
        String dni,
        String email,
        String telefono,
        String password,
        String repeticionPassword,
        String imagenPerfil,
        boolean aceptaPrivacidad) {

    @Override
    public String toString() {
        return "RegistrarVendedorCommand[nombre=" + nombre + ", apellidos=" + apellidos
                + ", nombreComercial=" + nombreComercial + ", categoriaPrincipalId=" + categoriaPrincipalId
                + ", dni=" + dni + ", email=" + email + ", telefono=" + telefono
                + ", password=****, repeticionPassword=****, imagenPerfil=" + imagenPerfil
                + ", aceptaPrivacidad=" + aceptaPrivacidad + "]";
    }
}
