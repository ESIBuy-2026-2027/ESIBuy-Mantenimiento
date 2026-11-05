package es.uclm.esibuy.backend.direcciones.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionCommand;
import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionParaPedido;
import es.uclm.esibuy.backend.direcciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.direcciones.application.port.out.DireccionRepositoryPort;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionNoEncontradaException;
import es.uclm.esibuy.backend.direcciones.domain.exception.LimiteDireccionesException;
import es.uclm.esibuy.backend.direcciones.domain.exception.NuevaPrincipalObligatoriaException;
import es.uclm.esibuy.backend.direcciones.domain.model.CodigoPostal;
import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import es.uclm.esibuy.backend.direcciones.domain.model.TelefonoContacto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba (clases internas de este test)
class GestionDireccionesServiceTest {

    private static final String EMAIL_CLIENTE = "cliente@esibuy.es";
    private static final String ID_CLIENTE = "cliente-1";

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final GestionDireccionesService servicio =
            new GestionDireccionesService(repositorio, new ClienteEnSesionFijo());

    private static DireccionCommand direccion(String calle) {
        return new DireccionCommand("Juan Pérez", calle, "2º B", "13071", "Ciudad Real", "Ciudad Real",
                "612345678");
    }

    private DireccionEnvio guardarDeOtroCliente() {
        return repositorio.guardar(DireccionEnvio.nueva("otro-cliente", new DatosDireccion("Ana López",
                "Calle Ancha 3", null, new CodigoPostal("28001"), "Madrid", "Madrid",
                new TelefonoContacto("698765432"))));
    }

    @Test
    @DisplayName("HU5-CP08: añadir guarda la dirección con todos sus datos para el cliente en sesión")
    void anadirGuardaLaDireccionDelClienteEnSesion() {
        DireccionEnvio anadida = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1"));

        assertEquals(1, repositorio.guardados.size());
        DireccionEnvio guardada = repositorio.guardados.get(0);
        assertEquals(anadida.getId(), guardada.getId());
        assertEquals(ID_CLIENTE, guardada.getClienteId());
        assertEquals(new DatosDireccion("Juan Pérez", "Calle Mayor 1", "2º B", new CodigoPostal("13071"),
                "Ciudad Real", "Ciudad Real", new TelefonoContacto("612345678")), guardada.getDatos());
    }

    @Test
    @DisplayName("HU5-CP08: rechaza añadir una dirección con datos inválidos y no la guarda")
    void rechazaAnadirDireccionInvalida() {
        DireccionCommand sinCodigoPostal = new DireccionCommand("Juan Pérez", "Calle Mayor 1", null, "1307",
                "Ciudad Real", "Ciudad Real", "612345678");
        DireccionCommand sinTelefono = new DireccionCommand("Juan Pérez", "Calle Mayor 1", null, "13071",
                "Ciudad Real", "Ciudad Real", null);

        assertThrows(DireccionInvalidaException.class, () -> servicio.anadir(EMAIL_CLIENTE, sinCodigoPostal));
        assertThrows(DireccionInvalidaException.class, () -> servicio.anadir(EMAIL_CLIENTE, sinTelefono));
        assertThrows(DireccionInvalidaException.class, () -> servicio.anadir(EMAIL_CLIENTE, null));
        assertTrue(repositorio.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU5-CP08: listar devuelve solo las direcciones del cliente en sesión")
    void listaSoloLasDireccionesDelClienteEnSesion() {
        String propia = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        guardarDeOtroCliente();

        List<DireccionEnvio> direcciones = servicio.listar(EMAIL_CLIENTE);

        assertEquals(List.of(propia), direcciones.stream().map(DireccionEnvio::getId).toList());
    }

    @Test
    @DisplayName("HU5-CP04: rechaza añadir una sexta dirección y no la guarda")
    void rechazaSextaDireccionYNoLaGuarda() {
        for (int i = 0; i < 5; i++) {
            servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor " + i));
        }

        DireccionCommand sexta = direccion("Calle Toledo 5");

        assertThrows(LimiteDireccionesException.class, () -> servicio.anadir(EMAIL_CLIENTE, sexta));
        assertEquals(5, repositorio.guardados.size());
    }

    @Test
    @DisplayName("HU5-CP05: la primera dirección añadida se guarda como principal")
    void primeraDireccionAnadidaSeGuardaPrincipal() {
        servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1"));
        servicio.anadir(EMAIL_CLIENTE, direccion("Calle Toledo 5"));

        // Lo que importa es lo guardado, no el objeto en memoria
        assertTrue(repositorio.guardados.get(0).isPrincipal());
        assertFalse(repositorio.guardados.get(1).isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP05: marcar otra dirección como principal guarda el cambio en las dos")
    void marcarPrincipalGuardaElCambio() {
        String primera = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        String segunda = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Toledo 5")).getId();

        servicio.marcarPrincipal(EMAIL_CLIENTE, segunda);

        // Se guardan las dos: la nueva principal y la que deja de serlo
        assertTrue(repositorio.buscarGuardada(segunda).isPrincipal());
        assertFalse(repositorio.buscarGuardada(primera).isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP05: rechaza marcar como principal una dirección de otro cliente")
    void rechazaMarcarDireccionDeOtroCliente() {
        String propia = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        String ajena = guardarDeOtroCliente().getId();

        assertThrows(DireccionNoEncontradaException.class, () -> servicio.marcarPrincipal(EMAIL_CLIENTE, ajena));
        // Ni la dirección ajena ni la propia cambian
        assertFalse(repositorio.buscarGuardada(ajena).isPrincipal());
        assertTrue(repositorio.buscarGuardada(propia).isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar una dirección la borra del repositorio")
    void eliminarDireccionLaBorraDelRepositorio() {
        String primera = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        String segunda = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Toledo 5")).getId();

        // No es la principal: no hace falta elegir sustituta
        servicio.eliminar(EMAIL_CLIENTE, segunda, null);

        assertEquals(List.of(primera), repositorio.guardados.stream().map(DireccionEnvio::getId).toList());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar la principal guarda la nueva principal elegida")
    void eliminarPrincipalGuardaLaNuevaPrincipal() {
        String principal = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        String sustituta = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Toledo 5")).getId();

        servicio.eliminar(EMAIL_CLIENTE, principal, sustituta);

        assertEquals(List.of(sustituta), repositorio.guardados.stream().map(DireccionEnvio::getId).toList());
        assertTrue(repositorio.buscarGuardada(sustituta).isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: eliminar la principal sin elegir otra no borra nada")
    void eliminarPrincipalSinElegirNoBorraNada() {
        String principal = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        servicio.anadir(EMAIL_CLIENTE, direccion("Calle Toledo 5"));

        assertThrows(NuevaPrincipalObligatoriaException.class,
                () -> servicio.eliminar(EMAIL_CLIENTE, principal, null));
        assertEquals(2, repositorio.guardados.size());
        assertTrue(repositorio.buscarGuardada(principal).isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP06: rechaza eliminar una dirección de otro cliente")
    void rechazaEliminarDireccionDeOtroCliente() {
        String ajena = guardarDeOtroCliente().getId();

        assertThrows(DireccionNoEncontradaException.class, () -> servicio.eliminar(EMAIL_CLIENTE, ajena, null));
        // La dirección del otro cliente sigue guardada
        assertEquals(List.of(ajena), repositorio.guardados.stream().map(DireccionEnvio::getId).toList());
    }

    @Test
    @DisplayName("HU5-CP07: editar guarda los datos nuevos y conserva la principal")
    void editarGuardaLosDatosNuevos() {
        String id = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        DireccionCommand nuevos = new DireccionCommand("Ana López", "Calle Toledo 5", null, "28001", "Madrid",
                "Madrid", "698765432");

        DireccionEnvio editada = servicio.editar(EMAIL_CLIENTE, id, nuevos);

        DireccionEnvio guardada = repositorio.buscarGuardada(id);
        assertEquals(editada.getDatos(), guardada.getDatos());
        assertEquals("Calle Toledo 5", guardada.getDatos().calle());
        assertNull(guardada.getDatos().detalles());
        assertTrue(guardada.isPrincipal());
        assertEquals(1, repositorio.guardados.size());
    }

    @Test
    @DisplayName("HU5-CP07: rechaza editar con datos inválidos y conserva los anteriores")
    void rechazaEditarConDatosInvalidos() {
        String id = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();
        DireccionCommand sinCalle = new DireccionCommand("Juan Pérez", "", null, "13071", "Ciudad Real",
                "Ciudad Real", "612345678");

        assertThrows(DireccionInvalidaException.class, () -> servicio.editar(EMAIL_CLIENTE, id, sinCalle));
        assertEquals("Calle Mayor 1", repositorio.buscarGuardada(id).getDatos().calle());
    }

    @Test
    @DisplayName("HU5-CP07: rechaza editar una dirección de otro cliente")
    void rechazaEditarDireccionDeOtroCliente() {
        String ajena = guardarDeOtroCliente().getId();
        DireccionCommand nuevos = direccion("Calle Toledo 5");

        assertThrows(DireccionNoEncontradaException.class, () -> servicio.editar(EMAIL_CLIENTE, ajena, nuevos));
        assertEquals("Calle Ancha 3", repositorio.buscarGuardada(ajena).getDatos().calle());
    }

    // Repositorio en memoria: asigna un id al guardar, como haría MongoDB. Guarda y devuelve copias, para que
    // un cambio solo llegue a lo guardado si el servicio llama a guardar()
    private static class RepositorioEnMemoria implements DireccionRepositoryPort {

        private final List<DireccionEnvio> guardados = new ArrayList<>();
        private int siguienteId = 1;

        @Override
        public List<DireccionEnvio> buscarPorCliente(String clienteId) {
            return guardados.stream()
                    .filter(direccion -> direccion.getClienteId().equals(clienteId))
                    .map(direccion -> copia(direccion, direccion.getId()))
                    .toList();
        }

        @Override
        public DireccionEnvio guardar(DireccionEnvio direccion) {
            String id = direccion.getId() != null ? direccion.getId() : "d" + siguienteId++;
            DireccionEnvio conId = copia(direccion, id);
            int posicion = posicionDe(id);
            if (posicion >= 0) {
                guardados.set(posicion, conId);
            } else {
                guardados.add(conId);
            }
            return copia(conId, id);
        }

        @Override
        public void eliminar(String direccionId) {
            guardados.removeIf(direccion -> direccion.getId().equals(direccionId));
        }

        private DireccionEnvio buscarGuardada(String id) {
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

        private static DireccionEnvio copia(DireccionEnvio direccion, String id) {
            return new DireccionEnvio(id, direccion.getClienteId(), direccion.getDatos(), direccion.isPrincipal());
        }
    }

    // Siempre el mismo cliente en sesión
    private static class ClienteEnSesionFijo implements ClienteEnSesionPort {

        @Override
        public String idPorCorreo(String email) {
            return EMAIL_CLIENTE.equals(email) ? ID_CLIENTE : "otro-cliente";
        }
    }

    // Para el pedido (HU10): los datos de una dirección del cliente, sin ver el dominio de direcciones
    @Test
    @DisplayName("HU10-CP16: consultarParaPedido devuelve los datos de una dirección del cliente en sesión")
    void consultarParaPedidoDevuelveLosDatosDeUnaDireccionPropia() {
        String propia = servicio.anadir(EMAIL_CLIENTE, direccion("Calle Mayor 1")).getId();

        assertEquals(Optional.of(new DireccionParaPedido("Juan Pérez", "Calle Mayor 1", "2º B", "13071",
                "Ciudad Real", "Ciudad Real", "612345678")), servicio.consultarParaPedido(EMAIL_CLIENTE, propia));
    }

    @Test
    @DisplayName("HU10-CP16: consultarParaPedido no devuelve una dirección de otro cliente ni una que no existe")
    void consultarParaPedidoNoDevuelveUnaDireccionAjena() {
        String ajena = guardarDeOtroCliente().getId();

        assertTrue(servicio.consultarParaPedido(EMAIL_CLIENTE, ajena).isEmpty());
        assertTrue(servicio.consultarParaPedido(EMAIL_CLIENTE, "no-existe").isEmpty());
    }
}
