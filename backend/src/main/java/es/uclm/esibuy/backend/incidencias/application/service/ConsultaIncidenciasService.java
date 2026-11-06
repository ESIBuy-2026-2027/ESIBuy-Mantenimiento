package es.uclm.esibuy.backend.incidencias.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.incidencias.application.port.in.ConsultaIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Consulta las incidencias del cliente o del vendedor en sesión (HU44). Solo habla con el exterior a través de los
 * puertos de salida. Cada usuario ve solo las suyas: el filtro es su propio id, que sale de la sesión.
 */
public class ConsultaIncidenciasService implements ConsultaIncidenciasUseCase {

    // Para que nadie pida de golpe todas las incidencias de una vez
    private static final int TAMANO_MAXIMO = 50;

    private final IncidenciaRepositoryPort incidenciaRepository;
    private final AutorEnSesionPort autorEnSesion;

    public ConsultaIncidenciasService(IncidenciaRepositoryPort incidenciaRepository, AutorEnSesionPort autorEnSesion) {
        this.incidenciaRepository = incidenciaRepository;
        this.autorEnSesion = autorEnSesion;
    }

    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO)
    @Override
    public IncidenciaPage consultarPropias(String email, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new IncidenciaInvalidaException("La página o el tamaño pedidos no son válidos");
        }
        String autorId = autorEnSesion.idPorCorreo(email);
        return incidenciaRepository.buscarPorAutor(autorId, pagina, tamano);
    }
}