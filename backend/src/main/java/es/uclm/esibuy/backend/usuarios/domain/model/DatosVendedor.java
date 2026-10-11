package es.uclm.esibuy.backend.usuarios.domain.model;

// Perfil del vendedor (T241): una sola categoría principal (decisión del equipo) y el teléfono opcional.
public record DatosVendedor(String nombreComercial, String categoriaPrincipalId, Dni dni, Telefono telefono)
        implements PerfilUsuario {
}
