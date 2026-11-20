package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.out.CarritoClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.DireccionesClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ListaDeseosClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.MetodosPagoClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.PedidosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.PreferenciasClientePort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ProductosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ValoracionesProductoPort;

public record DependenciasGestionCuenta(
                UsuarioRepositoryPort usuarioRepository,
                HashContrasenaPort hashContrasena,
                ProductosVendedorPort productos,
                PedidosVendedorPort consultaProductosConPedidos,
                ValoracionesProductoPort valoraciones,
                FactorAutenticacionRepositoryPort factores,
                CodigoVerificacionRepositoryPort codigosVerificacion,
                CodigoRecuperacionRepositoryPort codigosRecuperacion,
                ListaDeseosClientePort listaDeseos,
                CarritoClientePort carrito,
                PreferenciasClientePort preferencias,
                MetodosPagoClientePort metodosPago,
                DireccionesClientePort direcciones) {
}