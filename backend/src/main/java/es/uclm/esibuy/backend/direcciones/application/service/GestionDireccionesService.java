package es.uclm.esibuy.backend.direcciones.application.service;

import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionCommand;
import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.direcciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.direcciones.application.port.out.DireccionRepositoryPort;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import es.uclm.esibuy.backend.direcciones.domain.model.CodigoPostal;
import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionesCliente;
import es.uclm.esibuy.backend.direcciones.domain.model.TelefonoContacto;
import java.util.List;

/**
 * Implementa la gestión de direcciones de envío del cliente. Solo habla con el exterior a través de los
 * puertos de salida.
 */
public class GestionDireccionesService implements GestionDireccionesUseCase {

    private final DireccionRepositoryPort direccionRepository;
    private final ClienteEnSesionPort clienteEnSesion;

    public GestionDireccionesService(DireccionRepositoryPort direccionRepository,
            ClienteEnSesionPort clienteEnSesion) {
        this.direccionRepository = direccionRepository;
        this.clienteEnSesion = clienteEnSesion;
    }

    @Override
    public List<DireccionEnvio> listar(String emailCliente) {
        return direccionRepository.buscarPorCliente(clienteEnSesion.idPorCorreo(emailCliente));
    }

    @Override
    public DireccionEnvio anadir(String emailCliente, DireccionCommand direccion) {
        DatosDireccion datos = aDatos(direccion);
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        DireccionEnvio nueva = DireccionEnvio.nueva(clienteId, datos);
        // Las reglas del conjunto (límite y principal) viven en el dominio
        new DireccionesCliente(direccionRepository.buscarPorCliente(clienteId)).anadir(nueva);
        return direccionRepository.guardar(nueva);
    }

    @Override
    public DireccionEnvio editar(String emailCliente, String direccionId, DireccionCommand direccion) {
        DatosDireccion datos = aDatos(direccion);
        DireccionEnvio editada = direccionesDelCliente(emailCliente).editar(direccionId, datos);
        return direccionRepository.guardar(editada);
    }

    @Override
    public void marcarPrincipal(String emailCliente, String direccionId) {
        DireccionesCliente direcciones = direccionesDelCliente(emailCliente);
        direcciones.marcarPrincipal(direccionId);
        // Se guardan todas: el cambio afecta a la nueva principal y a la que deja de serlo
        direcciones.getDirecciones().forEach(direccionRepository::guardar);
    }

    @Override
    public void eliminar(String emailCliente, String direccionId, String nuevaPrincipalId) {
        DireccionesCliente direcciones = direccionesDelCliente(emailCliente);
        // Primero la regla del dominio: si falla, no se borra nada
        direcciones.eliminar(direccionId, nuevaPrincipalId);
        direccionRepository.eliminar(direccionId);
        // Las que quedan se guardan por si ha cambiado la principal
        direcciones.getDirecciones().forEach(direccionRepository::guardar);
    }

    // Solo las del cliente en sesión: un id de otro cliente no se encuentra
    private DireccionesCliente direccionesDelCliente(String emailCliente) {
        return new DireccionesCliente(
                direccionRepository.buscarPorCliente(clienteEnSesion.idPorCorreo(emailCliente)));
    }

    private static DatosDireccion aDatos(DireccionCommand direccion) {
        if (direccion == null) {
            throw new DireccionInvalidaException("Los datos de la dirección son obligatorios");
        }
        return new DatosDireccion(direccion.destinatario(), direccion.calle(), direccion.detalles(),
                new CodigoPostal(direccion.codigoPostal()), direccion.ciudad(), direccion.provincia(),
                new TelefonoContacto(direccion.telefono()));
    }
}
