package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import java.security.SecureRandom;

import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;

/**
 * Genera el código de 6 dígitos del segundo factor del login (HU2, T231).
 *
 * <p>Recibe la fuente aleatoria por constructor en vez de crearla dentro (T231, D5): así los tests
 * pueden pasarle una que devuelva siempre el mismo número y probar los casos frontera, igual que
 * {@code AutenticacionService} recibe un {@code Clock} para poder fijar la hora.
 *
 * <p>El parámetro es {@link SecureRandom} y no {@code Random}: {@code Random} es predecible y un
 * atacante que viera varios códigos podría adivinar los siguientes.
 */

public class SecureRandomGeneradorCodigoAdapter implements GeneradorCodigoPort {

    private static final int LIMITE_CODIGO = 1_000_000;

    // SecureRandom y no Math.random() ni Random: esos dos son generadores pseudoaleatorios predecibles.
    // Con unos pocos códigos observados se puede calcular su estado interno y adivinar los siguientes.
    // Para un factor de autenticación hace falta un generador criptográficamente seguro (Sonar java:S2245).
    private final SecureRandom random;

    public SecureRandomGeneradorCodigoAdapter(SecureRandom random) {
        this.random = random;
    }

    @Override
    public String generar() {
         return String.format("%06d", random.nextInt(LIMITE_CODIGO));
    }
    

}
