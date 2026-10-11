package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida de la ficha de un usuario ({@code GET /api/admin/usuarios/{id}}, HU20). No expone el
 * hash de la contraseña ni otros datos sensibles.
 *
 * <p>Sigue el contrato de {@code frontend/docs/hu20-ficha-usuario.md}: la imagen se llama {@code avatar}
 * (en el dominio, {@code imagenPerfil}). Cada ficha trae solo los campos de su rol: los datos de cliente
 * no aparecen en la de un administrador ({@code NON_NULL}).
 *
 * <p>La {@code fechaIncorporacion} del administrador es su fecha de alta, como pide el contrato del frontend
 * (automática y no editable); solo sale en la ficha de un administrador.
 *
 * <p>PENDIENTE: {@code sede} del administrador, cuando exista en el modelo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioDetalleResponse(
        String id,
        String nombre,
        String apellidos,
        String email,
        String rol,
        String estado,
        Instant fechaAlta,
        String dni,
        String telefono,
        LocalDate fechaNacimiento,
        String tipoCliente,
        String avatar,
        Instant fechaIncorporacion) {

    static UsuarioDetalleResponse desde(Usuario usuario) {
        DatosCliente cliente = usuario.getDatosCliente();
        return new UsuarioDetalleResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.getRol().name(),
                // Como en el listado (HU19): BLOQUEADO si tiene un bloqueo por intentos vigente
                usuario.tieneBloqueoPorIntentos(Instant.now()) ? "BLOQUEADO" : usuario.getEstado(),
                usuario.getFechaAlta(),
                cliente == null || cliente.dni() == null ? null : cliente.dni().valor(),
                cliente == null || cliente.telefono() == null ? null : cliente.telefono().valor(),
                cliente == null ? null : cliente.fechaNacimiento(),
                cliente == null || cliente.tipoCliente() == null ? null : cliente.tipoCliente().name(),
                usuario.getDatosPersonales().imagenPerfil(),
                usuario.getRol() == Rol.ADMINISTRADOR ? usuario.getFechaAlta() : null);
    }
}
