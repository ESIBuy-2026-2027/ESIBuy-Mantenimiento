package es.uclm.esibuy.backend.preferencias.application.port.in;

import java.util.List;

/**
 * Gestión de sus preferencias por parte del cliente (HU46): sus categorías favoritas (AB#528). Recibe el correo del
 * cliente en sesión: solo puede ver y cambiar las suyas.
 */
public interface GestionPreferenciasUseCase {

    /**
     * Las categorías favoritas del cliente, en el orden en que las marcó. Solo salen las que siguen activas; si aún no
     * ha marcado ninguna, una lista vacía.
     */
    List<String> consultar(String emailCliente);

    /**
     * Marca una categoría como favorita del cliente (AB#528) y devuelve sus favoritas, como en {@link #consultar}.
     * Marcar una que ya lo es no es un error: no cambia nada.
     *
     * @throws es.uclm.esibuy.backend.preferencias.domain.exception.CategoriaNoDisponibleException si no existe o no
     *         está activa; entonces no se guarda nada
     */
    List<String> marcar(String emailCliente, String categoriaId);

    /**
     * Quita una categoría de las favoritas del cliente (AB#528), esté o no activa, y devuelve las que quedan, como en
     * {@link #consultar}. Desmarcar una que no es favorita no es un error: no cambia nada ni se crea nada.
     */
    List<String> desmarcar(String emailCliente, String categoriaId);

    /**
     * Borra las preferencias del cliente, para cuando elimina su cuenta (HU25). Si no tiene no es un error. Hay que
     * llamarlo antes de borrar al usuario: las preferencias se buscan por el id del cliente, que se obtiene de su
     * correo.
     */
    void eliminar(String emailCliente);
}
