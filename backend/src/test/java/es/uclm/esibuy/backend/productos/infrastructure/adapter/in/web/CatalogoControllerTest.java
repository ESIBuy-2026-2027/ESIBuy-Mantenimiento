package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Catálogo de productos del cliente (HU7, T353): GET /api/products. El caso de uso es un doble de prueba;
// la seguridad es la real (SecurityConfig: /api/products solo para CLIENTE).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU7-CP16 a CP19, propuestos.
class CatalogoControllerTest {

    @Test
    @DisplayName("HU7-CP16: el cliente recibe el catálogo con el formato del contrato y sin valoraciones todavía")
    void clienteRecibeElCatalogoConElFormatoDelContrato() {
        // TODO: el caso de uso devuelve una página con un producto; 200 con items[0].id, nombre, imagen, categoriaId,
        // valoracion = null y numValoraciones = 0; page = 1, size, totalItems y totalPages. Sin precio, stock ni vendedorId.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP17: los parámetros llegan al caso de uso traducidos: página desde 0, criterio y sentido")
    void traduceLosParametrosALaConsulta() {
        // TODO: GET ?page=2&size=10&search=teclado&category=cat-2&minRating=4&sort=valoracion&order=desc;
        // con ArgumentCaptor: pagina = 1, tamano = 10, busqueda, categoriaId, valoracionMinima = 4,
        // criterio = VALORACION, descendente = true.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP18: un parámetro no válido devuelve 400 y no llega al caso de uso")
    void parametroNoValidoDevuelve400() {
        // TODO: parametrizado con page=0, size=0, sort=precio, order=arriba y minRating=6 → 400 y verify(never()).
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP19: sin sesión no se ve el catálogo")
    void sinSesionDevuelve401() {
        // TODO: GET sin usuario → 401.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU7-CP19: un usuario que no es cliente no ve el catálogo")
    void otroRolDevuelve403() {
        // TODO: parametrizado con VENDEDOR y ADMINISTRADOR → 403.
        fail("Funcionalidad no implementada");
    }
}
