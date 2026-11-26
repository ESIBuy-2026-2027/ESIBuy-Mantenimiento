package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios;

import java.util.Collection;
import java.util.Set;

import es.uclm.esibuy.backend.alertas.application.port.out.TiposClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.in.TipoClienteUseCase;

/** Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su dominio. */
public class TiposClienteAdapter implements TiposClientePort {

    private final TipoClienteUseCase tipoCliente;

    public TiposClienteAdapter(TipoClienteUseCase tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    @Override
    public Set<String> premiumEntre(Collection<String> clienteIds) {
        return tipoCliente.premiumEntre(clienteIds);
    }
}
