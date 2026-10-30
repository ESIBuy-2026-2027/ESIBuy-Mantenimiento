package es.uclm.esibuy.backend.shared.seguridad;

/** Expresiones de seguridad para @PreAuthorize: así no se repite el texto en cada servicio. */
public final class Permisos {

        public static final String PUBLICO = "permitAll()";
        public static final String SOLO_ADMINISTRADOR = "hasRole('ADMINISTRADOR')";
        public static final String SOLO_VENDEDOR = "hasRole('VENDEDOR')";
        public static final String SOLO_CLIENTE = "hasRole('CLIENTE')";
        public static final String CLIENTE_O_VENDEDOR = "hasAnyRole('CLIENTE', 'VENDEDOR')";
        public static final String SOLO_VENDEDOR_PROPIO = SOLO_VENDEDOR + " and #emailVendedor == authentication.name";
        public static final String SOLO_VENDEDOR_PROPIO_EN_COMANDO =
                SOLO_VENDEDOR + " and #comando.emailVendedor() == authentication.name";
        public static final String SOLO_CLIENTE_PROPIO = SOLO_CLIENTE + " and #emailCliente == authentication.name";
        public static final String SOLO_CLIENTE_PROPIO_EN_COMANDO =
                SOLO_CLIENTE + " and #comando.emailCliente() == authentication.name";
        public static final String SOLO_ADMINISTRADOR_PROPIO =
                SOLO_ADMINISTRADOR + " and #emailAdministrador == authentication.name";
        
        private Permisos() {
        }
    
}
