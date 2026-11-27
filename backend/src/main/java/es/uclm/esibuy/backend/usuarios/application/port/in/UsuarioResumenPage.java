package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.util.List;

public record UsuarioResumenPage(
        List<UsuarioResumen> usuarios,
        int pagina,
        int tamano,
        long totalElementos
) {
}
