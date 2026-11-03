package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.pedidos.application.port.in.ProductosConPedidosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosVendedorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.valoraciones.application.port.in.LimpiezaValoracionesUseCase;

@ExtendWith(MockitoExtension.class)
class GestionCuentaServiceTest {

    private static final String EMAIL = "vendedor@esibuy.test";
    private static final String PASSWORD = "Password1!";

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private HashContrasenaPort hashContrasena;
    @Mock
    private GestionProductosVendedorUseCase productos;
    @Mock
    private ProductosConPedidosUseCase productosConPedidos;
    @Mock
    private LimpiezaValoracionesUseCase valoraciones;
    @Mock
    private FactorAutenticacionRepositoryPort factores;
    @Mock
    private CodigoVerificacionRepositoryPort codigosVerificacion;
    @Mock
    private CodigoRecuperacionRepositoryPort codigosRecuperacion;

    private GestionCuentaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new GestionCuentaService(usuarioRepository, hashContrasena, productos, productosConPedidos,
                valoraciones, factores, codigosVerificacion, codigosRecuperacion);
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(vendedor()));
    }

    @Test
    @DisplayName("HU47-CP01: con la contraseña actual incorrecta no se elimina la cuenta ni se modifica ningún dato")
    void contraseñaIncorrectaNoModificaNada() {
        when(hashContrasena.coincide("incorrecta", "hash")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.eliminarCuentaVendedor(EMAIL, "incorrecta"));

        verifyNoInteractions(productos, productosConPedidos, valoraciones, factores, codigosVerificacion,
                codigosRecuperacion);
        verify(usuarioRepository, never()).eliminarPorId("v-1");
    }

    @Test
    @DisplayName("HU47-CP02: al eliminar la cuenta se conservan como no disponibles los productos con pedidos y se eliminan los demás junto con sus valoraciones")
    void conservaProductosConPedidosYEliminaElResto() {
        when(hashContrasena.coincide(PASSWORD, "hash")).thenReturn(true);
        when(productos.desactivarTodos("v-1")).thenReturn(List.of("p-con-pedido", "p-sin-pedido"));
        when(productosConPedidos.buscarDeVendedor("v-1")).thenReturn(Set.of("p-con-pedido"));

        servicio.eliminarCuentaVendedor(EMAIL, PASSWORD);

        verify(productos).desactivarTodos("v-1");
        verify(valoraciones).eliminarDeProducto("p-sin-pedido");
        verify(productos).eliminar("p-sin-pedido");
        verify(productos, never()).eliminar("p-con-pedido");
        verify(factores).eliminarPorUsuario("v-1");
        verify(codigosVerificacion).eliminarPorUsuario("v-1");
        verify(codigosRecuperacion).eliminarPorUsuario("v-1");
        verify(usuarioRepository).eliminarPorId("v-1");
    }

    @Test
    @DisplayName("HU47-CP04: si una eliminación quedó a medias se puede repetir y completarse sin errores")
    void reintentaTrasUnBorradoParcial() {
        when(hashContrasena.coincide(PASSWORD, "hash")).thenReturn(true);
        when(productos.desactivarTodos("v-1")).thenReturn(List.of("p-sin-pedido"));
        when(productosConPedidos.buscarDeVendedor("v-1")).thenReturn(Set.of());
        doThrow(new IllegalStateException("fallo posterior")).doNothing()
                .when(usuarioRepository).eliminarPorId("v-1");

        assertThrows(IllegalStateException.class, () -> servicio.eliminarCuentaVendedor(EMAIL, PASSWORD));
        servicio.eliminarCuentaVendedor(EMAIL, PASSWORD);

        verify(productos, org.mockito.Mockito.times(2)).eliminar("p-sin-pedido");
        verify(valoraciones, org.mockito.Mockito.times(2)).eliminarDeProducto("p-sin-pedido");
        verify(factores, org.mockito.Mockito.times(2)).eliminarPorUsuario("v-1");
        verify(codigosVerificacion, org.mockito.Mockito.times(2)).eliminarPorUsuario("v-1");
        verify(codigosRecuperacion, org.mockito.Mockito.times(2)).eliminarPorUsuario("v-1");
        verify(usuarioRepository, org.mockito.Mockito.times(2)).eliminarPorId("v-1");
    }

    private static Usuario vendedor() {
        return new Usuario("v-1", new es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales(
                "Vera", "Vendedora", null), new Email(EMAIL), "hash", Rol.VENDEDOR,
                new DatosVendedor("Tienda Vera", "cat-1", new Dni("87654321X"), null), null);
    }
}
