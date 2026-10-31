package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;

/**
 * Convierte el {@code CodigoVerificacion} del dominio en el documento que se guarda en MongoDB
 * ({@code tokens_seguridad}, T232), en los dos sentidos: {@code aDocumento} para guardar y
 * {@code aDominio} para leer.
 */
public class CodigoVerificacionDocumentMapper {

    // Propósito de los códigos del segundo factor del login en tokens_seguridad (decisión D4 de la T21).
    static final String PROPOSITO_LOGIN = "LOGIN_2FA";
    // Propósito de los códigos de recuperación de contraseña (HU3). Valor admitido por tokens_seguridad.
    static final String PROPOSITO_RECUPERACION = "PASSWORD_RECOVERY";

    // El login sigue usando este: sin propósito, el código es del segundo factor.
    public CodigoVerificacionDocument aDocumento(CodigoVerificacion codigo) {
        return aDocumento(codigo, PROPOSITO_LOGIN);
    }

    public CodigoVerificacionDocument aDocumento(CodigoVerificacion codigo, String proposito) {
        CodigoVerificacionDocument documento = new CodigoVerificacionDocument(codigo.getUsuarioId(),
                codigo.getCodigoHash(), proposito, codigo.getExpiraEn());
        documento.setId(codigo.getId());
        documento.setAttempts(codigo.getIntentosFallidos());
        documento.setUsedAt(codigo.getUsadoEn());
        return documento;
    }

    public CodigoVerificacion aDominio(CodigoVerificacionDocument documento) {
        return CodigoVerificacion.reconstruir(documento.getId(), documento.getUsuarioId(), documento.getTokenHash(),
                documento.getExpiresAt(), documento.getUsedAt(), documento.getAttempts());
    }
}
