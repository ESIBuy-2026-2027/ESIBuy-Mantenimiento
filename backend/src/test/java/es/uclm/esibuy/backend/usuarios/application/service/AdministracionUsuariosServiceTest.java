package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Gestión de cuentas por el administrador: activar (HU24, T33/T34), consultar la ficha de un usuario (HU20)
// y de un vendedor (HU26) y editar (HU22, T42/T44). El repositorio es un doble de prueba (Mockito): se prueba
// el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU24-CP01 a CP04, HU20-CP01 a CP04, HU26-CP01 a CP05 y
// HU22-CP01 a CP08, propuestos.
@ExtendWith(MockitoExtension.class)
class AdministracionUsuariosServiceTest {

    // Reloj fijo: la mayoría de edad de la edición se calcula con esta fecha
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-10T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private AdministracionUsuariosService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AdministracionUsuariosService(usuarioRepository, RELOJ);
    }

    @Test
    @DisplayName("HU22-CP01: editar un cliente cambia sus datos personales y de cliente y lo guarda")
    void editaUnClienteYLoGuarda() {
        Usuario cliente = clienteLucia();
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(cliente));
        EditarUsuarioCommand comando = new EditarUsuarioCommand(Rol.CLIENTE, "Carla", "Moreno Gil", "avatar1",
                "10004119A", "620023757", LocalDate.of(1978, 4, 13), TipoCliente.PREMIUM);

        Usuario editado = servicio.editar("u-1", comando);

        assertSame(cliente, editado);
        assertEquals(new DatosPersonales("Carla", "Moreno Gil", "avatar1"), editado.getDatosPersonales());
        assertEquals(new DatosCliente(TipoCliente.PREMIUM, new Dni("10004119A"), LocalDate.of(1978, 4, 13),
                new Telefono("620023757")), editado.getDatosCliente());
        assertEquals(new Email("lucia@uclm.es"), editado.getEmail());
        verify(usuarioRepository).guardar(editado);
    }

    @Test
    @DisplayName("HU22-CP02: editar un administrador cambia sus datos personales y lo guarda")
    void editaUnAdministradorYLoGuarda() {
        Usuario administrador = new Usuario("u-2", new DatosPersonales("Diego", "Delgado", null),
                new Email("diego@uclm.es"), "hash-simulado", Rol.ADMINISTRADOR, null, null);
        when(usuarioRepository.buscarPorId("u-2")).thenReturn(Optional.of(administrador));
        EditarUsuarioCommand comando = new EditarUsuarioCommand(Rol.ADMINISTRADOR, "Diego", "Delgado Iglesias",
                "avatar3", null, null, null, null);

        Usuario editado = servicio.editar("u-2", comando);

        assertEquals(new DatosPersonales("Diego", "Delgado Iglesias", "avatar3"), editado.getDatosPersonales());
        assertNull(editado.getDatosCliente());
        verify(usuarioRepository).guardar(editado);
    }

    @Test
    @DisplayName("HU22-CP03: editar un vendedor cambia sus datos personales y lo guarda")
    void editaUnVendedorYLoGuarda() {
        when(usuarioRepository.buscarPorId("u-4")).thenReturn(Optional.of(vendedorVera()));
        EditarUsuarioCommand comando = new EditarUsuarioCommand(Rol.VENDEDOR, "Vera", "Vendedora Ruiz",
                null, null, null, null, null);

        Usuario editado = servicio.editar("u-4", comando);

        assertEquals(new DatosPersonales("Vera", "Vendedora Ruiz", null), editado.getDatosPersonales());
        verify(usuarioRepository).guardar(editado);
    }

    @Test
    @DisplayName("HU22-CP04: editar no cambia el email, la contraseña, el estado ni el rol de la cuenta")
    void editarNoCambiaEmailContrasenaEstadoNiRol() {
        Usuario vendedor = vendedorVera();
        vendedor.activar();
        when(usuarioRepository.buscarPorId("u-4")).thenReturn(Optional.of(vendedor));
        EditarUsuarioCommand comando = new EditarUsuarioCommand(Rol.VENDEDOR, "Vera", "Vendedora Ruiz",
                null, null, null, null, null);

        Usuario editado = servicio.editar("u-4", comando);

        assertEquals(new Email("vendedor@esibuy.test"), editado.getEmail());
        assertEquals("hash-simulado", editado.getPasswordHash());
        assertEquals("ACTIVO", editado.getEstado());
        assertEquals(Rol.VENDEDOR, editado.getRol());
    }

    @Test
    @DisplayName("HU22-CP05: editar un cliente con datos no válidos falla y no guarda nada")
    void rechazaEditarUnClienteConDatosNoValidos() {
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(clienteLucia()));
        EditarUsuarioCommand dniNoValido = new EditarUsuarioCommand(Rol.CLIENTE, "Carla", "Moreno Gil", null,
                "1234", null, LocalDate.of(1978, 4, 13), TipoCliente.ESTANDAR);
        EditarUsuarioCommand menorDeEdad = new EditarUsuarioCommand(Rol.CLIENTE, "Carla", "Moreno Gil", null,
                "10004119A", null, LocalDate.of(2010, 1, 1), TipoCliente.ESTANDAR);

        assertThrows(UsuarioInvalidoException.class, () -> servicio.editar("u-1", dniNoValido));
        assertThrows(UsuarioInvalidoException.class, () -> servicio.editar("u-1", menorDeEdad));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU22-CP07: editar un usuario que no existe falla y no guarda nada")
    void rechazaEditarUnUsuarioQueNoExiste() {
        when(usuarioRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());
        EditarUsuarioCommand comando = new EditarUsuarioCommand(Rol.ADMINISTRADOR, "Diego", "Delgado Iglesias",
                null, null, null, null, null);

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.editar("no-existe", comando));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU22-CP05: editar con un rol distinto del actual falla y no guarda nada (el cambio de rol llega en la T43)")
    void rechazaEditarConUnRolDistintoDelActual() {
        Usuario cliente = clienteLucia();
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(cliente));
        // Datos de cliente válidos: solo falla el rol
        EditarUsuarioCommand aAdministrador = new EditarUsuarioCommand(Rol.ADMINISTRADOR, "Carla", "Moreno Gil",
                null, "10004119A", null, LocalDate.of(1978, 4, 13), TipoCliente.ESTANDAR);

        assertThrows(UsuarioInvalidoException.class, () -> servicio.editar("u-1", aAdministrador));
        assertEquals(Rol.CLIENTE, cliente.getRol());
        assertEquals(new DatosPersonales("Lucía", "Torres Vega", null), cliente.getDatosPersonales());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU22-CP05: editar sin rol falla y no guarda nada")
    void rechazaEditarSinRol() {
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(clienteLucia()));
        EditarUsuarioCommand sinRol = new EditarUsuarioCommand(null, "Carla", "Moreno Gil", null,
                "10004119A", null, LocalDate.of(1978, 4, 13), TipoCliente.ESTANDAR);

        assertThrows(UsuarioInvalidoException.class, () -> servicio.editar("u-1", sinRol));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU22-CP06: editar un cliente con el DNI de otra cuenta falla y no guarda nada")
    void rechazaEditarUnClienteConElDniDeOtraCuenta() {
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(clienteLucia()));
        when(usuarioRepository.existePorDni("10004119A")).thenReturn(true);
        EditarUsuarioCommand dniDeOtro = new EditarUsuarioCommand(Rol.CLIENTE, "Lucía", "Torres Vega", null,
                "10004119A", null, LocalDate.of(1996, 5, 20), TipoCliente.ESTANDAR);

        assertThrows(DniYaRegistradoException.class, () -> servicio.editar("u-1", dniDeOtro));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU22-CP06: editar un cliente que conserva su DNI no comprueba si está repetido y lo guarda")
    void editarUnClienteQueConservaSuDniNoLoComprueba() {
        Usuario cliente = clienteLucia();
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(cliente));
        EditarUsuarioCommand mismoDni = new EditarUsuarioCommand(Rol.CLIENTE, "Lucía", "Torres Vega", null,
                "12345678Z", "620023757", LocalDate.of(1996, 5, 20), TipoCliente.ESTANDAR);

        servicio.editar("u-1", mismoDni);

        verify(usuarioRepository, never()).existePorDni(any());
        verify(usuarioRepository).guardar(cliente);
    }

    @Test
    @DisplayName("HU22-CP01: editar un cliente con el teléfono en blanco lo deja sin teléfono (es opcional)")
    void editarUnClienteConElTelefonoEnBlancoLoDejaSinTelefono() {
        // El frontend envía los campos sin rellenar como "" (profilePayload)
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(clienteLucia()));
        EditarUsuarioCommand telefonoEnBlanco = new EditarUsuarioCommand(Rol.CLIENTE, "Lucía", "Torres Vega", null,
                "12345678Z", "  ", LocalDate.of(1996, 5, 20), TipoCliente.ESTANDAR);

        Usuario editado = servicio.editar("u-1", telefonoEnBlanco);

        assertNull(editado.getDatosCliente().telefono());
        verify(usuarioRepository).guardar(editado);
    }

    private static Usuario vendedorVera() {
        return new Usuario("u-4", new DatosPersonales("Vera", "Vendedora", null), new Email("vendedor@esibuy.test"),
                "hash-simulado", Rol.VENDEDOR, null, null);
    }

    private static Usuario clienteLucia() {
        return new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null), new Email("lucia@uclm.es"),
                "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
    }

    @Test
    @DisplayName("HU24-CP01: activar una cuenta desactivada la deja activa y la guarda")
    void activaUnaCuentaDesactivadaYLaGuarda() {
        Usuario desactivado = new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null),
                new Email("lucia@uclm.es"), "hash-simulado", Rol.CLIENTE, new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
        desactivado.desactivar();
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(desactivado));

        Usuario activado = servicio.activar("u-1");

        assertSame(desactivado, activado);
        assertTrue(activado.estaActivo());
        assertEquals("ACTIVO", activado.getEstado());
        verify(usuarioRepository).guardar(activado);
    }

    @Test
    @DisplayName("HU24-CP03: activar un usuario que no existe falla y no guarda nada")
    void rechazaActivarUnUsuarioQueNoExiste() {
        when(usuarioRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.activar("no-existe"));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU20-CP01: consultar devuelve el usuario y no cambia nada")
    void consultarDevuelveElUsuario() {
        Usuario usuario = new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null),
                new Email("lucia@uclm.es"), "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(usuario));

        assertSame(usuario, servicio.consultar("u-1"));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU20-CP03: consultar un usuario que no existe falla")
    void consultarUnUsuarioQueNoExisteFalla() {
        when(usuarioRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.consultar("no-existe"));
    }

    @Test
    @DisplayName("HU26-CP03: consultarVendedor devuelve el vendedor y no cambia nada")
    void consultarVendedorDevuelveElVendedor() {
        Usuario vendedor = new Usuario("v-1", new DatosPersonales("Marta", "Ruiz Gil", null),
                new Email("marta@esibuy.test"), "hash-simulado", Rol.VENDEDOR, null, null);
        when(usuarioRepository.buscarPorId("v-1")).thenReturn(Optional.of(vendedor));

        assertSame(vendedor, servicio.consultarVendedor("v-1"));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU26-CP04: consultarVendedor de un usuario que no es vendedor falla como si no existiera")
    void consultarVendedorDeOtroRolFalla() {
        Usuario cliente = new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null),
                new Email("lucia@uclm.es"), "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(cliente));

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.consultarVendedor("u-1"));
    }

    @Test
    @DisplayName("HU26-CP04: consultarVendedor de un usuario que no existe falla")
    void consultarVendedorQueNoExisteFalla() {
        when(usuarioRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.consultarVendedor("no-existe"));
    }

    // Desactivar una cuenta de usuario (HU23, T37/T38/T39). El segundo parámetro es el email del
    // administrador que hace la petición: el usuario en sesión se identifica por su email (/api/auth/me).
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU23-CPnn, propuestos a partir del contrato del frontend
    // (frontend/docs/hu23-desactivar-usuario.md).
    private static final String EMAIL_ADMINISTRADOR = "admin@esibuy.test";

    @Test
    @DisplayName("HU23-CP01: desactivar una cuenta activa la deja desactivada y la guarda")
    void desactivaUnaCuentaActivaYLaGuarda() {
        Usuario activo = cliente("u-2", "carlos@uclm.es");
        activo.activar();
        when(usuarioRepository.buscarPorId("u-2")).thenReturn(Optional.of(activo));

        Usuario desactivado = servicio.desactivar("u-2", EMAIL_ADMINISTRADOR);

        assertSame(activo, desactivado);
        assertEquals("DESACTIVADO", desactivado.getEstado());
        verify(usuarioRepository).guardar(desactivado);
    }

    @Test
    @DisplayName("HU23-CP02: desactivar un usuario que no existe falla y no guarda nada")
    void rechazaDesactivarUnUsuarioQueNoExiste() {
        when(usuarioRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.desactivar("no-existe", EMAIL_ADMINISTRADOR));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU23-CP03: un administrador no puede desactivar su propia cuenta y no se guarda nada")
    void rechazaQueUnAdministradorSeDesactiveASiMismo() {
        Usuario administrador = administrador("u-1", EMAIL_ADMINISTRADOR);
        administrador.activar();
        when(usuarioRepository.buscarPorId("u-1")).thenReturn(Optional.of(administrador));

        assertThrows(AutodesactivacionNoPermitidaException.class, () -> servicio.desactivar("u-1", EMAIL_ADMINISTRADOR));
        assertEquals("ACTIVO", administrador.getEstado());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU23-CP04: no se puede desactivar al último administrador activo y no se guarda nada")
    void rechazaDesactivarAlUltimoAdministradorActivo() {
        Usuario ultimoAdministrador = administrador("u-3", "ana@esibuy.test");
        ultimoAdministrador.activar();
        when(usuarioRepository.buscarPorId("u-3")).thenReturn(Optional.of(ultimoAdministrador));
        when(usuarioRepository.contarAdministradoresActivos()).thenReturn(1L);

        assertThrows(UltimoAdministradorActivoException.class, () -> servicio.desactivar("u-3", EMAIL_ADMINISTRADOR));
        assertEquals("ACTIVO", ultimoAdministrador.getEstado());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU23-CP05: se puede desactivar a un administrador activo si queda otro administrador activo")
    void desactivaAUnAdministradorSiQuedaOtroActivo() {
        Usuario otroAdministrador = administrador("u-3", "ana@esibuy.test");
        otroAdministrador.activar();
        when(usuarioRepository.buscarPorId("u-3")).thenReturn(Optional.of(otroAdministrador));
        when(usuarioRepository.contarAdministradoresActivos()).thenReturn(2L);

        Usuario desactivado = servicio.desactivar("u-3", EMAIL_ADMINISTRADOR);

        assertEquals("DESACTIVADO", desactivado.getEstado());
        verify(usuarioRepository).guardar(desactivado);
    }

    private static Usuario cliente(String id, String email) {
        return new Usuario(id, new DatosPersonales("Carlos", "Moreno Gil", null), new Email(email), "hash-simulado",
                Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1990, Month.MARCH, 14), null),
                null);
    }

    private static Usuario administrador(String id, String email) {
        return new Usuario(id, new DatosPersonales("Ada", "Admin", null), new Email(email), "hash-simulado",
                Rol.ADMINISTRADOR, null, null);
    }
}
