package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosAdministrador;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.time.LocalDate;
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
@Import({MongoTestcontainersConfig.class, MongoConfig.class})
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
    @DisplayName("Persistencia: la base de datos rechaza un DNI repetido en el mismo rol, sin confundirlo con un correo repetido")
    void rechazaDniDuplicadoEnElMismoRol() {
        // MVP del Sprint 1: el DNI es único. Una misma persona puede tener cuenta de cliente y de
        // vendedor con el mismo DNI (con otro correo), así que es único por rol.
        adapter.guardar(clienteConDni("juan@uclm.es", "12345678Z"));
        Usuario mismoDni = clienteConDni("otro@uclm.es", "12345678Z");

        assertThrows(DuplicateKeyException.class, () -> adapter.guardar(mismoDni));
        assertEquals(1, usuarioMongoRepository.count());
    }

    @Test
    @DisplayName("Persistencia: el mismo DNI se admite en roles distintos")
    void permiteMismoDniEnDistintoRol() {
        adapter.guardar(clienteConDni("juan@uclm.es", "12345678Z"));

        adapter.guardar(new Usuario(null, new DatosPersonales(null, null, "12345678Z", null),
                new Email("ventas@uclm.es"), "hash-simulado", new DatosVendedor("Electrónica La Mancha", "cat-1")));

        assertEquals(2, usuarioMongoRepository.count());
    }

    @Test
    @DisplayName("Persistencia: varios usuarios del mismo rol pueden no tener DNI")
    void permiteVariosUsuariosSinDni() {
        // El registro actual solo pide email y contraseña: el índice único no se aplica sin DNI.
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        adapter.guardar(Usuario.nuevoCliente(new Email("ana@uclm.es"), "hash-simulado"));

        assertEquals(2, usuarioMongoRepository.count());
    }

    @Test
    @DisplayName("Persistencia: la base de datos rechaza un nombre comercial repetido")
    void rechazaNombreComercialDuplicado() {
        // «Planteamiento inicial BBDD»: nombreComercial → único.
        adapter.guardar(vendedor("ventas@uclm.es", "Electrónica La Mancha"));
        Usuario mismoNombre = vendedor("otro@uclm.es", "Electrónica La Mancha");

        assertThrows(DuplicateKeyException.class, () -> adapter.guardar(mismoNombre));
        assertEquals(1, usuarioMongoRepository.count());
    }

    @Test
    @DisplayName("Persistencia: el documento guardado no incluye el campo _class")
    void elDocumentoNoIncluyeElCampoClass() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));

        assertFalse(documentoEnBruto().containsKey("_class"));
    }

    @Test
    @DisplayName("HU2-CP03: guardar un usuario existente lo actualiza con sus intentos fallidos")
    void guardarActualizaUnUsuarioExistente() {
        // Es lo que hace el login tras una contraseña incorrecta: leer, registrar el intento y guardar.
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();
        leido.registrarIntentoFallido(AHORA);

        adapter.guardar(leido);

        assertEquals(1, usuarioMongoRepository.count());
        assertEquals(1, documentoEnBruto().getInteger("failedLoginAttempts"));
    }

    @Test
    @DisplayName("HU2-CP03: el bloqueo por intentos fallidos se conserva al volver a leer el usuario")
    void conservaElBloqueoAlLeer() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();
        for (int intento = 0; intento < 5; intento++) {
            leido.registrarIntentoFallido(AHORA);
        }
        adapter.guardar(leido);

        Usuario releido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        assertEquals(5, releido.getIntentosFallidos());
        assertTrue(releido.tieneBloqueoPorIntentos(AHORA));
    }

    @Test
    @DisplayName("HU2-CP04: la desactivación de la cuenta se conserva al volver a leer el usuario")
    void conservaLaDesactivacionAlLeer() {
        adapter.guardar(Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-simulado"));
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();
        leido.desactivar();
        adapter.guardar(leido);

        Usuario releido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        assertTrue(releido.estaDesactivado());
        assertEquals("DESACTIVADO", documentoEnBruto().getString("estado"));
    }

    @Test
    @DisplayName("HU1-CP01: guardar conserva los datos personales y los de cliente, en su subdocumento")
    void conservaLosDatosDePerfil() {
        DatosPersonales datosPersonales = new DatosPersonales("Juan", "Pérez García", "12345678Z", "612345678");
        DatosCliente datosCliente = new DatosCliente(TipoCliente.PREMIUM, LocalDate.of(2001, 4, 12));
        adapter.guardar(new Usuario(null, datosPersonales, new Email("juan@uclm.es"), "hash-simulado",
                datosCliente));

        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        assertEquals(datosPersonales, leido.getDatosPersonales());
        assertEquals(datosCliente, leido.getDatosRol());
        // Forma del documento (opción B): lo común a varios roles en la raíz y lo del rol en su subdocumento.
        Document guardado = documentoEnBruto();
        assertEquals("12345678Z", guardado.getString("dni"));
        assertEquals("612345678", guardado.getString("telefono"));
        assertEquals("PREMIUM", guardado.get("cliente", Document.class).getString("tipoCliente"));
        assertFalse(guardado.containsKey("tipoCliente"));
    }

    @Test
    @DisplayName("Persistencia: guardar conserva los datos de un vendedor en su subdocumento")
    void conservaLosDatosDeVendedor() {
        DatosVendedor datosVendedor = new DatosVendedor("Electrónica La Mancha", "cat-1");
        adapter.guardar(new Usuario(null, new DatosPersonales("Pedro", "Gómez Sanz", "12345678Z", null),
                new Email("ventas@uclm.es"), "hash-simulado", datosVendedor));

        Usuario leido = adapter.buscarPorCorreo("ventas@uclm.es").orElseThrow();

        assertEquals(datosVendedor, leido.getDatosRol());
        assertEquals("Electrónica La Mancha",
                documentoEnBruto().get("vendedor", Document.class).getString("nombreComercial"));
    }

    @Test
    @DisplayName("Persistencia: guardar conserva los datos de un administrador en su subdocumento")
    void conservaLosDatosDeAdministrador() {
        DatosAdministrador datosAdministrador = new DatosAdministrador("Ciudad Real", LocalDate.of(2026, 9, 1));
        adapter.guardar(new Usuario(null, new DatosPersonales("Ana", "López Díaz", null, null),
                new Email("admin@uclm.es"), "hash-simulado", datosAdministrador));

        Usuario leido = adapter.buscarPorCorreo("admin@uclm.es").orElseThrow();

        assertEquals(datosAdministrador, leido.getDatosRol());
        assertEquals("Ciudad Real", documentoEnBruto().get("administrador", Document.class).getString("sede"));
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

    private static Usuario clienteConDni(String email, String dni) {
        return new Usuario(null, new DatosPersonales(null, null, dni, null), new Email(email), "hash-simulado",
                new DatosCliente(TipoCliente.ESTANDAR, null));
    }

    private static Usuario vendedor(String email, String nombreComercial) {
        return new Usuario(null, DatosPersonales.sinDatos(), new Email(email), "hash-simulado",
                new DatosVendedor(nombreComercial, "cat-1"));
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB; el repositorio lo convertiría
    // a UsuarioDocument y ahí no se verían campos como _class.
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection("usuarios").find().first();
        assertNotNull(guardado);
        return guardado;
    }
}
