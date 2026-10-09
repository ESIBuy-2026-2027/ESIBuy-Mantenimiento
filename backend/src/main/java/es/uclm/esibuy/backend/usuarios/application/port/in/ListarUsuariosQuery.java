package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Rol;

import java.util.Locale;
import java.util.Set;

/**
 * Criterios del caso de uso de listado, independientes de HTTP y MongoDB.
 *
 * @param busqueda texto que se buscará en los datos consultables del usuario
 * @param rol filtro opcional por rol
 * @param estado filtro opcional por estado persistido
 * @param pagina índice de página, empezando en cero
 * @param tamano número máximo de usuarios por página
 */
public record ListarUsuariosQuery(
        String busqueda,
        Rol rol,
        String estado,
        int pagina,
        int tamano
) {

    public ListarUsuariosQuery {
        // La consulta se valida en el borde de aplicación para que ningún adaptador
        // tenga que decidir qué significa una página o un tamaño inválidos.
        if (pagina < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }
        if (tamano < 1) {
            throw new IllegalArgumentException("El tamaño debe ser mayor que cero");
        }
        if (estado != null) {
            estado = estado.trim().toUpperCase(Locale.ROOT);
            if (!Set.of("PENDIENTE", "ACTIVO", "BLOQUEADO", "DESACTIVADO").contains(estado)) {
                throw new IllegalArgumentException("El estado no es válido");
            }
        }
    }
}
