package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test de integración del adaptador de MongoDB (HU1, AB#216; índice único, AB#210).
// Usa un MongoDB real en Docker (Testcontainers): Docker Desktop debe estar abierto.
// PENDIENTE DE REVISAR CON EL EQUIPO: IDs de caso propuestos.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class UsuarioMongoAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-10-08T12:00:00Z");

    @Autowired
    private UsuarioMongoRepository usuarioMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

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

    @Test
    @DisplayName("Persistencia: el documento guardado no incluye el campo _class")
    void elDocumentoNoIncluyeElCampoClass() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        assertFalse(documentoEnBruto().containsKey("_class"));
    }

    @Test
    @DisplayName("HU1-CP01: guardar conserva el nombre, los apellidos y el tipo de cliente")
    void conservaLosDatosDePerfil() {
        adapter.guardar(new Usuario(null, "Juan", "Pérez García", new Email("juan@uclm.es"),
                "hash-simulado", Rol.CLIENTE, TipoCliente.PREMIUM));

        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        assertEquals("Juan", leido.getNombre());
        assertEquals("Pérez García", leido.getApellidos());
        assertEquals(TipoCliente.PREMIUM, leido.getTipoCliente());
    }

    @Test
    @DisplayName("Persistencia: guarda createdAt y updatedAt, y createdAt no cambia al actualizar")
    void guardaFechasDeCreacionYModificacion() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));
        Date creado = documentoEnBruto().getDate("createdAt");
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();
        leido.registrarIntentoFallido(AHORA);

        adapter.guardar(leido);

        Document actualizado = documentoEnBruto();
        assertNotNull(creado);
        assertEquals(creado, actualizado.getDate("createdAt"));
        assertFalse(actualizado.getDate("updatedAt").before(creado));
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
    @DisplayName("HU2-CP01: buscarPorCorreo devuelve el usuario guardado con sus datos")
    void buscarPorCorreoDevuelveElUsuarioGuardado() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        Optional<Usuario> encontrado = adapter.buscarPorCorreo("juan@uclm.es");

        assertTrue(encontrado.isPresent());
        assertEquals("juan@uclm.es", encontrado.get().getEmail().valor());
        assertEquals("hash-simulado", encontrado.get().getPasswordHash());
        assertEquals(Rol.CLIENTE, encontrado.get().getRol());
    }

    @Test
    @DisplayName("HU2-CP02: buscarPorCorreo devuelve vacío si el email no está guardado")
    void buscarPorCorreoDevuelveVacioSiNoEstaGuardado() {
        assertTrue(adapter.buscarPorCorreo("nadie@uclm.es").isEmpty());
    }

    @Test
    @DisplayName("HU2-CP03: guardar un usuario leído lo actualiza en vez de duplicarlo")
    void guardarUnUsuarioLeidoLoActualizaSinDuplicarlo() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        adapter.guardar(leido);   // como hace el login tras un intento fallido

        assertEquals(1, usuarioMongoRepository.findAll().size());
    }

    @Test
    @DisplayName("HU2-CP04: un usuario desactivado sigue desactivado al leerlo de la base de datos")
    void conservaLaDesactivacion() {
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado");
        usuario.desactivar();

        adapter.guardar(usuario);

        assertFalse(adapter.buscarPorCorreo("juan@uclm.es").orElseThrow().estaActivo());
        assertEquals("DESACTIVADO", documentoEnBruto().getString("estado"));   // nombre del modelo de BBDD
    }

    @Test
    @DisplayName("HU2-CP04: un usuario activo sigue activo al leerlo de la base de datos")
    void conservaUnUsuarioActivo() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        assertTrue(adapter.buscarPorCorreo("juan@uclm.es").orElseThrow().estaActivo());
    }

    @Test
    @DisplayName("HU2-CP03: los intentos fallidos y el bloqueo se conservan entre un login y otro")
    void conservaLosIntentosFallidosYElBloqueo() {
        Instant ahora = Instant.parse("2026-10-08T10:00:00Z");
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado");
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(ahora);
        }

        adapter.guardar(usuario);
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        // Se compara el comportamiento y no el Instant exacto: MongoDB guarda las fechas en milisegundos.
        assertTrue(leido.tieneBloqueoPorIntentos(ahora));
        assertFalse(leido.tieneBloqueoPorIntentos(ahora.plus(Duration.ofMinutes(15))));
        assertEquals(5, documentoEnBruto().getInteger("failedLoginAttempts"));   // nombre del modelo de BBDD
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB; el repositorio lo convertiría
    // a UsuarioDocument y ahí no se verían campos como _class.
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection("usuarios").find().first();
        assertNotNull(guardado);
        return guardado;
    }
}
