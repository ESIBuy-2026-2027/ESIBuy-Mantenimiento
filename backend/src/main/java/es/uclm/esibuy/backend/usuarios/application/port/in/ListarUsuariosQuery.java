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
 * @param categoriaId filtro opcional por la categoría principal (vendedores, HU26)
 * @param pagina índice de página, empezando en cero
 * @param tamano número máximo de usuarios por página
 * @param ordenacion orden opcional (por nombre o por estado, y sentido); null = por fecha de alta, de la más reciente a la más antigua
 */
public record ListarUsuariosQuery(
        String busqueda,
        Rol rol,
        String estado,
        String categoriaId,
        int pagina,
        int tamano,
        Ordenacion ordenacion
) {

    // Campo y sentido del orden, juntos para no pasar de 7 componentes (Sonar S107).
    public record Ordenacion(Orden campo, boolean descendente) {
    }

    // Orden del listado (HU26). Por estado, el orden ascendente es pendientes, activos y desactivados.
    public enum Orden { NOMBRE, ESTADO }

    // Listado sin orden propio (HU19): por fecha de alta, como hasta ahora.
    public ListarUsuariosQuery(String busqueda, Rol rol, String estado, int pagina, int tamano) {
        this(busqueda, rol, estado, null, pagina, tamano, null);
    }

    // Forma anterior a los fallos de la demo del Sprint 1 (HU26): campo y sentido sueltos, sin categoría.
    public ListarUsuariosQuery(String busqueda, Rol rol, String estado, int pagina, int tamano,
            Orden ordenarPor, boolean descendente) {
        this(busqueda, rol, estado, null, pagina, tamano, ordenarPor == null ? null : new Ordenacion(ordenarPor, descendente));
    }

    public Orden ordenarPor() {
        return ordenacion == null ? null : ordenacion.campo();
    }

    public boolean descendente() {
        return ordenacion != null && ordenacion.descendente();
    }

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
