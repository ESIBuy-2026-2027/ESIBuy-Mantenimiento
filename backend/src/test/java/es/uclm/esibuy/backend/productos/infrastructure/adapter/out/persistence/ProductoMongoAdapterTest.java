package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test de integración del adaptador de MongoDB de productos (HU31, T202). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea la colección productos
// con su validación, como en la aplicación real, así que un documento que no la cumpla hace fallar el test.
// También prueba el listado de los productos propios del vendedor (HU32, T184).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP11, CP12 y CP23, y HU32-CP01 a CP03, propuestos.
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
    @DisplayName("HU31-CP23: el nombre cuenta como repetido aunque cambien las mayúsculas, pero solo para el mismo vendedor")
    void existePorVendedorYNombreNoDistingueMayusculas() {
        guardarProducto("v-1", "Teclado", "cat-1", "2026-10-01T10:00:00Z");

        assertTrue(adapter.existePorVendedorYNombre("v-1", "Teclado"));
        assertTrue(adapter.existePorVendedorYNombre("v-1", "teclado"));
        assertTrue(adapter.existePorVendedorYNombre("v-1", "TECLADO"));
        assertFalse(adapter.existePorVendedorYNombre("v-2", "Teclado"));
        assertFalse(adapter.existePorVendedorYNombre("v-1", "Ratón"));
    }

    @Test
    @DisplayName("HU31-CP23: el nombre se compara completo y como texto literal, no como expresión regular")
    void existePorVendedorYNombreComparaElNombreCompletoYLiteral() {
        guardarProducto("v-1", "Ratón (inalámbrico)", "cat-1", "2026-10-01T10:00:00Z");

        assertTrue(adapter.existePorVendedorYNombre("v-1", "Ratón (inalámbrico)"));
        assertFalse(adapter.existePorVendedorYNombre("v-1", "Ratón"));
        assertFalse(adapter.existePorVendedorYNombre("v-1", "Ratón (.*)"));
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

    // Listado de los productos propios (HU32, T184).

    // Guarda un producto y fija su createdAt a mano, para que el orden no dependa del reloj.
    private String guardarProducto(String vendedorId, String nombre, String categoriaId, String creadoEn) {
        String id = adapter.guardar(new Producto(null, vendedorId, nombre, "", new BigDecimal("10"), 1, categoriaId,
                "", true, Descuento.sinDescuento(), Descuento.sinDescuento())).getId();
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(id))),
                Update.update("createdAt", Date.from(Instant.parse(creadoEn))), COLECCION);
        return id;
    }

    private static List<String> nombres(ProductoPage pagina) {
        return pagina.productos().stream().map(Producto::getNombre).toList();
    }

    @Test
    @DisplayName("HU32-CP01: el listado solo devuelve los productos del vendedor, y el total solo cuenta los suyos")
    void listarPorVendedorSoloDevuelveLosSuyos() {
        guardarProducto("v-1", "Teclado mecánico", "cat-1", "2026-10-01T10:00:00Z");
        guardarProducto("v-2", "Teclado de otro vendedor", "cat-1", "2026-10-02T10:00:00Z");

        ProductoPage pagina = adapter.listarPorVendedor("v-1", new ListarProductosQuery(null, null, 0, 10));

        assertEquals(List.of("Teclado mecánico"), nombres(pagina));
        assertEquals(1, pagina.totalElementos());
    }

    @Test
    @DisplayName("HU32-CP02: la búsqueda por nombre no distingue mayúsculas y trata el texto como literal")
    void listarPorVendedorBuscaPorNombreSinDistinguirMayusculas() {
        guardarProducto("v-1", "Teclado mecánico", "cat-1", "2026-10-01T10:00:00Z");
        guardarProducto("v-1", "TECLADO numérico", "cat-1", "2026-10-02T10:00:00Z");
        guardarProducto("v-1", "Ratón (inalámbrico)", "cat-2", "2026-10-03T10:00:00Z");

        assertEquals(List.of("TECLADO numérico", "Teclado mecánico"), nombres(
                adapter.listarPorVendedor("v-1", new ListarProductosQuery("teclado", null, 0, 10))));
        // El paréntesis no se interpreta como expresión regular
        assertEquals(List.of("Ratón (inalámbrico)"), nombres(
                adapter.listarPorVendedor("v-1", new ListarProductosQuery("(inal", null, 0, 10))));
    }

    @Test
    @DisplayName("HU32-CP02: el filtro por categoría se puede combinar con la búsqueda")
    void listarPorVendedorFiltraPorCategoriaYBusqueda() {
        guardarProducto("v-1", "Teclado mecánico", "cat-1", "2026-10-01T10:00:00Z");
        guardarProducto("v-1", "Teclado numérico", "cat-2", "2026-10-02T10:00:00Z");
        guardarProducto("v-1", "Ratón", "cat-2", "2026-10-03T10:00:00Z");

        assertEquals(List.of("Ratón", "Teclado numérico"), nombres(
                adapter.listarPorVendedor("v-1", new ListarProductosQuery(null, "cat-2", 0, 10))));
        assertEquals(List.of("Teclado numérico"), nombres(
                adapter.listarPorVendedor("v-1", new ListarProductosQuery("teclado", "cat-2", 0, 10))));
    }

    @Test
    @DisplayName("HU32-CP03: el listado va del más reciente al más antiguo, paginado y con el total completo")
    void listarPorVendedorPaginaDelMasRecienteAlMasAntiguo() {
        guardarProducto("v-1", "Antiguo", "cat-1", "2026-10-01T10:00:00Z");
        guardarProducto("v-1", "Reciente", "cat-1", "2026-10-03T10:00:00Z");
        guardarProducto("v-1", "Intermedio", "cat-1", "2026-10-02T10:00:00Z");

        ProductoPage primera = adapter.listarPorVendedor("v-1", new ListarProductosQuery(null, null, 0, 2));
        ProductoPage segunda = adapter.listarPorVendedor("v-1", new ListarProductosQuery(null, null, 1, 2));

        assertEquals(List.of("Reciente", "Intermedio"), nombres(primera));
        assertEquals(List.of("Antiguo"), nombres(segunda));
        assertEquals(3, segunda.totalElementos());
    }

        @Test
    @DisplayName("HU31-CP24: la base de datos rechaza un nombre repetido del mismo vendedor aunque se salte la comprobación previa")
    void guardarRechazaUnNombreRepetidoDelMismoVendedor() {
        adapter.guardar(productoNuevo("v-1", "Teclado"));

        assertThrows(ProductoDuplicadoException.class, () -> adapter.guardar(productoNuevo("v-1", "TECLADO")));
        assertDoesNotThrow(() -> adapter.guardar(productoNuevo("v-2", "Teclado")));
        assertEquals(2, mongoTemplate.getCollection(COLECCION).countDocuments());
    }

    // T258: el dominio guarda el nombre recortado, así que el índice único también para los espacios.
    @Test
    @DisplayName("HU31-CP25: la base de datos rechaza el mismo nombre con espacios en los extremos")
    void guardarRechazaUnNombreRepetidoConEspacios() {
        adapter.guardar(productoNuevo("v-1", "Teclado"));
        Producto conEspacios = productoNuevo("v-1", "  Teclado ");

        assertThrows(ProductoDuplicadoException.class, () -> adapter.guardar(conEspacios));
        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
    }

    private static Producto productoNuevo(String vendedorId, String nombre) {
        return new Producto(null, vendedorId, nombre, "", new BigDecimal("10"), 1, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }
}
