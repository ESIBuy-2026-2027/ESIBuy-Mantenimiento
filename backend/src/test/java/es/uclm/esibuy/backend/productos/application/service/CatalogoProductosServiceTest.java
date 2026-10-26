package es.uclm.esibuy.backend.productos.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Catálogo de productos del cliente (HU7, T352). El repositorio es un doble de prueba (Mockito): se prueba
// el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU7-CP10, propuesto.
class CatalogoProductosServiceTest {

    @Test
    @DisplayName("HU7-CP10: busca en el catálogo con los criterios recibidos y devuelve la página del repositorio")
    void buscaEnElCatalogoConLosCriteriosRecibidos() {
        // TODO: con un ProductoRepositoryPort de Mockito que devuelve una ProductoPage para una BuscarCatalogoQuery,
        // buscarProductos(query) devuelve esa misma página (assertSame).
        fail("Funcionalidad no implementada");
    }
}
