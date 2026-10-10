package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

/**
 * Un documento de MongoDB no cumple las reglas del dominio y no se puede reconstruir como {@code Usuario}.
 *
 * <p>Es un fallo del servidor (datos corruptos o un cambio de reglas sin migrar), no de quien hace la
 * petición. Por eso es una excepción de infraestructura y no de dominio: el manejador global no la
 * conoce y acaba en un 500, en vez de en el 400 con el que se responde a los datos inválidos del cliente
 * (T228). La excepción de dominio original se conserva como causa para el log.
 */
public class UsuarioPersistidoInvalidoException extends RuntimeException {

    public UsuarioPersistidoInvalidoException(String id, RuntimeException causa) {
        super("El usuario guardado con id " + id + " no cumple las reglas del dominio", causa);
    }
}
