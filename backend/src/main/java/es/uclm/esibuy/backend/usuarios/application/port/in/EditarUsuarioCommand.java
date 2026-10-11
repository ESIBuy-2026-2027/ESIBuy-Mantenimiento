package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;

/**
 * Datos de entrada del caso de uso de edición de un usuario por el administrador (HU22, T42).
 *
 * <p>Como {@link RegistrarClienteCommand}, lleva texto plano y es el servicio quien construye los value
 * objects del dominio. Trae los campos de los tres roles; solo se usan los del rol del usuario
 * ({@code dni}, {@code telefono}, {@code fechaNacimiento} y {@code tipoCliente} para un cliente;
 * {@code nombreComercial}, {@code categoriaPrincipalId}, {@code dni} y {@code telefono} para un vendedor).
 *
 * <p>No lleva email, contraseña, estado, rol ni fecha de incorporación: no se pueden cambiar por esta vía.
 * El email no se edita en ningún rol (decisión del equipo, 10/10/2026) y el rol de una cuenta no cambia
 * nunca ("el que nace cliente muere cliente", decisión del equipo, 11/10/2026).
 *
 * <p>PENDIENTE: la sede del administrador, cuando exista en el modelo (decisión del equipo: este sprint no).
 */
public record EditarUsuarioCommand(
        String nombre,
        String apellidos,
        String imagenPerfil,
        String dni,
        String telefono,
        LocalDate fechaNacimiento,
        TipoCliente tipoCliente,
        String nombreComercial,
        String categoriaPrincipalId) {
}
