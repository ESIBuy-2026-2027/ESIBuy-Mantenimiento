package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

public record VerificarCodigoRequest(String code) {

    @Override
    public String toString() {
       return "VerificarCodigoRequest[code=****]";
    }
    
}
