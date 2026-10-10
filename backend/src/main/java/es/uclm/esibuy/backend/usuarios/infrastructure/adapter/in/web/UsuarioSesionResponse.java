package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public record UsuarioSesionResponse(String id, String nombre, String apellidos, String email, String rol, String estado) {

    public static UsuarioSesionResponse desde(Usuario usuario) {
        return new UsuarioSesionResponse(usuario.getId(), usuario.getDatosPersonales().nombre(), usuario.getDatosPersonales().apellidos(), usuario.getEmail().valor(), usuario.getRol().name(), usuario.getEstado());
    }
}
