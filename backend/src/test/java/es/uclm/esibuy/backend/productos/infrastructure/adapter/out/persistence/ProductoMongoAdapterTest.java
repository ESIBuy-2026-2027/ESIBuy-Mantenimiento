package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
// También prueba el listado de los productos propios del vendedor (HU32, T184), el borrado físico (HU35, T346),
// el catálogo del cliente (HU7, T352) y el recuento de productos por categoría (HU39, T332).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP11, CP12 y CP23, HU32-CP01 a CP03, HU7-CP11 a CP15
// y HU39-CP02, propuestos. La media y el número de valoraciones (HU13, T415): caso HU13-CP24, propuesto.
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

    @Test
    @DisplayName("HU35-CP03: eliminar un producto lo borra de la colección (borrado físico) y deja los demás")
    void eliminarBorraElDocumentoYDejaLosDemas() {
        String id = adapter.guardar(productoNuevo("v-1", "Teclado")).getId();
        String otro = adapter.guardar(productoNuevo("v-1", "Ratón")).getId();

        adapter.eliminar(id);

        assertTrue(adapter.buscarPorId(id).isEmpty());
        assertTrue(adapter.buscarPorId(otro).isPresent());
        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
    }

    // Recuento de productos por categoría (HU39, T332).

    @Test
    @DisplayName("HU39-CP02: el recuento agrupa los productos de todos los vendedores por su categoría")
    void contarPorCategoriaAgrupaLosProductos() {
        guardarProducto("v-1", "Teclado", "cat-1", "2026-10-01T10:00:00Z");
        guardarProducto("v-2", "Ratón", "cat-1", "2026-10-02T10:00:00Z");
        guardarProducto("v-1", "Lámpara", "cat-2", "2026-10-03T10:00:00Z");

        assertEquals(Map.of("cat-1", 2L, "cat-2", 1L), adapter.contarPorCategoria());
    }

    @Test
    @DisplayName("HU39-CP02: sin productos el recuento está vacío")
    void contarPorCategoriaSinProductosDevuelveVacio() {
        assertTrue(adapter.contarPorCategoria().isEmpty());
    }

    @Test
    @DisplayName("HU7-CP11: el catálogo solo devuelve productos disponibles, de todos los vendedores")
    void buscarEnCatalogoSoloDevuelveDisponiblesDeTodosLosVendedores() {
        adapter.guardar(productoNuevo("v-1", "Teclado"));
        adapter.guardar(productoNuevo("v-2", "Ratón"));
        adapter.guardar(new Producto(null, "v-1", "Monitor retirado", "", new BigDecimal("10"), 1, "cat-1", "", false,
                Descuento.sinDescuento(), Descuento.sinDescuento()));

        ProductoPage pagina = adapter.buscarEnCatalogo(sinFiltros());

        // Como un Set: el orden del catálogo no se fija hasta el ciclo 6.
        assertEquals(Set.of("Teclado", "Ratón"), Set.copyOf(nombres(pagina)));
        assertEquals(2, pagina.totalElementos());
    }

    @Test
    @DisplayName("HU7-CP12: la búsqueda por nombre no distingue mayúsculas ni tildes")
    void buscarEnCatalogoBuscaSinDistinguirMayusculasNiTildes() {
        adapter.guardar(productoNuevo("v-1", "Lámpara de escritorio"));
        adapter.guardar(productoNuevo("v-2", "Teclado"));

        // Sin tilde y en mayúsculas, y con tilde en minúsculas: las dos encuentran «Lámpara».
        assertEquals(List.of("Lámpara de escritorio"), nombres(adapter.buscarEnCatalogo(buscando("LAMPARA"))));
        assertEquals(List.of("Lámpara de escritorio"), nombres(adapter.buscarEnCatalogo(buscando("lámpara"))));
    }

    @Test
    @DisplayName("HU7-CP12: la búsqueda trata el texto como literal, no como expresión regular")
    void buscarEnCatalogoTrataLaBusquedaComoTextoLiteral() {
        adapter.guardar(productoNuevo("v-1", "Cable (USB-C)"));
        adapter.guardar(productoNuevo("v-1", "Cable USB"));

        // «(» y «.*» se buscan como caracteres: como regex, «(usb» no compilaría y «.*» lo encontraría todo.
        assertEquals(List.of("Cable (USB-C)"), nombres(adapter.buscarEnCatalogo(buscando("(usb"))));
        assertEquals(List.of(), nombres(adapter.buscarEnCatalogo(buscando(".*"))));
    }

    @Test
    @DisplayName("HU7-CP13: el filtro por categoría se puede combinar con la búsqueda")
    void buscarEnCatalogoFiltraPorCategoriaYBusqueda() {
        // Dos «Teclado» de vendedores distintos (un vendedor no repite nombre): uno en cat-1 y otro en cat-2.
        guardarProducto("v-1", "Teclado", "cat-1", "2026-10-01T10:00:00Z");
        guardarProducto("v-2", "Teclado", "cat-2", "2026-10-02T10:00:00Z");
        guardarProducto("v-2", "Ratón", "cat-2", "2026-10-03T10:00:00Z");

        ProductoPage pagina = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery("teclado", "cat-2", null, CriterioOrdenCatalogo.NOMBRE, false, 0, 10));

        assertEquals(1, pagina.totalElementos());
        assertEquals("Teclado", pagina.productos().getFirst().getNombre());
        assertEquals("cat-2", pagina.productos().getFirst().getCategoriaId());
    }

    @Test
    @DisplayName("HU7-CP14: el catálogo se ordena por nombre en los dos sentidos, paginado y con el total completo")
    void buscarEnCatalogoOrdenaPorNombreYPagina() {
        // Guardados fuera de orden y sin tilde inicial, para no depender del orden alfabético en español.
        adapter.guardar(productoNuevo("v-1", "Cesta"));
        adapter.guardar(productoNuevo("v-1", "Atril"));
        adapter.guardar(productoNuevo("v-2", "Banco"));

        ProductoPage ascendente = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery(null, null, null, CriterioOrdenCatalogo.NOMBRE, false, 0, 2));
        ProductoPage descendente = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery(null, null, null, CriterioOrdenCatalogo.NOMBRE, true, 0, 2));

        assertEquals(List.of("Atril", "Banco"), nombres(ascendente));
        assertEquals(3, ascendente.totalElementos());
        assertEquals(List.of("Cesta", "Banco"), nombres(descendente));
    }

    // Valoración mínima y orden por valoración (HU7) con la media de las valoraciones de HU13 (T415). Un producto
    // sin valoraciones no llega a ningún mínimo y al ordenar cuenta como el que menos.
    @Test
    @DisplayName("HU7-CP15: con una valoración mínima solo salen los productos con esa media o más")
    void buscarEnCatalogoConValoracionMinimaFiltraPorLaMedia() {
        guardarValorado("Atril", 4.5);
        guardarValorado("Banco", 3.0);
        adapter.guardar(productoNuevo("v-1", "Cesta"));

        ProductoPage desdeCuatro = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery(null, null, 4, CriterioOrdenCatalogo.NOMBRE, false, 0, 10));
        ProductoPage desdeTres = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery(null, null, 3, CriterioOrdenCatalogo.NOMBRE, false, 0, 10));

        assertEquals(List.of("Atril"), nombres(desdeCuatro));
        assertEquals(1, desdeCuatro.totalElementos());
        assertEquals(List.of("Atril", "Banco"), nombres(desdeTres));
    }

    @Test
    @DisplayName("HU7-CP15: por valoración ordena por la media, desempata por nombre y sin valoraciones es la menor")
    void buscarEnCatalogoPorValoracionOrdenaPorLaMedia() {
        adapter.guardar(productoNuevo("v-1", "Atril"));
        guardarValorado("Cesta", 4.0);
        guardarValorado("Banco", 4.0);
        guardarValorado("Dado", 2.0);

        ProductoPage descendente = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery(null, null, null, CriterioOrdenCatalogo.VALORACION, true, 0, 10));
        ProductoPage ascendente = adapter.buscarEnCatalogo(
                new BuscarCatalogoQuery(null, null, null, CriterioOrdenCatalogo.VALORACION, false, 0, 10));

        // Banco y Cesta empatan a 4,0: el empate se resuelve de la A a la Z en los dos sentidos
        assertEquals(List.of("Banco", "Cesta", "Dado", "Atril"), nombres(descendente));
        assertEquals(List.of("Atril", "Dado", "Banco", "Cesta"), nombres(ascendente));
    }

    private void guardarValorado(String nombre, double media) {
        String id = adapter.guardar(productoNuevo("v-1", nombre)).getId();
        adapter.actualizarValoracion(id, media, 2);
    }

    // Catálogo sin búsqueda, categoría ni valoración mínima, por nombre de la A a la Z, primera página de 10.
    private static BuscarCatalogoQuery sinFiltros() {
        return new BuscarCatalogoQuery(null, null, null, CriterioOrdenCatalogo.NOMBRE, false, 0, 10);
    }

    // Como sinFiltros, pero buscando ese texto en el nombre.
    private static BuscarCatalogoQuery buscando(String texto) {
        return new BuscarCatalogoQuery(texto, null, null, CriterioOrdenCatalogo.NOMBRE, false, 0, 10);
    }

    private static Producto productoNuevo(String vendedorId, String nombre) {
        return new Producto(null, vendedorId, nombre, "", new BigDecimal("10"), 1, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }

    // Stock al realizar un pedido (HU10): se descuenta en una sola operación de MongoDB, condicionada al stock
    // Media y número de valoraciones del producto (HU13, T415): el módulo de valoraciones los calcula con todas las
    // valoraciones del producto y el adaptador solo los guarda, para que el catálogo pueda ordenar y filtrar por ellos.
    @Test
    @DisplayName("HU13-CP24: guarda en el producto la media y el número de valoraciones que recibe")
    void guardaLaMediaYElNumeroDeValoraciones() {
        String id = adapter.guardar(producto(null, 5)).getId();

        adapter.actualizarValoracion(id, 4.5, 2);

        Document guardado = documentoEnBruto();
        assertEquals(4.5, ((Number) guardado.get("valoracionMedia")).doubleValue());
        assertEquals(2, guardado.getInteger("numeroValoraciones"));
    }

    @Test
    @DisplayName("HU13-CP24: una actualización nueva sustituye la media y el número y no toca el resto del producto")
    void unaActualizacionNuevaSustituyeLaMediaYElNumero() {
        String id = adapter.guardar(producto(null, 5)).getId();

        adapter.actualizarValoracion(id, 4.5, 2);
        adapter.actualizarValoracion(id, 4.0, 3);

        Document guardado = documentoEnBruto();
        assertEquals(4.0, ((Number) guardado.get("valoracionMedia")).doubleValue());
        assertEquals(3, guardado.getInteger("numeroValoraciones"));
        assertEquals("Teclado mecánico", guardado.getString("nombre"));
        assertEquals(5, guardado.getInteger("stock"));
    }

    @Test
    @DisplayName("HU13-CP25: al leer el producto trae su media y su número de valoraciones")
    void alLeerElProductoTraeSuMediaYSuNumero() {
        String id = adapter.guardar(producto(null, 5)).getId();
        adapter.actualizarValoracion(id, 4.5, 2);

        Producto leido = adapter.buscarPorId(id).orElseThrow();

        assertEquals(4.5, leido.getValoracionMedia());
        assertEquals(2, leido.getNumeroValoraciones());
    }

    @Test
    @DisplayName("HU13-CP25: modificar el producto conserva su media y su número de valoraciones")
    void modificarElProductoConservaSuMediaYSuNumero() {
        String id = adapter.guardar(producto(null, 5)).getId();
        adapter.actualizarValoracion(id, 4.5, 2);

        adapter.guardar(producto(id, 9));

        Document guardado = documentoEnBruto();
        assertEquals(9, guardado.getInteger("stock"));
        assertEquals(4.5, ((Number) guardado.get("valoracionMedia")).doubleValue());
        assertEquals(2, guardado.getInteger("numeroValoraciones"));
    }

    @Test
    @DisplayName("HU10-CP15: descontar stock lo resta si hay suficiente y no lo deja negativo")
    void descontarStockLoRestaSiHaySuficiente() {
        String id = adapter.guardar(producto(null, 5)).getId();

        assertTrue(adapter.descontarStock(id, 3));
        assertFalse(adapter.descontarStock(id, 3));
        assertEquals(2, adapter.buscarPorId(id).orElseThrow().getStock());
    }

    @Test
    @DisplayName("HU10-CP15: dos descuentos a la vez del último stock no lo dejan negativo")
    void dosDescuentosALaVezNoDejanElStockNegativo() throws Exception {
        String id = adapter.guardar(producto(null, 1)).getId();

        List<Callable<Boolean>> compras = List.of(() -> adapter.descontarStock(id, 1),
                () -> adapter.descontarStock(id, 1));
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        List<Boolean> resultados = new ArrayList<>();
        for (Future<Boolean> compra : hilos.invokeAll(compras)) {
            resultados.add(compra.get());
        }
        hilos.shutdown();

        assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count());
        assertEquals(0, adapter.buscarPorId(id).orElseThrow().getStock());
    }

    @Test
    @DisplayName("HU10-CP15: reponer stock devuelve las unidades descontadas")
    void reponerStockDevuelveLasUnidades() {
        String id = adapter.guardar(producto(null, 5)).getId();
        adapter.descontarStock(id, 4);

        adapter.reponerStock(id, 4);

        assertEquals(5, adapter.buscarPorId(id).orElseThrow().getStock());
    }
}
