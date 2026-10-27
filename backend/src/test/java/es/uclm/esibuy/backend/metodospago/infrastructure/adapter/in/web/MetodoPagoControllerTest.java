package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.metodospago.application.port.in.AnadirMetodoPagoCommand;
import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.metodospago.domain.exception.LimiteMetodosPagoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.NuevoPredeterminadoObligatorioException;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
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

// Métodos de pago del cliente (HU6): GET, POST, PUT /{id}/default y DELETE /{id} sobre
// /api/customer/payment-methods. El caso de uso es un doble de prueba; la seguridad es la real
// (SecurityConfig: /api/customer/** solo para CLIENTE, con CSRF).
@WebMvcTest(MetodoPagoController.class)
@Import(SecurityConfig.class)
class MetodoPagoControllerTest {

    private static final String URL = "/api/customer/payment-methods";
    private static final String CLIENTE = "cliente@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionMetodosPagoUseCase gestionMetodosPago;

    // Tarjeta de prueba de Visa; el CVV solo se valida
    private static final String TARJETA_JSON = """
            {"tipo": "TARJETA", "numeroTarjeta": "4111111111111111", "titular": "Juan Pérez",
             "caducidad": "12/27", "cvv": "123", "clienteId": "otro-cliente"}
            """;

    private static MetodoPago tarjeta(String id, boolean predeterminado) {
        return new MetodoPago(id, "c-1", TipoMetodoPago.TARJETA, "•••• 1111", "bloque-cifrado", predeterminado);
    }

    @Test
    @DisplayName("HU6-CP20: listar devuelve 200 con los métodos del cliente en sesión, sin datos cifrados")
    void listarDevuelveLosMetodosSinDatosCifrados() throws Exception {
        when(gestionMetodosPago.listar(CLIENTE)).thenReturn(List.of(tarjeta("m1", true)));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("m1"))
                .andExpect(jsonPath("$[0].tipo").value("TARJETA"))
                .andExpect(jsonPath("$[0].mascara").value("•••• 1111"))
                .andExpect(jsonPath("$[0].predeterminado").value(true))
                // Ni el bloque cifrado ni el cliente salen nunca en la respuesta
                .andExpect(jsonPath("$[0].datosCifrados").doesNotExist())
                .andExpect(jsonPath("$[0].clienteId").doesNotExist());
    }

    @Test
    @DisplayName("HU6-CP21: añadir una tarjeta devuelve 201 con el método creado; el cliente sale de la sesión")
    void anadirTarjetaDevuelve201() throws Exception {
        when(gestionMetodosPago.anadir(any())).thenReturn(tarjeta("m1", true));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(TARJETA_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("m1"))
                .andExpect(jsonPath("$.mascara").value("•••• 1111"))
                .andExpect(jsonPath("$.datosCifrados").doesNotExist());

        // El cliente es el de la sesión: el clienteId del cuerpo se ignora
        ArgumentCaptor<AnadirMetodoPagoCommand> comando = ArgumentCaptor.forClass(AnadirMetodoPagoCommand.class);
        verify(gestionMetodosPago).anadir(comando.capture());
        assertEquals(CLIENTE, comando.getValue().emailCliente());
    }

    @Test
    @DisplayName("HU6-CP21: el CVV no llega al caso de uso")
    void elCvvNoLlegaAlCasoDeUso() throws Exception {
        when(gestionMetodosPago.anadir(any())).thenReturn(tarjeta("m1", true));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(TARJETA_JSON));

        // El comando no tiene campo para el CVV: lo que llega es exactamente esto
        ArgumentCaptor<AnadirMetodoPagoCommand> comando = ArgumentCaptor.forClass(AnadirMetodoPagoCommand.class);
        verify(gestionMetodosPago).anadir(comando.capture());
        assertEquals(new AnadirMetodoPagoCommand(CLIENTE, TipoMetodoPago.TARJETA, "4111111111111111", "Juan Pérez",
                "12/27", null, null), comando.getValue());
        assertFalse(comando.getValue().toString().contains("123"), comando.getValue().toString());
    }

    @Test
    @DisplayName("HU6-CP21: una tarjeta sin CVV o con un CVV que no tiene 3 o 4 dígitos devuelve 400")
    void tarjetaConCvvAusenteOInvalidoDevuelve400() throws Exception {
        // Sin CVV, y en las fronteras de 2 y 5 dígitos, y con una letra; 3 (Visa) y 4 (Amex) sí valen
        for (String cvv : new String[] {"", "\"cvv\": \"12\",", "\"cvv\": \"12345\",", "\"cvv\": \"12a\","}) {
            String json = """
                    {"tipo": "TARJETA", "numeroTarjeta": "4111111111111111", "titular": "Juan Pérez", %s
                     "caducidad": "12/27"}
                    """.formatted(cvv);

            mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                            .contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest());
        }
        verify(gestionMetodosPago, never()).anadir(any());
    }

    @Test
    @DisplayName("HU6-CP21: un método sin tipo devuelve 400")
    void metodoSinTipoDevuelve400() throws Exception {
        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"telefonoBizum\": \"612345678\"}"))
                .andExpect(status().isBadRequest());

        verify(gestionMetodosPago, never()).anadir(any());
    }

    @Test
    @DisplayName("HU6-CP21: un dato inválido o una tarjeta caducada devuelve 400 con el código VALIDATION_ERROR sin repetir los datos")
    void datoInvalidoDevuelve400SinRepetirLosDatos() throws Exception {
        // Lo lanza el servicio al validar con los value objects (número, caducidad vencida...)
        when(gestionMetodosPago.anadir(any())).thenThrow(new MetodoPagoInvalidoException("La tarjeta está caducada"));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(TARJETA_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                // Mensaje genérico: nunca el detalle de la regla ni los datos enviados
                .andExpect(content().string(not(containsString("4111111111111111"))))
                .andExpect(content().string(not(containsString("caducada"))));
    }

    @Test
    @DisplayName("HU6-CP21: superar el límite de métodos devuelve 409 con el código PAYMENT_METHOD_LIMIT_REACHED")
    void superarElLimiteDevuelve409() throws Exception {
        when(gestionMetodosPago.anadir(any()))
                .thenThrow(new LimiteMetodosPagoException("No se pueden tener más de 5 métodos de pago"));

        mockMvc.perform(post(URL).with(csrf()).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(TARJETA_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAYMENT_METHOD_LIMIT_REACHED"));
    }

    @Test
    @DisplayName("HU6-CP22: marcar un método como predeterminado devuelve 204")
    void marcarPredeterminadoDevuelve204() throws Exception {
        mockMvc.perform(put(URL + "/m2/default").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(gestionMetodosPago).marcarPredeterminado(CLIENTE, "m2");
    }

    @Test
    @DisplayName("HU6-CP22: marcar un método inexistente o de otro cliente devuelve 404 con el código PAYMENT_METHOD_NOT_FOUND")
    void marcarMetodoAjenoDevuelve404() throws Exception {
        // Uno ajeno se responde igual que uno inexistente, para no revelar que existe
        doThrow(new MetodoPagoNoEncontradoException("El método de pago no existe"))
                .when(gestionMetodosPago).marcarPredeterminado(CLIENTE, "de-otro");

        mockMvc.perform(put(URL + "/de-otro/default").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PAYMENT_METHOD_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU6-CP23: eliminar un método devuelve 204 y pasa el sustituto elegido")
    void eliminarDevuelve204ConElSustituto() throws Exception {
        mockMvc.perform(delete(URL + "/m1").param("nuevoPredeterminado", "m2")
                        .with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        // Sin sustituto (no es el predeterminado): el parámetro es opcional
        mockMvc.perform(delete(URL + "/m3").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNoContent());

        verify(gestionMetodosPago).eliminar(CLIENTE, "m1", "m2");
        verify(gestionMetodosPago).eliminar(CLIENTE, "m3", null);
    }

    @Test
    @DisplayName("HU6-CP23: eliminar el predeterminado sin elegir sustituto devuelve 409 con el código DEFAULT_PAYMENT_METHOD_REQUIRED")
    void eliminarPredeterminadoSinSustitutoDevuelve409() throws Exception {
        doThrow(new NuevoPredeterminadoObligatorioException("Hay que elegir qué método pasa a ser el predeterminado"))
                .when(gestionMetodosPago).eliminar(CLIENTE, "m1", null);

        mockMvc.perform(delete(URL + "/m1").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEFAULT_PAYMENT_METHOD_REQUIRED"));
    }

    @Test
    @DisplayName("HU6-CP23: eliminar un método inexistente o de otro cliente devuelve 404 con el código PAYMENT_METHOD_NOT_FOUND")
    void eliminarMetodoAjenoDevuelve404() throws Exception {
        doThrow(new MetodoPagoNoEncontradoException("El método de pago no existe"))
                .when(gestionMetodosPago).eliminar(CLIENTE, "de-otro", null);

        mockMvc.perform(delete(URL + "/de-otro").with(csrf()).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PAYMENT_METHOD_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU6-CP24: sin sesión no se accede a los métodos de pago")
    void sinSesionNoSeAccede() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TARJETA_JSON))
                .andExpect(status().isUnauthorized());

        verify(gestionMetodosPago, never()).listar(any());
        verify(gestionMetodosPago, never()).anadir(any());
    }

    @Test
    @DisplayName("HU6-CP24: un usuario que no es cliente no accede a los métodos de pago")
    void unUsuarioQueNoEsClienteNoAccede() throws Exception {
        // Solo los clientes tienen métodos de pago: vendedores y administradores reciben 403
        for (String rol : new String[] {"VENDEDOR", "ADMINISTRADOR"}) {
            mockMvc.perform(get(URL).with(user("usuario@esibuy.test").roles(rol)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete(URL + "/m1").with(csrf()).with(user("usuario@esibuy.test").roles(rol)))
                    .andExpect(status().isForbidden());
        }

        verify(gestionMetodosPago, never()).listar(any());
        verify(gestionMetodosPago, never()).eliminar(any(), any(), any());
    }

    @Test
    @DisplayName("HU6-CP24: una petición que modifica datos sin token CSRF se rechaza")
    void sinTokenCsrfSeRechaza() throws Exception {
        // Las tres operaciones que modifican datos, con sesión de cliente pero sin token
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(TARJETA_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/m1/default").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/m1").with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isForbidden());

        verify(gestionMetodosPago, never()).anadir(any());
        verify(gestionMetodosPago, never()).marcarPredeterminado(any(), any());
        verify(gestionMetodosPago, never()).eliminar(any(), any(), any());
    }
}
