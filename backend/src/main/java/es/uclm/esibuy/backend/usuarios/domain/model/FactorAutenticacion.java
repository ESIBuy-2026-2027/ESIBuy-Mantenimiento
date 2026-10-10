package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Instant;

public class FactorAutenticacion {

        private final String id;
        private final String usuarioId;
        private final String secretoCifrado;
        private boolean confirmado;
        private Instant verificadoEn;
        private Long ultimoPeriodoUsado;

        private FactorAutenticacion (String id, String usuarioId, String secretoCifrado) {
            this.id = id;
            this.usuarioId = usuarioId;
            this.secretoCifrado = secretoCifrado;
        }

        public static FactorAutenticacion nuevo(String usuarioId, String secretoCifrado){
            return new FactorAutenticacion(null, usuarioId, secretoCifrado);
        }

        // Factor leído de la base de datos, con el estado que tenía (T20, ciclo 4)
        public static FactorAutenticacion reconstruir(String id, String usuarioId, String secretoCifrado,
                boolean confirmado, Instant verificadoEn, Long ultimoPeriodoUsado) {
            FactorAutenticacion factor = new FactorAutenticacion(id, usuarioId, secretoCifrado);
            factor.confirmado = confirmado;
            factor.verificadoEn = verificadoEn;
            factor.ultimoPeriodoUsado = ultimoPeriodoUsado;
            return factor;
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

        public String getId() {
            return id;
        }

        public String getUsuarioId() {
            return usuarioId;
        }

        public String getSecretoCifrado() {
            return secretoCifrado;
        }

        public Long getUltimoPeriodoUsado() {
            return ultimoPeriodoUsado;
        }

        

        
}
