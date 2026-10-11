package es.uclm.esibuy.backend.categorias.domain.model;

/**
 * Categoría del catálogo. Un vendedor tiene una sola categoría principal y un producto una sola categoría
 * (decisiones del equipo); los demás módulos guardan solo su identificador.
 */
public record Categoria(String id, String nombre) {
}
