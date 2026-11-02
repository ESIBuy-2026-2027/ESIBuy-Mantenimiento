package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.categorias.application.port.out.VendedoresPorCategoriaPort;

/** Adaptador de salida hacia el módulo de usuarios. Esqueleto del ciclo rojo (HU42): se completa en su ciclo. */
public class VendedoresPorCategoriaAdapter implements VendedoresPorCategoriaPort {

    @Override
    public boolean existenVendedores(String categoriaId) {
        return false;
    }
}