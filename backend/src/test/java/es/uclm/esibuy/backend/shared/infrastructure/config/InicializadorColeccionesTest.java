package es.uclm.esibuy.backend.shared.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.ServerAddress;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;

// Test de integración del inicializador de colecciones (T210, HU36; HU36 no tiene casos de prueba en
// Azure Boards: son tests técnicos). Sustituye al antiguo script probar-colecciones.js.
// Usa un MongoDB real en Docker (Testcontainers): Docker Desktop debe estar abierto.
// Los documentos se insertan tal cual (sin Spring Data) para probar las reglas de la base de datos,
// que deben parar los datos incorrectos aunque fallen las capas de arriba.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class InicializadorColeccionesTest {

    private static final int VALIDACION = 121;   // "Document failed validation"
    private static final int DUPLICADO = 11000;  // "E11000 duplicate key error"
    private static final Date AHORA = Date.from(Instant.parse("2026-10-08T12:00:00Z"));
        private static final String INCIDENCIAS = "incidencias";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InicializadorColecciones inicializador;

    @BeforeEach
    void vaciarColecciones() {
        // Se vacían sin borrarlas, para conservar la validación y los índices que creó el inicializador.
        ColeccionesEsiBuy.todas().forEach(c -> mongoTemplate.getCollection(c.nombre()).deleteMany(new Document()));
    }

    @Test
    @DisplayName("Inicialización: crea las 14 colecciones del modelo de BBDD")
    void creaLasCatorceColecciones() {
        // Las 10 del modelo inicial, metodos_pago (HU6), direcciones_envio (HU5), contadores (HU10) e incidencias (HU44)
        List<String> esperadas = List.of("usuarios", "sesiones", "factores_autenticacion", "tokens_seguridad",
                "productos", "pedidos", "carritos", "valoraciones", "categorias", "listas_deseos", "metodos_pago",
                "direcciones_envio", "contadores", "incidencias");

        assertTrue(mongoTemplate.getCollectionNames().containsAll(esperadas));
    }

    @Test
    @DisplayName("Inicialización: se puede ejecutar otra vez sin fallar ni duplicar nada")
    void sePuedeEjecutarOtraVez() {
        int indicesAntes = mongoTemplate.indexOps("sesiones").getIndexInfo().size();

        assertDoesNotThrow(() -> inicializador.run(null));
        assertEquals(indicesAntes, mongoTemplate.indexOps("sesiones").getIndexInfo().size());
    }

    @Test
    @DisplayName("Inicialización: aplica la validación a una colección que ya existía sin ella")
    void actualizaLaValidacionDeUnaColeccionExistente() {
        mongoTemplate.dropCollection("categorias");
        mongoTemplate.createCollection("categorias");

        inicializador.run(null);

        rechaza(VALIDACION, "categorias", new Document("nombre", ""));
    }

    @Test
    @DisplayName("Inicialización: sin permiso para cambiar la validación, avisa y la aplicación arranca igual")
    void sinPermisoParaCambiarLaValidacionNoFalla() {
        // Es lo que pasa en Atlas con el usuario de la aplicación (mínimo privilegio): AtlasError 8000.
        MongoTemplate sinPermiso = mock(MongoTemplate.class);
        when(sinPermiso.collectionExists(anyString())).thenReturn(true);
        when(sinPermiso.indexOps(anyString())).thenReturn(mock(IndexOperations.class));
        MongoCommandException noPermitido = new MongoCommandException(BsonDocument.parse(
                "{ok: 0, code: 8000, codeName: 'AtlasError', errmsg: 'user is not allowed to do action [collMod]'}"),
                new ServerAddress());
        when(sinPermiso.executeCommand(any(Document.class)))
                .thenThrow(new UncategorizedMongoDbException(noPermitido.getMessage(), noPermitido));

        InicializadorColecciones conUsuarioDeLaAplicacion =
                new InicializadorColecciones(sinPermiso, new MongoMappingContext());

        assertDoesNotThrow(() -> conUsuarioDeLaAplicacion.run(null));
    }

    @Test
    @DisplayName("Validación de usuarios: acepta un usuario como lo guarda la aplicación y los campos opcionales")
    void aceptaUsuariosValidos() {
        acepta("usuarios", new Document("email", "app@uclm.es").append("passwordHash", "$2a$10$hash")
                .append("rol", "CLIENTE").append("tipoCliente", "ESTANDAR").append("estado", "ACTIVO")
                .append("dni", "12345678Z").append("fechaNacimiento", AHORA)
                .append("failedLoginAttempts", 0).append("createdAt", AHORA).append("updatedAt", AHORA));
        acepta("usuarios", usuario("ventas@uclm.es", "VENDEDOR").append("nombreComercial", "Electrónica La Mancha")
                .append("categoriaPrincipalId", "cat-1"));
        acepta("usuarios", usuario("admin@uclm.es", "ADMINISTRADOR").append("sede", "Ciudad Real")
                .append("fechaIncorporacion", AHORA));
        acepta("usuarios", usuario("nulo@uclm.es", "CLIENTE").append("lockedUntil", null).append("lastLoginAt", null));
        acepta("usuarios", usuario("nuevo@uclm.es", "CLIENTE").append("campoNoPrevisto", "valor"));
        acepta("usuarios", usuario("bloqueado@uclm.es", "CLIENTE").append("estado", "BLOQUEADO"));
    }

    @Test
    @DisplayName("Validación de usuarios: rechaza campos obligatorios ausentes y valores fuera del modelo")
    void rechazaUsuariosInvalidos() {
        rechaza(VALIDACION, "usuarios", new Document("email", "x@uclm.es").append("rol", "CLIENTE"));
        rechaza(VALIDACION, "usuarios", usuario("y@uclm.es", "SUPERUSUARIO"));
        rechaza(VALIDACION, "usuarios", usuario("d@uclm.es", "CLIENTE").append("estado", "DESACTIVADO"));
        rechaza(VALIDACION, "usuarios", usuario("z@uclm.es", "CLIENTE").append("tipoCliente", "VIP"));
        rechaza(VALIDACION, "usuarios", usuario("w@uclm.es", "CLIENTE").append("failedLoginAttempts", -1));
        rechaza(VALIDACION, "usuarios", usuario("v@uclm.es", "VENDEDOR").append("nombreComercial", ""));
        rechaza(VALIDACION, "usuarios", usuario("a@uclm.es", "ADMINISTRADOR").append("fechaIncorporacion", "ayer"));
    }

    @Test
    @DisplayName("Índices de usuarios: DNI único por rol (opcional) y nombre comercial único")
    void usuariosConDniYNombreComercialUnicos() {
        // MVP del Sprint 1: una persona puede tener cuenta de cliente y de vendedor con el mismo DNI.
        acepta("usuarios", usuario("juan@uclm.es", "CLIENTE").append("dni", "12345678Z"));
        rechaza(DUPLICADO, "usuarios", usuario("otro@uclm.es", "CLIENTE").append("dni", "12345678Z"));
        acepta("usuarios", usuario("ventas@uclm.es", "VENDEDOR").append("dni", "12345678Z")
                .append("nombreComercial", "Electrónica La Mancha"));
        rechaza(DUPLICADO, "usuarios", usuario("tienda@uclm.es", "VENDEDOR")
                .append("nombreComercial", "Electrónica La Mancha"));
        // Sin DNI ni nombre comercial no hay duplicados: el registro actual solo pide email y contraseña.
        acepta("usuarios", usuario("ana@uclm.es", "CLIENTE"));
        acepta("usuarios", usuario("luis@uclm.es", "CLIENTE"));
    }

    @Test
    @DisplayName("Validación de sesiones, 2FA y tokens: estados, caducidad y tipos")
    void validaSesionesFactoresYTokens() {
        acepta("sesiones", sesion("u1", "ACTIVA"));
        rechaza(VALIDACION, "sesiones", sesion("u1", "ABIERTA"));
        rechaza(VALIDACION, "sesiones", new Document("usuarioId", "u1").append("tokenHash", "t").append("estado", "ACTIVA"));
        acepta("factores_autenticacion", new Document("usuarioId", "u1").append("totpEnabled", true));
        rechaza(VALIDACION, "factores_autenticacion", new Document("usuarioId", "u2").append("totpEnabled", "sí"));
        acepta("tokens_seguridad", token("LOGIN_2FA").append("tokenHash", "hash-del-codigo"));
        rechaza(VALIDACION, "tokens_seguridad", token("OTRO").append("tokenHash", "hash-del-codigo"));
        // T232: el hash del código o token es obligatorio (nunca se guarda en claro).
        rechaza(VALIDACION, "tokens_seguridad", token("LOGIN_2FA"));
        rechaza(VALIDACION, "tokens_seguridad", token("LOGIN_2FA").append("tokenHash", ""));
    }

    private static Document token(String purpose) {
        return new Document("usuarioId", "u1").append("purpose", purpose).append("expiresAt", AHORA);
    }

    @Test
    @DisplayName("Validación de productos: precio mayor que cero, stock no negativo, y nombre y categoría obligatorios")
    void validaProductos() {
        acepta("productos", producto(29.99, 10));
        rechaza(VALIDACION, "productos", producto(0, 1));
        rechaza(VALIDACION, "productos", producto(5, -1));
        rechaza(VALIDACION, "productos", producto(5, 1).append("precio", "29.99"));
        rechaza(VALIDACION, "productos", new Document("vendedorId", "v1").append("precio", 5).append("stock", 1));
        rechaza(VALIDACION, "productos", producto(5, 1).append("categoriaId", null));
        rechaza(VALIDACION, "productos", producto(5, 1).append("categoriaId", ""));
    }

    @Test
    @DisplayName("Validación de productos: descuentos como en el formulario de producto (activo y porcentaje hasta 100)")
    void validaDescuentosDeProductos() {
        // Formato del formulario de producto del frontend (HU31): {activo, porcentaje}.
        acepta("productos", producto(29.99, 10).append("descuento", descuento(true, 10))
                .append("descuentoPremium", descuento(false, null)));
        rechaza(VALIDACION, "productos", producto(5, 1).append("descuento", descuento(true, 150)));
        rechaza(VALIDACION, "productos", producto(5, 1).append("descuentoPremium", descuento(true, 0)));
        rechaza(VALIDACION, "productos", producto(5, 1).append("descuento", 10));
        rechaza(VALIDACION, "productos", producto(5, 1).append("descuento", new Document("porcentaje", 10)));
    }

    @Test
    @DisplayName("Validación de pedidos, carritos, valoraciones, categorías y listas de deseos")
    void validaElRestoDeColecciones() {
        acepta("pedidos", pedido("c1", 2));
        rechaza(VALIDACION, "pedidos", new Document("clienteId", "c1").append("estado", "PENDIENTE").append("lineas", List.of()));
        rechaza(VALIDACION, "pedidos", pedido("c1", 0));
        rechaza(VALIDACION, "carritos", new Document("clienteId", "c9")
                .append("items", List.of(new Document("productoId", "p1").append("cantidad", 0))));
        rechaza(VALIDACION, "valoraciones", valoracion("u9", "p9", 6));
        rechaza(VALIDACION, "valoraciones", valoracion("u9", "p9", 0));
        rechaza(VALIDACION, "categorias", new Document("nombre", ""));
        rechaza(VALIDACION, "listas_deseos", new Document("items", List.of()));
    }

    @Test
    @DisplayName("Índices únicos: rechazan duplicados en carritos, valoraciones, categorías, listas de deseos y 2FA")
    void rechazaDuplicadosEnLosIndicesUnicos() {
        acepta("carritos", new Document("clienteId", "c1"));
        rechaza(DUPLICADO, "carritos", new Document("clienteId", "c1"));
        acepta("valoraciones", valoracion("u1", "p1", 5));
        acepta("valoraciones", valoracion("u1", "p2", 4));
        rechaza(DUPLICADO, "valoraciones", valoracion("u1", "p1", 3));
        acepta("categorias", new Document("nombre", "Hogar"));
        rechaza(DUPLICADO, "categorias", new Document("nombre", "Hogar"));
        acepta("listas_deseos", new Document("usuarioId", "u1"));
        rechaza(DUPLICADO, "listas_deseos", new Document("usuarioId", "u1"));
        acepta("factores_autenticacion", new Document("usuarioId", "u1"));
        rechaza(DUPLICADO, "factores_autenticacion", new Document("usuarioId", "u1"));
    }

    @Test
    @DisplayName("HU10-CP19: el número de pedido tiene el formato ESB-AAAA-NNNNNN y no se repite")
    void elNumeroDePedidoTieneFormatoYNoSeRepite() {
        acepta("pedidos", pedido("c1", 1).append("numeroPedido", "ESB-2026-000001"));
        rechaza(DUPLICADO, "pedidos", pedido("c2", 1).append("numeroPedido", "ESB-2026-000001"));
        rechaza(VALIDACION, "pedidos", pedido("c1", 1).append("numeroPedido", "2026-1"));
    }

    @Test
    @DisplayName("HU10-CP13: el contador de pedidos de cada año es un número de al menos 1")
    void elContadorDePedidosEsUnNumeroDeAlMenosUno() {
        acepta("contadores", new Document("_id", "pedidos-2026").append("valor", 1L));
        rechaza(VALIDACION, "contadores", new Document("_id", "pedidos-2027").append("valor", 0L));
        rechaza(VALIDACION, "contadores", new Document("_id", "pedidos-2028"));
    }

    @Test
    @DisplayName("HU44-CP08: la colección incidencias admite incidencias válidas, varias por usuario y con los máximos")
    void aceptaIncidenciasValidas() {
        acepta(INCIDENCIAS, incidencia("u1", "Asunto", "Descripción", "PENDIENTE"));
        acepta(INCIDENCIAS, incidencia("u1", "Otro asunto", "Descripción", "ABIERTA").append("respuesta", "Revisada"));
        acepta(INCIDENCIAS, incidencia("u1", "a".repeat(100), "b".repeat(1000), "PENDIENTE"));
    }

    @Test
    @DisplayName("HU44-CP08: la colección incidencias rechaza un asunto o una descripción vacíos o demasiado largos")
    void rechazaIncidenciasConTextosInvalidos() {
        rechaza(VALIDACION, INCIDENCIAS, incidencia("u1", "", "Descripción", "PENDIENTE"));
        rechaza(VALIDACION, INCIDENCIAS, incidencia("u1", "a".repeat(101), "Descripción", "PENDIENTE"));
        rechaza(VALIDACION, INCIDENCIAS, incidencia("u1", "Asunto", "", "PENDIENTE"));
        rechaza(VALIDACION, INCIDENCIAS, incidencia("u1", "Asunto", "b".repeat(1001), "PENDIENTE"));
    }

    @Test
    @DisplayName("HU44-CP08: la colección incidencias rechaza un estado que no existe y una incidencia sin usuario")
    void rechazaIncidenciasConEstadoOUsuarioInvalidos() {
        rechaza(VALIDACION, INCIDENCIAS, incidencia("u1", "Asunto", "Descripción", "CERRADA"));
        rechaza(VALIDACION, INCIDENCIAS, new Document("asunto", "Asunto").append("descripcion", "Descripción")
                .append("estado", "PENDIENTE").append("fechaCreacion", AHORA));
    }

    @Test
    @DisplayName("Índices: los de las anotaciones de los @Document también se crean (email único en usuarios)")
    void creaLosIndicesDeLasAnotaciones() {
        acepta("usuarios", usuario("unico@uclm.es", "CLIENTE"));
        rechaza(DUPLICADO, "usuarios", usuario("unico@uclm.es", "CLIENTE"));
    }

    @Test
    @DisplayName("Índices TTL: sesiones y tokens caducan al llegar a expiresAt")
    void sesionesYTokensCaducan() {
        // Se comprueba el índice y no el borrado, que MongoDB hace cada 60 segundos.
        for (String coleccion : List.of("sesiones", "tokens_seguridad")) {
            Document indice = mongoTemplate.getCollection(coleccion).listIndexes().into(new ArrayList<>())
                    .stream().filter(i -> i.get("key", Document.class).containsKey("expiresAt"))
                    .findFirst().orElseThrow();
            assertEquals(0, ((Number) indice.get("expireAfterSeconds")).intValue(), coleccion);
        }
    }

    private void acepta(String coleccion, Document documento) {
        assertDoesNotThrow(() -> mongoTemplate.getCollection(coleccion).insertOne(documento),
                () -> coleccion + " debería aceptar " + documento.toJson());
    }

    private void rechaza(int codigo, String coleccion, Document documento) {
        MongoCollection<Document> destino = mongoTemplate.getCollection(coleccion);
        MongoWriteException error = assertThrows(MongoWriteException.class, () -> destino.insertOne(documento),
                () -> coleccion + " debería rechazar " + documento.toJson());
        assertEquals(codigo, error.getCode(), () -> coleccion + ": " + error.getMessage());
    }

    private static Document usuario(String email, String rol) {
        return new Document("email", email).append("passwordHash", "h").append("rol", rol);
    }

    private static Document sesion(String usuarioId, String estado) {
        return new Document("usuarioId", usuarioId).append("tokenHash", "t").append("estado", estado)
                .append("expiresAt", Date.from(Instant.now().plusSeconds(3600)));
    }

    private static Document producto(double precio, int stock) {
        return new Document("vendedorId", "v1").append("nombre", "Teclado").append("precio", precio)
                .append("stock", stock).append("categoriaId", "cat-1");
    }

    private static Document descuento(boolean activo, Integer porcentaje) {
        return new Document("activo", activo).append("porcentaje", porcentaje);
    }

    private static Document pedido(String clienteId, int cantidad) {
        return new Document("clienteId", clienteId).append("estado", "PENDIENTE").append("lineas", List.of(
                new Document("productoId", "p1").append("cantidad", cantidad).append("precioUnitario", 19.99)));
    }

    private static Document valoracion(String usuarioId, String productoId, int puntuacion) {
        return new Document("usuarioId", usuarioId).append("productoId", productoId).append("puntuacion", puntuacion);
    }

    private static Document incidencia(String usuarioId, String asunto, String descripcion, String estado) {
        return new Document("usuarioId", usuarioId).append("asunto", asunto).append("descripcion", descripcion)
                .append("estado", estado).append("fechaCreacion", AHORA);
    }
}
