package es.uclm.esibuy.backend.usuarios.application.port.out;

/** Lo que usuarios necesita del módulo de carrito para eliminar una cuenta de cliente (HU25). */
public interface CarritoClientePort {

    void eliminar(String emailCliente);
}
