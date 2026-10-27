package es.uclm.esibuy.backend.categorias.application.port.in;

import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

/** Datos para crear una categoría (HU40). El administrador elige si nace activa o inactiva. */
public record CrearCategoriaCommand(String nombre, String descripcion, EstadoCategoria estado) {
}
