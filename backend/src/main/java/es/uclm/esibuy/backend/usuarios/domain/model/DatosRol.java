package es.uclm.esibuy.backend.usuarios.domain.model;

/**
 * Datos propios del rol de un usuario («Planteamiento inicial BBDD» y MVP del Sprint 1).
 *
 * <p>Es una interfaz sellada: solo la implementan los datos de los roles que existen, y cada uno
 * sabe a qué rol corresponde. Así, el rol de un {@link Usuario} sale de sus datos de rol y no
 * puede haber, por ejemplo, un vendedor con tipo de cliente.
 */
public sealed interface DatosRol permits DatosCliente, DatosVendedor {

    Rol rol();
}
