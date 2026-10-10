package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoDocument.DescuentoDocument;

/**
 * Convierte entre la entidad de dominio y el documento de MongoDB. Al leer se vuelve a pasar por el
 * constructor de Producto, así que un documento que no cumpla las reglas del dominio se rechaza.
 */
public class ProductoDocumentMapper {

    public ProductoDocument aDocumento(Producto producto) {
        ProductoDocument documento = new ProductoDocument();
        documento.setId(producto.getId());
        documento.setVendedorId(producto.getVendedorId());
        documento.setNombre(producto.getNombre());
        documento.setDescripcion(producto.getDescripcion());
        documento.setPrecio(producto.getPrecio());
        documento.setStock(producto.getStock());
        documento.setCategoriaId(producto.getCategoriaId());
        documento.setImagen(producto.getImagen());
        documento.setDisponible(producto.isDisponible());
        documento.setDescuento(aDocumento(producto.getDescuento()));
        documento.setDescuentoPremium(aDocumento(producto.getDescuentoPremium()));
        return documento;
    }

    public Producto aDominio(ProductoDocument documento) {
        return new Producto(documento.getId(), documento.getVendedorId(), documento.getNombre(),
                documento.getDescripcion(), documento.getPrecio(), documento.getStock(),
                documento.getCategoriaId(), documento.getImagen(), documento.isDisponible(),
                aDominio(documento.getDescuento()), aDominio(documento.getDescuentoPremium()));
    }

    private static DescuentoDocument aDocumento(Descuento descuento) {
        DescuentoDocument documento = new DescuentoDocument();
        documento.setActivo(descuento.activo());
        documento.setPorcentaje(descuento.porcentaje());
        return documento;
    }

    // Un documento sin descuento lo rechaza el constructor de Producto (los dos son obligatorios).
    private static Descuento aDominio(DescuentoDocument documento) {
        return documento == null ? null : new Descuento(documento.isActivo(), documento.getPorcentaje());
    }
}
