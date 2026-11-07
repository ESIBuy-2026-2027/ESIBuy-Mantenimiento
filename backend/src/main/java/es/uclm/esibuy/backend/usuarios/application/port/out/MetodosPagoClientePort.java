package es.uclm.esibuy.backend.usuarios.application.port.out;

/** Lo que usuarios necesita del módulo de métodos de pago para eliminar una cuenta de cliente (HU25). */
public interface MetodosPagoClientePort {

    void eliminarTodos(String emailCliente);
}
