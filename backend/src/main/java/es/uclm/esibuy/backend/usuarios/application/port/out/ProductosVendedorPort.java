package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.List;

/** Lo que usuarios necesita del módulo de productos para eliminar una cuenta de vendedor. */
public interface ProductosVendedorPort {

    List<String> desactivarTodos(String vendedorId);

    void eliminar(String productoId);
}
