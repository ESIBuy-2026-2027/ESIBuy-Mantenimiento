package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

/** La copia de la dirección de envío dentro del pedido (campo direccionEnvio de la colección pedidos). */
public class DireccionEnvioDocument {

    private String destinatario;
    private String calle;
    private String detalles;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String telefono;

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
