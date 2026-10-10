package es.uclm.esibuy.backend.usuarios.application.port.out;

public interface CifradoSecretoPort {

    String cifrar(String textoEnClaro);

    String descifrar(String textoCifrado);
    
}
