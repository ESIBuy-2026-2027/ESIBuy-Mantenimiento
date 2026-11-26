package es.uclm.esibuy.backend.deseos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;

// Clientes que tienen un producto en sus favoritos, para avisarles de sus cambios (AB#529). El repositorio es un
// doble de prueba: la consulta en MongoDB se prueba en ListaDeseosMongoAdapterTest.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP11, propuesto.
@ExtendWith(MockitoExtension.class)
class FavoritosProductoServiceTest {

    @Mock
    private ListaDeseosRepositoryPort listaDeseosRepository;
    @InjectMocks
    private FavoritosProductoService servicio;

    @Test
    @DisplayName("HU529-CP11: devuelve los clientes que tienen el producto en sus favoritos")
    void devuelveLosClientesConElProductoEnFavoritos() {
        when(listaDeseosRepository.clientesConFavorito("p-1")).thenReturn(List.of("c-1", "c-2"));

        assertEquals(List.of("c-1", "c-2"), servicio.clientesConFavorito("p-1"));
    }
}
