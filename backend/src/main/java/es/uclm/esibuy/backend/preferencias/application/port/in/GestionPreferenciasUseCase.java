package es.uclm.esibuy.backend.preferencias.application.port.in;

import java.util.List;

/**
 * Gestión de sus preferencias por parte del cliente (HU46): las categorías que le interesan. Recibe el correo del
 * cliente en sesión: solo puede ver y cambiar las suyas.
 */
public interface GestionPreferenciasUseCase {

    /**
     * Las categorías que interesan al cliente, en el orden en que las eligió. Solo salen las que siguen activas; si
     * aún no ha elegido nada, una lista vacía.
     */
    List<String> consultar(String emailCliente);

    /**
     * Sustituye las categorías que interesan al cliente (sin repetidas; una lista vacía quita todas) y las devuelve.
     *
     * @throws es.uclm.esibuy.backend.preferencias.domain.exception.PreferenciasInvalidasException si faltan las
     *         categorías o alguna no existe o no está activa; entonces no se guarda nada
     */
    List<String> guardar(String emailCliente, List<String> categorias);

    /**
     * Borra las preferencias del cliente, para cuando elimina su cuenta (HU25). Si no tiene no es un error. Hay que
     * llamarlo antes de borrar al usuario: las preferencias se buscan por el id del cliente, que se obtiene de su
     * correo.
     */
    void eliminar(String emailCliente);
}
