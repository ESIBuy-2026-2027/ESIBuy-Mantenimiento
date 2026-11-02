package es.uclm.esibuy.backend.shared.infrastructure.config;

import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.array;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.bool;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.date;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.int32;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.number;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.object;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.untyped;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;

/**
 * Colecciones de la base de datos de ESIBuy, con su validación y sus índices (T210, HU36).
 *
 * <p>Modelo: «Planteamiento inicial BBDD» (resumen general: 10 colecciones) y MVP del Sprint 1.
 * Las clases Java de cada colección (documento, repositorio, mapper y adaptador) se crean con su HU.
 *
 * <p>Validación permisiva: exige los campos imprescindibles y comprueba el tipo y las reglas del
 * modelo de los campos que existan; admite campos nuevos para no bloquear al equipo mientras el
 * modelo es preliminar.
 *
 * <p>Índices: los de los campos que ya tiene una clase {@code @Document} se declaran en esa clase
 * con {@code @Indexed} y {@code @CompoundIndex} (p. ej. el email de {@code usuarios}); aquí están los
 * demás. Cuando un campo llegue a su documento, su índice pasa a las anotaciones y se quita de aquí.
 */
final class ColeccionesEsiBuy {

    // Campos que se repiten en varias colecciones.
    private static final String ESTADO = "estado";
    private static final String NOMBRE = "nombre";
    private static final String EXPIRES_AT = "expiresAt";
    private static final String USUARIO_ID = "usuarioId";
    private static final String PURPOSE = "purpose";
    private static final String VENDEDOR_ID = "vendedorId";
    private static final String CLIENTE_ID = "clienteId";
    private static final String CANTIDAD = "cantidad";
    private static final String PRODUCTO_ID = "productoId";
    private static final String TOKEN_HASH = "tokenHash";
    private static final String CATEGORIA_ID = "categoriaId";

    /** Una colección: su nombre, su validación y los índices que no vienen de anotaciones. */
    record Coleccion(String nombre, MongoJsonSchema esquema, List<Index> indices) {
    }

    private ColeccionesEsiBuy() {
    }

    static List<Coleccion> todas() {
        return List.of(usuarios(), sesiones(), factoresAutenticacion(), tokensSeguridad(), productos(),
                pedidos(), carritos(), valoraciones(), categorias(), listasDeseos(), metodosPago(), direccionesEnvio(),
                contadores());
    }

    // 1. usuarios: identidad, rol, estado y perfil, con los campos del modelo en la raíz del documento,
    // también los propios de vendedores y administradores. El índice único de email lo declara UsuarioDocument
    // (@Indexed); los de dni y nombreComercial van aquí: el del dni es compuesto con el rol, y UsuarioDocument
    // todavía no tiene nombreComercial.
    private static Coleccion usuarios() {
        return new Coleccion("usuarios", esquema(List.of("email", "passwordHash", "rol"),
                textoNoVacio("email"),
                textoNoVacio("passwordHash"),
                string("rol").possibleValues("ADMINISTRADOR", "VENDEDOR", "CLIENTE"),
                string("tipoCliente").possibleValues("PREMIUM", "ESTANDAR"),
                // Toda cuenta nueva nace PENDIENTE hasta que un administrador la verifica (decisión del equipo del 09/10/2026).
                string(ESTADO).possibleValues("ACTIVO", "BLOQUEADO", "PENDIENTE"),
                textoNoVacio(NOMBRE),
                textoNoVacio("apellidos"),
                textoNoVacio("dni"),
                textoNoVacio("telefono"),
                string("imagenPerfil"),
                bool("emailVerificado"),
                date("fechaNacimiento"),
                bool("privacyPolicyAccepted"),
                date("privacyPolicyAcceptedAt"),
                string("privacyPolicyVersion"),
                enteroNoNegativo("failedLoginAttempts"),
                fechaOpcional("lockedUntil"), // null = sin bloqueo, como en el ejemplo del modelo
                fechaOpcional("lastLoginAt"),
                // Vendedor y administrador (modelo: «campos específicos»).
                textoNoVacio("nombreComercial"),
                textoNoVacio("categoriaPrincipalId"),
                textoNoVacio("sede"),
                date("fechaIncorporacion")),
                // MVP del Sprint 1: el DNI es único, pero una persona puede tener cuenta de cliente y de
                // vendedor con el mismo DNI (y distinto correo): es único por rol. El filtro parcial deja
                // fuera a los usuarios sin DNI, porque el registro actual no lo pide.
                List.of(indice("rol").on("dni", Sort.Direction.ASC).unique().named("rol_dni")
                                .partial(PartialIndexFilter.of(Criteria.where("dni").exists(true))),
                        // Modelo: nombreComercial → único, si aplica (sparse: solo cuentan los vendedores).
                        indice("nombreComercial").unique().sparse()));
    }

    // 2. sesiones: revocables y caducables. TTL: MongoDB borra la sesión al llegar a expiresAt.
    private static Coleccion sesiones() {
        return new Coleccion("sesiones", esquema(List.of(USUARIO_ID, TOKEN_HASH, ESTADO, EXPIRES_AT),
                textoNoVacio(USUARIO_ID),
                textoNoVacio(TOKEN_HASH),
                string(ESTADO).possibleValues("ACTIVA", "REVOCADA", "EXPIRADA"),
                date(EXPIRES_AT),
                fechaOpcional("revokedAt"),
                date("lastUsedAt"),
                string("ipHash"),
                string("userAgent"),
                string("deviceIdentifier")),
                List.of(caducidad(EXPIRES_AT), indice(USUARIO_ID)));
    }

    // 3. factores_autenticacion: configuración 2FA/3FA, una por usuario.
    // El secreto TOTP se guarda cifrado; la clave no va ni en MongoDB ni en el repositorio.
    private static Coleccion factoresAutenticacion() {
        return new Coleccion("factores_autenticacion", esquema(List.of(USUARIO_ID),
                textoNoVacio(USUARIO_ID),
                bool("totpEnabled"),
                textoNoVacio("totpSecretEncrypted"),
                bool("emailFactorEnabled"),
                date("verifiedAt"),
                date("lastUsedAt")),
                List.of(indice(USUARIO_ID).unique()));
    }

    // 4. tokens_seguridad: códigos temporales (login, verificación de email) y recuperación de contraseña,
    // distinguidos por purpose (decisión del equipo: una colección en vez de codigos_verificacion y
    // recuperaciones_password por separado).
    // Solo se guarda el hash del código o token (tokenHash, obligatorio; T232), nunca en claro.
    // PENDIENTE: índice de tokenHash si la recuperación de contraseña busca por él.
    private static Coleccion tokensSeguridad() {
        return new Coleccion("tokens_seguridad", esquema(List.of(USUARIO_ID, PURPOSE, TOKEN_HASH, EXPIRES_AT),
                textoNoVacio(USUARIO_ID),
                textoNoVacio(TOKEN_HASH),
                string(PURPOSE).possibleValues("LOGIN_2FA", "EMAIL_VERIFICATION", "PASSWORD_RECOVERY"),
                string("status"),
                enteroNoNegativo("attempts"),
                int32("maxAttempts").gte(1),
                date(EXPIRES_AT),
                fechaOpcional("usedAt")),
                List.of(caducidad(EXPIRES_AT), indice(USUARIO_ID).on(PURPOSE, Sort.Direction.ASC)));
    }

    // 5. productos: catálogo y stock. Reglas del modelo: precio > 0, stock >= 0, nombre y vendedorId
    // obligatorios. Los descuentos siguen el formulario de producto del frontend (HU31): descuento y
    // descuentoPremium (MVP del Sprint 1) son {activo, porcentaje}, no un número como en el modelo.
    // Una sola categoría por producto (decisión del equipo, T201): categoriaId obligatorio, no una lista.
    private static Coleccion productos() {
        return new Coleccion("productos", esquema(List.of(VENDEDOR_ID, NOMBRE, "precio", "stock", CATEGORIA_ID),
                textoNoVacio(VENDEDOR_ID),
                textoNoVacio(NOMBRE),
                string("descripcion"),
                number("precio").gt(0),
                enteroNoNegativo("stock"),
                descuento("descuento"),
                descuento("descuentoPremium"),
                importe("precioFinal"),
                textoNoVacio(CATEGORIA_ID),
                string("imagen"),
                string(ESTADO),
                bool("visible"),
                bool("disponible"),
                number("valoracionMedia").gte(0).lte(5),
                enteroNoNegativo("numeroValoraciones")),
                List.of(indice(VENDEDOR_ID), indice(CATEGORIA_ID), indice(NOMBRE), indice(ESTADO),
                        indice("visible"),
                        // Un vendedor no repite nombre de producto (HU31). La collation de nivel 2 ignora
                        // mayúsculas y minúsculas pero no tildes. Cierra la condición de carrera de dos
                        // altas simultáneas que pasaron a la vez existePorVendedorYNombre.
                        indice(VENDEDOR_ID).on(NOMBRE, Sort.Direction.ASC).unique()
                                .named("vendedor_nombre")
                                .collation(Collation.of("es").strength(Collation.ComparisonLevel.secondary()))));
    }

    // 6. pedidos: compras y precios históricos. Los estados posibles están pendientes de acordar: solo se exige texto.
    // HU10: un pedido por proveedor (vendedorId y nombreComercial en la raíz) y su número ESB-AAAA-NNNNNN, único.
    // El índice del número es sparse: los pedidos sin número (anteriores a la HU10) no cuentan.
    private static Coleccion pedidos() {
        return new Coleccion("pedidos", esquema(List.of(CLIENTE_ID, ESTADO, "lineas"),
                textoNoVacio(CLIENTE_ID),
                textoNoVacio(ESTADO),
                array("lineas").minItems(1).items(JsonSchemaObject.object()
                        .required(PRODUCTO_ID, CANTIDAD, "precioUnitario")
                        .properties(
                                textoNoVacio(PRODUCTO_ID),
                                string("nombreProducto"),
                                string(VENDEDOR_ID),
                                int32(CANTIDAD).gte(1),
                                importe("precioUnitario"),
                                importe("descuento"),
                                importe("subtotal"))),
                importe("subtotal"),
                importe("gastosEnvio"),
                importe("descuentoTotal"),
                importe("total"),
                object("direccionEnvio"),
                string("paymentMethodId"),
                string("numeroPedido").matching("^ESB-[0-9]{4}-[0-9]{6}$"),
                textoNoVacio(VENDEDOR_ID),
                string("nombreComercial")),
                List.of(indice(CLIENTE_ID), indice(ESTADO), indice("createdAt"), indice("lineas.productoId"),
                        indice("lineas.vendedorId"), indice("numeroPedido").unique().sparse()));
    }

    // 7. carritos: un carrito activo por cliente.
    private static Coleccion carritos() {
        return new Coleccion("carritos", esquema(List.of(CLIENTE_ID),
                textoNoVacio(CLIENTE_ID),
                array("items").items(JsonSchemaObject.object()
                        .required(PRODUCTO_ID, CANTIDAD)
                        .properties(textoNoVacio(PRODUCTO_ID), int32(CANTIDAD).gte(1), date("addedAt"),
                                date("updatedAt")))),
                List.of(indice(CLIENTE_ID).unique()));
    }

    // 8. valoraciones: una por usuario y producto. Regla del modelo: 1 <= puntuacion <= 5.
    private static Coleccion valoraciones() {
        return new Coleccion("valoraciones", esquema(List.of(USUARIO_ID, PRODUCTO_ID, "puntuacion"),
                textoNoVacio(USUARIO_ID),
                textoNoVacio(PRODUCTO_ID),
                int32("puntuacion").gte(1).lte(5),
                string("comentario"),
                string(ESTADO)),
                List.of(indice(USUARIO_ID).on(PRODUCTO_ID, Sort.Direction.ASC).unique()));
    }

    // 9. categorias: clasificación de productos. Mismas reglas que la entidad Categoria (HU40, T321): nombre
    // de hasta 50 caracteres, descripción de hasta 200 y estado ACTIVA o INACTIVA. Las categorías guardadas
    // antes no tienen estado: por eso no es obligatorio aquí, y el mapper las lee como ACTIVAS.
    private static Coleccion categorias() {
        return new Coleccion("categorias", esquema(List.of(NOMBRE),
                string(NOMBRE).minLength(1).maxLength(50),
                string("descripcion").maxLength(200),
                string(ESTADO).possibleValues("ACTIVA", "INACTIVA")),
                List.of(indice(NOMBRE).unique(),
                        // No se repite el nombre sin distinguir mayúsculas ni tildes (HU40, T323): collation de
                        // nivel 1. Lleva nombre propio para no chocar con el índice anterior, que se deja como
                        // estaba, al arrancar contra una base que ya lo tiene.
                        indice(NOMBRE).unique().named("nombre_sin_mayusculas")
                                .collation(Collation.of("es").strength(Collation.ComparisonLevel.primary()))));
    }

    // 10. listas_deseos: una lista principal por cliente (decisión del equipo).
    private static Coleccion listasDeseos() {
        return new Coleccion("listas_deseos", esquema(List.of(USUARIO_ID),
                textoNoVacio(USUARIO_ID),
                string(NOMBRE),
                array("items").items(JsonSchemaObject.object()
                        .required(PRODUCTO_ID)
                        .properties(textoNoVacio(PRODUCTO_ID), date("addedAt")))),
                List.of(indice(USUARIO_ID).unique()));
    }

    // 11. metodos_pago: métodos de pago de cada cliente (HU6), fuera del modelo inicial. Solo la máscara y
    // el bloque cifrado: los datos de pago nunca en claro. El índice de clienteId lo declara MetodoPagoDocument.
    private static Coleccion metodosPago() {
        return new Coleccion("metodos_pago",
                esquema(List.of(CLIENTE_ID, "tipo", "mascara", "datosCifrados", "predeterminado"),
                        textoNoVacio(CLIENTE_ID),
                        string("tipo").possibleValues("TARJETA", "PAYPAL", "BIZUM"),
                        textoNoVacio("mascara"),
                        textoNoVacio("datosCifrados"),
                        bool("predeterminado")),
                List.of());
    }

    // 12. direcciones_envio: direcciones de envío de cada cliente (HU5), fuera del modelo inicial. Mismas reglas
    // que DatosDireccion: el pedido (HU10) copia estos campos en su direccionEnvio. Los detalles son opcionales.
    // El índice de clienteId lo declara DireccionDocument.
    private static Coleccion direccionesEnvio() {
        return new Coleccion("direcciones_envio",
                esquema(List.of(CLIENTE_ID, "destinatario", "calle", "codigoPostal", "ciudad", "provincia",
                                "telefono", "principal"),
                        textoNoVacio(CLIENTE_ID),
                        string("destinatario").minLength(1).maxLength(100),
                        string("calle").minLength(1).maxLength(150),
                        string("detalles").maxLength(100),
                        string("codigoPostal").matching("^[0-9]{5}$"),
                        string("ciudad").minLength(1).maxLength(100),
                        string("provincia").minLength(1).maxLength(100),
                        string("telefono").matching("^[0-9]{9}$"),
                        bool("principal")),
                List.of());
    }

    // 13. contadores: correlativos atómicos, fuera del modelo inicial. HU10: uno por año para los números de pedido
    // (_id = "pedidos-2026"), que vuelven a empezar cada año (decisión del equipo).
    private static Coleccion contadores() {
        return new Coleccion("contadores", esquema(List.of("valor"), number("valor").gte(1)), List.of());
    }

    // Todas las colecciones llevan createdAt y updatedAt («Convenciones comunes» del modelo).
    private static MongoJsonSchema esquema(List<String> obligatorios, JsonSchemaProperty... propiedades) {
        List<JsonSchemaProperty> todas = new ArrayList<>(Arrays.asList(propiedades));
        todas.add(date("createdAt"));
        todas.add(date("updatedAt"));
        return MongoJsonSchema.builder()
                .required(obligatorios.toArray(String[]::new))
                .properties(todas.toArray(JsonSchemaProperty[]::new))
                .build();
    }

    private static JsonSchemaProperty textoNoVacio(String nombre) {
        return string(nombre).minLength(1);
    }

    // Java guarda los int como enteros de 32 bits.
    private static JsonSchemaProperty enteroNoNegativo(String nombre) {
        return int32(nombre).gte(0);
    }

    // Importes como número (modelo: "precio": 29.99). Si en Java se usa BigDecimal, guardarlo como
    // Decimal128 (@Field(targetType = FieldType.DECIMAL128)): por defecto Spring Data lo guardaría como texto.
    private static JsonSchemaProperty importe(String nombre) {
        return number(nombre).gte(0);
    }

    // Descuento de un producto (HU31): si está activo, porcentaje mayor que 0 y hasta 100; si no, null.
    private static JsonSchemaProperty descuento(String nombre) {
        return object(nombre).required("activo").properties(
                bool("activo"),
                untyped("porcentaje").anyOf(JsonSchemaObject.number().gt(0).lte(100), JsonSchemaObject.nil()));
    }

    // Fecha que puede ser null, como lockedUntil en el ejemplo del modelo.
    private static JsonSchemaProperty fechaOpcional(String nombre) {
        return untyped(nombre).anyOf(JsonSchemaObject.date(), JsonSchemaObject.nil());
    }

    private static Index indice(String campo) {
        return new Index().on(campo, Sort.Direction.ASC);
    }

    // Índice TTL: MongoDB borra el documento cuando llega la fecha del campo.
    private static Index caducidad(String campo) {
        return indice(campo).expire(Duration.ZERO);
    }
}
