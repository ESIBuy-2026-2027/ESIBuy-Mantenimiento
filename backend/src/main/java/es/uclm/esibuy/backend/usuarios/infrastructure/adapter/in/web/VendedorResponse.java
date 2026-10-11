package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida de la supervisión de vendedores (HU26), para el listado y la ficha.
 * No expone el hash de contraseña ni otros datos sensibles.
 *
 * <p>Los datos de vendedor (T241) son nulos en un vendedor anterior a su registro, que no los tiene.
 *
 * <p>PENDIENTE: {@code categoriaPrincipal} (el nombre de la categoría) del contrato, cuando exista el
 * módulo de categorías.
 */
public record VendedorResponse(
        String id,
        String nombre,
        String apellidos,
        String email,
        String estado,
        String nombreComercial,
        String categoriaPrincipalId,
        String dni,
        String telefono
) {

    static VendedorResponse desde(Usuario usuario) {
        DatosVendedor datos = usuario.getDatosVendedor();
        return new VendedorResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.tieneBloqueoPorIntentos(Instant.now()) ? "BLOQUEADO" : usuario.getEstado(),
                datos == null ? null : datos.nombreComercial(),
                datos == null ? null : datos.categoriaPrincipalId(),
                datos == null ? null : datos.dni().valor(),
                datos == null || datos.telefono() == null ? null : datos.telefono().valor()
        );
    }
}
