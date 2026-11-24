package es.uclm.esibuy.backend.pedidos.application.service;

import java.time.Clock;

import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;

public record DependenciasRealizacionPedido(
        PedidoRepositoryPort pedidoRepository,
        ClienteEnSesionPort clienteEnSesion,
        CarritoPort carrito,
        MetodosPagoPort metodosPago,
        DireccionesPort direcciones,
        ExistenciasPort existencias,
        PagoPort pago,
        ContadorPedidosPort contador,
        Clock reloj) {
}