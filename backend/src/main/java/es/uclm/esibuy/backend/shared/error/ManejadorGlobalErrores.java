package es.uclm.esibuy.backend.shared.error;

import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones que salen de los controladores a respuestas HTTP con un mensaje
 * genérico, para que los controladores no necesiten {@code try/catch}.
 *
 * <p>PENDIENTE DE DECIDIR (equipo): texto exacto de los mensajes genéricos.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    @ExceptionHandler(CorreoYaRegistradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError correoYaRegistrado() {
        return new RespuestaError("No se ha podido completar el registro");
    }

    @ExceptionHandler({ContrasenaInvalidaException.class, EmailInvalidoException.class})
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
}
