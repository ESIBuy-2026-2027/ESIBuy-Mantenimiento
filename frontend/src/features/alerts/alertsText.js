// Cuántas alertas sin leer tiene el cliente (AB#529), para el aviso al iniciar sesión y el indicador de la cabecera
export const textoSinLeer = (sinLeer) =>
  sinLeer === 1 ? 'Tienes 1 alerta sin leer' : `Tienes ${sinLeer} alertas sin leer`
