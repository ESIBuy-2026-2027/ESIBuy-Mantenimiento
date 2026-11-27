package es.uclm.esibuy.backend.estadisticas.application.port.out;

/** Consulta el nombre comercial de un vendedor para mostrarlo en el ranking. */
public interface NombreComercialVendedorPort {

    String consultar(String vendedorId);
}
