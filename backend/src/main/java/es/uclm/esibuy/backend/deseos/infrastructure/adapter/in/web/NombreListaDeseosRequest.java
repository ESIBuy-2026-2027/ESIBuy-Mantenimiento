package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Petición para crear o renombrar una lista de deseos (PBI 534). */
public record NombreListaDeseosRequest(
        @NotBlank(message = "El nombre de la lista es obligatorio")
        @Size(max = ListaDeseos.NOMBRE_MAXIMO, message = "El nombre de la lista es demasiado largo")
        String nombre) {
}
