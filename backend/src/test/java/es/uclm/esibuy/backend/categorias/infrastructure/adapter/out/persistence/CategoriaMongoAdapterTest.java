package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

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
// con su validación y sus índices, como en la aplicación real. También prueba la creación (HU40, T323) y la
// con su validación y sus índices, como en la aplicación real. También prueba la creación (HU40, T323), la
// edición (HU41, T329) y la eliminación (HU42, T326).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU22-CP15, HU40-CP05, HU40-CP06, HU41-CP07 a CP09 y HU42-CP06, propuestos.
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

    // Edición de categorías (HU41, T329).

    @Test
    @DisplayName("HU41-CP07: buscarPorId devuelve la categoría con su descripción y su estado")
    void buscarPorIdDevuelveLaCategoria() {
        Categoria guardada = adapter.guardar(new Categoria(null, "Hogar", "Muebles", EstadoCategoria.INACTIVA));

        assertEquals(Optional.of(guardada), adapter.buscarPorId(guardada.id()));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"000000000000000000000000", "no-es-un-id"})
    @DisplayName("HU41-CP07: buscarPorId devuelve vacío si no existe, también con un id que no tiene formato de id")
    void buscarPorIdDevuelveVacioSiNoExiste(String id) {
        guardar("Hogar");

        assertEquals(Optional.empty(), adapter.buscarPorId(id));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"Electrónica", "ELECTRÓNICA", "electronica", " Electronica "})
    @DisplayName("HU41-CP08: otra categoría con ese nombre cuenta como repetido aunque cambien las mayúsculas o las tildes")
    void existeOtraConNombreNoDistingueMayusculasNiTildes(String nombre) {
        guardar("Electrónica");
        String idHogar = guardar("Hogar").getId();

        assertTrue(adapter.existeOtraConNombre(nombre, idHogar));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"Electrónica", "ELECTRÓNICA", "electronica"})
    @DisplayName("HU41-CP08: el nombre de la propia categoría no cuenta como repetido, aunque cambien las mayúsculas o las tildes")
    void existeOtraConNombreIgnoraLaPropiaCategoria(String nombre) {
        String id = guardar("Electrónica").getId();

        assertFalse(adapter.existeOtraConNombre(nombre, id));
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"Electr.nica", "Electrón", "Electrónicas"})
    @DisplayName("HU41-CP08: el nombre se compara completo y como texto literal, no como expresión regular")
    void existeOtraConNombreComparaElNombreCompletoYLiteral(String nombre) {
        guardar("Electrónica");
        String idHogar = guardar("Hogar").getId();

        assertFalse(adapter.existeOtraConNombre(nombre, idHogar));
    }

    @Test
    @DisplayName("HU41-CP09: guardar una categoría que ya tiene id la actualiza sin crear otra")
    void guardarConIdActualizaLaCategoria() {
        Categoria original = adapter.guardar(new Categoria(null, "Hogar", "Muebles", EstadoCategoria.INACTIVA));
        Categoria editada = new Categoria(original.id(), "Hogar y Jardín", "Muebles y plantas", EstadoCategoria.INACTIVA);

        assertEquals(editada, adapter.guardar(editada));
        assertEquals(List.of(editada), adapter.listar());
    }

    @Test
    @DisplayName("HU41-CP09: la base de datos rechaza cambiar el nombre al de otra categoría aunque se salte la comprobación previa")
    void guardarRechazaCambiarElNombreAlDeOtraCategoria() {
        adapter.guardar(Categoria.nueva("Electrónica", ""));
        Categoria hogar = adapter.guardar(Categoria.nueva("Hogar", ""));
        Categoria renombrada = new Categoria(hogar.id(), "ELECTRÓNICA", "", EstadoCategoria.ACTIVA);

        assertThrows(CategoriaDuplicadaException.class, () -> adapter.guardar(renombrada));
        assertEquals("Hogar", adapter.buscarPorId(hogar.id()).orElseThrow().nombre());
    }

    // Eliminación de categorías (HU42, T326).

    @Test
    @DisplayName("HU42-CP06: eliminar borra físicamente la categoría y deja las demás")
    void eliminarBorraSoloLaCategoriaIndicada() {
        Categoria hogar = adapter.guardar(Categoria.nueva("Hogar", ""));
        Categoria tecnologia = adapter.guardar(Categoria.nueva("Tecnología", ""));

        adapter.eliminar(hogar.id());

        assertEquals(Optional.empty(), adapter.buscarPorId(hogar.id()));
        assertEquals(List.of(tecnologia), adapter.listar());
        assertEquals(1, categoriaMongoRepository.count());
    }

    @ParameterizedTest(name = "«{0}»")
    @ValueSource(strings = {"000000000000000000000000", "no-es-un-id"})
    @DisplayName("HU42-CP06: eliminar un id que no existe no falla ni borra nada")
    void eliminarUnIdInexistenteNoBorraNada(String id) {
        Categoria hogar = adapter.guardar(Categoria.nueva("Hogar", ""));

        adapter.eliminar(id);

        assertEquals(List.of(hogar), adapter.listar());
    }

    private CategoriaDocument guardar(String nombre) {
        CategoriaDocument documento = new CategoriaDocument();
        documento.setNombre(nombre);
        return categoriaMongoRepository.save(documento);
    }
}
