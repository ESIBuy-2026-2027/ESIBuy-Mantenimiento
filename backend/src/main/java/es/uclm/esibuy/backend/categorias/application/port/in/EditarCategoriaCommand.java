package es.uclm.esibuy.backend.categorias.application.port.in;

/** Datos para editar una categoría (HU41): su id y el nuevo nombre y descripción. El estado no se edita. */
public record EditarCategoriaCommand(String id, String nombre, String descripcion) {
}