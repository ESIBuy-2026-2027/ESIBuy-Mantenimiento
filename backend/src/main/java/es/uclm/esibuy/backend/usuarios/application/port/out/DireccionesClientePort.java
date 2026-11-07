package es.uclm.esibuy.backend.usuarios.application.port.out;

/** Lo que usuarios necesita del módulo de direcciones para eliminar una cuenta de cliente (HU25). */
public interface DireccionesClientePort {

    void eliminarTodas(String emailCliente);
}
