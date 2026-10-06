package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHashContrasenaAdapter implements HashContrasenaPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hashear(String contrasena) {
        return encoder.encode(contrasena);
    }
}