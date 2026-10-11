package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.PerfilUsuario;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida de la ficha de un usuario ({@code GET /api/admin/usuarios/{id}}, HU20). No expone el
 * hash de la contraseña ni otros datos sensibles.
 *
 * <p>Sigue el contrato de {@code frontend/docs/hu20-ficha-usuario.md}: la imagen se llama {@code avatar}
 * (en el dominio, {@code imagenPerfil}). Cada ficha trae solo los campos de su rol ({@code NON_NULL}): el DNI
 * y el teléfono, los de cliente y vendedor; la fecha de nacimiento y el tipo, los de cliente; el nombre
 * comercial y la categoría principal, los de vendedor.
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
        Instant fechaIncorporacion,
        String nombreComercial,
        String categoriaPrincipalId) {

    static UsuarioDetalleResponse desde(Usuario usuario) {
        DatosCliente cliente = usuario.getDatosCliente();
        DatosVendedor vendedor = usuario.getDatosVendedor();
        PerfilUsuario perfil = cliente != null ? cliente : vendedor;
        return new UsuarioDetalleResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.getRol().name(),
                // Como en el listado (HU19): BLOQUEADO si tiene un bloqueo por intentos vigente
                usuario.tieneBloqueoPorIntentos(Instant.now()) ? "BLOQUEADO" : usuario.getEstado(),
                usuario.getFechaAlta(),
                perfil == null ? null : perfil.dni().valor(),
                perfil == null || perfil.telefono() == null ? null : perfil.telefono().valor(),
                cliente == null ? null : cliente.fechaNacimiento(),
                cliente == null || cliente.tipoCliente() == null ? null : cliente.tipoCliente().name(),
                usuario.getDatosPersonales().imagenPerfil(),
                usuario.getRol() == Rol.ADMINISTRADOR ? usuario.getFechaAlta() : null,
                vendedor == null ? null : vendedor.nombreComercial(),
                vendedor == null ? null : vendedor.categoriaPrincipalId());
    }
}
