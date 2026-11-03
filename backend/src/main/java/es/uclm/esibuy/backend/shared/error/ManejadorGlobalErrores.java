package es.uclm.esibuy.backend.shared.error;

import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaConProductosException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaConVendedoresException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaInvalidaException;
import es.uclm.esibuy.backend.carrito.domain.exception.CantidadInvalidaException;
import es.uclm.esibuy.backend.carrito.domain.exception.LineaCarritoNoEncontradaException;
import es.uclm.esibuy.backend.carrito.domain.exception.ProductoNoComprableException;
import es.uclm.esibuy.backend.carrito.domain.exception.StockInsuficienteException;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.LimiteDireccionesException;
import es.uclm.esibuy.backend.direcciones.domain.exception.NuevaPrincipalObligatoriaException;
import es.uclm.esibuy.backend.metodospago.domain.exception.LimiteMetodosPagoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.NuevoPredeterminadoObligatorioException;
import es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException;
import es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficientePedidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ImagenNoSeguraException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AuthenticatorYaDadoDeAltaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutobloqueoNoPermitidoException;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaNoEncontradaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.BloqueoPorIntentosException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYNombreComercialYaRegistradosException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionDuplicadaException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutoeliminacionNoPermitidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaPendienteException;
import org.springframework.security.access.AccessDeniedException;

/**
 * Traduce las excepciones que salen de los controladores a respuestas HTTP con un mensaje
 * genérico, para que los controladores no necesiten {@code try/catch}.
 *
 * <p>PENDIENTE DE DECIDIR (equipo): texto exacto de los mensajes genéricos.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    // El 409 del registro lleva un código para que el frontend marque el campo repetido; el mensaje sigue
    // siendo genérico. Decisión acordada con el frontend (T229), con la convención de ACCOUNT_BLOCKED.
    private static final String REGISTRO_NO_COMPLETADO = "No se ha podido completar el registro";

    @ExceptionHandler(CorreoYaRegistradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError correoYaRegistrado() {
        return new RespuestaError(REGISTRO_NO_COMPLETADO, "EMAIL_ALREADY_REGISTERED");
    }

    @ExceptionHandler(DniYaRegistradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError dniYaRegistrado() {
        return new RespuestaError(REGISTRO_NO_COMPLETADO, "DNI_ALREADY_REGISTERED");
    }

    // Registro del vendedor (T241): los códigos que espera el formulario del frontend.
    @ExceptionHandler(NombreComercialYaRegistradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError nombreComercialYaRegistrado() {
        return new RespuestaError(REGISTRO_NO_COMPLETADO, "COMMERCIAL_NAME_ALREADY_REGISTERED");
    }

    @ExceptionHandler(CorreoYNombreComercialYaRegistradosException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError correoYNombreComercialYaRegistrados() {
        return new RespuestaError(REGISTRO_NO_COMPLETADO, "EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED");
    }

    // Los 400 del registro llevan un code por motivo (fallos de la demo del Sprint 1) para que el frontend
    // marque el campo. El mensaje sigue siendo genérico; el motivo lo pone el dominio y el texto del code, aquí.
    private static final String DATOS_DE_REGISTRO_INVALIDOS = "Datos de registro no válidos";

    @ExceptionHandler(ContrasenaInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError contrasenaInvalida(ContrasenaInvalidaException e) {
        return new RespuestaError(DATOS_DE_REGISTRO_INVALIDOS, codigo(e.getMotivo()));
    }

    @ExceptionHandler(UsuarioInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError usuarioInvalido(UsuarioInvalidoException e) {
        return new RespuestaError(DATOS_DE_REGISTRO_INVALIDOS, codigo(e.getMotivo()));
    }

    @ExceptionHandler(EmailInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError emailInvalido() {
        return new RespuestaError(DATOS_DE_REGISTRO_INVALIDOS, "INVALID_EMAIL");
    }

    // Un cuerpo que no pasa @Valid (campo vacío o demasiado largo) o que no se puede leer (JSON mal formado o un
    // valor de un tipo incompatible) responde en todos los endpoints con el formato y el código del resto de 400 de
    // validación, y no con el cuerpo por defecto de Spring. El mensaje es genérico: el detalle de la validación o de
    // Jackson no sale al cliente (decisión del equipo en la daily del 03/11/2026).
    private static final String DATOS_ENVIADOS_INVALIDOS = "Los datos enviados no son válidos";

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError cuerpoDeLaPeticionInvalido() {
        return new RespuestaError(DATOS_ENVIADOS_INVALIDOS, CODIGO_VALIDACION);
    }

    // Switch exhaustivo: si el dominio añade un motivo, esto deja de compilar hasta darle su code.
    private static String codigo(MotivoRechazo motivo) {
        return switch (motivo) {
            case CONTRASENA_CORTA -> "PASSWORD_TOO_SHORT";
            case CONTRASENA_LARGA -> "PASSWORD_TOO_LONG";
            case CONTRASENA_DEBIL -> "PASSWORD_WEAK";
            case CONTRASENA_COMUN -> "PASSWORD_TOO_COMMON";
            case CONTRASENA_CON_DATOS_PERSONALES -> "PASSWORD_CONTAINS_PERSONAL_DATA";
            case CONTRASENAS_DISTINTAS -> "PASSWORDS_DO_NOT_MATCH";
            case DNI_INVALIDO -> "INVALID_DNI";
            case TELEFONO_INVALIDO -> "INVALID_PHONE";
            case MENOR_DE_EDAD -> "UNDERAGE";
            case PRIVACIDAD_NO_ACEPTADA -> "PRIVACY_NOT_ACCEPTED";
            case DATO_INVALIDO -> CODIGO_VALIDACION;
        };
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RespuestaError credencialesInvalidas() {
        return new RespuestaError("Credenciales Incorrectas");
    }

    // 400 + code al eliminar la cuenta propia con una contraseña incorrecta (HU25). No es 401: la sesión sigue
    // siendo válida y el frontend no debe tratarlo como una sesión caducada.
    @ExceptionHandler(ContrasenaActualIncorrectaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError contrasenaActualIncorrecta() {
        return new RespuestaError("La contraseña actual no es correcta", "WRONG_PASSWORD");
    }

    @ExceptionHandler(BloqueoPorIntentosException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public RespuestaError bloqueoPorIntentos() {
        return new RespuestaError("Demasiados intentos. Inténtalo más tarde");
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError cuentaBloqueada() {
        return new RespuestaError("Esta cuenta está bloqueada", "ACCOUNT_BLOCKED");
    }

    @ExceptionHandler(EnvioEmailException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public RespuestaError envioEmailFallido() {
        return new RespuestaError("No se ha podido completar el inicio de sesión. Inténtalo más tarde");
    }

    @ExceptionHandler(CodigoInvalidoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RespuestaError codigoInvalido() {
        return new RespuestaError("Código incorrecto");
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError usuarioNoEncontrado() {
        return new RespuestaError("El usuario no existe");
    }

    // Sin @ResponseStatus Spring respondería 200 y el frontend lo tomaría por un login correcto.
    // 403 + code, como la cuenta bloqueada: el frontend elige su mensaje por el code (T236).
    @ExceptionHandler(CuentaPendienteException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError cuentaPendiente() {
        return new RespuestaError("La cuenta está pendiente de verificación", "ACCOUNT_PENDING");
    }

    // Seguridad en la capa de negocio (R8): sin permiso, siempre el mismo 403 genérico. También recoge
    // AuthorizationDeniedException, que es una AccessDeniedException.
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError accesoDenegado() {
        return new RespuestaError(ManejadorAccesoDenegado.MENSAJE, ManejadorAccesoDenegado.CODIGO);
    }

    // Quien ya tiene Authenticator no puede volver a darse de alta: impediría saltarse el tercer factor (T20)
    @ExceptionHandler(AuthenticatorYaDadoDeAltaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError authenticatorYaDadoDeAlta() {
        return new RespuestaError("El usuario ya tiene Authenticator", "AUTHENTICATOR_ALREADY_ENROLLED");
    }

    // 409 + code al bloquear (HU23): códigos acordados con el frontend (frontend/docs/hu23-desactivar-usuario.md).
    @ExceptionHandler(AutobloqueoNoPermitidoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError autobloqueoNoPermitido() {
        return new RespuestaError("No puedes bloquear tu propia cuenta", "CANNOT_BLOCK_SELF");
    }

    // 409 + code al eliminar (HU37): código esperado por el frontend (CANNOT_DELETE_SELF).
    @ExceptionHandler(AutoeliminacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError autoeliminacionNoPermitida() {
        return new RespuestaError("No puedes eliminar tu propia cuenta", "CANNOT_DELETE_SELF");
    }

    @ExceptionHandler(UltimoAdministradorActivoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError ultimoAdministradorActivo() {
        return new RespuestaError("No se puede bloquear al último administrador activo", "LAST_ACTIVE_ADMIN");
    }

    // Código común de los 400 de validación de toda la API: datos de dominio no válidos, un @Valid que falla y un
    // cuerpo que no se puede leer. Los motivos del registro con campo propio llevan su code (ver codigo()).
    private static final String CODIGO_VALIDACION = "VALIDATION_ERROR";

    // 400 + code al dar de alta un producto (HU31, T179): el mensaje es genérico, nunca el detalle de la regla.
    @ExceptionHandler(ProductoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError productoInvalido() {
        return new RespuestaError("El producto es inválido", CODIGO_VALIDACION);
    }

    // 409 + code cuando el vendedor ya tiene un producto con ese nombre (HU31, T179).
    // PENDIENTE: confirmar el code con el frontend (el contrato actual no define ningún 409 de productos).
    @ExceptionHandler(ProductoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError productoDuplicado() {
        return new RespuestaError("El producto ya existe con el mismo nombre", "PRODUCT_NAME_ALREADY_EXISTS");
    }

    // 400 + code cuando la imagen del producto no supera el antivirus (HU31 y HU34, T397), al crear o al modificar.
    @ExceptionHandler(ImagenNoSeguraException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError imagenNoSegura() {
        return new RespuestaError("La imagen no ha superado el análisis antivirus", "IMAGE_NOT_SAFE");
    }

    // 404 + code al consultar un producto propio (HU33, T348): un producto ajeno se responde igual que uno
    // inexistente, para no revelar que existe (frontend/docs/hu31-formulario-producto.md).
    @ExceptionHandler(ProductoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError productoNoEncontrado() {
        return new RespuestaError("El producto no existe", "PRODUCT_NOT_FOUND");
    }

    // 400 + code al crear una categoría (HU40, T323): el mensaje es genérico, nunca el detalle de la regla.
    @ExceptionHandler(CategoriaInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError categoriaInvalida() {
        return new RespuestaError("La categoría es inválida", CODIGO_VALIDACION);
    }

    // 409 + code cuando ya hay una categoría con ese nombre, sin distinguir mayúsculas ni tildes (HU40, T323).
    // PENDIENTE: confirmar el code con quien haga la vista de creación (T320).
    @ExceptionHandler(CategoriaDuplicadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError categoriaDuplicada() {
        return new RespuestaError("La categoría ya existe con el mismo nombre", "CATEGORY_NAME_ALREADY_EXISTS");
    }

    // 404 + code cuando la categoría que se edita no existe (HU41, T328).
    @ExceptionHandler(CategoriaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError categoriaNoEncontrada() {
        return new RespuestaError("La categoría no existe", "CATEGORY_NOT_FOUND");
    }

    // 409 + code cuando la categoría que se elimina tiene productos (HU42, T325): el mensaje es genérico.
    // PENDIENTE: confirmar el code con quien haga la vista de eliminación (T324).
    @ExceptionHandler(CategoriaConProductosException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError categoriaConProductos() {
        return new RespuestaError("La categoría tiene productos y no se puede eliminar", "CATEGORY_HAS_PRODUCTS");
    }

    // 409 + code cuando la categoría que se elimina es la principal de algún vendedor (HU42, T325).
    // PENDIENTE: confirmar el code con quien haga la vista de eliminación (T324).
    @ExceptionHandler(CategoriaConVendedoresException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError categoriaConVendedores() {
        return new RespuestaError("La categoría es la principal de algún vendedor y no se puede eliminar",
                "CATEGORY_HAS_SELLERS");
    }

    // 400 + code al dar de alta un método de pago (HU6), también si la tarjeta está caducada. El mensaje es
    // genérico: nunca el detalle de la regla ni los datos de pago enviados.
    // PENDIENTE: confirmar los codes de métodos de pago con quien haga la vista (T113, T269).
    @ExceptionHandler(MetodoPagoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError metodoPagoInvalido() {
        return new RespuestaError("El método de pago es inválido", CODIGO_VALIDACION);
    }

    // 409 + code cuando el cliente ya tiene el máximo de métodos de pago (HU6: 5)
    @ExceptionHandler(LimiteMetodosPagoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError limiteMetodosPago() {
        return new RespuestaError("Se ha alcanzado el máximo de métodos de pago", "PAYMENT_METHOD_LIMIT_REACHED");
    }

    // 404 + code al marcar o eliminar un método de pago (HU6): uno ajeno se responde igual que uno
    // inexistente, para no revelar que existe
    @ExceptionHandler(MetodoPagoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError metodoPagoNoEncontrado() {
        return new RespuestaError("El método de pago no existe", "PAYMENT_METHOD_NOT_FOUND");
    }

    // 409 + code al eliminar el método predeterminado sin elegir cuál lo sustituye (HU6)
    @ExceptionHandler(NuevoPredeterminadoObligatorioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError nuevoPredeterminadoObligatorio() {
        return new RespuestaError("Hay que elegir el nuevo método de pago predeterminado",
                "DEFAULT_PAYMENT_METHOD_REQUIRED");
    }

    // Carrito (HU9): los codes que ya espera el frontend (frontend/src/api/cart.js)
    @ExceptionHandler(CantidadInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError cantidadInvalida() {
        return new RespuestaError("La cantidad no es válida", CODIGO_VALIDACION);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError stockInsuficiente() {
        return new RespuestaError("No hay stock suficiente", "INSUFFICIENT_STOCK");
    }

    // Un producto que no existe o no está disponible se responde igual que uno inexistente
    @ExceptionHandler(ProductoNoComprableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError productoNoComprable() {
        return new RespuestaError("El producto no existe", "PRODUCT_NOT_FOUND");
    }

    @ExceptionHandler(LineaCarritoNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError lineaCarritoNoEncontrada() {
        return new RespuestaError("El producto no está en el carrito", "CART_ITEM_NOT_FOUND");
    }

    // 400 + code al añadir o editar una dirección de envío (HU5). El mensaje es genérico: nunca el detalle de
    // la regla ni los datos enviados.
    @ExceptionHandler(DireccionInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError direccionInvalida() {
        return new RespuestaError("La dirección de envío es inválida", CODIGO_VALIDACION);
    }

    // 409 + code cuando el cliente ya tiene el máximo de direcciones de envío (HU5: 5)
    @ExceptionHandler(LimiteDireccionesException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError limiteDirecciones() {
        return new RespuestaError("Se ha alcanzado el máximo de direcciones de envío", "ADDRESS_LIMIT_REACHED");
    }

    // 404 + code al editar, marcar o eliminar una dirección (HU5): una ajena se responde igual que una
    // inexistente, para no revelar que existe
    @ExceptionHandler(DireccionNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError direccionNoEncontrada() {
        return new RespuestaError("La dirección de envío no existe", "ADDRESS_NOT_FOUND");
    }

    // 409 + code al eliminar la dirección principal sin elegir cuál la sustituye (HU5)
    @ExceptionHandler(NuevaPrincipalObligatoriaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError nuevaPrincipalObligatoria() {
        return new RespuestaError("Hay que elegir la nueva dirección de envío principal",
                "PRINCIPAL_ADDRESS_REQUIRED");
    }

    // Pedidos (HU10): un método de pago o una dirección que no son del cliente se responden igual que unos datos
    // inválidos, para no revelar que existen
    @ExceptionHandler({MetodoPagoNoValidoException.class, DireccionNoValidaException.class,
            PedidoInvalidoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError pedidoInvalido() {
        return new RespuestaError("El pedido es inválido", CODIGO_VALIDACION);
    }

    // Pedidos (HU11): uno que no existe o es de otro cliente responde 400, no 404, para no revelar si existe
    @ExceptionHandler(PedidoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError pedidoNoEncontrado() {
        return new RespuestaError("El pedido no existe", "ORDER_NOT_FOUND");
    }

    @ExceptionHandler(CarritoSinProductosDelProveedorException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError carritoSinProductosDelProveedor() {
        return new RespuestaError("No hay productos de ese proveedor en el carrito", "CART_EMPTY");
    }

    @ExceptionHandler(StockInsuficientePedidoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError stockInsuficienteEnElPedido() {
        return new RespuestaError("No hay stock suficiente", "INSUFFICIENT_STOCK");
    }

    // Valoraciones (HU13): puntuación fuera de 1 a 5 o comentario de más de 500 caracteres
    @ExceptionHandler(ValoracionInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError valoracionInvalida() {
        return new RespuestaError("La valoración es inválida", CODIGO_VALIDACION);
    }

    // Solo se valora lo comprado: un pedido PAGADO con el producto
    @ExceptionHandler(ProductoNoCompradoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError productoNoComprado() {
        return new RespuestaError("Solo puedes valorar productos que has comprado", "PRODUCT_NOT_PURCHASED");
    }

    // Una valoración por cliente y producto, también si llegan dos a la vez (índice único)
    @ExceptionHandler(ValoracionDuplicadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError valoracionDuplicada() {
        return new RespuestaError("Ya has valorado este producto", "REVIEW_ALREADY_EXISTS");
    }
}
