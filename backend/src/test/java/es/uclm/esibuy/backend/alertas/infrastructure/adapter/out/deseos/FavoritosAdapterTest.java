package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.deseos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.deseos.application.port.in.FavoritosProductoUseCase;

// Adaptador de las alertas hacia el módulo de deseos (AB#529), sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP16, propuesto.
@ExtendWith(MockitoExtension.class)
class FavoritosAdapterTest {

    @Mock
    private FavoritosProductoUseCase favoritosProducto;

    @Test
    @DisplayName("HU529-CP16: quién tiene el producto en favoritos se pide al módulo de deseos por su puerto de entrada")
    void preguntaAlModuloDeDeseos() {
        when(favoritosProducto.clientesConFavorito("p-1")).thenReturn(List.of("c-1"));

        assertEquals(List.of("c-1"), new FavoritosAdapter(favoritosProducto).clientesConFavorito("p-1"));
    }
}
