package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.util.Map;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida de la supervisión de vendedores (HU26), para el listado y la ficha.
 * No expone el hash de contraseña ni otros datos sensibles.
 *
 * <p>Los datos de vendedor (T241) son nulos en un vendedor anterior a su registro, que no los tiene. La
 * {@code categoriaPrincipal} es el nombre de la categoría, que se busca por su identificador en el módulo de
 * categorías; es nula si la categoría no existe.
 */
public record VendedorResponse(
        String id,
        String nombre,
        String apellidos,
        String email,
        String estado,
        String nombreComercial,
        String categoriaPrincipalId,
        String categoriaPrincipal,
        String dni,
        String telefono
) {

    static VendedorResponse desde(Usuario usuario, Map<String, String> nombresDeCategorias) {
        DatosVendedor datos = usuario.getDatosVendedor();
        return new VendedorResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.getEstado(),
                datos == null ? null : datos.nombreComercial(),
                datos == null ? null : datos.categoriaPrincipalId(),
                datos == null ? null : nombresDeCategorias.get(datos.categoriaPrincipalId()),
                datos == null ? null : datos.dni().valor(),
                datos == null || datos.telefono() == null ? null : datos.telefono().valor()
        );
    }
}
