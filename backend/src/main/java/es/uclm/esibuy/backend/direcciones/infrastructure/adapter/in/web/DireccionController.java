package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;

/**
 * Adaptador de entrada web del módulo de direcciones de envío (HU5). Recibe la petición, la traduce a un
 * comando y delega en el puerto de entrada. El cliente sale siempre de la sesión, nunca de un parámetro.
 */
@RestController
@RequestMapping("/api/customer/addresses")
public class DireccionController {

    private final GestionDireccionesUseCase gestionDirecciones;

    public DireccionController(GestionDireccionesUseCase gestionDirecciones) {
        this.gestionDirecciones = gestionDirecciones;
    }

    // Con la marca de la principal: el checkout (HU10) la preselecciona
    @GetMapping
    public List<DireccionResponse> listar(Authentication autenticado) {
        return gestionDirecciones.listar(autenticado.getName()).stream().map(DireccionResponse::desde).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DireccionResponse anadir(@RequestBody DireccionRequest request, Authentication autenticado) {
        return DireccionResponse.desde(gestionDirecciones.anadir(autenticado.getName(), request.aComando()));
    }

    // Edita una dirección propia sin cambiar cuál es la principal; una ajena o inexistente da 404
    @PutMapping("/{id}")
    public DireccionResponse editar(@PathVariable String id, @RequestBody DireccionRequest request,
            Authentication autenticado) {
        return DireccionResponse.desde(gestionDirecciones.editar(autenticado.getName(), id, request.aComando()));
    }

    @PutMapping("/{id}/principal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarPrincipal(@PathVariable String id, Authentication autenticado) {
        gestionDirecciones.marcarPrincipal(autenticado.getName(), id);
    }

    // Elimina una dirección propia. Si es la principal y quedan otras, el cliente elige la sustituta
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id, @RequestParam(required = false) String nuevaPrincipal,
            Authentication autenticado) {
        gestionDirecciones.eliminar(autenticado.getName(), id, nuevaPrincipal);
    }
}
