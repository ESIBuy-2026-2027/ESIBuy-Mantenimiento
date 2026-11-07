package es.uclm.esibuy.backend.usuarios.application.port.out;

/** Lo que usuarios necesita del módulo de preferencias para eliminar una cuenta de cliente (HU25). */
public interface PreferenciasClientePort {

    void eliminar(String emailCliente);
}
