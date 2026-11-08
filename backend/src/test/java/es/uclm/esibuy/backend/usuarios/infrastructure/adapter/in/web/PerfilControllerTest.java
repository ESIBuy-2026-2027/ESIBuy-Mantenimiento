package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarPerfilCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionCuentaUseCase;
import es.uclm.esibuy.backend.usuarios.domain.exception.ClienteYaPremiumException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Consulta del perfil propio (HU4, T297): GET /api/perfil; edición del perfil propio (HU45, T101): PUT /api/perfil;
// y eliminación de la cuenta propia del cliente (HU25, T274) o del vendedor (HU47): DELETE /api/perfil. Los casos
// de uso son dobles de prueba; la seguridad es la real (SecurityConfig). El usuario en sesión se identifica por su
// email (el username de user(...)).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU4-CP01 a CP04, HU25-CP02 a CP05 y HU45-CP06 a CP10, propuestos;
// HU47-CP06 pasa de «un cliente recibe 403» a «un administrador recibe 403» con el endpoint único.
@WebMvcTest(PerfilController.class)
@Import(SecurityConfig.class)
class PerfilControllerTest {

    private static final String RUTA = "/api/perfil";
    private static final String EMAIL_CLIENTE = "lucia@esibuy.test";
    private static final String EMAIL_VENDEDOR = "vera@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionPerfilUseCase gestionPerfil;

    @MockitoBean
    private GestionCuentaUseCase gestionCuenta;

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
    @DisplayName("HU47-CP05: al eliminar la cuenta se invalida la sesión y se limpia el contexto de seguridad")
    void eliminarCuentaInvalidaSesionYLimpiaContexto() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        mockMvc.perform(delete(RUTA).session(sesion).with(user(EMAIL_VENDEDOR).roles("VENDEDOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"password\":\"Password1!\"}"))
                .andExpect(status().isNoContent());

        verify(gestionCuenta).eliminarCuentaPropia(EMAIL_VENDEDOR, "Password1!");
        assertTrue(sesion.isInvalid());
    }

    @Test
    @DisplayName("HU47-CP06: un administrador recibe 403 y no se modifica nada")
    void unAdministradorNoPuedeEliminarCuentaPorAqui() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        mockMvc.perform(delete(RUTA).session(sesion).with(user("admin@esibuy.test").roles("ADMINISTRADOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"password\":\"Password1!\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionCuenta);
        assertTrue(!sesion.isInvalid());
    }

    @Test
    @DisplayName("HU25-CP03: un cliente elimina su cuenta: se responde 204, se invalida la sesión y se borra la cookie JSESSIONID")
    void unClienteEliminaSuCuentaYSeCierraLaSesion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        mockMvc.perform(delete(RUTA).session(sesion).with(user(EMAIL_CLIENTE).roles("CLIENTE"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"password\":\"Password1!\"}"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0));

        verify(gestionCuenta).eliminarCuentaPropia(EMAIL_CLIENTE, "Password1!");
        assertTrue(sesion.isInvalid());
    }

    @Test
    @DisplayName("HU25-CP02: con una contraseña incorrecta el cliente recibe 400 WRONG_PASSWORD y la sesión sigue abierta")
    void unClienteConContrasenaIncorrectaRecibe400() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        doThrow(new ContrasenaActualIncorrectaException("La contraseña actual no es correcta"))
                .when(gestionCuenta).eliminarCuentaPropia(EMAIL_CLIENTE, "incorrecta");

        mockMvc.perform(delete(RUTA).session(sesion).with(user(EMAIL_CLIENTE).roles("CLIENTE"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"password\":\"incorrecta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));

        assertFalse(sesion.isInvalid());
    }

    @Test
    @DisplayName("HU25-CP04: sin token CSRF no se puede eliminar la cuenta (403)")
    void sinTokenCsrfNoSeEliminaLaCuenta() throws Exception {
        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE"))
                        .contentType("application/json")
                        .content("{\"password\":\"Password1!\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionCuenta);
    }

    @Test
    @DisplayName("HU25-CP05: sin contraseña (vacía o ausente) se responde 400 y no se llama a la eliminación")
    void sinContrasenaNoSeLlamaALaEliminacion() throws Exception {
        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json").content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gestionCuenta);
    }

    @Test
    @DisplayName("HU47-CP07: una petición sin autenticar recibe 401 y no se modifica nada")
    void sinAutenticarNoPuedeEliminarCuenta() throws Exception {
        mockMvc.perform(delete(RUTA)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"password\":\"Password1!\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gestionCuenta);
    }

    @Test
    @DisplayName("HU47-CP08: con la contraseña incorrecta se recibe 400 WRONG_PASSWORD y la sesión sigue abierta")
    void contrasenaIncorrectaDevuelve400SinCerrarLaSesion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        doThrow(new ContrasenaActualIncorrectaException("La contraseña actual no es correcta"))
                .when(gestionCuenta).eliminarCuentaPropia(EMAIL_VENDEDOR, "incorrecta");

        mockMvc.perform(delete(RUTA).session(sesion).with(user(EMAIL_VENDEDOR).roles("VENDEDOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"password\":\"incorrecta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));

        assertTrue(!sesion.isInvalid());
    }

    @Test
    @DisplayName("HU45-CP06: un cliente edita su perfil y recibe 200 con el perfil actualizado")
    void unClienteEditaSuPerfil() throws Exception {
        EditarPerfilCommand comando = new EditarPerfilCommand("Lucía María", "Torres Gil", "avatar3", "612345678",
                LocalDate.of(1980, Month.JUNE, 1), null, null);
        when(gestionPerfil.editarPerfil(EMAIL_CLIENTE, comando)).thenReturn(new Usuario("u-1",
                new DatosPersonales("Lucía María", "Torres Gil", "avatar3"), new Email(EMAIL_CLIENTE),
                "hash-no-debe-salir", Rol.CLIENTE, new DatosCliente(TipoCliente.PREMIUM, new Dni("12345678Z"),
                        LocalDate.of(1980, Month.JUNE, 1), new Telefono("612345678")), null));

        mockMvc.perform(put(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"nombre":"Lucía María","apellidos":"Torres Gil","avatar":"avatar3",
                                 "telefono":"612345678","fechaNacimiento":"1980-06-01"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lucía María"))
                .andExpect(jsonPath("$.apellidos").value("Torres Gil"))
                .andExpect(jsonPath("$.avatar").value("avatar3"))
                .andExpect(jsonPath("$.telefono").value("612345678"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1980-06-01"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // El correo sale de la sesión y el avatar llega al comando como imagen de perfil
        verify(gestionPerfil).editarPerfil(EMAIL_CLIENTE, comando);
    }

    @Test
    @DisplayName("HU45-CP06: un vendedor edita su perfil y recibe 200 con su nombre comercial y su categoría")
    void unVendedorEditaSuPerfil() throws Exception {
        EditarPerfilCommand comando = new EditarPerfilCommand("Vera", "Vendedora", null, null, null, "Tienda Nueva",
                "cat-2");
        when(gestionPerfil.editarPerfil(EMAIL_VENDEDOR, comando)).thenReturn(new Usuario("u-4",
                new DatosPersonales("Vera", "Vendedora", null), new Email(EMAIL_VENDEDOR), "hash-no-debe-salir",
                Rol.VENDEDOR, new DatosVendedor("Tienda Nueva", "cat-2", new Dni("87654321X"), null), null));

        mockMvc.perform(put(RUTA).with(user(EMAIL_VENDEDOR).roles("VENDEDOR")).with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"nombre":"Vera","apellidos":"Vendedora","nombreComercial":"Tienda Nueva",
                                 "categoriaPrincipalId":"cat-2"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Nueva"))
                .andExpect(jsonPath("$.categoriaPrincipalId").value("cat-2"))
                .andExpect(jsonPath("$.dni").value("87654321X"));

        verify(gestionPerfil).editarPerfil(EMAIL_VENDEDOR, comando);
    }

    @Test
    @DisplayName("HU45-CP07: el cuerpo no puede cambiar el correo, el DNI, el rol ni el tipo de cliente")
    void elCuerpoNoCambiaElCorreoNiElDni() throws Exception {
        when(gestionPerfil.editarPerfil(eq(EMAIL_CLIENTE), any())).thenReturn(clienteLucia());

        mockMvc.perform(put(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"nombre":"Lucía","apellidos":"Torres Vega","email":"otro@esibuy.test",
                                 "dni":"00000000T","rol":"ADMINISTRADOR","tipoCliente":"ESTANDAR"}"""))
                .andExpect(status().isOk());

        // Se edita al usuario de la sesión, no al del cuerpo; el comando no tiene sitio para DNI, rol ni tipo
        verify(gestionPerfil).editarPerfil(EMAIL_CLIENTE,
                new EditarPerfilCommand("Lucía", "Torres Vega", null, null, null, null, null));
    }

    @Test
    @DisplayName("HU45-CP08: sin nombre o con uno de más de 30 caracteres se responde 400 y no se llama al caso de uso")
    void sinNombreSeResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(put(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json").content("{\"nombre\":\" \",\"apellidos\":\"Torres\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(put(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"" + "a".repeat(31) + "\",\"apellidos\":\"Torres\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU45-CP08: un dato que rechaza el dominio responde 400 con el code de su motivo")
    void unDatoQueRechazaElDominioDa400ConSuCode() throws Exception {
        when(gestionPerfil.editarPerfil(eq(EMAIL_CLIENTE), any()))
                .thenThrow(new UsuarioInvalidoException(MotivoRechazo.MENOR_DE_EDAD,
                        "El cliente debe ser mayor de edad"));

        mockMvc.perform(put(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"Lucía\",\"apellidos\":\"Torres\",\"fechaNacimiento\":\"2015-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNDERAGE"));
    }

    @Test
    @DisplayName("HU45-CP08: un dato no válido sin motivo concreto responde 400 VALIDATION_ERROR")
    void unDatoNoValidoSinMotivoDa400Generico() throws Exception {
        when(gestionPerfil.editarPerfil(eq(EMAIL_VENDEDOR), any()))
                .thenThrow(new UsuarioInvalidoException("Un vendedor debe tener datos de vendedor"));

        mockMvc.perform(put(RUTA).with(user(EMAIL_VENDEDOR).roles("VENDEDOR")).with(csrf())
                        .contentType("application/json").content("{\"nombre\":\"Vera\",\"apellidos\":\"Vendedora\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU45-CP09: un nombre comercial que ya usa otro vendedor responde 409")
    void unNombreComercialOcupadoDa409() throws Exception {
        when(gestionPerfil.editarPerfil(eq(EMAIL_VENDEDOR), any()))
                .thenThrow(new NombreComercialYaRegistradoException("El nombre comercial ya está registrado"));

        mockMvc.perform(put(RUTA).with(user(EMAIL_VENDEDOR).roles("VENDEDOR")).with(csrf())
                        .contentType("application/json")
                        .content("""
                                {"nombre":"Vera","apellidos":"Vendedora","nombreComercial":"Tienda Ajena"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMERCIAL_NAME_ALREADY_REGISTERED"));
    }

    @Test
    @DisplayName("HU45-CP10: un administrador no edita su perfil por aquí (403)")
    void unAdministradorNoEditaSuPerfilPorAqui() throws Exception {
        mockMvc.perform(put(RUTA).with(user("admin@esibuy.test").roles("ADMINISTRADOR")).with(csrf())
                        .contentType("application/json").content("{\"nombre\":\"Ana\",\"apellidos\":\"Admin\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU45-CP10: sin sesión no se edita el perfil (401)")
    void sinSesionNoSeEditaElPerfil() throws Exception {
        mockMvc.perform(put(RUTA).with(csrf())
                        .contentType("application/json").content("{\"nombre\":\"Lucía\",\"apellidos\":\"Torres\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gestionPerfil);
    }

    @Test
    @DisplayName("HU45-CP10: sin token CSRF no se edita el perfil (403)")
    void sinTokenCsrfNoSeEditaElPerfil() throws Exception {
        mockMvc.perform(put(RUTA).with(user(EMAIL_CLIENTE).roles("CLIENTE"))
                        .contentType("application/json").content("{\"nombre\":\"Lucía\",\"apellidos\":\"Torres\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionPerfil);
    }

        @Test
        @DisplayName("HU45-CP20: un cliente pasa a Premium y recibe 200 con su perfil Premium")
        void unClientePasaAPremium() throws Exception {
        when(gestionPerfil.pasarAPremium(EMAIL_CLIENTE)).thenReturn(new Usuario("u-1",
                new DatosPersonales("Lucía", "Torres Vega", null), new Email(EMAIL_CLIENTE), "hash-no-debe-salir",
                Rol.CLIENTE, new DatosCliente(TipoCliente.PREMIUM, new Dni("12345678Z"),
                        LocalDate.of(1996, Month.MAY, 20), null), null));

        mockMvc.perform(post(RUTA + "/premium").with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCliente").value("PREMIUM"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        @DisplayName("HU45-CP20: si el cliente ya es Premium se responde 400 ALREADY_PREMIUM")
        void unClienteQueYaEsPremiumRecibe400() throws Exception {
        when(gestionPerfil.pasarAPremium(EMAIL_CLIENTE)).thenThrow(new ClienteYaPremiumException("El cliente ya es Premium"));

        mockMvc.perform(post(RUTA + "/premium").with(user(EMAIL_CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_PREMIUM"));
        }

        @Test
        @DisplayName("HU45-CP20: un administrador recibe 403 y no se llama al caso de uso")
        void unAdministradorNoPasaAPremium() throws Exception {
        mockMvc.perform(post(RUTA + "/premium").with(user("admin@esibuy.test").roles("ADMINISTRADOR")).with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gestionPerfil);
        }

        @Test
        @DisplayName("HU45-CP20: sin sesión se responde 401 y no se llama al caso de uso")
        void sinSesionNoSePasaAPremium() throws Exception {
        mockMvc.perform(post(RUTA + "/premium").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(gestionPerfil);
        }
}
