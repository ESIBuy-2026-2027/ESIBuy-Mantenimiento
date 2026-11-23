
package es.uclm.esibuy.backend.usuarios.application.port.out;

public interface HashContrasenaPort {

    String hashear(String contrasena);

    boolean coincide(String contrasena, String hash);

    boolean necesitaRehash(String hash);
}
