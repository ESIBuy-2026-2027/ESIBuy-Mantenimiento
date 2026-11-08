package es.uclm.esibuy.backend.incidencias.domain.model;

/**
 * Una incidencia junto a quien la reportó, para la supervisión del administrador (HU49).
 *
 * @param incidencia la incidencia
 * @param autor      quien la reportó; {@code null} si su cuenta se ha eliminado
 */
public record IncidenciaSupervisada(Incidencia incidencia, AutorIncidencia autor) {
}
