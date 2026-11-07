package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.PedidosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ProductosVendedorPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.ValoracionesProductoPort;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Seguridad en la capa de negocio (R8) de eliminar la cuenta propia: solo el cliente (HU25) o el vendedor (HU47), y
// con su propio correo. Contexto mínimo (sin MongoDB) con el servicio real y dobles de los puertos de salida.
@SpringJUnitConfig(SeguridadGestionCuentaServiceTest.Configuracion.class)
class SeguridadGestionCuentaServiceTest {

    private static final String VENDEDOR = "vendedor@esibuy.test";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String ADMINISTRADOR = "admin@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        UsuarioRepositoryPort usuarioRepository() {
            return mock(UsuarioRepositoryPort.class);
        }

        @Bean
        HashContrasenaPort hashContrasena() {
            return mock(HashContrasenaPort.class);
        }

        @Bean
        ProductosVendedorPort productos() {
            return mock(ProductosVendedorPort.class);
        }

        @Bean
        PedidosVendedorPort pedidos() {
            return mock(PedidosVendedorPort.class);
        }

        @Bean
        ValoracionesProductoPort valoraciones() {
            return mock(ValoracionesProductoPort.class);
        }

        @Bean
        FactorAutenticacionRepositoryPort factores() {
            return mock(FactorAutenticacionRepositoryPort.class);
        }

        @Bean
        CodigoVerificacionRepositoryPort codigosVerificacion() {
            return mock(CodigoVerificacionRepositoryPort.class);
        }

        @Bean
        CodigoRecuperacionRepositoryPort codigosRecuperacion() {
            return mock(CodigoRecuperacionRepositoryPort.class);
        }

        @Bean
        GestionCuentaUseCase gestionCuenta(UsuarioRepositoryPort usuarios, HashContrasenaPort hash,
                ProductosVendedorPort productos, PedidosVendedorPort pedidos, ValoracionesProductoPort valoraciones,
                FactorAutenticacionRepositoryPort factores, CodigoVerificacionRepositoryPort codigosVerificacion,
                CodigoRecuperacionRepositoryPort codigosRecuperacion) {
            return new GestionCuentaService(usuarios, hash, productos, pedidos, valoraciones, factores,
                    codigosVerificacion, codigosRecuperacion);
        }
    }

    @Autowired
    private UsuarioRepositoryPort usuarioRepository;
    @Autowired
    private HashContrasenaPort hashContrasena;
    @Autowired
    private ProductosVendedorPort productos;
    @Autowired
    private PedidosVendedorPort pedidos;
    @Autowired
    private ValoracionesProductoPort valoraciones;
    @Autowired
    private FactorAutenticacionRepositoryPort factores;
    @Autowired
    private CodigoVerificacionRepositoryPort codigosVerificacion;
    @Autowired
    private CodigoRecuperacionRepositoryPort codigosRecuperacion;
    @Autowired
    private GestionCuentaUseCase gestionCuenta;

    @BeforeEach
    void limpiarDobles() {
        reset(usuarioRepository, hashContrasena, productos, pedidos, valoraciones, factores, codigosVerificacion,
                codigosRecuperacion);
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU47-CP05: un vendedor puede invocar su eliminación de cuenta")
    void unVendedorPuedeInvocarSuEliminacion() {
        when(usuarioRepository.buscarPorCorreo(VENDEDOR)).thenReturn(Optional.of(vendedor()));
        when(hashContrasena.coincide("Password1!", "hash")).thenReturn(true);
        when(productos.desactivarTodos("v-1")).thenReturn(List.of());
        when(pedidos.buscarDeVendedor("v-1")).thenReturn(Set.of());

        assertDoesNotThrow(() -> gestionCuenta.eliminarCuentaPropia(VENDEDOR, "Password1!"));
    }

    @Test
    @WithMockUser(username = "cliente@esibuy.test", roles = "CLIENTE")
    @DisplayName("HU47-CP06: un cliente no puede invocar la eliminación de cuenta de vendedor")
    void unClienteNoPuedeInvocarLaEliminacion() {
        assertThrows(AccessDeniedException.class,
                () -> gestionCuenta.eliminarCuentaPropia(VENDEDOR, "Password1!"));

        verifyNoInteractions(usuarioRepository, hashContrasena, productos, pedidos, valoraciones, factores,
                codigosVerificacion, codigosRecuperacion);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU25-CP04: un cliente elimina su propia cuenta")
    void unClienteEliminaSuCuenta() {
        // El repositorio y el hash dejan que la llamada termine: así se ve que la seguridad la ha dejado pasar
        when(usuarioRepository.buscarPorCorreo(CLIENTE)).thenReturn(Optional.of(cliente()));
        when(hashContrasena.coincide("Password1!", "hash")).thenReturn(true);

        gestionCuenta.eliminarCuentaPropia(CLIENTE, "Password1!");

        verify(usuarioRepository).eliminarPorId("u-1");
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU25-CP04: un cliente no elimina la cuenta de otro cliente")
    void unClienteNoEliminaLaCuentaDeOtro() {
        assertThrows(AccessDeniedException.class,
                () -> gestionCuenta.eliminarCuentaPropia("otro@esibuy.test", "Password1!"));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    @Test
    @WithMockUser(username = ADMINISTRADOR, roles = "ADMINISTRADOR")
    @DisplayName("HU25-CP04: un administrador no elimina su cuenta por aquí")
    void unAdministradorNoEliminaSuCuentaPorAqui() {
        assertThrows(AccessDeniedException.class,
                () -> gestionCuenta.eliminarCuentaPropia(ADMINISTRADOR, "Password1!"));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    @Test
    @DisplayName("HU25-CP04: sin sesión no se elimina ninguna cuenta")
    void sinSesionNoSeEliminaNingunaCuenta() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionCuenta.eliminarCuentaPropia(CLIENTE, "Password1!"));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    private static Usuario cliente() {
        return new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null), new Email(CLIENTE), "hash",
                Rol.CLIENTE, new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"),
                        LocalDate.of(1996, Month.MAY, 20), null), null);
    }

    private static Usuario vendedor() {
        return new Usuario("v-1", new DatosPersonales("Vera", "Vendedora", null), new Email(VENDEDOR), "hash",
                Rol.VENDEDOR, new DatosVendedor("Tienda Vera", "cat-1", new Dni("87654321X"), null), null);
    }
}
