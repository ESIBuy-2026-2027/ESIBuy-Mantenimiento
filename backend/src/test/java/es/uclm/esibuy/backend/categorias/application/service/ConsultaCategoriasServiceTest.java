package es.uclm.esibuy.backend.categorias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

// Consulta de las categorías (AB#43): la edición del vendedor (HU22) elige su categoría principal entre ellas.
// El repositorio es un doble de prueba: se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU22-CP15, propuesto.
@ExtendWith(MockitoExtension.class)
class ConsultaCategoriasServiceTest {

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @InjectMocks
    private ConsultaCategoriasService servicio;

    @Test
    @DisplayName("HU22-CP15: listar devuelve las categorías del repositorio")
    void listarDevuelveLasCategoriasDelRepositorio() {
        List<Categoria> categorias = List.of(new Categoria("c-2", "Hogar", "", EstadoCategoria.ACTIVA), new Categoria("c-1", "Tecnología", "", EstadoCategoria.ACTIVA));
        when(categoriaRepository.listar()).thenReturn(categorias);

        assertEquals(categorias, servicio.listar());
    }

    @Test
    @DisplayName("HU39-CP05: el listado público de categorías solo devuelve las activas")
    void listarSoloDevuelveLasActivas() {
        Categoria activa = new Categoria("c-1", "Tecnología", "", EstadoCategoria.ACTIVA);
        Categoria inactiva = new Categoria("c-2", "Hogar", "", EstadoCategoria.INACTIVA);
        when(categoriaRepository.listar()).thenReturn(List.of(activa, inactiva));

        assertEquals(List.of(activa), servicio.listar());
    }
}
