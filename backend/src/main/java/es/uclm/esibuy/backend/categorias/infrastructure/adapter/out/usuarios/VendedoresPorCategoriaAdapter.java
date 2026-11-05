package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.categorias.application.port.out.VendedoresPorCategoriaPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;

/**
 * Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su dominio
 * (comunicación entre módulos del CLAUDE.md).
 */
public class VendedoresPorCategoriaAdapter implements VendedoresPorCategoriaPort {

    private final AdministracionUsuariosUseCase administracionUsuarios;

    public VendedoresPorCategoriaAdapter(AdministracionUsuariosUseCase administracionUsuarios) {
        this.administracionUsuarios = administracionUsuarios;
    }

    @Override
    public boolean existenVendedores(String categoriaId) {
        return administracionUsuarios.existenVendedoresConCategoria(categoriaId);
    }
}