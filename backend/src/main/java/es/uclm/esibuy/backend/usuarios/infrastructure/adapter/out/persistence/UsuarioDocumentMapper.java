package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosRol;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte el {@code Usuario} del dominio en el documento que se guarda en MongoDB.
 *
 * <p>Convierte en los dos sentidos: {@code aDocumento} para guardar y {@code aDominio} para leer.
 * Los datos personales van en la raíz del documento y los del rol, en su subdocumento.
 */
public class UsuarioDocumentMapper {

    // Valores del campo "estado" del «Planteamiento inicial BBDD»; en el dominio, Usuario.estaDesactivado().
    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_DESACTIVADO = "DESACTIVADO";

    public UsuarioDocument aDocumento(Usuario usuario) {
        UsuarioDocument documento = new UsuarioDocument(usuario.getEmail().valor(), usuario.getPasswordHash(),
                usuario.getRol().name());
        DatosPersonales datosPersonales = usuario.getDatosPersonales();
        documento.setId(usuario.getId());
        documento.setNombre(datosPersonales.nombre());
        documento.setApellidos(datosPersonales.apellidos());
        documento.setDni(datosPersonales.dni());
        documento.setTelefono(datosPersonales.telefono());
        if (usuario.getDatosRol() instanceof DatosCliente cliente) {
            documento.setCliente(new DatosClienteDocument(cliente.tipoCliente().name(), cliente.fechaNacimiento()));
        }
        documento.setEstado(usuario.estaDesactivado() ? ESTADO_DESACTIVADO : ESTADO_ACTIVO);
        documento.setFailedLoginAttempts(usuario.getIntentosFallidos());
        documento.setLockedUntil(usuario.getBloqueadoHasta());
        return documento;
    }

    public Usuario aDominio(UsuarioDocument documento) {
        DatosPersonales datosPersonales = new DatosPersonales(documento.getNombre(), documento.getApellidos(),
                documento.getDni(), documento.getTelefono());
        Usuario datos = new Usuario(documento.getId(), datosPersonales, new Email(documento.getEmail()),
                documento.getPasswordHash(), datosRol(documento));
        boolean desactivado = ESTADO_DESACTIVADO.equals(documento.getEstado());
        return Usuario.reconstruir(datos, desactivado, documento.getFailedLoginAttempts(), documento.getLockedUntil());
    }

    // Vendedores y administradores aún no guardan sus datos de rol.
    private static DatosRol datosRol(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());
        if (rol != Rol.CLIENTE) {
            throw new IllegalStateException("Los datos de rol de " + rol + " todavía no se guardan");
        }
        return datosCliente(documento.getCliente());
    }

    // Los clientes guardados antes de la T210 no tienen subdocumento cliente: se leen como ESTANDAR,
    // igual que Usuario.nuevoCliente.
    private static DatosCliente datosCliente(DatosClienteDocument cliente) {
        if (cliente == null) {
            return new DatosCliente(TipoCliente.ESTANDAR, null);
        }
        return new DatosCliente(TipoCliente.valueOf(cliente.tipoCliente()), cliente.fechaNacimiento());
    }
}
