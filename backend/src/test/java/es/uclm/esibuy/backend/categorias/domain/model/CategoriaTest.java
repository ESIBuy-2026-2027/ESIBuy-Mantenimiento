package es.uclm.esibuy.backend.categorias.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaInvalidaException;

// Entidad Categoria (HU40, T321): nombre obligatorio de hasta 50 caracteres, descripción opcional de hasta 200
// y estado ACTIVA o INACTIVA. Java puro, sin Spring ni base de datos.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU40-CP01 a CP04, propuestos.
class CategoriaTest {

    @Test
    @DisplayName("HU40-CP01: crea una categoría con sus datos")
    void creaUnaCategoriaConDatosCorrectos() {
        Categoria categoria = new Categoria("c-1", "Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA);

        assertEquals("c-1", categoria.id());
        assertEquals("Hogar", categoria.nombre());
        assertEquals("Muebles y decoración", categoria.descripcion());
        assertEquals(EstadoCategoria.INACTIVA, categoria.estado());
    }

    @Test
    @DisplayName("HU40-CP01: una categoría nueva nace ACTIVA y sin id")
    void unaCategoriaNuevaNaceActivaYSinId() {
        Categoria categoria = Categoria.nueva("Hogar", "Muebles y decoración");

        assertNull(categoria.id());
        assertEquals(EstadoCategoria.ACTIVA, categoria.estado());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU40-CP02: rechaza una categoría sin nombre")
    void rechazaUnaCategoriaSinNombre(String nombre) {
        assertThrows(CategoriaInvalidaException.class, () -> Categoria.nueva(nombre, ""));
    }

    @Test
    @DisplayName("HU40-CP02: el nombre se guarda sin espacios en los extremos y admite hasta 50 caracteres")
    void elNombreSeRecortaYAdmiteHasta50Caracteres() {
        String cincuentaConEspacios = "  " + "a".repeat(50) + " ";
        String cincuentaYUno = "a".repeat(51);

        assertEquals("Hogar", Categoria.nueva(" \tHogar  ", "").nombre());
        assertEquals("a".repeat(50), Categoria.nueva(cincuentaConEspacios, "").nombre());
        assertThrows(CategoriaInvalidaException.class, () -> Categoria.nueva(cincuentaYUno, ""));
    }

    @Test
    @DisplayName("HU40-CP03: la descripción es opcional y admite hasta 200 caracteres")
    void laDescripcionEsOpcionalYAdmiteHasta200Caracteres() {
        String doscientos = "d".repeat(200);
        String doscientosUno = "d".repeat(201);

        assertEquals("", Categoria.nueva("Hogar", null).descripcion());
        assertEquals("Muebles", Categoria.nueva("Hogar", "  Muebles ").descripcion());
        assertEquals(doscientos, Categoria.nueva("Hogar", doscientos).descripcion());
        assertThrows(CategoriaInvalidaException.class, () -> Categoria.nueva("Hogar", doscientosUno));
    }

    @Test
    @DisplayName("HU40-CP04: rechaza una categoría sin estado")
    void rechazaUnaCategoriaSinEstado() {
        assertThrows(CategoriaInvalidaException.class, () -> new Categoria("c-1", "Hogar", "", null));
    }
}
