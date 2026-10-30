package es.uclm.esibuy.backend.productos.application.port.out;

/**
 * Lo que productos necesita del módulo de usuarios para la supervisión de vendedores (HU26): saber si un id
 * es de un vendedor antes de enseñar sus productos.
 */
public interface VendedorPort {

    // Falla si el id no existe o no es de un vendedor (el módulo de usuarios responde con 404).
    void comprobarQueEsVendedor(String vendedorId);

    // Nombre comercial del vendedor (HU8), para mostrarlo en el detalle de sus productos.
    String nombreComercial(String vendedorId);
}
