package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.imagen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.productos.application.port.out.ImagenStoragePort.ImagenAlmacenada;

@DataMongoTest
@Import(MongoTestcontainersConfig.class)
class GridFsImagenAdapterTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Test
    @DisplayName("GridFS almacena, recupera y elimina una imagen conservando sus metadatos")
    void almacenaRecuperaYEliminaImagen() {
        GridFsImagenAdapter adapter = new GridFsImagenAdapter(gridFsTemplate);
        byte[] contenido = new byte[] { 1, 2, 3, 4, 5 };
        String nombre = "imagen-" + UUID.randomUUID() + ".png";

        String id = adapter.almacenar(contenido, nombre, "image/png");

        assertTrue(id.matches("[0-9a-fA-F]{24}"));
        ImagenAlmacenada recuperada = adapter.obtener(id).orElseThrow();
        assertArrayEquals(contenido, recuperada.contenido());
        assertEquals("image/png", recuperada.tipoContenido());
        assertEquals(nombre, recuperada.nombreArchivo());

        adapter.eliminar(id);

        assertTrue(adapter.obtener(id).isEmpty());
    }

    @Test
    @DisplayName("GridFS devuelve vacío para identificadores inválidos o inexistentes")
    void devuelveVacioParaIdentificadoresNoValidos() {
        GridFsImagenAdapter adapter = new GridFsImagenAdapter(gridFsTemplate);

        assertTrue(adapter.obtener("no-es-un-object-id").isEmpty());
        assertTrue(adapter.obtener("000000000000000000000000").isEmpty());
        assertTrue(adapter.obtener(null).isEmpty());
    }

    @Test
    @DisplayName("Eliminar ignora identificadores inválidos")
    void eliminarIgnoraIdentificadoresInvalidos() {
        GridFsImagenAdapter adapter = new GridFsImagenAdapter(gridFsTemplate);

        adapter.eliminar("id-invalido");
        adapter.eliminar(null);
    }
}
