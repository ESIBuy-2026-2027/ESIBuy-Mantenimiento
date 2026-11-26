package es.uclm.esibuy.backend.productos.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;

/**
 * Producto del catálogo de un vendedor. Java puro: las reglas de negocio se
 * validan aquí, no en el
 * controlador ni en la base de datos. El vendedor y la categoría se referencian
 * por identificador.
 */
public class Producto {

    private static final int NOMBRE_MAXIMO = 100;
    private static final int DESCRIPCION_MAXIMA = 1000;
    private static final Set<String> ESQUEMAS_IMAGEN = Set.of("http", "https");
    private static final BigDecimal CIEN = new BigDecimal("100");

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
    private final Double valoracionMedia;
    private final int numeroValoraciones;

    // Producto sin valoraciones: el alta, la modificación del vendedor y los tests
    public Producto(String id, DatosProducto datos) {
        DatosProducto.DatosBasicos basicos = datos.datosBasicos();
        DatosProducto.Configuracion configuracion = datos.configuracion();
        DatosProducto.Valoraciones valoraciones = datos.valoraciones();

        this.id = id;
        this.vendedorId = exigirTexto(basicos.vendedorId(), "El vendedor es obligatorio");
        this.nombre = exigirNombre(basicos.nombre());
        this.descripcion = exigirDescripcion(basicos.descripcion());
        this.precio = exigirPrecio(basicos.precio());
        this.stock = exigirStock(basicos.stock());
        this.categoriaId = exigirTexto(basicos.categoriaId(), "La categoría es obligatoria");
        this.imagen = exigirImagen(basicos.imagen());
        this.disponible = configuracion.disponible();
        this.descuento = exigirNoNulo(configuracion.descuento(), "El descuento es obligatorio");
        this.descuentoPremium = exigirNoNulo(
                configuracion.descuentoPremium(),
                "El descuento premium es obligatorio");
        this.valoracionMedia = valoraciones.valoracionMedia();
        this.numeroValoraciones = valoraciones.numeroValoraciones();
    }

    // Se guarda sin espacios en los extremos: así el índice único (vendedorId,
    // nombre) trata " Teclado" y
    // "Teclado" como el mismo nombre, igual que la comprobación previa (T258). El
    // límite cuenta sin ellos.
    private static String exigirNombre(String nombre) {
        String valor = exigirTexto(nombre, "El nombre es obligatorio").strip();
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

    // La imagen es opcional; si existe, solo se admiten direcciones http o https
    // (nunca javascript: ni data:).
    private static String exigirImagen(String imagen) {
        if (imagen == null || imagen.isBlank()) {
            return "";
        }
        String valor = imagen.trim();
        // Las imágenes subidas se sirven desde un endpoint interno y autenticado del catálogo.
        // El identificador es un ObjectId hexadecimal de MongoDB (24 caracteres).
        if (valor.matches("/api/product-images/[a-fA-F0-9]{24}")) {
            return valor;
        }
        String esquema;
        try {
            esquema = URI.create(valor).getScheme();
        } catch (IllegalArgumentException e) {
            throw new ProductoInvalidoException("La imagen debe ser una dirección web válida (http o https)");
        }
        if (esquema == null || !ESQUEMAS_IMAGEN.contains(esquema.toLowerCase(Locale.ROOT))) {
            throw new ProductoInvalidoException("La imagen debe ser una dirección web válida (http o https)");
        }
        return valor;
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

    public Double getValoracionMedia() {
        return valoracionMedia;
    }

    public int getNumeroValoraciones() {
        return numeroValoraciones;
    }

    /**
     * Descuento que se aplica al cliente (HU8): el premium si el cliente es premium
     * y lo tiene activo; si no, el
     * general. Puede ser un descuento inactivo (sin descuento).
     */
    public Descuento descuentoAplicado(boolean clientePremium) {
        return clientePremium && descuentoPremium.activo() ? descuentoPremium : descuento;
    }

    /**
     * Copia del producto con la disponibilidad indicada (HU16) y el resto de datos
     * iguales. El producto es
     * inmutable: activarlo o desactivarlo no lo cambia, sino que devuelve otro con
     * el mismo id.
     */
    public Producto conDisponibilidad(boolean nuevaDisponibilidad) {
        DatosProducto datos = new DatosProducto(
                new DatosProducto.DatosBasicos(
                        vendedorId,
                        nombre,
                        descripcion,
                        precio,
                        stock,
                        categoriaId,
                        imagen),
                new DatosProducto.Configuracion(
                        nuevaDisponibilidad,
                        descuento,
                        descuentoPremium),
                new DatosProducto.Valoraciones(
                        valoracionMedia,
                        numeroValoraciones));

        return new Producto(id, datos);
    }

    /** Si un cliente lo puede comprar: disponible y con stock (alertas de disponibilidad, AB#529). */
    public boolean esComprable() {
        return disponible && stock > 0;
    }

    /**
     * Copia del producto con {@code cantidad} unidades más de stock y el resto de datos iguales: cómo queda al
     * reponer el stock de un pedido cancelado (AB#529).
     */
    public Producto conStockRepuesto(int cantidad) {
        DatosProducto datos = new DatosProducto(
                new DatosProducto.DatosBasicos(
                        vendedorId, nombre, descripcion, precio, stock + cantidad, categoriaId, imagen),
                new DatosProducto.Configuracion(disponible, descuento, descuentoPremium),
                new DatosProducto.Valoraciones(valoracionMedia, numeroValoraciones));
        return new Producto(id, datos);
    }

    /**
     * Copia del producto sin imagen y con el resto de datos iguales (HU31, T397):
     * se usa cuando el antivirus no ha
     * podido analizar la imagen.
     */
    public Producto sinImagen() {
        DatosProducto datos = new DatosProducto(
                new DatosProducto.DatosBasicos(
                        vendedorId,
                        nombre,
                        descripcion,
                        precio,
                        stock,
                        categoriaId,
                        ""),
                new DatosProducto.Configuracion(
                        disponible,
                        descuento,
                        descuentoPremium),
                new DatosProducto.Valoraciones(
                        valoracionMedia,
                        numeroValoraciones));

        return new Producto(id, datos);
    }

    /** Copia inmutable del producto con una nueva referencia de imagen. */
    public Producto conImagen(String nuevaImagen) {
        DatosProducto datos = new DatosProducto(
                new DatosProducto.DatosBasicos(
                        vendedorId, nombre, descripcion, precio, stock, categoriaId, nuevaImagen),
                new DatosProducto.Configuracion(disponible, descuento, descuentoPremium),
                new DatosProducto.Valoraciones(valoracionMedia, numeroValoraciones));
        return new Producto(id, datos);
    }

    /**
     * Precio que paga el cliente (HU9): con el descuento premium si el cliente es
     * premium y lo tiene activo;
     * si no, con el general. Se redondea a céntimos, la mitad hacia arriba.
     */
    public BigDecimal precioFinal(boolean clientePremium) {
        Descuento aplicable = descuentoAplicado(clientePremium);
        if (!aplicable.activo()) {
            return precio;
        }
        BigDecimal pagado = CIEN.subtract(aplicable.porcentaje());
        return precio.multiply(pagado).divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}