package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.util.Collection;
import java.util.Set;

/**
 * Tipo (estándar o premium) de unos clientes, para otros módulos: las alertas de bajada de precio (AB#529) comparan el
 * precio que paga cada cliente. Solo devuelve ids, sin el dominio de usuarios.
 */
public interface TipoClienteUseCase {

    /** De los clientes indicados, los que son premium. Un id que no existe o no es de un cliente no lo es. */
    Set<String> premiumEntre(Collection<String> clienteIds);
}
