package es.uclm.esibuy.backend.preferencias.application.port.out;

import java.util.Set;

/** Lo que las preferencias necesitan del módulo de categorías: qué categorías se pueden elegir. */
public interface CategoriasPort {

    // Identificadores de las categorías activas
    Set<String> activas();
}
