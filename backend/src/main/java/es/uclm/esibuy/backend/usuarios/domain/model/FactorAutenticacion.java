package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Instant;

public class FactorAutenticacion {

        private final String usuarioId;
        private final String secretoCifrado;
        private boolean confirmado;
        private Instant verificadoEn;
        private Long ultimoPeriodoUsado;

        private FactorAutenticacion (String usuarioId, String secretoCifrado) {
            this.usuarioId = usuarioId;
            this.secretoCifrado = secretoCifrado;
        }

        public static FactorAutenticacion nuevo(String usuarioId, String secretoCifrado){
            return new FactorAutenticacion(usuarioId, secretoCifrado);
        }

        public boolean puedeUsarPeriodo(long periodo) {
            return ultimoPeriodoUsado == null || periodo > ultimoPeriodoUsado;
        }

        public void registrarUso(long periodo, Instant ahora) {
            ultimoPeriodoUsado = periodo;
            if (!confirmado) {
                confirmado = true;
                verificadoEn = ahora;
            }
        }

        public boolean estaConfirmado() {
            return confirmado;
        }

        public Instant getVerificadoEn() {
            return verificadoEn;
        }

        
}
