package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida de la supervisión de vendedores (HU26), para el listado y la ficha.
 * No expone el hash de contraseña ni otros datos sensibles.
 *
 * <p>PENDIENTE: {@code nombreComercial}, {@code categoriaPrincipal} y {@code nif} del contrato se
 * añadirán cuando exista el registro de vendedores; hasta entonces no se devuelven.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VendedorResponse(
        String id,
        String nombre,
        String apellidos,
        String email,
        String estado
) {

    static VendedorResponse desde(Usuario usuario) {
        return new VendedorResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.tieneBloqueoPorIntentos(Instant.now()) ? "BLOQUEADO" : usuario.getEstado()
        );
    }
}
