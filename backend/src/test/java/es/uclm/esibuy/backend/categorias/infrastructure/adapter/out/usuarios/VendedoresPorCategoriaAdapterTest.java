package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;

// Adaptador de categorías hacia el módulo de usuarios (HU42, T326). El puerto de entrada de usuarios es un
// doble de prueba (Mockito): se prueba sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU42-CP10, propuesto.
@ExtendWith(MockitoExtension.class)
class VendedoresPorCategoriaAdapterTest {

    @Mock
    private AdministracionUsuariosUseCase administracionUsuarios;

    @Test
    @DisplayName("HU42-CP10: si algún vendedor tiene la categoría como principal, se pregunta al módulo de usuarios por su puerto de entrada")
    void existenVendedoresPreguntaAlModuloDeUsuarios() {
        when(administracionUsuarios.existenVendedoresConCategoria("cat-1")).thenReturn(true);

        assertTrue(new VendedoresPorCategoriaAdapter(administracionUsuarios).existenVendedores("cat-1"));
    }

    @Test
    @DisplayName("HU42-CP10: si ningún vendedor tiene la categoría como principal, no hay vendedores")
    void noExistenVendedoresSiElModuloDeUsuariosDiceQueNo() {
        when(administracionUsuarios.existenVendedoresConCategoria("cat-1")).thenReturn(false);

        assertFalse(new VendedoresPorCategoriaAdapter(administracionUsuarios).existenVendedores("cat-1"));
    }
}