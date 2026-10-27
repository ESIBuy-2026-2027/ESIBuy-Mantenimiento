package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;

// Test de integración del adaptador de MongoDB de categorías (AB#43). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea la colección categorias
// con su validación, como en la aplicación real.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU22-CP15, propuesto.
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

    private CategoriaDocument guardar(String nombre) {
        CategoriaDocument documento = new CategoriaDocument();
        documento.setNombre(nombre);
        return categoriaMongoRepository.save(documento);
    }
}
