package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.email;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;

public class SmtpEnvioEmailAdapter implements EnvioEmailPort {

    private static final String ASUNTO = "Código de verificación de ESIBuy";
    private final JavaMailSender mailSender;
    private final String remitente; // debe ser el remitente verificado en Brevo

    public SmtpEnvioEmailAdapter(JavaMailSender mailSender, String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarCodigoVerificacion(String email, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject(ASUNTO);
        mensaje.setText("Tu código de verificación de ESIBuy es: " + codigo + "\n\n"
                + "Caduca en 10 minutos. Si no has intentado iniciar sesión, ignora este correo.");

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
             throw new EnvioEmailException("No se ha podido enviar el código de verificación", e);
        }
        
    }


}
