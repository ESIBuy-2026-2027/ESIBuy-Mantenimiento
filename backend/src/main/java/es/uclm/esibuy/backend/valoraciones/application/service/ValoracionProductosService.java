package es.uclm.esibuy.backend.valoraciones.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Valora un producto en nombre del cliente (HU13). Solo habla con el exterior a través de los puertos de salida. */
public class ValoracionProductosService implements ValoracionProductosUseCase {

    private final ValoracionRepositoryPort valoracionRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final ComprasPort compras;

    public ValoracionProductosService(ValoracionRepositoryPort valoracionRepository,
            ClienteEnSesionPort clienteEnSesion, ComprasPort compras) {
        this.valoracionRepository = valoracionRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.compras = compras;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO_EN_COMANDO)
    @Override
    public Valoracion valorar(ValorarProductoCommand comando) {
        if (!compras.haComprado(comando.emailCliente(), comando.productoId())) {
            throw new ProductoNoCompradoException();
        }
        String clienteId = clienteEnSesion.idPorCorreo(comando.emailCliente());
        Valoracion valoracion = Valoracion.nueva(comando.productoId(), clienteId, comando.puntuacion(),
                comando.comentario());
        return valoracionRepository.guardar(valoracion);
    }
}