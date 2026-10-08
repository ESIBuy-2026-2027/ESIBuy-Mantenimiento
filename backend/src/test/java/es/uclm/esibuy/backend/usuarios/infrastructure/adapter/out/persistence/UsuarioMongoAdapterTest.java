package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test de integración del adaptador de MongoDB (HU1, AB#216; índice único, AB#210).
// Usa un MongoDB real en Docker (Testcontainers): Docker Desktop debe estar abierto.
// PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
@DataMongoTest
@Import(MongoTestcontainersConfig.class)
class UsuarioMongoAdapterTest {

    @Autowired
    private UsuarioMongoRepository usuarioMongoRepository;

    private UsuarioMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        usuarioMongoRepository.deleteAll();
        adapter = new UsuarioMongoAdapter(usuarioMongoRepository, new UsuarioDocumentMapper());
    }

    @Test
    @DisplayName("HU1-CP01: guardar persiste el usuario en la colección usuarios")
    void guardarPersisteElUsuarioEnLaColeccionUsuarios() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        List<UsuarioDocument> guardados = usuarioMongoRepository.findAll();
        assertEquals(1, guardados.size());
        assertEquals("juan@uclm.es", guardados.get(0).getEmail());
        assertEquals("hash-simulado", guardados.get(0).getPasswordHash());
        assertEquals("CLIENTE", guardados.get(0).getRol());
    }

    @Test
    @DisplayName("HU1-CP03: existePorCorreo devuelve true si el email ya está guardado")
    void existePorCorreoDevuelveTrueSiEstaGuardado() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        assertTrue(adapter.existePorCorreo("juan@uclm.es"));
    }

    @Test
    @DisplayName("HU1-CP03: existePorCorreo devuelve false si el email no está guardado")
    void existePorCorreoDevuelveFalseSiNoEstaGuardado() {
        assertFalse(adapter.existePorCorreo("juan@uclm.es"));
    }

    @Test
    @DisplayName("HU1-CP03: la base de datos rechaza un segundo usuario con el mismo email")
    void rechazaEmailDuplicadoEnLaBaseDeDatos() {
        // Llama a guardar dos veces sin pasar por existePorCorreo: simula dos registros simultáneos,
        // que solo el índice único de email (AB#210) puede parar. El adaptador traduce el rechazo
        // de MongoDB a la excepción del dominio, que la API devuelve como 409.
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-1"));
        Usuario duplicado = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-2");

        assertThrows(CorreoYaRegistradoException.class, () -> adapter.guardar(duplicado));
        assertEquals(1, usuarioMongoRepository.count());
    }
}
