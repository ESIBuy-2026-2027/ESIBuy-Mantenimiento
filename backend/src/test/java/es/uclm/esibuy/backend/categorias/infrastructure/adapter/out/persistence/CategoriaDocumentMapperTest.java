package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

// Conversión entre la categoría del dominio y su documento de MongoDB (categorias, HU40, T321).
// Sin Spring ni base de datos.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU40-CP04, propuesto.
class CategoriaDocumentMapperTest {

    private final CategoriaDocumentMapper mapper = new CategoriaDocumentMapper();

    @Test
    @DisplayName("HU40-CP04: el documento guarda los datos de la categoría y su estado como texto")
    void convierteCategoriaEnDocumento() {
        CategoriaDocument documento = mapper.aDocumento(
                new Categoria("c-1", "Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA));

        assertEquals("c-1", documento.getId());
        assertEquals("Hogar", documento.getNombre());
        assertEquals("Muebles y decoración", documento.getDescripcion());
        assertEquals("INACTIVA", documento.getEstado());
    }

    @Test
    @DisplayName("HU40-CP04: un documento leído recupera la categoría con su descripción y su estado")
    void convierteDocumentoEnCategoria() {
        CategoriaDocument documento = documento("c-1", "Hogar");
        documento.setDescripcion("Muebles y decoración");
        documento.setEstado("INACTIVA");

        assertEquals(new Categoria("c-1", "Hogar", "Muebles y decoración", EstadoCategoria.INACTIVA),
                mapper.aDominio(documento));
    }

    @Test
    @DisplayName("HU40-CP04: una categoría guardada sin estado ni descripción se lee ACTIVA y sin descripción")
    void unDocumentoSinEstadoNiDescripcionSeLeeActivo() {
        assertEquals(new Categoria("c-1", "Hogar", "", EstadoCategoria.ACTIVA),
                mapper.aDominio(documento("c-1", "Hogar")));
    }

    private static CategoriaDocument documento(String id, String nombre) {
        CategoriaDocument documento = new CategoriaDocument();
        documento.setId(id);
        documento.setNombre(nombre);
        return documento;
    }
}
