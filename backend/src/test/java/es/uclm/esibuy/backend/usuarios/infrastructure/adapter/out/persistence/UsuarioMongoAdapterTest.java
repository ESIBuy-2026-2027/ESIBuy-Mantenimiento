package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import java.time.LocalDate;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
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
        adapter = new UsuarioMongoAdapter(
                usuarioMongoRepository,
                new UsuarioDocumentMapper(),
                mongoTemplate
        );
    }

    @Test
    @DisplayName("HU1-CP01: guardar persiste el usuario en la colección usuarios")
    void guardarPersisteElUsuarioEnLaColeccionUsuarios() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));

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
        adapter.guardar(cliente("juan@uclm.es", "hash-1"));
        Usuario duplicado = cliente("juan@uclm.es", "hash-2", "87654321X");   // mismo email, distinto DNI

        assertThrows(CorreoYaRegistradoException.class, () -> adapter.guardar(duplicado));
        assertEquals(1, usuarioMongoRepository.count());
    }

    @Test
    @DisplayName("HU1-CP16: la base de datos rechaza un segundo cliente con el mismo DNI y lo distingue del email repetido")
    void rechazaDniDuplicadoEnLaBaseDeDatos() {
        // Dos emails distintos y el mismo DNI: solo el índice único rol_dni (AB#210) lo para. El adaptador
        // tiene que distinguir qué índice ha fallado para no confundirlo con un correo repetido.
        adapter.guardar(cliente("juan@uclm.es", "hash-1"));
        Usuario mismoDni = cliente("otro@uclm.es", "hash-2");

        assertThrows(DniYaRegistradoException.class, () -> adapter.guardar(mismoDni));
        assertEquals(1, usuarioMongoRepository.count());
    }

    @Test
    @DisplayName("HU1-CP16: existePorDni devuelve true si el DNI ya está guardado y false si no")
    void existePorDniDistingueLosDniGuardados() {
        adapter.guardar(cliente("juan@uclm.es", "hash-1"));

        assertTrue(adapter.existePorDni("12345678Z"));
        assertFalse(adapter.existePorDni("87654321X"));
    }

    @Test
    @DisplayName("Persistencia: el documento guardado no incluye el campo _class")
    void elDocumentoNoIncluyeElCampoClass() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));

        assertFalse(documentoEnBruto().containsKey("_class"));
    }

    @Test
    @DisplayName("HU1-CP01: guardar conserva los datos personales, los de cliente y el consentimiento")
    void conservaLosDatosDePerfil() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));

        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        assertEquals(DATOS_PERSONALES, leido.getDatosPersonales());
        assertEquals(DATOS_CLIENTE, leido.getDatosCliente());
        assertEquals(CONSENTIMIENTO, leido.getConsentimiento());
    }

    @Test
    @DisplayName("Persistencia: guarda createdAt y updatedAt, y createdAt no cambia al actualizar")
    void guardaFechasDeCreacionYModificacion() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));
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
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));

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
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));

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
    @DisplayName("HU24-CP01: buscarPorId devuelve el usuario guardado")
    void buscarPorIdDevuelveElUsuarioGuardado() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));
        String id = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow().getId();

        Usuario encontrado = adapter.buscarPorId(id).orElseThrow();

        assertEquals(id, encontrado.getId());
        assertEquals("juan@uclm.es", encontrado.getEmail().valor());
    }

    @Test
    @DisplayName("HU24-CP03: buscarPorId devuelve vacío si el usuario no existe")
    void buscarPorIdDevuelveVacioSiNoExiste() {
        assertTrue(adapter.buscarPorId("000000000000000000000000").isEmpty());
    }

    @Test
    @DisplayName("HU22-CP01: guardar un usuario editado actualiza su documento sin crear otro")
    void guardarUnUsuarioEditadoActualizaSuDocumento() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));
        Usuario guardado = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();
        DatosCliente nuevosDatosCliente = new DatosCliente(TipoCliente.ESTANDAR, new Dni("10004119A"),
                LocalDate.of(1978, 4, 13), new Telefono("620023757"));

        guardado.cambiarRol(Rol.CLIENTE, new DatosPersonales("Carla", "Moreno Gil", "avatar1"), nuevosDatosCliente, HOY);
        adapter.guardar(guardado);

        assertEquals(1, usuarioMongoRepository.count());
        Usuario leido = adapter.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals(new DatosPersonales("Carla", "Moreno Gil", "avatar1"), leido.getDatosPersonales());
        assertEquals(nuevosDatosCliente, leido.getDatosCliente());
        assertEquals("juan@uclm.es", leido.getEmail().valor());
    }

    @Test
    @DisplayName("HU22-CP09: un cliente que pasa a administrador ya no tiene sus datos de cliente en la base de datos")
    void unClienteQuePasaAAdministradorPierdeSusDatosDeClienteEnLaBaseDeDatos() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));
        Usuario guardado = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        guardado.cambiarRol(Rol.ADMINISTRADOR, guardado.getDatosPersonales(), null, HOY);
        adapter.guardar(guardado);

        Document enBruto = documentoEnBruto();
        assertEquals("ADMINISTRADOR", enBruto.getString("rol"));
        assertFalse(enBruto.containsKey("dni"));
        assertFalse(enBruto.containsKey("fechaNacimiento"));
        assertFalse(enBruto.containsKey("telefono"));
        assertFalse(enBruto.containsKey("tipoCliente"));
        assertEquals("juan@uclm.es", enBruto.getString("email"));
    }

    @Test
    @DisplayName("HU22-CP10: una cuenta pendiente que pasa a administrador queda activa en la base de datos")
    void unaCuentaPendienteQuePasaAAdministradorQuedaActivaEnLaBaseDeDatos() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));   // nace PENDIENTE
        Usuario guardado = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        guardado.cambiarRol(Rol.ADMINISTRADOR, guardado.getDatosPersonales(), null, HOY);
        adapter.guardar(guardado);

        assertEquals("ACTIVO", documentoEnBruto().getString("estado"));
        assertTrue(adapter.buscarPorId(guardado.getId()).orElseThrow().estaActivo());
    }

    @Test
    @DisplayName("HU22-CP06: la base de datos rechaza editar un cliente con el DNI de otro cliente")
    void rechazaEditarUnClienteConElDniDeOtroEnLaBaseDeDatos() {
        // Sin pasar por existePorDni (p. ej. dos ediciones simultáneas): solo el índice rol_dni lo para.
        adapter.guardar(cliente("juan@uclm.es", "hash-1", "12345678Z"));
        adapter.guardar(cliente("ana@uclm.es", "hash-2", "87654321X"));
        Usuario ana = adapter.buscarPorCorreo("ana@uclm.es").orElseThrow();
        DatosCliente conDniDeJuan = new DatosCliente(TipoCliente.PREMIUM, new Dni("12345678Z"),
                LocalDate.of(1996, 5, 20), null);

        ana.cambiarRol(Rol.CLIENTE, ana.getDatosPersonales(), conDniDeJuan, HOY);

        assertThrows(DniYaRegistradoException.class, () -> adapter.guardar(ana));
        assertEquals("87654321X", adapter.buscarPorCorreo("ana@uclm.es").orElseThrow().getDatosCliente().dni().valor());
    }

    @Test
    @DisplayName("HU2-CP03: guardar un usuario leído lo actualiza en vez de duplicarlo")
    void guardarUnUsuarioLeidoLoActualizaSinDuplicarlo() {
        adapter.guardar(cliente("juan@uclm.es", "hash-simulado"));
        Usuario leido = adapter.buscarPorCorreo("juan@uclm.es").orElseThrow();

        adapter.guardar(leido);   // como hace el login tras un intento fallido

        assertEquals(1, usuarioMongoRepository.findAll().size());
    }

    @Test
    @DisplayName("HU2-CP04: un usuario desactivado sigue desactivado al leerlo de la base de datos")
    void conservaLaDesactivacion() {
        Usuario usuario = cliente("juan@uclm.es", "hash-simulado");
        usuario.desactivar();

        adapter.guardar(usuario);

        assertFalse(adapter.buscarPorCorreo("juan@uclm.es").orElseThrow().estaActivo());
        assertEquals("DESACTIVADO", documentoEnBruto().getString("estado"));   // nombre del modelo de BBDD
    }

    @Test
    @DisplayName("HU2-CP04: un usuario activo sigue activo al leerlo de la base de datos")
    void conservaUnUsuarioActivo() {
        Usuario usuario = cliente("juan@uclm.es", "hash-simulado");
        usuario.activar();   // una cuenta nueva nace PENDIENTE (T233): se activa para probar la lectura de un activo

        adapter.guardar(usuario);

        assertTrue(adapter.buscarPorCorreo("juan@uclm.es").orElseThrow().estaActivo());
    }

    @Test
    @DisplayName("HU2-CP03: los intentos fallidos y el bloqueo se conservan entre un login y otro")
    void conservaLosIntentosFallidosYElBloqueo() {
        Instant ahora = Instant.parse("2026-10-08T10:00:00Z");
        Usuario usuario = cliente("juan@uclm.es", "hash-simulado");
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

    private static final LocalDate HOY = LocalDate.of(2026, 10, 10);
    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Juan", "Pérez García", null);
    private static final DatosCliente DATOS_CLIENTE = new DatosCliente(
            TipoCliente.PREMIUM, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null);
    private static final ConsentimientoPrivacidad CONSENTIMIENTO = new ConsentimientoPrivacidad(
            Instant.parse("2026-10-10T10:00:00Z"), "1.0");

    // Cliente recién registrado (T229): nace PENDIENTE y sin id.
    private static Usuario cliente(String email, String hash) {
        return Usuario.nuevoCliente(DATOS_PERSONALES, new Email(email), hash, DATOS_CLIENTE, CONSENTIMIENTO, HOY);
    }

    private static Usuario cliente(String email, String hash, String dni) {
        DatosCliente datosCliente = new DatosCliente(TipoCliente.PREMIUM, new Dni(dni), LocalDate.of(1996, 5, 20), null);
        return Usuario.nuevoCliente(DATOS_PERSONALES, new Email(email), hash, datosCliente, CONSENTIMIENTO, HOY);
    }

    // Documento con los campos que el dominio exige para reconstruir el usuario (T229). Los tests del
    // listado lo guardan directamente con el repositorio y después lo leen a través del mapper.
    private static UsuarioDocument documento(String email, String hash, String rol) {
        UsuarioDocument doc = new UsuarioDocument(email, hash, rol);
        doc.setNombre("Nombre");
        doc.setApellidos("Apellidos");
        if ("CLIENTE".equals(rol)) {
            doc.setTipoCliente("ESTANDAR");
            doc.setDni("0000000" + (Math.abs(email.hashCode()) % 10) + "A");   // DNI distinto por email: índice único rol_dni
            doc.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        }
        return doc;
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB; el repositorio lo convertiría
    // a UsuarioDocument y ahí no se verían campos como _class.
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection("usuarios").find().first();
        assertNotNull(guardado);
        return guardado;
    }

    @Test
    @DisplayName("HU19-CP02: el adaptador busca por email, nombre y apellidos")
    void listarBuscaEnLosCamposVisiblesDelUsuario() {
        usuarioMongoRepository.saveAll(List.of(
                documento("ana@esibuy.test", "hash-1", "CLIENTE"),
                documento("bruno@esibuy.test", "hash-2", "VENDEDOR")
        ));
        UsuarioDocument ana = usuarioMongoRepository.findByEmail("ana@esibuy.test").orElseThrow();
        ana.setNombre("Ana");
        ana.setApellidos("García");
        ana.setTipoCliente("ESTANDAR");
        ana.setEstado("ACTIVO");
        usuarioMongoRepository.save(ana);

        UsuarioPage resultado = adapter.listar(
                new ListarUsuariosQuery("García", null, null, 0, 10)
        );

        assertEquals(1, resultado.totalElementos());
        assertEquals("ana@esibuy.test", resultado.usuarios().get(0).getEmail().valor());
    }

    @Test
    @DisplayName("HU19-CP03: el adaptador aplica el filtro por rol")
    void listarFiltraPorRol() {
        UsuarioDocument ana = documento("ana@esibuy.test", "hash-1", "CLIENTE");
        ana.setEstado("ACTIVO");
        UsuarioDocument bruno = documento("bruno@esibuy.test", "hash-2", "VENDEDOR");
        bruno.setEstado("ACTIVO");
        usuarioMongoRepository.saveAll(List.of(ana, bruno));

        UsuarioPage resultado = adapter.listar(
                new ListarUsuariosQuery(null, Rol.VENDEDOR, null, 0, 10)
        );

        assertEquals(1, resultado.totalElementos());
        assertEquals(Rol.VENDEDOR, resultado.usuarios().get(0).getRol());
    }

    @Test
    @DisplayName("HU19-CP04: el adaptador aplica el filtro por estado activo")
    void listarFiltraPorEstadoActivo() {
        UsuarioDocument activo = documento("ana@esibuy.test", "hash-1", "CLIENTE");
        activo.setEstado("ACTIVO");
        UsuarioDocument desactivado = documento("bruno@esibuy.test", "hash-2", "VENDEDOR");
        desactivado.setEstado("DESACTIVADO");
        usuarioMongoRepository.saveAll(List.of(activo, desactivado));

        UsuarioPage resultado = adapter.listar(
                new ListarUsuariosQuery(null, null, "ACTIVO", 0, 10)
        );

        assertEquals(1, resultado.totalElementos());
        assertTrue(resultado.usuarios().get(0).estaActivo());
    }

    @Test
    @DisplayName("HU19-CP05: el adaptador devuelve la página solicitada y el total completo")
    void listarAplicaPaginacionYConservaElTotal() {
        UsuarioDocument primero = documento("ana@esibuy.test", "hash-1", "CLIENTE");
        primero.setEstado("ACTIVO");
        UsuarioDocument segundo = documento("bruno@esibuy.test", "hash-2", "VENDEDOR");
        segundo.setEstado("ACTIVO");
        UsuarioDocument tercero = documento("carla@esibuy.test", "hash-3", "CLIENTE");
        tercero.setEstado("ACTIVO");
        usuarioMongoRepository.saveAll(List.of(primero, segundo, tercero));

        UsuarioPage resultado = adapter.listar(
                new ListarUsuariosQuery(null, null, null, 1, 1)
        );

        assertEquals(1, resultado.pagina());
        assertEquals(1, resultado.tamano());
        assertEquals(1, resultado.usuarios().size());
        assertEquals(3, resultado.totalElementos());
    }

    @Test
    @DisplayName("HU19-CP02: la búsqueda trata los caracteres especiales como texto literal")
    void listarEscapaCaracteresEspecialesDeLaBusqueda() {
        UsuarioDocument usuario = documento("ana+vip@esibuy.test", "hash-1", "CLIENTE");
        usuario.setEstado("ACTIVO");
        usuarioMongoRepository.save(usuario);

        UsuarioPage resultado = adapter.listar(
                new ListarUsuariosQuery("ana+", null, null, 0, 10)
        );

        assertEquals(1, resultado.totalElementos());
        assertEquals("ana+vip@esibuy.test", resultado.usuarios().get(0).getEmail().valor());
    }

    @Test
    @DisplayName("HU19-CP10: el adaptador filtra BLOQUEADO por lockedUntil")
    void listarFiltraPorBloqueoTemporal() {
        UsuarioDocument bloqueado = documento("bloqueado@esibuy.test", "hash-1", "CLIENTE");
        bloqueado.setEstado("ACTIVO");
        bloqueado.setLockedUntil(Instant.now().plus(Duration.ofMinutes(10)));
        UsuarioDocument activo = documento("activo@esibuy.test", "hash-2", "CLIENTE");
        activo.setEstado("ACTIVO");
        usuarioMongoRepository.saveAll(List.of(bloqueado, activo));

        UsuarioPage resultado = adapter.listar(
                new ListarUsuariosQuery(null, null, "BLOQUEADO", 0, 10)
        );

        assertEquals(1, resultado.totalElementos());
        assertEquals("bloqueado@esibuy.test", resultado.usuarios().get(0).getEmail().valor());
    }

    @Test
    @DisplayName("HU26-CP02: el adaptador ordena por estado ascendente: pendientes, activos y desactivados")
    void listarOrdenaPorEstadoAscendente() {
        guardarVendedoresConEstadosDesordenados();

        UsuarioPage resultado = adapter.listar(new ListarUsuariosQuery(
                null, Rol.VENDEDOR, null, 0, 10, ListarUsuariosQuery.Orden.ESTADO, false));

        assertEquals(List.of("PENDIENTE", "ACTIVO", "DESACTIVADO"), estados(resultado));
    }

    @Test
    @DisplayName("HU26-CP02: el adaptador ordena por estado descendente: desactivados, activos y pendientes")
    void listarOrdenaPorEstadoDescendente() {
        guardarVendedoresConEstadosDesordenados();

        UsuarioPage resultado = adapter.listar(new ListarUsuariosQuery(
                null, Rol.VENDEDOR, null, 0, 10, ListarUsuariosQuery.Orden.ESTADO, true));

        assertEquals(List.of("DESACTIVADO", "ACTIVO", "PENDIENTE"), estados(resultado));
    }

    @Test
    @DisplayName("HU26-CP02: el adaptador ordena por nombre")
    void listarOrdenaPorNombre() {
        usuarioMongoRepository.saveAll(List.of(
                vendedorLlamado("carla@esibuy.test", "Carla"),
                vendedorLlamado("ana@esibuy.test", "Ana"),
                vendedorLlamado("bruno@esibuy.test", "Bruno")));

        UsuarioPage resultado = adapter.listar(new ListarUsuariosQuery(
                null, Rol.VENDEDOR, null, 0, 10, ListarUsuariosQuery.Orden.NOMBRE, false));

        assertEquals(List.of("Ana", "Bruno", "Carla"),
                resultado.usuarios().stream().map(u -> u.getDatosPersonales().nombre()).toList());
    }

    // Se guardan en un orden que no coincide con el esperado, para que el orden lo ponga la consulta.
    private void guardarVendedoresConEstadosDesordenados() {
        UsuarioDocument activo = documento("activo@esibuy.test", "hash-1", "VENDEDOR");
        activo.setEstado("ACTIVO");
        UsuarioDocument desactivado = documento("desactivado@esibuy.test", "hash-2", "VENDEDOR");
        desactivado.setEstado("DESACTIVADO");
        UsuarioDocument pendiente = documento("pendiente@esibuy.test", "hash-3", "VENDEDOR");
        pendiente.setEstado("PENDIENTE");
        usuarioMongoRepository.saveAll(List.of(activo, desactivado, pendiente));
    }

    private static UsuarioDocument vendedorLlamado(String email, String nombre) {
        UsuarioDocument doc = documento(email, "hash", "VENDEDOR");
        doc.setNombre(nombre);
        doc.setEstado("ACTIVO");
        return doc;
    }

    private static List<String> estados(UsuarioPage pagina) {
        return pagina.usuarios().stream().map(Usuario::getEstado).toList();
    }

    @Test
    @DisplayName("HU23-CP04: contarAdministradoresActivos solo cuenta los administradores en estado ACTIVO")
    void contarAdministradoresActivosSoloCuentaLosAdministradoresActivos() {
        usuarioMongoRepository.saveAll(List.of(
                documentoConEstado("ana@esibuy.test", "ADMINISTRADOR", "ACTIVO"),
                documentoConEstado("ada@esibuy.test", "ADMINISTRADOR", "ACTIVO"),
                documentoConEstado("diego@esibuy.test", "ADMINISTRADOR", "DESACTIVADO"),
                documentoConEstado("carlos@esibuy.test", "CLIENTE", "ACTIVO"),
                documentoConEstado("vera@esibuy.test", "VENDEDOR", "ACTIVO")
        ));

        assertEquals(2, adapter.contarAdministradoresActivos());
    }

    @Test
    @DisplayName("HU23-CP04: contarAdministradoresActivos devuelve 0 si no hay administradores activos")
    void contarAdministradoresActivosDevuelveCeroSinAdministradoresActivos() {
        usuarioMongoRepository.save(documentoConEstado("diego@esibuy.test", "ADMINISTRADOR", "DESACTIVADO"));

        assertEquals(0, adapter.contarAdministradoresActivos());
    }

    private static UsuarioDocument documentoConEstado(String email, String rol, String estado) {
        UsuarioDocument doc = documento(email, "hash-simulado", rol);
        doc.setEstado(estado);
        return doc;
    }
}
