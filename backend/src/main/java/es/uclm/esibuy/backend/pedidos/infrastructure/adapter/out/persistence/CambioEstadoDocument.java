package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.time.Instant;

/** Un paso del histórico de un pedido dentro de su documento de MongoDB. */
public class CambioEstadoDocument {

    private String estado;
    private Instant fecha;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
