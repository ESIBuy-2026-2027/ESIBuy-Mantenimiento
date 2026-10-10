package es.uclm.esibuy.backend.productos.domain.model;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;

/**
 * Producto del catálogo de un vendedor. Java puro: las reglas de negocio se validan aquí, no en el
 * controlador ni en la base de datos. El vendedor y la categoría se referencian por identificador.
 */
public class Producto {

    private static final int NOMBRE_MAXIMO = 100;
    private static final int DESCRIPCION_MAXIMA = 1000;
    private static final Set<String> ESQUEMAS_IMAGEN = Set.of("http", "https");

    private final String id;
    private final String vendedorId;
    private final String nombre;
    private final String descripcion;
    private final BigDecimal precio;
    private final int stock;
    private final String categoriaId;
    private final String imagen;
    private final boolean disponible;
    private final Descuento descuento;
    private final Descuento descuentoPremium;

    public Producto(String id, String vendedorId, String nombre, String descripcion, BigDecimal precio,
            int stock, String categoriaId, String imagen, boolean disponible, Descuento descuento,
            Descuento descuentoPremium) {
        this.id = id;
        this.vendedorId = exigirTexto(vendedorId, "El vendedor es obligatorio");
        this.nombre = exigirNombre(nombre);
        this.descripcion = exigirDescripcion(descripcion);
        this.precio = exigirPrecio(precio);
        this.stock = exigirStock(stock);
        this.categoriaId = exigirTexto(categoriaId, "La categoría es obligatoria");
        this.imagen = exigirImagen(imagen);
        this.disponible = disponible;
        this.descuento = exigirNoNulo(descuento, "El descuento es obligatorio");
        this.descuentoPremium = exigirNoNulo(descuentoPremium, "El descuento premium es obligatorio");
    }

    private static String exigirNombre(String nombre) {
        String valor = exigirTexto(nombre, "El nombre es obligatorio");
        if (valor.length() > NOMBRE_MAXIMO) {
            throw new ProductoInvalidoException("El nombre no puede superar los " + NOMBRE_MAXIMO + " caracteres");
        }
        return valor;
    }

    private static String exigirDescripcion(String descripcion) {
        if (descripcion == null) {
            return "";
        }
        if (descripcion.length() > DESCRIPCION_MAXIMA) {
            throw new ProductoInvalidoException(
                    "La descripción no puede superar los " + DESCRIPCION_MAXIMA + " caracteres");
        }
        return descripcion;
    }

    private static BigDecimal exigirPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() <= 0) {
            throw new ProductoInvalidoException("El precio debe ser mayor que 0");
        }
        return precio;
    }

    private static int exigirStock(int stock) {
        if (stock < 0) {
            throw new ProductoInvalidoException("El stock no puede ser negativo");
        }
        return stock;
    }

    // La imagen es opcional; si existe, solo se admiten direcciones http o https (nunca javascript: ni data:).
    private static String exigirImagen(String imagen) {
        if (imagen == null || imagen.isBlank()) {
            return "";
        }
        String esquema;
        try {
            esquema = URI.create(imagen.trim()).getScheme();
        } catch (IllegalArgumentException e) {
            throw new ProductoInvalidoException("La imagen debe ser una dirección web válida (http o https)");
        }
        if (esquema == null || !ESQUEMAS_IMAGEN.contains(esquema.toLowerCase(Locale.ROOT))) {
            throw new ProductoInvalidoException("La imagen debe ser una dirección web válida (http o https)");
        }
        return imagen.trim();
    }

    private static String exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ProductoInvalidoException(mensaje);
        }
        return valor;
    }

    private static <T> T exigirNoNulo(T valor, String mensaje) {
        if (valor == null) {
            throw new ProductoInvalidoException(mensaje);
        }
        return valor;
    }

    public String getId() {
        return id;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public String getImagen() {
        return imagen;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public Descuento getDescuentoPremium() {
        return descuentoPremium;
    }
}