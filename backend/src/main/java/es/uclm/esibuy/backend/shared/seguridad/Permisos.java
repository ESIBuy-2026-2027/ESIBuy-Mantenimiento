package es.uclm.esibuy.backend.shared.seguridad;

/** Expresiones de seguridad para @PreAuthorize: así no se repite el texto en cada servicio. */
public final class Permisos {

    public static final String PUBLICO = "permitAll()";
    public static final String SOLO_ADMINISTRADOR = "hasRole('ADMINISTRADOR')";
    public static final String SOLO_VENDEDOR = "hasRole('VENDEDOR')";
    public static final String SOLO_CLIENTE = "hasRole('CLIENTE')";
    public static final String CLIENTE_O_VENDEDOR = "hasAnyRole('CLIENTE', 'VENDEDOR')";

    private Permisos() {
    }
}
