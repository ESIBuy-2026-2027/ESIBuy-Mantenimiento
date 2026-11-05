package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import java.util.Base64;

import es.uclm.esibuy.backend.shared.infrastructure.seguridad.CifradorAesGcm;
import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;

// Cifra el secreto TOTP con AES-GCM; el algoritmo vive en CifradorAesGcm, compartido con metodospago
public class AesGcmCifradoSecretoAdapter implements CifradoSecretoPort {

    private final CifradorAesGcm cifrador;

    public AesGcmCifradoSecretoAdapter(String claveBase64) {
        this.cifrador = new CifradorAesGcm(Base64.getDecoder().decode(claveBase64));
    }

    @Override
    public String cifrar(String textoEnClaro) {
        return cifrador.cifrar(textoEnClaro);
    }

    @Override
    public String descifrar(String textoCifrado) {
        return cifrador.descifrar(textoCifrado);
    }
}
