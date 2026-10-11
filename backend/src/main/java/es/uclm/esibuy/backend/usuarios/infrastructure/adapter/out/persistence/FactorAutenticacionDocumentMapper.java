package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;

public class FactorAutenticacionDocumentMapper {

    public FactorAutenticacionDocument aDocumento(FactorAutenticacion factor) {
        FactorAutenticacionDocument documento =
                new FactorAutenticacionDocument(factor.getUsuarioId(), factor.getSecretoCifrado());
        documento.setId(factor.getId());
        documento.setTotpEnabled(factor.estaConfirmado());
        documento.setVerifiedAt(factor.getVerificadoEn());
        documento.setLastUsedStep(factor.getUltimoPeriodoUsado());
        return documento;
    }

    public FactorAutenticacion aDominio(FactorAutenticacionDocument documento) {
        return FactorAutenticacion.reconstruir(documento.getId(), documento.getUsuarioId(),
                documento.getTotpSecretEncrypted(), documento.isTotpEnabled(), documento.getVerifiedAt(),
                documento.getLastUsedStep());
    }
}