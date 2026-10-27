package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda un método de pago en MongoDB (colección metodos_pago, HU6). Solo lleva la máscara
 * y el bloque ya cifrado: los datos de pago nunca llegan en claro a la base de datos.
 */
@Document(collection = "metodos_pago")
public class MetodoPagoDocument {

    @Id
    private String id;
    // Se busca siempre por cliente: el índice evita recorrer toda la colección
    @Indexed
    private String clienteId;
    private String tipo;
    private String mascara;
    private String datosCifrados;
    private boolean predeterminado;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMascara() { return mascara; }
    public void setMascara(String mascara) { this.mascara = mascara; }
    public String getDatosCifrados() { return datosCifrados; }
    public void setDatosCifrados(String datosCifrados) { this.datosCifrados = datosCifrados; }
    public boolean isPredeterminado() { return predeterminado; }
    public void setPredeterminado(boolean predeterminado) { this.predeterminado = predeterminado; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
