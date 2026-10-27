package es.uclm.esibuy.backend.categorias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

// Listado de las categorías del administrador con su número de productos (HU39, T332). El repositorio y el
// puerto hacia productos son dobles de prueba: se prueba el caso de uso sin MongoDB ni Spring. También prueba la
// creación de categorías (HU40, T323).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU39-CP01 a CP03 y HU40-CP05 y CP06, propuestos.
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

    // Creación de categorías (HU40, T323).

    @ParameterizedTest(name = "estado {0}")
    @EnumSource(EstadoCategoria.class)
    @DisplayName("HU40-CP05: crear guarda la categoría sin id, sin espacios en los extremos y con el estado elegido")
    void crearGuardaLaCategoriaConElEstadoElegido(EstadoCategoria estado) {
        Categoria aGuardar = new Categoria(null, "Hogar", "Muebles y decoración", estado);
        Categoria guardada = new Categoria("c-1", "Hogar", "Muebles y decoración", estado);
        when(categoriaRepository.existePorNombre("Hogar")).thenReturn(false);
        when(categoriaRepository.guardar(aGuardar)).thenReturn(guardada);

        assertEquals(guardada, servicio.crear(new CrearCategoriaCommand(" Hogar ", "Muebles y decoración", estado)));
    }

    @Test
    @DisplayName("HU40-CP06: crear rechaza un nombre que ya existe y no guarda nada")
    void crearRechazaUnNombreRepetido() {
        when(categoriaRepository.existePorNombre("Hogar")).thenReturn(true);
        CrearCategoriaCommand comando = new CrearCategoriaCommand("Hogar", "", EstadoCategoria.ACTIVA);

        assertThrows(CategoriaDuplicadaException.class, () -> servicio.crear(comando));
        verify(categoriaRepository, never()).guardar(any());
    }
}
