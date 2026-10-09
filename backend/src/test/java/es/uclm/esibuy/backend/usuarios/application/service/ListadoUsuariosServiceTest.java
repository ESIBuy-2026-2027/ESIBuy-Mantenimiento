package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Cómo encaja el test en la arquitectura hexagonal:
// - ListadoUsuariosUseCase (application.port.in): puerto de entrada que representa
//   la operación de listado ofrecida por la aplicación.
// - ListadoUsuariosService (application.service): implementación del caso de uso
//   y clase bajo prueba.
// - UsuarioRepositoryPort (application.port.out): dependencia externa del servicio.
//   En producción la implementará el adaptador de MongoDB; aquí la sustituye un mock.
// - Usuario, Email, Rol, TipoCliente y UsuarioPage (domain.model): objetos de dominio
//   independientes de Spring, MongoDB y los detalles de infraestructura.

@ExtendWith(MockitoExtension.class)
class ListadoUsuariosServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private ListadoUsuariosUseCase listadoUsuarios;

    @BeforeEach
    void setUp() {
        listadoUsuarios = new ListadoUsuariosService(usuarioRepository);
    }

    @Test
    @DisplayName("HU19-CP01: el servicio delega la consulta intacta al repositorio")
    void listar_delegaLaConsultaIntactaAlRepositorio() {
        ListarUsuariosQuery query = new ListarUsuariosQuery(
                "Ana", Rol.CLIENTE, "DESACTIVADO", 0, 10
        );
        UsuarioPage paginaEsperada = new UsuarioPage(List.of(), 0, 10, 0);

        when(usuarioRepository.listar(query)).thenReturn(paginaEsperada);

        UsuarioPage resultado = listadoUsuarios.listar(query);

        verify(usuarioRepository).listar(query);
        assertEquals(paginaEsperada, resultado);
    }

    @Test
    @DisplayName("HU19-CP02: el servicio devuelve la página proporcionada por el repositorio")
    void listar_devuelveLaPaginaProporcionadaPorElRepositorio() {
        ListarUsuariosQuery query = new ListarUsuariosQuery(null, null, null, 1, 20);
        Usuario usuario = new Usuario(
                "usuario-1",
                "Ana",
                "García",
                new Email("ana@esibuy.test"),
                "hash",
                Rol.CLIENTE,
                TipoCliente.ESTANDAR
        );
        UsuarioPage paginaEsperada = new UsuarioPage(List.of(usuario), 1, 20, 1);

        when(usuarioRepository.listar(query)).thenReturn(paginaEsperada);

        UsuarioPage resultado = listadoUsuarios.listar(query);

        verify(usuarioRepository).listar(query);
        assertEquals(paginaEsperada, resultado);
    }
}