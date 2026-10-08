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
 * <p>Índices: los de las colecciones que ya tienen clase {@code @Document} se declaran en esa clase
 * con {@code @Indexed} y {@code @CompoundIndex} (p. ej. {@code usuarios}); aquí solo están los de
 * las colecciones que todavía no la tienen. Cuando una colección tenga su documento, sus índices se
 * pasan a las anotaciones y se quitan de aquí.
 */
final class ColeccionesEsiBuy {

    /** Una colección: su nombre, su validación y los índices que no vienen de anotaciones. */
    record Coleccion(String nombre, MongoJsonSchema esquema, List<Index> indices) {
    }

    private ColeccionesEsiBuy() {
    }

    static List<Coleccion> todas() {
        return List.of(usuarios(), sesiones(), factoresAutenticacion(), tokensSeguridad(), productos(),
                pedidos(), carritos(), valoraciones(), categorias(), listasDeseos());
    }

    // 1. usuarios: identidad, rol, estado y perfil. Los datos personales comunes a varios roles van en la
    // raíz y los propios de cada rol, en su subdocumento (opción B). Índices: anotaciones de UsuarioDocument.
    private static Coleccion usuarios() {
        return new Coleccion("usuarios", esquema(List.of("email", "passwordHash", "rol"),
                textoNoVacio("email"),
                textoNoVacio("passwordHash"),
                string("rol").possibleValues("ADMINISTRADOR", "VENDEDOR", "CLIENTE"),
                // PENDIENTE solo para vendedores a la espera de verificación (decisión del equipo del 07/10/2026).
                string("estado").possibleValues("ACTIVO", "DESACTIVADO", "PENDIENTE"),
                textoNoVacio("nombre"),
                textoNoVacio("apellidos"),
                textoNoVacio("dni"),
                textoNoVacio("telefono"),
                string("imagenPerfil"),
                bool("emailVerificado"),
                bool("privacyPolicyAccepted"),
                date("privacyPolicyAcceptedAt"),
                string("privacyPolicyVersion"),
                enteroNoNegativo("failedLoginAttempts"),
                fechaOpcional("lockedUntil"), // null = sin bloqueo, como en el ejemplo del modelo
                fechaOpcional("lastLoginAt"),
                object("cliente").required("tipoCliente").properties(
                        string("tipoCliente").possibleValues("PREMIUM", "ESTANDAR"),
                        date("fechaNacimiento")),
                object("vendedor").required("nombreComercial", "categoriaPrincipalId").properties(
                        textoNoVacio("nombreComercial"),
                        textoNoVacio("categoriaPrincipalId")),
                object("administrador").required("fechaIncorporacion").properties(
                        textoNoVacio("sede"),
                        date("fechaIncorporacion"))),
                List.of());
    }

    // 2. sesiones: revocables y caducables. TTL: MongoDB borra la sesión al llegar a expiresAt.
    private static Coleccion sesiones() {
        return new Coleccion("sesiones", esquema(List.of("usuarioId", "tokenHash", "estado", "expiresAt"),
                textoNoVacio("usuarioId"),
                textoNoVacio("tokenHash"),
                string("estado").possibleValues("ACTIVA", "REVOCADA", "EXPIRADA"),
                date("expiresAt"),
                fechaOpcional("revokedAt"),
                date("lastUsedAt"),
                string("ipHash"),
                string("userAgent"),
                string("deviceIdentifier")),
                List.of(caducidad("expiresAt"), indice("usuarioId")));
    }

    // 3. factores_autenticacion: configuración 2FA/3FA, una por usuario.
    // El secreto TOTP se guarda cifrado; la clave no va ni en MongoDB ni en el repositorio.
    private static Coleccion factoresAutenticacion() {
        return new Coleccion("factores_autenticacion", esquema(List.of("usuarioId"),
                textoNoVacio("usuarioId"),
                bool("totpEnabled"),
                textoNoVacio("totpSecretEncrypted"),
                bool("emailFactorEnabled"),
                date("verifiedAt"),
                date("lastUsedAt")),
                List.of(indice("usuarioId").unique()));
    }

    // 4. tokens_seguridad: códigos temporales (login, verificación de email) y recuperación de contraseña,
    // distinguidos por "purpose" (decisión del equipo: una colección en vez de codigos_verificacion y
    // recuperaciones_password por separado).
    // PENDIENTE: índice del hash del código/token cuando se fije el nombre del campo.
    private static Coleccion tokensSeguridad() {
        return new Coleccion("tokens_seguridad", esquema(List.of("usuarioId", "purpose", "expiresAt"),
                textoNoVacio("usuarioId"),
                string("purpose").possibleValues("LOGIN_2FA", "EMAIL_VERIFICATION", "PASSWORD_RECOVERY"),
                string("status"),
                enteroNoNegativo("attempts"),
                int32("maxAttempts").gte(1),
                date("expiresAt"),
                fechaOpcional("usedAt")),
                List.of(caducidad("expiresAt"), indice("usuarioId").on("purpose", Sort.Direction.ASC)));
    }

    // 5. productos: catálogo y stock. Reglas del modelo: precio > 0, stock >= 0, nombre y vendedorId
    // obligatorios. descuentoPremium sale del MVP del Sprint 1.
    private static Coleccion productos() {
        return new Coleccion("productos", esquema(List.of("vendedorId", "nombre", "precio", "stock"),
                textoNoVacio("vendedorId"),
                textoNoVacio("nombre"),
                string("descripcion"),
                number("precio").gt(0),
                enteroNoNegativo("stock"),
                importe("descuento"),
                importe("descuentoPremium"),
                importe("precioFinal"),
                array("categoriaIds").items(JsonSchemaObject.string()),
                string("imagen"),
                string("estado"),
                bool("visible"),
                bool("disponible"),
                number("valoracionMedia").gte(0).lte(5),
                enteroNoNegativo("numeroValoraciones")),
                List.of(indice("vendedorId"), indice("categoriaIds"), indice("nombre"), indice("estado"),
                        indice("visible")));
    }

    // 6. pedidos: compras y precios históricos. Los estados posibles están pendientes de acordar: solo se exige texto.
    private static Coleccion pedidos() {
        return new Coleccion("pedidos", esquema(List.of("clienteId", "estado", "lineas"),
                textoNoVacio("clienteId"),
                textoNoVacio("estado"),
                array("lineas").minItems(1).items(JsonSchemaObject.object()
                        .required("productoId", "cantidad", "precioUnitario")
                        .properties(
                                textoNoVacio("productoId"),
                                string("nombreProducto"),
                                string("vendedorId"),
                                int32("cantidad").gte(1),
                                importe("precioUnitario"),
                                importe("descuento"),
                                importe("subtotal"))),
                importe("subtotal"),
                importe("gastosEnvio"),
                importe("descuentoTotal"),
                importe("total"),
                object("direccionEnvio"),
                string("paymentMethodId")),
                List.of(indice("clienteId"), indice("estado"), indice("createdAt"), indice("lineas.productoId"),
                        indice("lineas.vendedorId")));
    }

    // 7. carritos: un carrito activo por cliente.
    private static Coleccion carritos() {
        return new Coleccion("carritos", esquema(List.of("clienteId"),
                textoNoVacio("clienteId"),
                array("items").items(JsonSchemaObject.object()
                        .required("productoId", "cantidad")
                        .properties(textoNoVacio("productoId"), int32("cantidad").gte(1), date("addedAt"),
                                date("updatedAt")))),
                List.of(indice("clienteId").unique()));
    }

    // 8. valoraciones: una por usuario y producto. Regla del modelo: 1 <= puntuacion <= 5.
    private static Coleccion valoraciones() {
        return new Coleccion("valoraciones", esquema(List.of("usuarioId", "productoId", "puntuacion"),
                textoNoVacio("usuarioId"),
                textoNoVacio("productoId"),
                int32("puntuacion").gte(1).lte(5),
                string("comentario"),
                string("estado")),
                List.of(indice("usuarioId").on("productoId", Sort.Direction.ASC).unique()));
    }

    // 9. categorias: clasificación de productos.
    private static Coleccion categorias() {
        return new Coleccion("categorias", esquema(List.of("nombre"),
                textoNoVacio("nombre"),
                string("descripcion"),
                string("estado")),
                List.of(indice("nombre").unique()));
    }

    // 10. listas_deseos: una lista principal por cliente (decisión del equipo).
    private static Coleccion listasDeseos() {
        return new Coleccion("listas_deseos", esquema(List.of("usuarioId"),
                textoNoVacio("usuarioId"),
                string("nombre"),
                array("items").items(JsonSchemaObject.object()
                        .required("productoId")
                        .properties(textoNoVacio("productoId"), date("addedAt")))),
                List.of(indice("usuarioId").unique()));
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
