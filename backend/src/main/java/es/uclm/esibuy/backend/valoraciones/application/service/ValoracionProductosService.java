package es.uclm.esibuy.backend.valoraciones.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionDuplicadaException;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Valora un producto en nombre del cliente (HU13). Solo habla con el exterior a través de los puertos de salida. */
public class ValoracionProductosService implements ValoracionProductosUseCase {

    private final ValoracionRepositoryPort valoracionRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final ComprasPort compras;
    private final ProductosPort productos;

    public ValoracionProductosService(ValoracionRepositoryPort valoracionRepository,
            ClienteEnSesionPort clienteEnSesion, ComprasPort compras, ProductosPort productos) {
        this.valoracionRepository = valoracionRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.compras = compras;
        this.productos = productos;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO_EN_COMANDO)
    @Override
    public Valoracion valorar(ValorarProductoCommand comando) {
        if (!compras.haComprado(comando.emailCliente(), comando.productoId())) {
            throw new ProductoNoCompradoException();
        }
        String clienteId = clienteEnSesion.idPorCorreo(comando.emailCliente());
        if (valoracionRepository.existePorClienteYProducto(clienteId, comando.productoId())) {
            throw new ValoracionDuplicadaException();
        }
        Valoracion valoracion = Valoracion.nueva(comando.productoId(), clienteId, comando.puntuacion(),
                comando.comentario());
        Valoracion guardada = valoracionRepository.guardar(valoracion);
        actualizarValoracionDelProducto(comando.productoId());
        return guardada;
    }

    // La media se calcula con todas las valoraciones del producto, la nueva incluida (decisión del equipo: es simple y
    // se corrige sola). Si dos clientes valoran el mismo producto a la vez, el último en escribir puede no contar la
    // del otro hasta la siguiente valoración: riesgo asumido.
    private void actualizarValoracionDelProducto(String productoId) {
        List<Valoracion> todas = valoracionRepository.buscarPorProducto(productoId);
        double media = todas.stream().mapToInt(Valoracion::puntuacion).average().orElse(0);
        productos.actualizarValoracion(productoId, media, todas.size());
    }
}
