package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.Set;

/** Lo que usuarios necesita del módulo de pedidos para conservar el historial del vendedor. */
public interface PedidosVendedorPort {

    Set<String> buscarDeVendedor(String vendedorId);
}
