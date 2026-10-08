package es.uclm.esibuy.backend.usuarios.application.port.in;

/**
 * Datos de entrada del caso de uso de registro de usuario.
 *
 * <p>Se declara como {@code record} porque es un objeto que solo transporta datos:
 * no tiene lógica y no debe cambiar desde que lo crea el adaptador web hasta que
 * lo recibe el servicio. Un record es inmutable y genera automáticamente el
 * constructor, los métodos de acceso, {@code equals}, {@code hashCode} y
 * {@code toString}, lo que evita escribir código repetitivo.
 *
 * <p>Vive en {@code application.port.in} porque forma parte del contrato del caso
 * de uso. El controlador REST traducirá su DTO a este comando, de modo que el
 * núcleo de la aplicación no depende de la API web.
 *
 * <p>PENDIENTE: el {@code toString} generado muestra la contraseña en claro;
 * redefinirlo para ocultarla antes de que el comando pueda llegar a un log. SOLUCIONADO
 */

public record RegistrarUsuarioCommand(String correo, String contrasena) {

    @Override
    public String toString() {
        return "RegistrarUsuarioCommand[correo=" + correo + ", contrasena=****]"; // Oculta la contraseña en el toString para evitar que se registre en los logs
    }

}
