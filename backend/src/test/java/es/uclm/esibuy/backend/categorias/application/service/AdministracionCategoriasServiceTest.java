package es.uclm.esibuy.backend.categorias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

// Listado de las categorías del administrador con su número de productos (HU39, T332). El repositorio y el
// puerto hacia productos son dobles de prueba: se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU39-CP01 a CP03, propuestos.
@ExtendWith(MockitoExtension.class)
class AdministracionCategoriasServiceTest {

    private static final Categoria HOGAR =
            new Categoria("c-1", "Hogar", "Muebles y decoración", EstadoCategoria.ACTIVA);
    private static final Categoria TECNOLOGIA = new Categoria("c-2", "Tecnología", "", EstadoCategoria.ACTIVA);

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private ProductosPorCategoriaPort productosPorCategoria;

    @InjectMocks
    private AdministracionCategoriasService servicio;

    @Test
    @DisplayName("HU39-CP01: cada categoría sale con su nombre, descripción, estado y número de productos")
    void listarConProductosDevuelveCadaCategoriaConSuNumero() {
        when(categoriaRepository.listar()).thenReturn(List.of(HOGAR, TECNOLOGIA));
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of("c-1", 4L, "c-2", 7L));

        assertEquals(List.of(new CategoriaConProductos(HOGAR, 4), new CategoriaConProductos(TECNOLOGIA, 7)),
                servicio.listarConProductos());
    }

    @Test
    @DisplayName("HU39-CP02: una categoría sin productos sale con 0")
    void unaCategoriaSinProductosTieneCero() {
        when(categoriaRepository.listar()).thenReturn(List.of(HOGAR, TECNOLOGIA));
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of("c-2", 7L));

        assertEquals(List.of(new CategoriaConProductos(HOGAR, 0), new CategoriaConProductos(TECNOLOGIA, 7)),
                servicio.listarConProductos());
    }

    @Test
    @DisplayName("HU39-CP03: el administrador también ve las categorías inactivas")
    void incluyeLasCategoriasInactivas() {
        Categoria juguetes = new Categoria("c-3", "Juguetes", "", EstadoCategoria.INACTIVA);
        when(categoriaRepository.listar()).thenReturn(List.of(HOGAR, juguetes));
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of("c-3", 2L));

        assertEquals(List.of(new CategoriaConProductos(HOGAR, 0), new CategoriaConProductos(juguetes, 2)),
                servicio.listarConProductos());
    }
}
