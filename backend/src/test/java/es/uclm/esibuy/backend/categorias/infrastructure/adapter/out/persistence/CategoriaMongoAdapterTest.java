package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;

// Test de integración del adaptador de MongoDB de categorías (AB#43). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea la colección categorias
// con su validación y sus índices, como en la aplicación real. También prueba la creación (HU40, T323).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU22-CP15, HU40-CP05 y HU40-CP06, propuestos.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class CategoriaMongoAdapterTest {

    @Autowired
    private CategoriaMongoRepository categoriaMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private CategoriaMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        categoriaMongoRepository.deleteAll();
        adapter = new CategoriaMongoAdapter(categoriaMongoRepository, new CategoriaDocumentMapper(), mongoTemplate);
    }

    @Test
    @DisplayName("HU22-CP15: listar devuelve las categorías guardadas, ordenadas por nombre")
    void listarDevuelveLasCategoriasOrdenadasPorNombre() {
        CategoriaDocument tecnologia = guardar("Tecnología");
        CategoriaDocument hogar = guardar("Hogar");

        assertEquals(List.of(new Categoria(hogar.getId(), "Hogar", "", EstadoCategoria.ACTIVA),
                        new Categoria(tecnologia.getId(), "Tecnología", "", EstadoCategoria.ACTIVA)),
                adapter.listar());
    }

    @Test
    @DisplayName("HU40-CP04: la descripción y el estado se guardan en MongoDB y se leen igual")
    void guardaYLeeLaDescripcionYElEstado() {
        CategoriaDocument documento = new CategoriaDocumentMapper().aDocumento(
                new Categoria(null, "Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA));
        String id = categoriaMongoRepository.save(documento).getId();

        assertEquals(List.of(new Categoria(id, "Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA)),
                adapter.listar());
    }

    @Test
    @DisplayName("HU22-CP15: sin categorías guardadas, listar devuelve una lista vacía")
    void sinCategoriasDevuelveUnaListaVacia() {
        assertEquals(List.of(), adapter.listar());
    }

    // Creación de categorías (HU40, T323).

    @Test
    @DisplayName("HU40-CP05: guardar una categoría nueva le asigna un id y la guarda con su descripción y su estado")
    void guardarAsignaIdYLaGuardaConSuEstado() {
        Categoria guardada = adapter.guardar(new Categoria(null, "Hogar", "Muebles", EstadoCategoria.INACTIVA));

        assertNotNull(guardada.id());
        assertEquals(List.of(new Categoria(guardada.id(), "Hogar", "Muebles", EstadoCategoria.INACTIVA)),
                adapter.listar());
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"Electrónica", "ELECTRÓNICA", "electronica", " Electronica "})
    @DisplayName("HU40-CP06: el nombre cuenta como repetido aunque cambien las mayúsculas o las tildes")
    void existePorNombreNoDistingueMayusculasNiTildes(String nombre) {
        guardar("Electrónica");

        assertTrue(adapter.existePorNombre(nombre));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"Electr.nica", "Electrón", "Electrónicas"})
    @DisplayName("HU40-CP06: el nombre se compara completo y como texto literal, no como expresión regular")
    void existePorNombreComparaElNombreCompletoYLiteral(String nombre) {
        guardar("Electrónica");

        assertFalse(adapter.existePorNombre(nombre));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"ELECTRÓNICA", "Electronica"})
    @DisplayName("HU40-CP06: la base de datos rechaza el mismo nombre con otras mayúsculas o tildes aunque se salte la comprobación previa")
    void guardarRechazaUnNombreRepetidoConOtrasMayusculasOTildes(String nombre) {
        adapter.guardar(Categoria.nueva("Electrónica", ""));
        Categoria repetida = Categoria.nueva(nombre, "");

        assertThrows(CategoriaDuplicadaException.class, () -> adapter.guardar(repetida));
        assertEquals(1, adapter.listar().size());
    }

    private CategoriaDocument guardar(String nombre) {
        CategoriaDocument documento = new CategoriaDocument();
        documento.setNombre(nombre);
        return categoriaMongoRepository.save(documento);
    }
}
