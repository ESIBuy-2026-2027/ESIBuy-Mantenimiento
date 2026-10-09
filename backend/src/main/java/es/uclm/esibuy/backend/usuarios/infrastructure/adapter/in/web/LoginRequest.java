package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

/**
 * LoginRequest
 */
public record LoginRequest(String email, String password) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=****]";
    }

    
}
