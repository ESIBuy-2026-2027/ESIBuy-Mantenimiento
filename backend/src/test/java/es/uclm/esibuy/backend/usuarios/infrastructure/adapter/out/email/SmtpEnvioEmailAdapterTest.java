package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.email;

import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

// Test unitario con un mock de JavaMailSender (T231, D4): no se conecta a Brevo ni envía nada.
// Comprueba qué mensaje monta el adaptador y que se lo entrega a Spring para enviarlo. Que Brevo
// lo entregue de verdad se comprueba a mano al final de la task (no puede ir en el CI).
@ExtendWith(MockitoExtension.class)
class SmtpEnvioEmailAdapterTest {

    private static final String REMITENTE = "ESIBuy <esibuy02@outlook.com>";

    @Mock
    private JavaMailSender mailSender;

    private EnvioEmailPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new SmtpEnvioEmailAdapter(mailSender, REMITENTE);
    }

    @Test
    @DisplayName("HU2-CP05: envía el código al correo del usuario")
    void enviaElCodigoAlCorreoDelUsuario() {
        adapter.enviarCodigoVerificacion("juan@uclm.es", "042137");

        // El captor atrapa el mensaje que el adaptador le pasó a send(...) para poder revisarlo
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage mensaje = captor.getValue();

        assertArrayEquals(new String[] {"juan@uclm.es"}, mensaje.getTo());
        assertEquals(REMITENTE, mensaje.getFrom());
        assertEquals("Código de verificación de ESIBuy", mensaje.getSubject()); // D3: correo básico
        assertTrue(mensaje.getText().contains("042137"), "El cuerpo debe llevar el código");
        assertTrue(mensaje.getText().contains("10 minutos"), "El cuerpo debe indicar la validez");
    }
}
