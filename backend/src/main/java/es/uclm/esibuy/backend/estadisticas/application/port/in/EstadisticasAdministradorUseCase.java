package es.uclm.esibuy.backend.estadisticas.application.port.in;

/** Consulta los rankings de ventas y valoraciones de toda la plataforma para el administrador. */
public interface EstadisticasAdministradorUseCase {

    EstadisticasAdministradorResultado consultar();
}
