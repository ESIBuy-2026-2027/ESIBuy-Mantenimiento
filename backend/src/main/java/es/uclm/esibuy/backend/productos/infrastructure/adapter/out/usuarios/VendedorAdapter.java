package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;

/**
 * Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada. Reutiliza
 * consultarVendedor (HU26): si el id no es de un vendedor, su excepción llega al manejador global como 404.
 * El usuario que devuelve no se usa, así que productos no depende del dominio de usuarios.
 */
public class VendedorAdapter implements VendedorPort {

    private final AdministracionUsuariosUseCase administracionUsuarios;

    public VendedorAdapter(AdministracionUsuariosUseCase administracionUsuarios) {
        this.administracionUsuarios = administracionUsuarios;
    }

    @Override
    public void comprobarQueEsVendedor(String vendedorId) {
        administracionUsuarios.consultarVendedor(vendedorId);
    }

    @Override
    public String nombreComercial(String vendedorId) {
        return administracionUsuarios.consultarNombreComercial(vendedorId);
    }
}
