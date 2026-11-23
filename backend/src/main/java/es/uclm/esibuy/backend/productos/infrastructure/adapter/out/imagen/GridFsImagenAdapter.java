package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.imagen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import com.mongodb.client.gridfs.model.GridFSFile;

import es.uclm.esibuy.backend.productos.application.port.out.ImagenStoragePort;

/** Adaptador de almacenamiento de imágenes mediante GridFS. */
public class GridFsImagenAdapter implements ImagenStoragePort {

    private final GridFsTemplate gridFsTemplate;

    public GridFsImagenAdapter(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public String almacenar(byte[] contenido, String nombreArchivo, String tipoContenido) {
        ObjectId id = gridFsTemplate.store(
                new java.io.ByteArrayInputStream(contenido), nombreArchivo, tipoContenido);
        return id.toHexString();
    }

    @Override
    public Optional<ImagenAlmacenada> obtener(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            return Optional.empty();
        }
        GridFSFile archivo = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(id))));
        if (archivo == null) {
            return Optional.empty();
        }
        GridFsResource recurso = gridFsTemplate.getResource(archivo);
        try (InputStream entrada = recurso.getInputStream()) {
            return Optional.of(new ImagenAlmacenada(
                    entrada.readAllBytes(),
                    recurso.getContentType(),
                    archivo.getFilename()));
        } catch (IOException excepcion) {
            throw new IllegalStateException("No se ha podido leer la imagen almacenada", excepcion);
        }
    }

    @Override
    public void eliminar(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            return;
        }
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(id))));
    }
}
