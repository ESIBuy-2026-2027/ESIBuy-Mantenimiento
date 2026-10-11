package es.uclm.esibuy.backend.shared.error;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AuthenticatorYaDadoDeAltaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import org.springframework.http.HttpStatus;
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
    // siendo genérico. Decisión acordada con el frontend (T229), con la convención de ACCOUNT_DISABLED.
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

    @ExceptionHandler({ContrasenaInvalidaException.class, EmailInvalidoException.class, UsuarioInvalidoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError datosDeRegistroInvalidos() {
        return new RespuestaError("Datos de registro no válidos");
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
        return new RespuestaError("Esta cuenta está desactivada", "ACCOUNT_DISABLED");
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

    @ExceptionHandler(UltimoAdministradorActivoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError ultimoAdministradorActivo() {
        return new RespuestaError("No se puede desactivar al último administrador activo", "LAST_ACTIVE_ADMIN");
    }

    // 400 + code al dar de alta un producto (HU31, T179): el mensaje es genérico, nunca el detalle de la regla.
    @ExceptionHandler(ProductoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError productoInvalido() {
        return new RespuestaError("El producto es inválido", "VALIDATION_ERROR");
    }

    // 409 + code cuando el vendedor ya tiene un producto con ese nombre (HU31, T179).
    // PENDIENTE: confirmar el code con el frontend (el contrato actual no define ningún 409 de productos).
    @ExceptionHandler(ProductoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError productoDuplicado() {
        return new RespuestaError("El producto ya existe con el mismo nombre", "PRODUCT_NAME_ALREADY_EXISTS");
    }
}
