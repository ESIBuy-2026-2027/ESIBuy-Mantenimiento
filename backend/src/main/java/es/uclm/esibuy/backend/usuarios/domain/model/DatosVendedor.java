package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

/**
 * Datos propios de un vendedor: cómo se le conoce en su tienda y su categoría principal de venta,
 * que se referencia por identificador (las categorías son de otro módulo).
 */
public record DatosVendedor(String nombreComercial, String categoriaPrincipalId) implements DatosRol {

    public DatosVendedor {
        exigirTexto(nombreComercial, "Un vendedor debe tener nombre comercial");
        exigirTexto(categoriaPrincipalId, "Un vendedor debe tener categoría principal");
    }

    @Override
    public Rol rol() {
        return Rol.VENDEDOR;
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException(mensaje);
        }
    }
}
