package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionCommand;
import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.LimiteDireccionesException;
import es.uclm.esibuy.backend.direcciones.domain.exception.NuevaPrincipalObligatoriaException;
import es.uclm.esibuy.backend.direcciones.domain.model.CodigoPostal;
import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import es.uclm.esibuy.backend.direcciones.domain.model.TelefonoContacto;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Direcciones de envío del cliente (HU5): GET, POST, PUT /{id}, PUT /{id}/principal y DELETE /{id} sobre
// /api/customer/addresses. El caso de uso es un doble de prueba; la seguridad es la real
// (SecurityConfig: /api/customer/** solo para CLIENTE, con CSRF).
@WebMvcTest(DireccionController.class)
@Import(SecurityConfig.class)
class DireccionControllerTest {

    private static final String URL = "/api/customer/addresses";
    private static final String CLIENTE = "cliente@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionDireccionesUseCase gestionDirecciones;

    // El clienteId y el principal del cuerpo se ignoran: el cliente sale de la sesión y la principal se marca aparte
    private static final String DIRECCION_JSON = """
            {"destinatario": "Juan Pérez", "calle": "Calle Mayor 1", "detalles": "2º B", "codigoPostal": "13071",
             "ciudad": "Ciudad Real", "provincia": "Ciudad Real", "telefono": "612345678",
             "clienteId": "otro-cliente", "principal": true}
            """;

    private static final DireccionCommand COMANDO = new DireccionCommand("Juan Pérez", "Calle Mayor 1", "2º B",
            "13071", "Ciudad Real", "Ciudad Real", "612345678");

    private static DireccionEnvio direccion(String id, boolean principal) {
        return new DireccionEnvio(id, "c-1", new DatosDireccion("Juan Pérez", "Calle Mayor 1", "2º B",
                new CodigoPostal("13071"), "Ciudad Real", "Ciudad Real", new TelefonoContacto("612345678")),
                principal);
    }

    @Test
    @DisplayName("HU5-CP12: listar devuelve 200 con las direcciones del cliente en sesión y la marca de la principal")
    void listarDevuelveLasDireccionesConLaPrincipal() throws Exception {
        when(gestionDirecciones.listar(CLIENTE)).thenReturn(List.of(direccion("d1", true), direccion("d2", false)));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("d1"))
                .andExpect(jsonPath("$[0].destinatario").value("Juan Pérez"))
                .andExpect(jsonPath("$[0].calle").value("Calle Mayor 1"))
                .andExpect(jsonPath("$[0].detalles").value("2º B"))
                .andExpect(jsonPath("$[0].codigoPostal").value("13071"))
                .andExpect(jsonPath("$[0].ciudad").value("Ciudad Real"))
                .andExpect(jsonPath("$[0].provincia").value("Ciudad Real"))
                .andExpect(jsonPath("$[0].telefono").value("612345678"))
                .andExpect(jsonPath("$[0].principal").value(true))
                .andExpect(jsonPath("$[1].principal").value(false))
                // El cliente no sale nunca en la respuesta: ya es el de la sesión
                .andExpect(jsonPath("$[0].clienteId").doesNotExist());
    }

    @Test
    @DisplayName("HU5-CP13: añadir una dirección devuelve 201 con la dirección creada; el cliente sale de la sesión")
    void anadirDevuelve201() throws Exception {
        when(gestionDirecciones.anadir(CLIENTE, COMANDO)).thenReturn(direccion("d1", true));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("d1"))
                .andExpect(jsonPath("$.principal").value(true));

        // Llega exactamente lo escrito por el cliente, con el correo de la sesión
        verify(gestionDirecciones).anadir(CLIENTE, COMANDO);
    }

    @Test
    @DisplayName("HU5-CP13: una dirección inválida devuelve 400 con el código VALIDATION_ERROR sin repetir los datos")
    void direccionInvalidaDevuelve400SinRepetirLosDatos() throws Exception {
        // Lo lanza el servicio al validar con los value objects
        when(gestionDirecciones.anadir(eq(CLIENTE), any()))
                .thenThrow(new DireccionInvalidaException("El código postal no es válido"));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                // Mensaje genérico: nunca el detalle de la regla ni los datos enviados
                .andExpect(content().string(not(containsString("612345678"))))
                .andExpect(content().string(not(containsString("código postal"))));
    }

    @Test
    @DisplayName("HU5-CP13: superar el límite de direcciones devuelve 409 con el código ADDRESS_LIMIT_REACHED")
    void superarElLimiteDevuelve409() throws Exception {
        when(gestionDirecciones.anadir(eq(CLIENTE), any()))
                .thenThrow(new LimiteDireccionesException("No se pueden tener más de 5 direcciones de envío"));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ADDRESS_LIMIT_REACHED"));
    }

    @Test
    @DisplayName("HU5-CP14: editar una dirección devuelve 200 con la dirección editada")
    void editarDevuelve200() throws Exception {
        when(gestionDirecciones.editar(CLIENTE, "d1", COMANDO)).thenReturn(direccion("d1", false));

        mockMvc.perform(put(URL + "/d1").with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("d1"))
                .andExpect(jsonPath("$.calle").value("Calle Mayor 1"))
                // El principal del cuerpo no cuenta: editar no cambia la principal
                .andExpect(jsonPath("$.principal").value(false));

        verify(gestionDirecciones).editar(CLIENTE, "d1", COMANDO);
    }

    @Test
    @DisplayName("HU5-CP14: editar con datos inválidos devuelve 400 con el código VALIDATION_ERROR")
    void editarConDatosInvalidosDevuelve400() throws Exception {
        when(gestionDirecciones.editar(eq(CLIENTE), eq("d1"), any()))
                .thenThrow(new DireccionInvalidaException("La calle es obligatoria"));

        mockMvc.perform(put(URL + "/d1").with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU5-CP14: editar una dirección inexistente o de otro cliente devuelve 404 con el código ADDRESS_NOT_FOUND")
    void editarDireccionAjenaDevuelve404() throws Exception {
        // Una ajena se responde igual que una inexistente, para no revelar que existe
        when(gestionDirecciones.editar(eq(CLIENTE), eq("de-otro"), any()))
                .thenThrow(new DireccionNoEncontradaException("La dirección de envío no existe"));

        mockMvc.perform(put(URL + "/de-otro").with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU5-CP15: marcar una dirección como principal devuelve 204")
    void marcarPrincipalDevuelve204() throws Exception {
        mockMvc.perform(put(URL + "/d2/principal").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(gestionDirecciones).marcarPrincipal(CLIENTE, "d2");
    }

    @Test
    @DisplayName("HU5-CP15: marcar una dirección inexistente o de otro cliente devuelve 404 con el código ADDRESS_NOT_FOUND")
    void marcarDireccionAjenaDevuelve404() throws Exception {
        doThrow(new DireccionNoEncontradaException("La dirección de envío no existe"))
                .when(gestionDirecciones).marcarPrincipal(CLIENTE, "de-otro");

        mockMvc.perform(put(URL + "/de-otro/principal").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU5-CP15: eliminar una dirección devuelve 204 y pasa la sustituta elegida")
    void eliminarDevuelve204ConLaSustituta() throws Exception {
        mockMvc.perform(delete(URL + "/d1").param("nuevaPrincipal", "d2")
                        .with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        // Sin sustituta (no es la principal): el parámetro es opcional
        mockMvc.perform(delete(URL + "/d3").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNoContent());

        verify(gestionDirecciones).eliminar(CLIENTE, "d1", "d2");
        verify(gestionDirecciones).eliminar(CLIENTE, "d3", null);
    }

    @Test
    @DisplayName("HU5-CP15: eliminar la principal sin elegir sustituta devuelve 409 con el código PRINCIPAL_ADDRESS_REQUIRED")
    void eliminarPrincipalSinSustitutaDevuelve409() throws Exception {
        doThrow(new NuevaPrincipalObligatoriaException("Hay que elegir qué dirección pasa a ser la principal"))
                .when(gestionDirecciones).eliminar(CLIENTE, "d1", null);

        mockMvc.perform(delete(URL + "/d1").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRINCIPAL_ADDRESS_REQUIRED"));
    }

    @Test
    @DisplayName("HU5-CP15: eliminar una dirección inexistente o de otro cliente devuelve 404 con el código ADDRESS_NOT_FOUND")
    void eliminarDireccionAjenaDevuelve404() throws Exception {
        doThrow(new DireccionNoEncontradaException("La dirección de envío no existe"))
                .when(gestionDirecciones).eliminar(CLIENTE, "de-otro", null);

        mockMvc.perform(delete(URL + "/de-otro").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU5-CP16: sin sesión no se accede a las direcciones")
    void sinSesionNoSeAccede() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isUnauthorized());

        verify(gestionDirecciones, never()).listar(any());
        verify(gestionDirecciones, never()).anadir(any(), any());
    }

    @Test
    @DisplayName("HU5-CP16: un usuario que no es cliente no accede a las direcciones")
    void unUsuarioQueNoEsClienteNoAccede() throws Exception {
        // Solo los clientes tienen direcciones de envío: vendedores y administradores reciben 403
        for (String rol : new String[] {"VENDEDOR", "ADMINISTRADOR"}) {
            mockMvc.perform(get(URL).with(user("usuario@esibuy.test").roles(rol)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete(URL + "/d1").with(csrf()).with(user("usuario@esibuy.test").roles(rol)))
                    .andExpect(status().isForbidden());
        }

        verify(gestionDirecciones, never()).listar(any());
        verify(gestionDirecciones, never()).eliminar(any(), any(), any());
    }

    @Test
    @DisplayName("HU5-CP16: una petición que modifica datos sin token CSRF se rechaza")
    void sinTokenCsrfSeRechaza() throws Exception {
        // Las cuatro operaciones que modifican datos, con sesión de cliente pero sin token
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/d1").with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/d1/principal").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/d1").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isForbidden());

        verify(gestionDirecciones, never()).anadir(any(), any());
        verify(gestionDirecciones, never()).editar(any(), any(), any());
        verify(gestionDirecciones, never()).marcarPrincipal(any(), any());
        verify(gestionDirecciones, never()).eliminar(any(), any(), any());
    }
}
