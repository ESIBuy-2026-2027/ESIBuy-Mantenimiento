package es.uclm.esibuy.backend.preferencias.domain.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Preferencias de un cliente (HU46): sus categorías favoritas (AB#528), en el orden en que las marcó y sin repetir
 * ninguna. Hay unas por cliente; un cliente que aún no ha marcado nada no tiene ninguna categoría.
 */
public class Preferencias {

    private final String id;
    private final String clienteId;
    private final List<String> categorias;

    public Preferencias(String id, String clienteId, List<String> categorias) {
        this.id = id;
        this.clienteId = clienteId;
        this.categorias = sinRepetidas(categorias);
    }

    // Preferencias aún no guardadas, para un cliente que todavía no ha elegido nada
    public static Preferencias ninguna(String clienteId) {
        return new Preferencias(null, clienteId, List.of());
    }

    // Marcar una categoría que ya es favorita no es un error: ya queda como se quería. Dice si ha cambiado
    public boolean marcar(String categoriaId) {
        return !categorias.contains(categoriaId) && categorias.add(categoriaId);
    }

    // Desmarcar una que no es favorita no es un error: ya queda como se quería. Dice si ha cambiado
    public boolean desmarcar(String categoriaId) {
        return categorias.remove(categoriaId);
    }

    private static List<String> sinRepetidas(List<String> categorias) {
        return new ArrayList<>(new LinkedHashSet<>(categorias));
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<String> getCategorias() {
        return List.copyOf(categorias);
    }
}
