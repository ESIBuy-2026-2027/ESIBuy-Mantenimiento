package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.categorias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

// Adaptador de las preferencias hacia el módulo de categorías (HU46, T287), sin Spring ni MongoDB. El listado público
// de categorías solo devuelve las activas (HU39-CP05).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU46-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class CategoriasAdapterTest {

    @Mock
    private ConsultaCategoriasUseCase consultaCategorias;

    @Test
    @DisplayName("HU46-CP13: las categorías que se pueden elegir se piden al módulo de categorías por su puerto de entrada")
    void activasPreguntaAlModuloDeCategorias() {
        when(consultaCategorias.listar()).thenReturn(List.of(
                new Categoria("cat-1", "Hogar", "", EstadoCategoria.ACTIVA),
                new Categoria("cat-2", "Libros", "", EstadoCategoria.ACTIVA)));

        assertEquals(Set.of("cat-1", "cat-2"), new CategoriasAdapter(consultaCategorias).activas());
    }
}
