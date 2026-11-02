package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.pedidos;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;

/** Adaptador de salida que pregunta al módulo de pedidos por su puerto de entrada, sin usar su dominio. */
public class ComprasAdapter implements ComprasPort {

    private final ConsultaComprasUseCase consultaCompras;

    public ComprasAdapter(ConsultaComprasUseCase consultaCompras) {
        this.consultaCompras = consultaCompras;
    }

    @Override
    public boolean haComprado(String emailCliente, String productoId) {
        return consultaCompras.haComprado(emailCliente, productoId);
    }
}