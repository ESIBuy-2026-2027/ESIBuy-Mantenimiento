package es.uclm.esibuy.backend.alertas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import es.uclm.esibuy.backend.alertas.domain.exception.AlertaInvalidaException;

/**
 * Alerta que un cliente ve en su cuenta (AB#529). No se envía por correo: se guarda y el cliente la consulta al
 * iniciar sesión. Genérica para todas las historias de alertas: tipo, cliente, producto, fecha y si está leída
 * (AB#531), más los datos del producto en ese momento. Cliente y producto se referencian por identificador.
 */
public class Alerta {

    private final String id;
    private final String clienteId;
    private final String productoId;
    private final TipoAlerta tipo;
    private final Instant fecha;
    private final boolean leida;
    private final DetalleAlerta detalle;

    public Alerta(String id, String clienteId, String productoId, TipoAlerta tipo, Instant fecha, boolean leida,
            DetalleAlerta detalle) {
        this.id = id;
        this.clienteId = exigirTexto(clienteId, "El cliente es obligatorio");
        this.productoId = exigirTexto(productoId, "El producto es obligatorio");
        this.tipo = exigir(tipo, "El tipo es obligatorio");
        this.fecha = exigir(fecha, "La fecha es obligatoria");
        this.leida = leida;
        this.detalle = detalle == null ? DetalleAlerta.vacio() : detalle;
    }

    /** Solo hay bajada si el precio nuevo es menor que el anterior: igual o mayor no avisa. */
    public static boolean esBajadaDePrecio(BigDecimal precioAnterior, BigDecimal precioNuevo) {
        return precioAnterior != null && precioNuevo != null && precioNuevo.compareTo(precioAnterior) < 0;
    }

    /** El producto vuelve a estar disponible cuando pasa de no poder comprarse a poder comprarse. */
    public static boolean vuelveAEstarDisponible(boolean comprableAntes, boolean comprableDespues) {
        return !comprableAntes && comprableDespues;
    }

    public static Alerta bajadaDePrecio(String clienteId, String productoId, String nombreProducto,
            String imagenProducto, BigDecimal precioAnterior, BigDecimal precioNuevo, Instant fecha) {
        if (!esBajadaDePrecio(precioAnterior, precioNuevo)) {
            throw new AlertaInvalidaException("El precio nuevo debe ser menor que el anterior");
        }
        return new Alerta(null, clienteId, productoId, TipoAlerta.BAJADA_PRECIO, fecha, false,
                new DetalleAlerta(nombreProducto, imagenProducto, precioAnterior, precioNuevo));
    }

    public static Alerta disponibilidad(String clienteId, String productoId, String nombreProducto,
            String imagenProducto, Instant fecha) {
        return new Alerta(null, clienteId, productoId, TipoAlerta.DISPONIBILIDAD, fecha, false,
                new DetalleAlerta(nombreProducto, imagenProducto, null, null));
    }

    private static String exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new AlertaInvalidaException(mensaje);
        }
        return valor;
    }

    private static <T> T exigir(T valor, String mensaje) {
        if (valor == null) {
            throw new AlertaInvalidaException(mensaje);
        }
        return valor;
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getProductoId() {
        return productoId;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public Instant getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public DetalleAlerta getDetalle() {
        return detalle;
    }
}
