package es.uclm.esibuy.backend.categorias.application.port.out;

/** Lo que categorías necesita del módulo de usuarios: si algún vendedor tiene una categoría como principal. */
public interface VendedoresPorCategoriaPort {

    // ¿Hay algún vendedor cuya categoría principal sea esta?
    boolean existenVendedores(String categoriaId);
}