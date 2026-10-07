package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Test de integración del adaptador de MongoDB (HU1, AB#216).
// Usa un MongoDB real en Docker (Testcontainers): Docker Desktop debe estar abierto.
// PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
class UsuarioMongoAdapterTest {

    @Test
    @DisplayName("HU1-CP01: guardar persiste el usuario en la colección usuarios")
    void guardarPersisteElUsuarioEnLaColeccionUsuarios() {
        // TODO guardar(usuario) -> la colección tiene 1 documento con su email, hash y rol
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP03: existePorCorreo devuelve true si el email ya está guardado")
    void existePorCorreoDevuelveTrueSiEstaGuardado() {
        // TODO guardar(usuario) y existePorCorreo("juan@uclm.es") -> true
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP03: existePorCorreo devuelve false si el email no está guardado")
    void existePorCorreoDevuelveFalseSiNoEstaGuardado() {
        // TODO colección vacía y existePorCorreo("juan@uclm.es") -> false
        fail("Funcionalidad no implementada");
    }
}
