package es.uclm.esibuy.backend.categorias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.in.EditarCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.application.port.out.VendedoresPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaConProductosException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaConVendedoresException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaInvalidaException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaNoEncontradaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

// Listado de las categorías del administrador con su número de productos (HU39, T332). El repositorio y el
// puerto hacia productos son dobles de prueba: se prueba el caso de uso sin MongoDB ni Spring. También prueba la
// creación de categorías (HU40, T323), la edición (HU41, T329) y la eliminación (HU42, T326).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU39-CP01 a CP03, HU40-CP05 y CP06, HU41-CP01 a CP05 y HU42-CP01 a CP04, propuestos.
@ExtendWith(MockitoExtension.class)
class AdministracionCategoriasServiceTest {

    private static final Categoria HOGAR =
            new Categoria("c-1", "Hogar", "Muebles y decoración", EstadoCategoria.ACTIVA);
    private static final Categoria TECNOLOGIA = new Categoria("c-2", "Tecnología", "", EstadoCategoria.ACTIVA);

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private ProductosPorCategoriaPort productosPorCategoria;

    @Mock
    private VendedoresPorCategoriaPort vendedoresPorCategoria;

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

    // Edición de categorías (HU41, T329).

    @Test
    @DisplayName("HU41-CP01: editar cambia el nombre y la descripción, conserva el id y el estado y devuelve el número de productos")
    void editarCambiaElNombreYLaDescripcion() {
        Categoria actual = new Categoria("c-1", "Hogar", "Muebles", EstadoCategoria.INACTIVA);
        Categoria editada = new Categoria("c-1", "Hogar y Jardín", "Muebles y plantas", EstadoCategoria.INACTIVA);
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(actual));
        when(categoriaRepository.existeOtraConNombre("Hogar y Jardín", "c-1")).thenReturn(false);
        when(categoriaRepository.guardar(editada)).thenReturn(editada);
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of("c-1", 4L));

        assertEquals(new CategoriaConProductos(editada, 4),
                servicio.editar(new EditarCategoriaCommand("c-1", " Hogar y Jardín ", "Muebles y plantas")));
    }

    @Test
    @DisplayName("HU41-CP02: editar permite cambiar solo las mayúsculas o las tildes del propio nombre")
    void editarPermiteCambiarLasMayusculasDelPropioNombre() {
        Categoria actual = new Categoria("c-1", "Hogar", "", EstadoCategoria.ACTIVA);
        Categoria editada = new Categoria("c-1", "hogar", "", EstadoCategoria.ACTIVA);
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(actual));
        when(categoriaRepository.existeOtraConNombre("hogar", "c-1")).thenReturn(false);
        when(categoriaRepository.guardar(editada)).thenReturn(editada);
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of());

        assertEquals(new CategoriaConProductos(editada, 0),
                servicio.editar(new EditarCategoriaCommand("c-1", "hogar", "")));
    }

    @Test
    @DisplayName("HU41-CP03: editar rechaza un nombre que ya tiene otra categoría y no guarda nada")
    void editarRechazaElNombreDeOtraCategoria() {
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(HOGAR));
        when(categoriaRepository.existeOtraConNombre("Tecnología", "c-1")).thenReturn(true);
        EditarCategoriaCommand comando = new EditarCategoriaCommand("c-1", "Tecnología", "");

        assertThrows(CategoriaDuplicadaException.class, () -> servicio.editar(comando));
        verify(categoriaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU41-CP04: editar una categoría que no existe lanza «no encontrada» y no guarda nada")
    void editarUnaCategoriaInexistenteFalla() {
        when(categoriaRepository.buscarPorId("c-9")).thenReturn(Optional.empty());
        EditarCategoriaCommand comando = new EditarCategoriaCommand("c-9", "Hogar", "");

        assertThrows(CategoriaNoEncontradaException.class, () -> servicio.editar(comando));
        verify(categoriaRepository, never()).existeOtraConNombre(any(), any());
        verify(categoriaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU41-CP05: editar con datos que incumplen el dominio lanza «inválida» y no guarda nada")
    void editarConDatosInvalidosFalla() {
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(HOGAR));
        EditarCategoriaCommand comando = new EditarCategoriaCommand("c-1", "   ", "");

        assertThrows(CategoriaInvalidaException.class, () -> servicio.editar(comando));
        verify(categoriaRepository, never()).existeOtraConNombre(any(), any());
        verify(categoriaRepository, never()).guardar(any());
    }

    // Eliminación de categorías (HU42, T326).

    @Test
    @DisplayName("HU42-CP01: eliminar borra una categoría sin productos ni vendedores")
    void eliminarBorraUnaCategoriaSinProductosNiVendedores() {
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(HOGAR));
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of("c-2", 7L));
        when(vendedoresPorCategoria.existenVendedores("c-1")).thenReturn(false);

        servicio.eliminar("c-1");

        verify(categoriaRepository).eliminar("c-1");
    }

    @Test
    @DisplayName("HU42-CP02: eliminar rechaza una categoría con productos y no borra nada")
    void eliminarRechazaUnaCategoriaConProductos() {
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(HOGAR));
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of("c-1", 4L));

        assertThrows(CategoriaConProductosException.class, () -> servicio.eliminar("c-1"));
        verify(categoriaRepository, never()).eliminar(any());
        verifyNoInteractions(vendedoresPorCategoria);
    }

    @Test
    @DisplayName("HU42-CP03: eliminar rechaza una categoría que es la principal de un vendedor y no borra nada")
    void eliminarRechazaUnaCategoriaConVendedores() {
        when(categoriaRepository.buscarPorId("c-1")).thenReturn(Optional.of(HOGAR));
        when(productosPorCategoria.contarPorCategoria()).thenReturn(Map.of());
        when(vendedoresPorCategoria.existenVendedores("c-1")).thenReturn(true);

        assertThrows(CategoriaConVendedoresException.class, () -> servicio.eliminar("c-1"));
        verify(categoriaRepository, never()).eliminar(any());
    }

    @Test
    @DisplayName("HU42-CP04: eliminar una categoría que no existe lanza «no encontrada» sin consultar ni borrar nada")
    void eliminarUnaCategoriaInexistenteFalla() {
        when(categoriaRepository.buscarPorId("c-9")).thenReturn(Optional.empty());

        assertThrows(CategoriaNoEncontradaException.class, () -> servicio.eliminar("c-9"));
        verifyNoInteractions(productosPorCategoria, vendedoresPorCategoria);
        verify(categoriaRepository, never()).eliminar(any());
    }
}
