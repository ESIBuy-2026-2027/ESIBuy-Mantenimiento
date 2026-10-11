package es.uclm.esibuy.backend.usuarios.domain.model;

/**
 * Datos propios del rol de un usuario. Sellada: solo existen el perfil de cliente y el de vendedor,
 * así que {@code Usuario} recibe un único parámetro de perfil y sigue dentro del límite de Sonar (S107).
 */
public sealed interface PerfilUsuario permits DatosCliente, DatosVendedor {

    Dni dni();

    Telefono telefono();
}
