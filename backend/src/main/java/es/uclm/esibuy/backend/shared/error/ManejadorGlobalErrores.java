package es.uclm.esibuy.backend.shared.error;

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
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficientePedidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AuthenticatorYaDadoDeAltaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYNombreComercialYaRegistradosException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutoeliminacionNoPermitidaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaPendienteException;

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

    // Un @Valid que falla en el cuerpo de la petición (campo vacío o demasiado largo) responde con el mismo
    // formato que el resto de 400, y no con el cuerpo por defecto de Spring. Afecta a todos los endpoints con @Valid.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError cuerpoDeLaPeticionInvalido() {
        return new RespuestaError(DATOS_DE_REGISTRO_INVALIDOS, "INVALID_FIELD");
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
            case DATO_INVALIDO -> "INVALID_FIELD";
        };
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RespuestaError credencialesInvalidas() {
        return new RespuestaError("Credenciales Incorrectas");
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public RespuestaError cuentaBloqueada() {
        return new RespuestaError("Demasiados intentos. Inténtalo más tarde");
    }

    @ExceptionHandler(CuentaDesactivadaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError cuentaDesactivada() {
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
    // 403 + code, como la cuenta desactivada: el frontend elige su mensaje por el code (T236).
    @ExceptionHandler(CuentaPendienteException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError cuentaPendiente() {
        return new RespuestaError("La cuenta está pendiente de verificación", "ACCOUNT_PENDING");
    }

    // Quien ya tiene Authenticator no puede volver a darse de alta: impediría saltarse el tercer factor (T20)
    @ExceptionHandler(AuthenticatorYaDadoDeAltaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RespuestaError authenticatorYaDadoDeAlta() {
        return new RespuestaError("El usuario ya tiene Authenticator", "AUTHENTICATOR_ALREADY_ENROLLED");
    }

    // 409 + code al desactivar (HU23): códigos acordados con el frontend (frontend/docs/hu23-desactivar-usuario.md).
    @ExceptionHandler(AutodesactivacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError autodesactivacionNoPermitida() {
        return new RespuestaError("No puedes desactivar tu propia cuenta", "CANNOT_DEACTIVATE_SELF");
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
        return new RespuestaError("No se puede desactivar al último administrador activo", "LAST_ACTIVE_ADMIN");
    }

    // Código común de los 400 de producto, categoría y método de pago.
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
}
