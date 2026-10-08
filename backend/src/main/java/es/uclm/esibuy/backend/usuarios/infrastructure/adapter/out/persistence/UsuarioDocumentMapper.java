package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.DatosAdministrador;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosRol;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte el {@code Usuario} del dominio en el documento que se guarda en MongoDB.
 *
 * <p>Convierte en los dos sentidos: {@code aDocumento} para guardar y {@code aDominio} para leer.
 * Los datos personales van en la raíz del documento y los del rol, en su subdocumento
 * ({@code cliente}, {@code vendedor} o {@code administrador}).
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
        switch (usuario.getDatosRol()) {
            case DatosCliente cliente -> documento.setCliente(
                    new DatosClienteDocument(cliente.tipoCliente().name(), cliente.fechaNacimiento()));
            case DatosVendedor vendedor -> documento.setVendedor(
                    new DatosVendedorDocument(vendedor.nombreComercial(), vendedor.categoriaPrincipalId()));
            case DatosAdministrador administrador -> documento.setAdministrador(
                    new DatosAdministradorDocument(administrador.sede(), administrador.fechaIncorporacion()));
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

    private static DatosRol datosRol(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());
        return switch (rol) {
            case CLIENTE -> datosCliente(documento.getCliente());
            case VENDEDOR -> {
                DatosVendedorDocument vendedor = exigirSubdocumento(documento.getVendedor(), rol);
                yield new DatosVendedor(vendedor.nombreComercial(), vendedor.categoriaPrincipalId());
            }
            case ADMINISTRADOR -> {
                DatosAdministradorDocument administrador = exigirSubdocumento(documento.getAdministrador(), rol);
                yield new DatosAdministrador(administrador.sede(), administrador.fechaIncorporacion());
            }
        };
    }

    // Los clientes guardados antes de la T210 no tienen subdocumento cliente: se leen como ESTANDAR,
    // igual que Usuario.nuevoCliente.
    private static DatosCliente datosCliente(DatosClienteDocument cliente) {
        if (cliente == null) {
            return new DatosCliente(TipoCliente.ESTANDAR, null);
        }
        return new DatosCliente(TipoCliente.valueOf(cliente.tipoCliente()), cliente.fechaNacimiento());
    }

    // Vendedores y administradores no existían antes de la T210: sin sus datos de rol, el documento está mal.
    private static <T> T exigirSubdocumento(T subdocumento, Rol rol) {
        if (subdocumento == null) {
            throw new IllegalStateException("Usuario con rol " + rol + " guardado sin sus datos de rol");
        }
        return subdocumento;
    }
}
