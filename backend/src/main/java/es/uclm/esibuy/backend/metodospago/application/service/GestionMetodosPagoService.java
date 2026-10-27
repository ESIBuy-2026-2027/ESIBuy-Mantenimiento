package es.uclm.esibuy.backend.metodospago.application.service;

import es.uclm.esibuy.backend.metodospago.application.port.in.AnadirMetodoPagoCommand;
import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.metodospago.application.port.out.CifradoDatosPagoPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.MetodoPagoRepositoryPort;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import es.uclm.esibuy.backend.metodospago.domain.model.CaducidadTarjeta;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodosPagoCliente;
import es.uclm.esibuy.backend.metodospago.domain.model.NumeroTarjeta;
import es.uclm.esibuy.backend.metodospago.domain.model.TelefonoBizum;
import es.uclm.esibuy.backend.metodospago.domain.model.TitularTarjeta;
import es.uclm.esibuy.backend.metodospago.domain.model.UrlPayPal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

/**
 * Implementa la gestión de métodos de pago del cliente. Solo habla con el exterior a través de los puertos
 * de salida. El reloj se inyecta para fijar en los tests el mes con el que se comprueba la vigencia.
 */
public class GestionMetodosPagoService implements GestionMetodosPagoUseCase {

    // Separa los campos del bloque que se cifra. Ningún campo puede contenerlo: el titular solo admite
    // letras, espacios, guion y apóstrofo; el número y la caducidad, dígitos y '/'.
    private static final String SEPARADOR = ";";

    private final MetodoPagoRepositoryPort metodoPagoRepository;
    private final CifradoDatosPagoPort cifradoDatosPago;
    private final ClienteEnSesionPort clienteEnSesion;
    private final Clock reloj;

    public GestionMetodosPagoService(MetodoPagoRepositoryPort metodoPagoRepository,
            CifradoDatosPagoPort cifradoDatosPago, ClienteEnSesionPort clienteEnSesion, Clock reloj) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.cifradoDatosPago = cifradoDatosPago;
        this.clienteEnSesion = clienteEnSesion;
        this.reloj = reloj;
    }

    @Override
    public List<MetodoPago> listar(String emailCliente) {
        return metodoPagoRepository.buscarPorCliente(clienteEnSesion.idPorCorreo(emailCliente));
    }

    @Override
    public MetodoPago anadir(AnadirMetodoPagoCommand comando) {
        // Sin tipo no se sabe qué datos validar; el switch sobre null fallaría con NullPointerException
        if (comando.tipo() == null) {
            throw new MetodoPagoInvalidoException("El tipo de método de pago es obligatorio");
        }
        String clienteId = clienteEnSesion.idPorCorreo(comando.emailCliente());
        DatosValidados datos = switch (comando.tipo()) {
            case TARJETA -> validarTarjeta(comando);
            case PAYPAL -> validarPayPal(comando);
            case BIZUM -> validarBizum(comando);
        };
        String datosCifrados = cifradoDatosPago.cifrar(datos.enClaro());
        MetodoPago metodo = MetodoPago.nuevo(clienteId, comando.tipo(), datos.mascara(), datosCifrados);
        // Las reglas del conjunto (límite y predeterminado) viven en el dominio
        new MetodosPagoCliente(metodoPagoRepository.buscarPorCliente(clienteId)).anadir(metodo);
        return metodoPagoRepository.guardar(metodo);
    }

    @Override
    public void marcarPredeterminado(String emailCliente, String metodoId) {
        MetodosPagoCliente metodos = metodosDelCliente(emailCliente);
        metodos.marcarPredeterminado(metodoId);
        // Se guardan todos: el cambio afecta al nuevo predeterminado y al que deja de serlo
        metodos.getMetodos().forEach(metodoPagoRepository::guardar);
    }

    @Override
    public void eliminar(String emailCliente, String metodoId, String nuevoPredeterminadoId) {
        MetodosPagoCliente metodos = metodosDelCliente(emailCliente);
        // Primero la regla del dominio: si falla, no se borra nada
        metodos.eliminar(metodoId, nuevoPredeterminadoId);
        metodoPagoRepository.eliminar(metodoId);
        // Los que quedan se guardan por si ha cambiado el predeterminado
        metodos.getMetodos().forEach(metodoPagoRepository::guardar);
    }

    // Solo los del cliente en sesión: un id de otro cliente no se encuentra
    private MetodosPagoCliente metodosDelCliente(String emailCliente) {
        return new MetodosPagoCliente(
                metodoPagoRepository.buscarPorCliente(clienteEnSesion.idPorCorreo(emailCliente)));
    }

    private DatosValidados validarTarjeta(AnadirMetodoPagoCommand comando) {
        NumeroTarjeta numero = new NumeroTarjeta(comando.numeroTarjeta());
        TitularTarjeta titular = new TitularTarjeta(comando.titular());
        CaducidadTarjeta caducidad = new CaducidadTarjeta(comando.caducidad());
        // Solo se exige al dar de alta: una tarjeta guardada que caduque después se sigue pudiendo cargar
        if (!caducidad.vigenteEn(YearMonth.now(reloj))) {
            throw new MetodoPagoInvalidoException("La tarjeta está caducada");
        }
        return new DatosValidados(numero.mascara(),
                String.join(SEPARADOR, numero.valor(), titular.valor(), caducidad.valor()));
    }

    private static DatosValidados validarPayPal(AnadirMetodoPagoCommand comando) {
        UrlPayPal url = new UrlPayPal(comando.urlPayPal());
        return new DatosValidados(url.mascara(), url.valor());
    }

    private static DatosValidados validarBizum(AnadirMetodoPagoCommand comando) {
        TelefonoBizum telefono = new TelefonoBizum(comando.telefonoBizum());
        return new DatosValidados(telefono.mascara(), telefono.valor());
    }

    // Lo que sale de validar un método: la máscara que puede mostrarse y el texto en claro que se cifra
    private record DatosValidados(String mascara, String enClaro) {
    }
}
