package es.uclm.esibuy.backend.productos.application.port.out;

// Lo que responde el antivirus sobre la URL de una imagen (HU31, T397)
public enum ResultadoAnalisis {
    SEGURA,
    MALICIOSA,
    SIN_RESULTADO   // el antivirus no ha podido dar una respuesta (caído, sin cuota, sin terminar...)
}
