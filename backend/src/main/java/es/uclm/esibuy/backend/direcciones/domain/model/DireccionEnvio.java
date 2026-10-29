package es.uclm.esibuy.backend.direcciones.domain.model;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;

/**
 * Dirección de envío de un cliente (HU5). Java puro. El cliente se referencia por identificador.
 */
public class DireccionEnvio {

    private final String id;
    private final String clienteId;
    private DatosDireccion datos;
    private boolean principal;

    // Constructor completo: también reconstruye una dirección ya guardada (el mapper de MongoDB)
    public DireccionEnvio(String id, String clienteId, DatosDireccion datos, boolean principal) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new DireccionInvalidaException("El cliente es obligatorio");
        }
        this.id = id;
        this.clienteId = clienteId;
        this.datos = exigirDatos(datos);
        this.principal = principal;
    }

    // Alta de una dirección nueva: sin id hasta que se guarda y no principal (lo decide DireccionesCliente)
    public static DireccionEnvio nueva(String clienteId, DatosDireccion datos) {
        return new DireccionEnvio(null, clienteId, datos, false);
    }

    private static DatosDireccion exigirDatos(DatosDireccion datos) {
        if (datos == null) {
            throw new DireccionInvalidaException("Los datos de la dirección son obligatorios");
        }
        return datos;
    }

    // Cambia lo que escribió el cliente; ni el id ni la principal cambian al editar
    public void editar(DatosDireccion nuevosDatos) {
        this.datos = exigirDatos(nuevosDatos);
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public DatosDireccion getDatos() {
        return datos;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void marcarPrincipal() {
        this.principal = true;
    }

    public void desmarcarPrincipal() {
        this.principal = false;
    }
}
