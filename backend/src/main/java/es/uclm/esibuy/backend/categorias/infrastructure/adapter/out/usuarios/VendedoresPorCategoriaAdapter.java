package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.categorias.application.port.out.VendedoresPorCategoriaPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;

/** Adaptador de salida hacia el módulo de usuarios. Esqueleto del ciclo rojo (HU42): se completa en su ciclo. */
public class VendedoresPorCategoriaAdapter implements VendedoresPorCategoriaPort {

    private final AdministracionUsuariosUseCase administracionUsuarios;

    public VendedoresPorCategoriaAdapter(AdministracionUsuariosUseCase administracionUsuarios) {
        this.administracionUsuarios = administracionUsuarios;
    }

    @Override
    public boolean existenVendedores(String categoriaId) {
        administracionUsuarios.existenVendedoresConCategoria(categoriaId);
        return false;
    }
}