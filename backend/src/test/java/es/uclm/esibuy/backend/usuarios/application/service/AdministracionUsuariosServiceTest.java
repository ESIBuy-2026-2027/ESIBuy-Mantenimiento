package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import java.time.LocalDate;
import java.time.Month;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Activar una cuenta de usuario (HU24, T33/T34). El repositorio es un doble de prueba (Mockito):
// se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU24-CP01 a CP04, propuestos a partir del contrato.
@ExtendWith(MockitoExtension.class)
class AdministracionUsuariosServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private AdministracionUsuariosService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AdministracionUsuariosService(usuarioRepository);
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
