package es.uclm.esibuy.backend.productos.application.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.application.port.out.ImagenStoragePort;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.exception.ImagenNoSeguraException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.infrastructure.imagen.ValidadorImagen;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Gestiona la carga, sustitución y consulta de imágenes binarias de productos. */
public class GestionImagenesProductoService {

    private static final Pattern REFERENCIA_INTERNA = Pattern.compile("^/api/product-images/([a-fA-F0-9]{24})$");

    private final ProductoRepositoryPort productos;
    private final ImagenStoragePort almacenamiento;
    private final AnalisisImagenPort antivirus;
    private final ValidadorImagen validador;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public GestionImagenesProductoService(ProductoRepositoryPort productos, ImagenStoragePort almacenamiento,
            AnalisisImagenPort antivirus, ValidadorImagen validador, UsuarioEnSesionPort usuarioEnSesion) {
        this.productos = productos;
        this.almacenamiento = almacenamiento;
        this.antivirus = antivirus;
        this.validador = validador;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    public String subirImagen(String emailVendedor, String productoId, byte[] contenido,
            String nombreArchivo, String tipoContenido) {
        String vendedorId = usuarioEnSesion.idPorCorreo(emailVendedor);
        Producto producto = productos.buscarPorId(productoId)
                .filter(p -> p.getVendedorId().equals(vendedorId))
                .orElseThrow(ProductoNoEncontradoException::new);

        validador.validar(contenido, nombreArchivo, tipoContenido);
        var resultado = antivirus.analizarArchivo(contenido, nombreArchivo, tipoContenido);
        if (resultado != es.uclm.esibuy.backend.productos.application.port.out.ResultadoAnalisis.SEGURA) {
            throw new ImagenNoSeguraException();
        }

        String idNuevo = almacenamiento.almacenar(contenido, nombreArchivo, tipoContenido);
        String imagenAnterior = producto.getImagen();
        try {
            Producto actualizado = producto.conImagen("/api/product-images/" + idNuevo);
            productos.guardar(actualizado);
        } catch (RuntimeException excepcion) {
            almacenamiento.eliminar(idNuevo);
            throw excepcion;
        }
        extraerId(imagenAnterior).ifPresent(almacenamiento::eliminar);
        return "/api/product-images/" + idNuevo;
    }

    public Optional<ImagenStoragePort.ImagenAlmacenada> obtener(String id) {
        return almacenamiento.obtener(id);
    }

    private Optional<String> extraerId(String referencia) {
        if (referencia == null) {
            return Optional.empty();
        }
        Matcher matcher = REFERENCIA_INTERNA.matcher(referencia);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
