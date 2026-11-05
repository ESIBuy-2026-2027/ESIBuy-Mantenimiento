package es.uclm.esibuy.backend.categorias.domain.model;

import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaInvalidaException;

/**
 * Categoría del catálogo. Un vendedor tiene una sola categoría principal y un producto una sola categoría
 * (decisiones del equipo); los demás módulos guardan solo su identificador.
 *
 * <p>Las reglas se validan al crearla (HU40, T321): nombre obligatorio de hasta 50 caracteres y descripción
 * opcional de hasta 200, los dos sin espacios en los extremos, y un estado. El id es nulo mientras la
 * categoría no se ha guardado.
 */
public record Categoria(String id, String nombre, String descripcion, EstadoCategoria estado) {

    private static final int NOMBRE_MAXIMO = 50;
    private static final int DESCRIPCION_MAXIMA = 200;

    public Categoria {
        nombre = exigirNombre(nombre);
        descripcion = exigirDescripcion(descripcion);
        if (estado == null) {
            throw new CategoriaInvalidaException("El estado es obligatorio");
        }
    }

    /** Una categoría nueva: todavía sin id y ACTIVA. */
    public static Categoria nueva(String nombre, String descripcion) {
        return new Categoria(null, nombre, descripcion, EstadoCategoria.ACTIVA);
    }

    private static String exigirNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new CategoriaInvalidaException("El nombre es obligatorio");
        }
        String valor = nombre.strip();
        if (valor.length() > NOMBRE_MAXIMO) {
            throw new CategoriaInvalidaException("El nombre no puede superar los " + NOMBRE_MAXIMO + " caracteres");
        }
        return valor;
    }

    private static String exigirDescripcion(String descripcion) {
        if (descripcion == null) {
            return "";
        }
        String valor = descripcion.strip();
        if (valor.length() > DESCRIPCION_MAXIMA) {
            throw new CategoriaInvalidaException(
                    "La descripción no puede superar los " + DESCRIPCION_MAXIMA + " caracteres");
        }
        return valor;
    }
}
