package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHashContrasenaAdapter implements HashContrasenaPort {

    private static final int COSTE_BCRYPT = 10; // D2: mínimo recomendado por OWASP
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(COSTE_BCRYPT);

    @Override
    public String hashear(String contrasena) {
        return encoder.encode(contrasena);
    }
}