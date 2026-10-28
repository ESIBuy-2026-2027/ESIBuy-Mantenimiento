package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.PerfilUsuario;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida del perfil propio ({@code GET /api/perfil}, HU4). Mismos campos que la ficha del administrador
 * (UsuarioDetalleResponse, HU20) sin los que solo le sirven a él (estado y fecha de incorporación). Nunca lleva
 * la contraseña ni los datos del segundo factor. Cada perfil trae solo los campos de su rol ({@code NON_NULL}):
 * la fecha de nacimiento y el tipo, el de cliente; el nombre comercial y la categoría principal, el de vendedor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PerfilResponse(
        String id,
        String nombre,
        String apellidos,
        String email,
        String rol,
        Instant fechaAlta,
        String dni,
        String telefono,
        String avatar,
        LocalDate fechaNacimiento,
        String tipoCliente,
        String nombreComercial,
        String categoriaPrincipalId) {

    static PerfilResponse desde(Usuario usuario) {
        DatosCliente cliente = usuario.getDatosCliente();
        DatosVendedor vendedor = usuario.getDatosVendedor();
        PerfilUsuario perfil = cliente != null ? cliente : vendedor;
        return new PerfilResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.getRol().name(),
                usuario.getFechaAlta(),
                perfil == null ? null : perfil.dni().valor(),
                perfil == null || perfil.telefono() == null ? null : perfil.telefono().valor(),
                usuario.getDatosPersonales().imagenPerfil(),
                cliente == null ? null : cliente.fechaNacimiento(),
                cliente == null || cliente.tipoCliente() == null ? null : cliente.tipoCliente().name(),
                vendedor == null ? null : vendedor.nombreComercial(),
                vendedor == null ? null : vendedor.categoriaPrincipalId());
    }
}
