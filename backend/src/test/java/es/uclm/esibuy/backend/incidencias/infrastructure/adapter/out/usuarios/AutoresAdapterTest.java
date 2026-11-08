package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ContactoUsuario;

// Adaptador de incidencias hacia el módulo de usuarios para la supervisión (HU49, T307), sin Spring: traduce el
// contacto que da el puerto de entrada de usuarios al autor de la incidencia.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU49-CP10, propuesto.
@ExtendWith(MockitoExtension.class)
class AutoresAdapterTest {

    @Mock
    private AdministracionUsuariosUseCase administracionUsuarios;

    @Test
    @DisplayName("HU49-CP10: el correo y el rol del autor se piden al módulo de usuarios")
    void elAutorSePideAlModuloDeUsuarios() {
        when(administracionUsuarios.consultarContacto("v-1"))
                .thenReturn(Optional.of(new ContactoUsuario("vendedor@esibuy.test", "VENDEDOR")));

        assertEquals(Optional.of(new AutorIncidencia("vendedor@esibuy.test", "VENDEDOR")),
                new AutoresAdapter(administracionUsuarios).buscarAutor("v-1"));
    }

    @Test
    @DisplayName("HU49-CP10: si la cuenta del autor ya no existe, no hay autor")
    void siLaCuentaYaNoExisteNoHayAutor() {
        when(administracionUsuarios.consultarContacto("eliminado")).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), new AutoresAdapter(administracionUsuarios).buscarAutor("eliminado"));
    }
}
