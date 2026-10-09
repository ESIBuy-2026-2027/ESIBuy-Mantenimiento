package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
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
        Usuario desactivado = new Usuario("u-1", "Lucía", "Torres Vega", new Email("lucia@uclm.es"),
                "hash-simulado", Rol.CLIENTE, TipoCliente.ESTANDAR);
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
}
