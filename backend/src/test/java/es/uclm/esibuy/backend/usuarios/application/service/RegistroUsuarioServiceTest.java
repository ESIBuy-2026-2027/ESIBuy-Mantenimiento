package es.uclm.esibuy.backend.usuarios.application.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// PENDIENTE DE DECIDIR (equipo): paquete raíz es.uclm.esibuy.backend.usuarios
// o es.uclm.esibuy.usuarios (el CLAUDE.md se contradice).
class RegistroUsuarioServiceTest {

    @Test
    void registraClienteConRolCliente() {
        // HU1-CP01
        // TODO: registrar un usuario con datos válidos.
        // TODO: comprobar que el usuario guardado tiene rol CLIENTE.
        // PENDIENTE DE REVISAR CON EL EQUIPO: ¿también estado PENDIENTE?
        fail("Funcionalidad no implementada");
    }

    @Test
    void guardaContrasenaHasheada() {
        // HU1-CP02
        // TODO: registrar un usuario con contraseña "clave1234".
        // TODO: comprobar que se guarda el hash devuelto por el puerto de hash
        //       y nunca la contraseña en claro.
        fail("Funcionalidad no implementada");
    }

    @Test
    void permiteContrasenaDeExactamente8Caracteres() {
        // HU1-CP05 (frontera: 8 caracteres → permitido)
        // TODO: registrar un usuario con contraseña de 8 caracteres.
        // TODO: comprobar que el usuario se guarda.
        fail("Funcionalidad no implementada");
    }

    @Test
    void rechazaContrasenaDeMenosDe8Caracteres() {
        // HU1-CP05 (frontera: 7 caracteres → excepción)
        // TODO: intentar registrar con contraseña de 7 caracteres.
        // TODO: comprobar que lanza una excepción de negocio y no guarda nada.
        fail("Funcionalidad no implementada");
    }

    @Test
    void rechazaEmailYaRegistrado() {
        // HU1-CP03
        // TODO: simular que el email ya existe en el repositorio.
        // TODO: comprobar que lanza una excepción de negocio y no guarda nada.
        fail("Funcionalidad no implementada");
    }
}