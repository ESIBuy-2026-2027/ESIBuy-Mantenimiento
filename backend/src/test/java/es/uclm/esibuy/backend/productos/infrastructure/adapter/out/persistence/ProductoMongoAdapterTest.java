package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test de integración del adaptador de MongoDB de productos (HU31, T202). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea la colección productos
// con su validación, como en la aplicación real, así que un documento que no la cumpla hace fallar el test.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP11 y CP12, propuestos.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class ProductoMongoAdapterTest {

    private static final String COLECCION = "productos";

    @Autowired
    private ProductoMongoRepository productoMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private ProductoMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        productoMongoRepository.deleteAll();
        adapter = new ProductoMongoAdapter(productoMongoRepository, new ProductoDocumentMapper(), mongoTemplate);
    }

    private static Producto producto(String id, int stock) {
        return new Producto(id, "v-1", "Teclado mecánico", "Switches rojos", new BigDecimal("89.50"), stock,
                "cat-2", "https://esibuy.test/teclado.png", true,
                new Descuento(true, new BigDecimal("12.5")), Descuento.sinDescuento());
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB, sin pasar por el mapper.
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection(COLECCION).find().first();
        assertNotNull(guardado);
        return guardado;
    }

    @Test
    @DisplayName("HU31-CP11: guardar un producto nuevo le asigna un id y lo deja en la colección productos")
    void guardarAsignaIdYLoGuardaEnProductos() {
        Producto guardado = adapter.guardar(producto(null, 12));

        assertNotNull(guardado.getId());
        Document documento = documentoEnBruto();
        assertEquals(guardado.getId(), documento.getObjectId("_id").toHexString());
        assertEquals("v-1", documento.getString("vendedorId"));
        assertEquals("cat-2", documento.getString("categoriaId"));
        assertFalse(documento.containsKey("_class"));
    }

    @Test
    @DisplayName("HU31-CP11: buscar por id devuelve el producto guardado con los mismos datos")
    void buscarPorIdDevuelveElProductoGuardado() {
        String id = adapter.guardar(producto(null, 12)).getId();

        Producto leido = adapter.buscarPorId(id).orElseThrow();

        assertEquals(id, leido.getId());
        assertEquals("v-1", leido.getVendedorId());
        assertEquals("Teclado mecánico", leido.getNombre());
        assertEquals("Switches rojos", leido.getDescripcion());
        assertEquals(new BigDecimal("89.50"), leido.getPrecio());
        assertEquals(12, leido.getStock());
        assertEquals("cat-2", leido.getCategoriaId());
        assertEquals("https://esibuy.test/teclado.png", leido.getImagen());
        assertTrue(leido.isDisponible());
        assertEquals(new Descuento(true, new BigDecimal("12.5")), leido.getDescuento());
        assertEquals(Descuento.sinDescuento(), leido.getDescuentoPremium());
    }

    @Test
    @DisplayName("HU31-CP12: buscar un producto que no existe devuelve vacío")
    void buscarPorIdDevuelveVacioSiNoExiste() {
        assertTrue(adapter.buscarPorId("6a0000000000000000000000").isEmpty());
    }

    @Test
    @DisplayName("Persistencia: guardar un producto leído lo actualiza sin duplicarlo y conserva createdAt")
    void guardarUnProductoLeidoLoActualiza() {
        String id = adapter.guardar(producto(null, 12)).getId();
        Object creadoEn = documentoEnBruto().get("createdAt");

        adapter.guardar(producto(id, 3));

        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
        Document documento = documentoEnBruto();
        assertEquals(3, documento.getInteger("stock"));
        assertNotNull(creadoEn);
        assertEquals(creadoEn, documento.get("createdAt"));
        assertNotNull(documento.get("updatedAt"));
    }

    @Test
    @DisplayName("Persistencia: el precio y los porcentajes se guardan como número decimal, no como texto")
    void elPrecioSeGuardaComoNumero() {
        adapter.guardar(producto(null, 12));

        Document documento = documentoEnBruto();
        assertInstanceOf(Decimal128.class, documento.get("precio"));
        assertInstanceOf(Decimal128.class, documento.get("descuento", Document.class).get("porcentaje"));
    }
}
