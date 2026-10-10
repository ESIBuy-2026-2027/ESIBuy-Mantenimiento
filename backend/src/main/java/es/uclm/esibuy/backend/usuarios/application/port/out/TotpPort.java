package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.time.Instant;
import java.util.OptionalLong;

public interface TotpPort {

        OptionalLong periodoDelCodigo(String secreto, String codigo, Instant ahora); // <- OptionalLong: si el código es válido, devuelve el periodo en el que lo es; si no, vacío
        String generarSecreto();
        String generarQr(String email, String secreto);
}
