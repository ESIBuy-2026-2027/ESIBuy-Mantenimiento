package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.direcciones;

import java.util.Optional;

import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;

/** Adaptador de salida que pide al módulo de direcciones (HU5) los datos de una dirección para copiarlos. */
public class DireccionesAdapter implements DireccionesPort {

    private final GestionDireccionesUseCase gestionDirecciones;

    public DireccionesAdapter(GestionDireccionesUseCase gestionDirecciones) {
        this.gestionDirecciones = gestionDirecciones;
    }

    @Override
    public Optional<DireccionEnvioPedido> consultar(String emailCliente, String direccionId) {
        return gestionDirecciones.consultarParaPedido(emailCliente, direccionId)
                .map(direccion -> new DireccionEnvioPedido(direccion.destinatario(), direccion.calle(),
                        direccion.detalles(), direccion.codigoPostal(), direccion.ciudad(), direccion.provincia(),
                        direccion.telefono()));
    }
}
