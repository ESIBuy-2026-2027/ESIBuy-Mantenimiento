package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;


/**
 * SiguientePasoResponse
 */
public record SiguientePasoResponse(String nextStep) { // <- "nextStep va en ingles porque pasa al JSON con ese nombre, se encarga el propio Spring de esta transformación"

    public static SiguientePasoResponse desde(PasoLogin paso) {
        return new SiguientePasoResponse(switch (paso) {
            case CODIGO_EMAIL -> "emailCode";
            case AUTHENTICATOR -> "authenticator";
        });

    }

}
