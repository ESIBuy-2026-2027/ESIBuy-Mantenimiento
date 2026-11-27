package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.estadisticas.application.port.out.NombreComercialVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;

/** Adaptador de salida hacia usuarios para resolver el nombre comercial de un vendedor. */
public class NombreComercialVendedorAdapter implements NombreComercialVendedorPort {

    private final AdministracionUsuariosUseCase administracionUsuarios;

    public NombreComercialVendedorAdapter(AdministracionUsuariosUseCase administracionUsuarios) {
        this.administracionUsuarios = administracionUsuarios;
    }

    @Override
    public String consultar(String vendedorId) {
        DatosVendedor datos = administracionUsuarios.consultarVendedor(vendedorId).getDatosVendedor();
        if (datos == null) {
            throw new IllegalStateException("El vendedor no tiene nombre comercial");
        }
        return datos.nombreComercial();
    }
}
