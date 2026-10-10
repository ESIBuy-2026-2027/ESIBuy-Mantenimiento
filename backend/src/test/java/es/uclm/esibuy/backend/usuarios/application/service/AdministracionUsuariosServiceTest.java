package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import java.time.LocalDate;
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
// Consultar la ficha de un usuario (HU20) y de un vendedor (HU26).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU24-CP01 a CP04, HU20-CP01 a CP04 y HU26-CP01 a CP05, propuestos.
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
}
