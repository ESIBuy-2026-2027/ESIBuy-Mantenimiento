package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.email;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;

public class SmtpEnvioEmailAdapter implements EnvioEmailPort {

    private static final String ASUNTO = "Código de verificación de ESIBuy";
    private static final String ASUNTO_RECUPERACION = "Recuperación de contraseña de ESIBuy";
    private final JavaMailSender mailSender;
    private final String remitente; // debe ser el remitente verificado en Brevo

    public SmtpEnvioEmailAdapter(JavaMailSender mailSender, String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarCodigoVerificacion(String email, String codigo) {
        enviar(email, ASUNTO,
                "Tu código de verificación de ESIBuy es: " + codigo + "\n\n"
                        + "Caduca en 10 minutos. Si no has intentado iniciar sesión, ignora este correo.",
                "No se ha podido enviar el código de verificación");
    }

    @Override
    public void enviarCodigoRecuperacion(String email, String codigo) {
        enviar(email, ASUNTO_RECUPERACION,
                "Tu código para restablecer la contraseña de ESIBuy es: " + codigo + "\n\n"
                        + "Caduca en 10 minutos. Si no has solicitado cambiar tu contraseña, ignora este correo.",
                "No se ha podido enviar el código de recuperación");
    }

    // El mensaje de error nunca lleva el código: la excepción puede acabar en los logs
    private void enviar(String email, String asunto, String texto, String mensajeError) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            throw new EnvioEmailException(mensajeError, e);
        }
    }
}
