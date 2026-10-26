package es.uclm.esibuy.backend.metodospago.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.metodospago.application.port.in.AnadirMetodoPagoCommand;
import es.uclm.esibuy.backend.metodospago.application.port.out.CifradoDatosPagoPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.MetodoPagoRepositoryPort;
import es.uclm.esibuy.backend.metodospago.domain.exception.LimiteMetodosPagoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.NuevoPredeterminadoObligatorioException;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba (clases internas de este test)
class GestionMetodosPagoServiceTest {

    private static final String EMAIL_CLIENTE = "cliente@esibuy.es";
    private static final String ID_CLIENTE = "cliente-1";
    // 15 de octubre de 2026: fija el mes con el que se comprueba la vigencia de las tarjetas
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneOffset.UTC);

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final CifradoFalso cifrado = new CifradoFalso();
    private final GestionMetodosPagoService servicio =
            new GestionMetodosPagoService(repositorio, cifrado, new ClienteEnSesionFijo(), RELOJ);

    private static AnadirMetodoPagoCommand tarjeta(String numero, String titular, String caducidad) {
        return new AnadirMetodoPagoCommand(EMAIL_CLIENTE, TipoMetodoPago.TARJETA, numero, titular, caducidad,
                null, null);
    }

    @Test
    @DisplayName("HU6-CP11: añadir una tarjeta guarda su máscara y un bloque cifrado")
    void anadirTarjetaGuardaMascaraYDatosCifrados() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));

        assertEquals(1, repositorio.guardados.size());
        MetodoPago guardado = repositorio.guardados.get(0);
        assertEquals(ID_CLIENTE, guardado.getClienteId());
        assertEquals(TipoMetodoPago.TARJETA, guardado.getTipo());
        assertEquals("•••• 1111", guardado.getMascara());
        assertEquals(cifrado.ultimoResultado, guardado.getDatosCifrados());
    }

    @Test
    @DisplayName("HU6-CP11: el bloque cifrado reúne número, titular y caducidad de la tarjeta")
    void anadirTarjetaCifraNumeroTitularYCaducidadEnUnBloque() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));

        // Formato acordado: campos separados por ';' (el titular no puede contener ';')
        assertEquals("4111111111111111;Juan Pérez;12/27", cifrado.ultimoEnClaro);
    }

    @Test
    @DisplayName("HU6-CP11: ni el número ni el titular de la tarjeta se guardan en claro")
    void anadirTarjetaNoGuardaNumeroNiTitularEnClaro() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));

        // Repasa todo lo que se guarda en texto: ni la máscara ni el bloque pueden llevar el dato en claro
        MetodoPago guardado = repositorio.guardados.get(0);
        for (String campo : List.of(guardado.getMascara(), guardado.getDatosCifrados())) {
            assertFalse(campo.contains("4111111111111111"), "El número de tarjeta aparece en claro: " + campo);
            assertFalse(campo.contains("Juan Pérez"), "El titular aparece en claro: " + campo);
        }
    }

    @Test
    @DisplayName("HU6-CP12: rechaza una tarjeta caducada y no la guarda")
    void rechazaTarjetaCaducada() {
        // El reloj marca octubre de 2026: septiembre ya ha pasado
        AnadirMetodoPagoCommand caducada = tarjeta("4111111111111111", "Juan Pérez", "09/26");

        assertThrows(MetodoPagoInvalidoException.class, () -> servicio.anadir(caducada));
        assertTrue(repositorio.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU6-CP12: acepta una tarjeta que caduca este mes")
    void aceptaTarjetaQueCaducaEsteMes() {
        // Frontera: el reloj marca octubre de 2026 y la tarjeta es válida durante todo su mes de caducidad
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "10/26"));

        assertEquals(1, repositorio.guardados.size());
    }

    @Test
    @DisplayName("HU6-CP13: añade un método PayPal con su máscara")
    void anadePayPalConSuMascara() {
        servicio.anadir(new AnadirMetodoPagoCommand(EMAIL_CLIENTE, TipoMetodoPago.PAYPAL, null, null, null,
                "https://paypal.me/juanperez", null));

        MetodoPago guardado = repositorio.guardados.get(0);
        assertEquals(TipoMetodoPago.PAYPAL, guardado.getTipo());
        assertEquals("paypal.me", guardado.getMascara());
        // Se cifra la URL completa: la ruta identifica la cuenta
        assertEquals("https://paypal.me/juanperez", cifrado.ultimoEnClaro);
        assertEquals(cifrado.ultimoResultado, guardado.getDatosCifrados());
    }

    @Test
    @DisplayName("HU6-CP13: añade un método Bizum con su máscara")
    void anadeBizumConSuMascara() {
        servicio.anadir(new AnadirMetodoPagoCommand(EMAIL_CLIENTE, TipoMetodoPago.BIZUM, null, null, null, null,
                "612345678"));

        MetodoPago guardado = repositorio.guardados.get(0);
        assertEquals(TipoMetodoPago.BIZUM, guardado.getTipo());
        assertEquals("•••• 678", guardado.getMascara());
        assertEquals("612345678", cifrado.ultimoEnClaro);
        assertEquals(cifrado.ultimoResultado, guardado.getDatosCifrados());
    }

    @Test
    @DisplayName("HU6-CP14: rechaza una tarjeta sin número, titular o caducidad")
    void rechazaTarjetaConDatosIncompletos() {
        assertThrows(MetodoPagoInvalidoException.class, () -> servicio.anadir(tarjeta(null, "Juan Pérez", "12/27")));
        assertThrows(MetodoPagoInvalidoException.class, () -> servicio.anadir(tarjeta("4111111111111111", null, "12/27")));
        assertThrows(MetodoPagoInvalidoException.class,
                () -> servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", null)));
        assertTrue(repositorio.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU6-CP14: rechaza un método PayPal sin URL")
    void rechazaPayPalSinUrl() {
        // Aunque traiga datos de otro tipo: para PayPal solo cuenta la URL
        AnadirMetodoPagoCommand sinUrl = new AnadirMetodoPagoCommand(EMAIL_CLIENTE, TipoMetodoPago.PAYPAL,
                "4111111111111111", "Juan Pérez", "12/27", null, "612345678");

        assertThrows(MetodoPagoInvalidoException.class, () -> servicio.anadir(sinUrl));
        assertTrue(repositorio.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU6-CP14: rechaza un método Bizum sin teléfono")
    void rechazaBizumSinTelefono() {
        // Aunque traiga datos de otro tipo: para Bizum solo cuenta el teléfono
        AnadirMetodoPagoCommand sinTelefono = new AnadirMetodoPagoCommand(EMAIL_CLIENTE, TipoMetodoPago.BIZUM,
                "4111111111111111", "Juan Pérez", "12/27", "https://paypal.me/juanperez", null);

        assertThrows(MetodoPagoInvalidoException.class, () -> servicio.anadir(sinTelefono));
        assertTrue(repositorio.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU6-CP14: rechaza un método sin tipo")
    void rechazaMetodoSinTipo() {
        AnadirMetodoPagoCommand sinTipo = new AnadirMetodoPagoCommand(EMAIL_CLIENTE, null,
                "4111111111111111", "Juan Pérez", "12/27", null, null);

        assertThrows(MetodoPagoInvalidoException.class, () -> servicio.anadir(sinTipo));
        assertTrue(repositorio.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU6-CP15: listar devuelve solo los métodos del cliente en sesión")
    void listaSoloLosMetodosDelClienteEnSesion() {
        MetodoPago propio = repositorio.guardar(
                MetodoPago.nuevo(ID_CLIENTE, TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado"));
        repositorio.guardar(MetodoPago.nuevo("otro-cliente", TipoMetodoPago.PAYPAL, "paypal.me", "bloque-cifrado"));

        List<MetodoPago> metodos = servicio.listar(EMAIL_CLIENTE);

        assertEquals(List.of(propio.getId()), metodos.stream().map(MetodoPago::getId).toList());
    }

    @Test
    @DisplayName("HU6-CP08: rechaza añadir un sexto método y no lo guarda")
    void rechazaSextoMetodoYNoLoGuarda() {
        for (int i = 0; i < 5; i++) {
            repositorio.guardar(MetodoPago.nuevo(ID_CLIENTE, TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado"));
        }

        AnadirMetodoPagoCommand sexto = tarjeta("4111111111111111", "Juan Pérez", "12/27");

        assertThrows(LimiteMetodosPagoException.class, () -> servicio.anadir(sexto));
        assertEquals(5, repositorio.guardados.size());
    }

    @Test
    @DisplayName("HU6-CP09: el primer método añadido se guarda como predeterminado")
    void primerMetodoAnadidoSeGuardaPredeterminado() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));
        servicio.anadir(tarjeta("5555555555554444", "Juan Pérez", "12/27"));

        // Lo que importa es lo guardado, no el objeto en memoria
        assertTrue(repositorio.guardados.get(0).isPredeterminado());
        assertFalse(repositorio.guardados.get(1).isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: marcar otro método como predeterminado guarda el cambio en los dos")
    void marcarPredeterminadoGuardaElCambio() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));
        String segundo = servicio.anadir(tarjeta("5555555555554444", "Juan Pérez", "12/27")).getId();

        servicio.marcarPredeterminado(EMAIL_CLIENTE, segundo);

        // Se guardan los dos: el nuevo predeterminado y el que deja de serlo
        assertTrue(repositorio.buscarGuardado(segundo).isPredeterminado());
        assertFalse(repositorio.buscarGuardado("m1").isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: rechaza marcar como predeterminado un método de otro cliente")
    void rechazaMarcarMetodoDeOtroCliente() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));
        String ajeno = repositorio.guardar(
                MetodoPago.nuevo("otro-cliente", TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado")).getId();

        assertThrows(MetodoPagoNoEncontradoException.class,
                () -> servicio.marcarPredeterminado(EMAIL_CLIENTE, ajeno));
        // Ni el método ajeno ni el propio cambian
        assertFalse(repositorio.buscarGuardado(ajeno).isPredeterminado());
        assertTrue(repositorio.buscarGuardado("m1").isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar un método lo borra del repositorio")
    void eliminarMetodoLoBorraDelRepositorio() {
        servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27"));
        String segundo = servicio.anadir(tarjeta("5555555555554444", "Juan Pérez", "12/27")).getId();

        // No es el predeterminado: no hace falta elegir sustituto
        servicio.eliminar(EMAIL_CLIENTE, segundo, null);

        assertEquals(List.of("m1"), repositorio.guardados.stream().map(MetodoPago::getId).toList());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar el predeterminado guarda el nuevo predeterminado elegido")
    void eliminarPredeterminadoGuardaElNuevoPredeterminado() {
        String predeterminado = servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27")).getId();
        String sustituto = servicio.anadir(tarjeta("5555555555554444", "Juan Pérez", "12/27")).getId();

        servicio.eliminar(EMAIL_CLIENTE, predeterminado, sustituto);

        assertEquals(List.of(sustituto), repositorio.guardados.stream().map(MetodoPago::getId).toList());
        assertTrue(repositorio.buscarGuardado(sustituto).isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar el predeterminado sin elegir otro no borra nada")
    void eliminarPredeterminadoSinElegirNoBorraNada() {
        String predeterminado = servicio.anadir(tarjeta("4111111111111111", "Juan Pérez", "12/27")).getId();
        servicio.anadir(tarjeta("5555555555554444", "Juan Pérez", "12/27"));

        assertThrows(NuevoPredeterminadoObligatorioException.class,
                () -> servicio.eliminar(EMAIL_CLIENTE, predeterminado, null));
        assertEquals(2, repositorio.guardados.size());
        assertTrue(repositorio.buscarGuardado(predeterminado).isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: rechaza eliminar un método de otro cliente")
    void rechazaEliminarMetodoDeOtroCliente() {
        String ajeno = repositorio.guardar(
                MetodoPago.nuevo("otro-cliente", TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado")).getId();

        assertThrows(MetodoPagoNoEncontradoException.class, () -> servicio.eliminar(EMAIL_CLIENTE, ajeno, null));
        // El método del otro cliente sigue guardado
        assertEquals(List.of(ajeno), repositorio.guardados.stream().map(MetodoPago::getId).toList());
    }

    // Repositorio en memoria: asigna un id al guardar, como haría MongoDB. Guarda y devuelve copias, para que
    // un cambio solo llegue a lo guardado si el servicio llama a guardar()
    private static class RepositorioEnMemoria implements MetodoPagoRepositoryPort {

        private final List<MetodoPago> guardados = new ArrayList<>();

        @Override
        public List<MetodoPago> buscarPorCliente(String clienteId) {
            return guardados.stream()
                    .filter(metodo -> metodo.getClienteId().equals(clienteId))
                    .map(metodo -> copia(metodo, metodo.getId()))
                    .toList();
        }

        @Override
        public MetodoPago guardar(MetodoPago metodo) {
            String id = metodo.getId() != null ? metodo.getId() : "m" + (guardados.size() + 1);
            MetodoPago conId = copia(metodo, id);
            int posicion = posicionDe(id);
            if (posicion >= 0) {
                guardados.set(posicion, conId);
            } else {
                guardados.add(conId);
            }
            return copia(conId, id);
        }

        @Override
        public void eliminar(String metodoId) {
            guardados.removeIf(metodo -> metodo.getId().equals(metodoId));
        }

        private MetodoPago buscarGuardado(String id) {
            return guardados.get(posicionDe(id));
        }

        private int posicionDe(String id) {
            for (int i = 0; i < guardados.size(); i++) {
                if (guardados.get(i).getId().equals(id)) {
                    return i;
                }
            }
            return -1;
        }

        private static MetodoPago copia(MetodoPago metodo, String id) {
            MetodoPago copia = new MetodoPago(id, metodo.getClienteId(), metodo.getTipo(), metodo.getMascara(),
                    metodo.getDatosCifrados());
            if (metodo.isPredeterminado()) {
                copia.marcarPredeterminado();
            }
            return copia;
        }
    }

    // Cifrado falso: recuerda lo que recibe en claro y devuelve un texto que no lo contiene literalmente
    private static class CifradoFalso implements CifradoDatosPagoPort {

        private String ultimoEnClaro;
        private String ultimoResultado;

        @Override
        public String cifrar(String datosEnClaro) {
            ultimoEnClaro = datosEnClaro;
            ultimoResultado = "cifrado:" + Base64.getEncoder().encodeToString(
                    datosEnClaro.getBytes(StandardCharsets.UTF_8));
            return ultimoResultado;
        }
    }

    // Siempre el mismo cliente en sesión
    private static class ClienteEnSesionFijo implements ClienteEnSesionPort {

        @Override
        public String idPorCorreo(String email) {
            return EMAIL_CLIENTE.equals(email) ? ID_CLIENTE : "otro-cliente";
        }
    }
}
