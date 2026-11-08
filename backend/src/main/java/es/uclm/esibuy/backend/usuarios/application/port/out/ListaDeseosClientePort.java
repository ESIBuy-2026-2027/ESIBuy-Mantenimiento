package es.uclm.esibuy.backend.usuarios.application.port.out;

/** Lo que usuarios necesita del módulo de deseos para eliminar una cuenta de cliente (HU25). */
public interface ListaDeseosClientePort {

    void eliminar(String emailCliente);
}
