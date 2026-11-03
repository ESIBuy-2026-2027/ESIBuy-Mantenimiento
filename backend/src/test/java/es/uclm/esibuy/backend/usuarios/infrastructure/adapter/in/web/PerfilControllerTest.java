package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Consulta del perfil propio (HU4, T297): GET /api/perfil; y eliminación de la cuenta propia (HU25, T274):
// DELETE /api/perfil. El caso de uso es un doble de prueba; la seguridad es la real (SecurityConfig). El usuario
// en sesión se identifica por su email (el username de user(...)).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU4-CP01 a CP04 y HU25-CP02 a CP05, propuestos.
@WebMvcTest(PerfilController.class)
@Import(SecurityConfig.class)
class PerfilControllerTest {

    private static final String RUTA = "/api/perfil";
    private static final String EMAIL_CLIENTE = "lucia@esibuy.test";
    private static final String EMAIL_VENDEDOR = "vera@esibuy.test";
    private static final String PASSWORD = "Clave-Segura-123";
    private static final String CUERPO_ELIMINAR = "{\"password\":\"" + PASSWORD + "\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionPerfilUseCase gestionPerfil;

    private static Usuario clienteLucia() {
        return new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", "avatar2"), new Email(EMAIL_CLIENTE),
                "hash-no-debe-salir", Rol.CLIENTE,
                new DatosCliente(TipoCliente.PREMIUM, new Dni("12345678Z"), LocalDate.of(1979, Month.MAY, 14),
                        new Telefono("620031676")), null);
    }

    @Test
    @DisplayName("HU4-CP01: un cliente recibe sus datos personales y los de cliente, sin los de vendedor")
    void unClienteRecibeSusDatos() throws Exception {
        when(gestionPerfil.consultarPerfil(EMAIL_CLIENTE)).thenReturn(clienteLucia());

        mockMvc.perform(get(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u-1"))
                .andExpect(jsonPath("$.nombre").value("Lucía"))
                .andExpect(jsonPath("$.apellidos").value("Torres Vega"))
                .andExpect(jsonPath("$.email").value(EMAIL_CLIENTE))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.dni").value("12345678Z"))
                .andExpect(jsonPath("$.telefono").value("620031676"))
                .andExpect(jsonPath("$.avatar").value("avatar2"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1979-05-14"))
                .andExpect(jsonPath("$.tipoCliente").value("PREMIUM"))
                .andExpect(jsonPath("$.nombreComercial").doesNotExist())
                .andExpect(jsonPath("$.categoriaPrincipalId").doesNotExist());
    }

    @Test
    @DisplayName("HU4-CP02: un vendedor recibe sus datos personales y los de vendedor, sin los de cliente")
    void unVendedorRecibeSusDatos() throws Exception {
        when(gestionPerfil.consultarPerfil(EMAIL_VENDEDOR)).thenReturn(new Usuario("u-4",
                new DatosPersonales("Vera", "Vendedora", null), new Email(EMAIL_VENDEDOR), "hash-no-debe-salir",
                Rol.VENDEDOR, new DatosVendedor("Tienda Vera", "cat-1", new Dni("87654321X"), null), null));

        mockMvc.perform(get(RUTA).with(user(EMAIL_VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u-4"))
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andExpect(jsonPath("$.dni").value("87654321X"))
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Vera"))
                .andExpect(jsonPath("$.categoriaPrincipalId").value("cat-1"))
                .andExpect(jsonPath("$.telefono").doesNotExist())
                .andExpect(jsonPath("$.fechaNacimiento").doesNotExist())
                .andExpect(jsonPath("$.tipoCliente").doesNotExist());
    }

    @Test
    @DisplayName("HU4-CP03: el perfil no expone la contraseña ni datos internos de la cuenta")
    void elPerfilNoExponeDatosSensibles() throws Exception {
        when(gestionPerfil.consultarPerfil(EMAIL_CLIENTE)).thenReturn(clienteLucia());

        mockMvc.perform(get(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.estado").doesNotExist())
                .andExpect(jsonPath("$.intentosFallidos").doesNotExist())
                .andExpect(jsonPath("$.bloqueadoHasta").doesNotExist());
    }

    @Test
    @DisplayName("HU4-CP04: un administrador no tiene perfil que consultar aquí (403)")
    void unAdministradorRecibe403() throws Exception {
        mockMvc.perform(get(RUTA).with(user("admin@esibuy.test").roles("ADMINISTRADOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU4-CP04: sin sesión no se puede consultar el perfil (401)")
    void sinSesionRecibe401() throws Exception {
        mockMvc.perform(get(RUTA))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU25-CP03: al eliminar la cuenta se responde 204 y se cierra la sesión")
    void eliminarLaCuentaCierraLaSesion() throws Exception {
        // La sesión solo se invalida si la petición trae una: se envía la del test y se comprueba después
        MockHttpSession sesion = new MockHttpSession();

        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf()).session(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_ELIMINAR))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0));

        verify(gestionPerfil).eliminarCuenta(EMAIL_CLIENTE, PASSWORD);
        assertTrue(sesion.isInvalid());
    }

    @Test
    @DisplayName("HU25-CP02: con una contraseña incorrecta se responde 400 WRONG_PASSWORD y la sesión sigue abierta")
    void eliminarConContrasenaIncorrectaRecibe400() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        doThrow(new ContrasenaActualIncorrectaException("La contraseña actual no es correcta"))
                .when(gestionPerfil).eliminarCuenta(EMAIL_CLIENTE, PASSWORD);

        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf()).session(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_ELIMINAR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));

        assertFalse(sesion.isInvalid());
    }

    @Test
    @DisplayName("HU25-CP04: sin sesión no se puede eliminar la cuenta (401)")
    void eliminarSinSesionRecibe401() throws Exception {
        mockMvc.perform(delete(RUTA).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CUERPO_ELIMINAR))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU25-CP04: sin token CSRF no se puede eliminar la cuenta (403)")
    void eliminarSinTokenCsrfRecibe403() throws Exception {
        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_ELIMINAR))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU25-CP05: sin contraseña no se llama a la eliminación (400)")
    void eliminarSinContrasenaRecibe400() throws Exception {
        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gestionPerfil);
    }
}
