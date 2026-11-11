package es.uclm.esibuy.backend.usuarios.application.port.in;

public interface GestionSeguridadUseCase {

    ConfiguracionSeguridadResponse consultar(String email);

    ConfiguracionSeguridadResponse actualizar(
            String email,
            ActualizarFactoresCommand comando);
}
