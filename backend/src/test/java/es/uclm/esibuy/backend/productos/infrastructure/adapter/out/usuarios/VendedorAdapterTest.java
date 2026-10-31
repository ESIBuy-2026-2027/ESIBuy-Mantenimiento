package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de productos hacia el módulo de usuarios para los datos del vendedor (HU8, T356). El puerto de
// entrada de usuarios es un doble de prueba (Mockito): se prueba sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU8-CP12, propuesto.
@ExtendWith(MockitoExtension.class)
class VendedorAdapterTest {

    @Mock
    private AdministracionUsuariosUseCase administracionUsuarios;

    @Test
    @DisplayName("HU8-CP12: el nombre comercial del vendedor se pide al módulo de usuarios por su puerto de entrada")
    void nombreComercialPreguntaAlModuloDeUsuarios() {
        when(administracionUsuarios.consultarNombreComercial("u-2")).thenReturn("TecnoVera");

        assertEquals("TecnoVera", new VendedorAdapter(administracionUsuarios).nombreComercial("u-2"));
    }
}
