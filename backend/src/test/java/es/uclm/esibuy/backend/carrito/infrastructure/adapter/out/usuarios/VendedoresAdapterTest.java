package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador del carrito hacia el módulo de usuarios para la cabecera de cada proveedor (HU9), sin Spring
@ExtendWith(MockitoExtension.class)
class VendedoresAdapterTest {

    @Mock
    private AdministracionUsuariosUseCase administracionUsuarios;

    @Test
    @DisplayName("HU9-CP15: el nombre comercial del proveedor se pide al módulo de usuarios")
    void nombreComercialPreguntaAlModuloDeUsuarios() {
        when(administracionUsuarios.consultarNombreComercial("u-2")).thenReturn("TecnoVera");

        assertEquals("TecnoVera", new VendedoresAdapter(administracionUsuarios).nombreComercial("u-2"));
    }
}
