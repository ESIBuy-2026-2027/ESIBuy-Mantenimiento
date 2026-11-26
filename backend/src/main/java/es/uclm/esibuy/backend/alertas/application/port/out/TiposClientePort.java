package es.uclm.esibuy.backend.alertas.application.port.out;

import java.util.Collection;
import java.util.Set;

/** Lo que las alertas necesitan saber del tipo de cada cliente (módulo de usuarios): el premium paga otro precio. */
public interface TiposClientePort {

    /** De los clientes indicados, los que son premium. */
    Set<String> premiumEntre(Collection<String> clienteIds);
}
