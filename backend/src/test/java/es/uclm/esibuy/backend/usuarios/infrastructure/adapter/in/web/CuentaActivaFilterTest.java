package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Invalidar la sesión de un usuario desactivado (HU23, T206). En cada petición con un usuario en sesión, el
// filtro comprueba que su cuenta sigue activa; si no, invalida la sesión y responde 401 sin llegar al endpoint
// (el frontend lo espera así: frontend/docs/hu23-desactivar-usuario.md). El usuario en sesión se identifica por
// su email, como lo guarda el login (AutenticacionController.abrirSesion).
// Test unitario sin Spring: petición, sesión y cadena de filtros simuladas; el caso de uso es un doble (Mockito).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU23-CP07 a CP10, propuestos para la T206.
@ExtendWith(MockitoExtension.class)
class CuentaActivaFilterTest {

    private static final String EMAIL = "vera@esibuy.test";

    @Mock
    private AutenticacionUseCase autenticacion;

    private CuentaActivaFilter filtro;
    private MockHttpServletRequest peticion;
    private MockHttpServletResponse respuesta;
    private MockFilterChain cadena;
    private MockHttpSession sesion;

    @BeforeEach
    void preparar() {
        filtro = new CuentaActivaFilter(autenticacion);
        sesion = new MockHttpSession();
        peticion = new MockHttpServletRequest("GET", "/api/auth/me");
        peticion.setSession(sesion);
        respuesta = new MockHttpServletResponse();
        cadena = new MockFilterChain();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("HU23-CP07: una petición sin usuario en sesión pasa sin consultar la cuenta")
    void sinUsuarioEnSesionDejaPasar() throws Exception {
        // Spring Security pone un usuario anónimo cuando nadie ha iniciado sesión (registro, login...)
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "clave", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        filtro.doFilter(peticion, respuesta, cadena);

        assertNotNull(cadena.getRequest(), "la petición debe seguir hacia el endpoint");
        assertEquals(200, respuesta.getStatus());
        verify(autenticacion, never()).consultarUsuarioEnSesion(any());
    }

    @Test
    @DisplayName("HU23-CP08: una petición de un usuario con la cuenta activa pasa y conserva su sesión")
    void cuentaActivaDejaPasar() throws Exception {
        iniciarSesionComo(EMAIL);
        Usuario activo = vendedor(EMAIL);
        activo.activar();
        when(autenticacion.consultarUsuarioEnSesion(EMAIL)).thenReturn(activo);

        filtro.doFilter(peticion, respuesta, cadena);

        assertNotNull(cadena.getRequest(), "la petición debe seguir hacia el endpoint");
        assertFalse(sesion.isInvalid());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("HU23-CP09: si la cuenta del usuario en sesión está desactivada, se invalida su sesión y responde 401")
    void cuentaDesactivadaInvalidaLaSesionYDevuelve401() throws Exception {
        iniciarSesionComo(EMAIL);
        Usuario desactivado = vendedor(EMAIL);
        desactivado.desactivar();
        when(autenticacion.consultarUsuarioEnSesion(EMAIL)).thenReturn(desactivado);

        filtro.doFilter(peticion, respuesta, cadena);

        assertSesionCerrada();
    }

    @Test
    @DisplayName("HU23-CP10: si el usuario en sesión ya no existe, se invalida su sesión y responde 401")
    void usuarioInexistenteInvalidaLaSesionYDevuelve401() throws Exception {
        iniciarSesionComo(EMAIL);
        when(autenticacion.consultarUsuarioEnSesion(EMAIL))
                .thenThrow(new CredencialesInvalidasException("Credenciales incorrectas"));

        filtro.doFilter(peticion, respuesta, cadena);

        assertSesionCerrada();
    }

    @Test
    @DisplayName("HU22-CP14: si al usuario en sesión le han cambiado el rol, se invalida su sesión y responde 401")
    void rolCambiadoInvalidaLaSesionYDevuelve401() throws Exception {
        // Inició sesión como administrador y otro administrador lo ha pasado a cliente (HU22, T43)
        iniciarSesionComo(EMAIL, "ROLE_ADMINISTRADOR");
        Usuario ahoraCliente = new Usuario("u-2", new DatosPersonales("Vera", "Vendedora", null), new Email(EMAIL),
                "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
        ahoraCliente.activar();
        when(autenticacion.consultarUsuarioEnSesion(EMAIL)).thenReturn(ahoraCliente);

        filtro.doFilter(peticion, respuesta, cadena);

        assertSesionCerrada();
    }

    // Como lo deja el login tras el tercer paso: el email como nombre y ROLE_<rol> como permiso.
    private static void iniciarSesionComo(String email) {
        iniciarSesionComo(email, "ROLE_VENDEDOR");
    }

    private static void iniciarSesionComo(String email, String permiso) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                email, null, AuthorityUtils.createAuthorityList(permiso)));
    }

    private void assertSesionCerrada() {
        assertNull(cadena.getRequest(), "la petición no debe llegar al endpoint");
        assertEquals(401, respuesta.getStatus());
        assertTrue(sesion.isInvalid(), "la sesión debe quedar invalidada");
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "no debe quedar usuario en el contexto");
    }

    private static Usuario vendedor(String email) {
        return new Usuario("u-2", new DatosPersonales("Vera", "Vendedora", null), new Email(email),
                "hash-simulado", Rol.VENDEDOR, null, null);
    }
}
