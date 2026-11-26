package es.uclm.esibuy.backend.incidencias.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.incidencias.application.port.in.SupervisionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutoresPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisada;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisadaPage;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Supervisión de las incidencias por el administrador (HU49): todas las incidencias, cada una con el correo y el rol
 * de quien la reportó.
 */
public class SupervisionIncidenciasService implements SupervisionIncidenciasUseCase {

    // El mismo límite que en ConsultaIncidenciasService
    private static final int TAMANO_MAXIMO = 50;
    private static final Set<String> CAMPOS_ORDEN = Set.of("fechaCreacion", "asunto", "estado");
    private static final Set<String> SENTIDOS = Set.of("asc", "desc");

    private final IncidenciaRepositoryPort incidenciaRepository;
    private final AutoresPort autores;

    public SupervisionIncidenciasService(IncidenciaRepositoryPort incidenciaRepository, AutoresPort autores) {
        this.incidenciaRepository = incidenciaRepository;
        this.autores = autores;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public IncidenciaSupervisadaPage consultarTodas(int pagina, int tamano) {
        return consultar(pagina, tamano, null, null, null, null);
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public IncidenciaSupervisadaPage consultarTodas(int pagina, int tamano, EstadoIncidencia estado) {
        return consultar(pagina, tamano, estado, null, null, null);
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public IncidenciaSupervisadaPage consultarTodas(int pagina, int tamano, EstadoIncidencia estado, String busqueda,
            String ordenarPor, String sentido) {
        return consultar(pagina, tamano, estado, busqueda, ordenarPor, sentido);
    }

    private IncidenciaSupervisadaPage consultar(int pagina, int tamano, EstadoIncidencia estado, String busqueda,
            String ordenarPor, String sentido) {
        if (pagina < 0 || tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new IncidenciaInvalidaException("La página o el tamaño no son válidos");
        }
        if (ordenarPor != null && !ordenarPor.isBlank() && !CAMPOS_ORDEN.contains(ordenarPor)) {
            throw new IncidenciaInvalidaException("El criterio de ordenación no es válido");
        }
        if (sentido != null && !sentido.isBlank() && !SENTIDOS.contains(sentido.toLowerCase())) {
            throw new IncidenciaInvalidaException("El sentido de ordenación no es válido");
        }

        boolean sinBusquedaNiOrden = (busqueda == null || busqueda.isBlank())
                && (ordenarPor == null || ordenarPor.isBlank())
                && (sentido == null || sentido.isBlank());
        IncidenciaPage encontradas;
        if (sinBusquedaNiOrden) {
            encontradas = estado == null
                    ? incidenciaRepository.buscarTodas(pagina, tamano)
                    : incidenciaRepository.buscarPorEstado(estado, pagina, tamano);
        } else {
            encontradas = incidenciaRepository.buscarSupervisadas(estado, busqueda, ordenarPor, sentido, pagina,
                    tamano);
        }

        // Cada autor se busca una sola vez, aunque tenga varias incidencias en la página. El mapa es local a la
        // llamada: así no guarda datos de una consulta para la siguiente.
        Map<String, Optional<AutorIncidencia>> autoresPorId = new HashMap<>();
        List<IncidenciaSupervisada> supervisadas = encontradas.incidencias().stream()
                .map(incidencia -> new IncidenciaSupervisada(incidencia,
                        autoresPorId.computeIfAbsent(incidencia.autorId(), autores::buscarAutor).orElse(null)))
                .toList();

        return new IncidenciaSupervisadaPage(supervisadas, encontradas.pagina(), encontradas.tamano(),
                encontradas.totalElementos());
    }
}
