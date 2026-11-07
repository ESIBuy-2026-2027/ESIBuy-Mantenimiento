package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.categorias;

import java.util.Set;
import java.util.stream.Collectors;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.out.CategoriasPort;

/**
 * Adaptador de salida que pregunta al módulo de categorías por su puerto de entrada: su listado público solo
 * devuelve las categorías activas. De cada una solo se queda con el identificador.
 */
public class CategoriasAdapter implements CategoriasPort {

    private final ConsultaCategoriasUseCase consultaCategorias;

    public CategoriasAdapter(ConsultaCategoriasUseCase consultaCategorias) {
        this.consultaCategorias = consultaCategorias;
    }

    @Override
    public Set<String> activas() {
        return consultaCategorias.listar().stream()
                .map(categoria -> categoria.id())
                .collect(Collectors.toSet());
    }
}
