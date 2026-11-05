package es.uclm.esibuy.backend.usuarios.application.port.out;


public interface HashContrasenaPort {

    String hashear(String string);

    boolean coincide(String contrasena, String hash);

}
