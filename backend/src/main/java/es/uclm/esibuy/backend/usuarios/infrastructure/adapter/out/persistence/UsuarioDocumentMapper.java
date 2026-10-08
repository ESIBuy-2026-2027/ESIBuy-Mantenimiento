package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte el {@code Usuario} del dominio en el documento que se guarda en MongoDB.
 *
 * <p>Convierte en los dos sentidos: {@code aDocumento} para guardar y {@code aDominio} para leer.
 */
public class UsuarioDocumentMapper {

    public UsuarioDocument aDocumento(Usuario usuario) {
        UsuarioDocument documento = new UsuarioDocument(usuario.getEmail().valor(), usuario.getPasswordHash(), usuario.getRol().name());
        documento.setId(usuario.getId());
        documento.setActivo(usuario.estaActivo());
        documento.setIntentosFallidos(usuario.getIntentosFallidos());
        documento.setBloqueadoHasta(usuario.getBloqueadoHasta());
        documento.setNombre(usuario.getNombre());
        documento.setApellidos(usuario.getApellidos());
        documento.setTipoCliente(usuario.getTipoCliente() == null ? null : usuario.getTipoCliente().name());
        return documento;
    }

    public Usuario aDominio(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());

        // Tipo de cliente: el guardado si existe; si no (documento anterior a la T225), ESTANDAR para
        // los clientes, igual que Usuario.nuevoCliente. Vendedores y administradores no tienen tipo.
        TipoCliente tipoCliente = documento.getTipoCliente() != null
                ? TipoCliente.valueOf(documento.getTipoCliente())
                : (rol == Rol.CLIENTE ? TipoCliente.ESTANDAR : null);
        Usuario usuario = new Usuario(documento.getId(), documento.getNombre(), documento.getApellidos(),
                new Email(documento.getEmail()), documento.getPasswordHash(), rol, tipoCliente);

        // Solo se desactiva si el dato es explícitamente false; null (documento antiguo) = activo.
        if (Boolean.FALSE.equals(documento.getActivo())) {
            usuario.desactivar();
        }
        usuario.restaurarBloqueoPorIntentos(documento.getIntentosFallidos(), documento.getBloqueadoHasta());
        return usuario;
    }
}
