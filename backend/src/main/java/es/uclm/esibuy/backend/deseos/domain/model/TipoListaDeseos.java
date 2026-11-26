package es.uclm.esibuy.backend.deseos.domain.model;

/**
 * Los favoritos del cliente (AB#528) son una lista fija, una por cliente, que no se renombra ni se elimina; las demás
 * son las listas de deseos que crea él (PBI 534).
 */
public enum TipoListaDeseos {
    FAVORITOS,
    LISTA
}
