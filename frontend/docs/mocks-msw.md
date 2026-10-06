# Mocks de la API con MSW (Frontend)

Guía de uso de **MSW (Mock Service Worker)** en el frontend de ESIBuy: qué es, cómo está montado, cómo activarlo o desactivarlo, cómo añadir respuestas simuladas y cómo usarlo en los tests.

- **Rama de origen:** `chore/front/configuracion-msw`
- **Fecha:** 6 de octubre de 2026
- **Versión de MSW:** 2.x (API `http` + `HttpResponse`)

---

## 1. Qué es y por qué lo usamos

MSW intercepta las peticiones `fetch` del frontend y responde con datos de prueba, sin necesidad de que el backend exista. El código del frontend llama a `/api/...` como si el servidor real estuviera ahí; cuando el backend esté listo, se desactivan los mocks y no hay que cambiar nada más.

Motivos de usarlo en este proyecto:

- El backend (Spring Boot) todavía no está disponible, y el frontend no debe quedarse bloqueado.
- Permite hacer **TDD** de código que depende de la API (login, listados, formularios) con respuestas controladas.
- Los mismos handlers sirven para el navegador (`npm run dev`) y para los tests (Vitest).

---

## 2. Estructura de archivos

```
frontend/
├── .env.development            activa los mocks en desarrollo
├── public/
│   └── mockServiceWorker.js    GENERADO por MSW, no editar a mano
└── src/
    ├── api/
    │   ├── client.js           cliente HTTP común (request, ApiError)
    │   └── client.test.js      tests del cliente con MSW
    ├── mocks/
    │   ├── handlers.js         respuestas simuladas (se amplía con cada HU)
    │   ├── server.js           servidor de mocks para Vitest (Node)
    │   └── browser.js          service worker para el navegador
    ├── test/
    │   └── setup.js            arranca/para el servidor de mocks en los tests
    └── main.jsx                arranca el worker si VITE_USE_MOCKS=true
```

Todo esto se versiona en Git (incluido `public/mockServiceWorker.js` y `.env.development`), porque el resto del equipo y el CI lo necesitan.

---

## 3. Activar y desactivar los mocks

La variable `VITE_USE_MOCKS` en `frontend/.env.development` controla si se simula la API en `npm run dev`:

```
VITE_USE_MOCKS=true
```

| Valor | Efecto |
|---|---|
| `true` | Las peticiones a `/api/...` las responde MSW. |
| `false` o ausente | `fetch` va al servidor real (o falla si no existe). |

Notas:

- Vite **solo lee las variables al arrancar**: tras cambiarla hay que reiniciar `npm run dev`.
- Al arrancar con mocks activos, la consola del navegador muestra `[MSW] Mocking enabled`.
- Para apuntar a un backend en otra dirección, define `VITE_API_URL` (por ejemplo `http://localhost:8080`). Si no se define, se usa el mismo origen que el frontend.
- En los tests, MSW siempre está activo (lo arranca `src/test/setup.js`), independientemente de esta variable.

---

## 4. El cliente HTTP (`src/api/client.js`)

Toda llamada a la API debe pasar por `request`, no por `fetch` directamente:

```js
import { request } from '../api/client'

const categorias = await request('/api/categorias')
await request('/api/auth/login', { method: 'POST', body: { email, password } })
```

Comportamiento:

- Envía las cookies (`credentials: 'include'`), necesario si la sesión va en cookie httpOnly.
- Si hay `body`, lo serializa a JSON y añade `Content-Type: application/json`.
- Si la respuesta no es correcta (4xx/5xx), lanza `ApiError` con la propiedad `status` (por ejemplo, 401 sin sesión o 403 sin permisos).
- Si la respuesta es `204`, devuelve `null`.
- Resuelve la URL contra `window.location.origin`, lo que permite usar rutas relativas también en los tests (el `fetch` de Node no las acepta por sí solo).

Ejemplo de manejo de errores:

```js
import { request, ApiError } from '../api/client'

try {
  await request('/api/auth/me')
} catch (error) {
  if (error instanceof ApiError && error.status === 401) {
    // No hay sesión: redirigir al login
  }
}
```

---

## 5. Añadir una respuesta simulada nueva

Cada vez que una HU necesite un endpoint, se añade un handler en `src/mocks/handlers.js`:

```js
import { http, HttpResponse } from 'msw'

export const handlers = [
  // ...handlers existentes
  http.get('/api/usuarios/:id', ({ params }) =>
    HttpResponse.json({ id: params.id, nombre: 'Usuario de prueba' }),
  ),
]
```

Estado actual de `handlers.js`:

| Método y ruta | Respuesta |
|---|---|
| `GET /api/auth/me` | `401` (sin sesión) |
| `GET /api/categorias` | Lista de 2 categorías de ejemplo |

Pautas:

- Las rutas y los formatos de los handlers deben seguir el **contrato de la API** acordado con backend. Si el contrato cambia, se cambia aquí.
- Los datos de ejemplo deben ser ficticios. No incluir nunca datos personales reales ni números de tarjeta.
- Si un handler no se usa, se borra (el proyecto exige cero código muerto).

---

## 6. Uso en los tests (Vitest)

`src/test/setup.js` arranca el servidor de mocks antes de los tests, reinicia los handlers tras cada test y lo cierra al terminar. Dos consecuencias importantes:

1. **Una petición sin handler hace fallar el test** (`onUnhandledRequest: 'error'`). Si un componente llama a un endpoint nuevo, hay que añadir su handler.
2. **Se pueden cambiar las respuestas dentro de un test** con `server.use(...)`, sin afectar al resto, porque los handlers se reinician solos.

Ejemplo, simulando un error 403 solo en un test:

```js
import { it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { request } from './client'

it('request_sinPermisos_lanzaErrorConEstado403', async () => {
  server.use(http.get('/api/x', () => new HttpResponse(null, { status: 403 })))

  await expect(request('/api/x')).rejects.toMatchObject({ status: 403 })
})
```

Recordatorio de convenciones de test del proyecto: importar `describe`, `it` y `expect` desde `vitest` (no hay globals) y nombrar los tests con el formato `funcion_condicion_resultado`.

---

## 7. Comandos útiles

Desde `frontend/`:

```bash
npx vitest run     # ejecuta todos los tests una vez
npm run lint       # ESLint
npm run build      # comprueba que compila
npm run dev        # desarrollo con mocks (si VITE_USE_MOCKS=true)
```

Para probar a mano que los mocks funcionan, con `npm run dev` abierto, pegar en la consola del navegador (F12):

```js
fetch('/api/categorias').then(r => r.json()).then(console.log)
```

Debe imprimir las categorías de ejemplo.

---

## 8. Calidad: ESLint y SonarQube

- `public/mockServiceWorker.js` es un archivo **generado** y grande. Debe quedar fuera del lint (`globalIgnores` en `eslint.config.js`) y del análisis de SonarQube:

  ```
  sonar.exclusions=**/public/mockServiceWorker.js,**/src/mocks/**
  ```

- No se debe editar `mockServiceWorker.js` a mano. Si se actualiza MSW, se regenera con `npx msw init public/ --save`.

---

## 9. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| No aparece `[MSW] Mocking enabled` | Falta `.env.development` o no se reinició Vite | Crear el archivo y reiniciar `npm run dev` |
| `fetch` devuelve 404 en desarrollo | El mock no se arrancó, o no hay handler para esa ruta | Revisar la variable y `handlers.js` |
| Un test falla con "unhandled request" | Falta un handler para el endpoint que llama el componente | Añadirlo a `handlers.js` o con `server.use` en el test |
| `Failed to parse URL` en un test | Se usó `fetch` con ruta relativa en vez de `request` | Usar siempre `request` de `src/api/client.js` |
| Aviso de ESLint en `mockServiceWorker.js` | Falta la exclusión del lint | Añadirlo a `globalIgnores` |

---

## 10. Cuando haya backend

1. Cambiar `VITE_USE_MOCKS` a `false` (o eliminarla) y, si hace falta, definir `VITE_API_URL`.
2. Comprobar que las rutas y formatos reales coinciden con el contrato; ajustar el frontend (y `handlers.js`) donde difieran.
3. Los mocks **se mantienen** para los tests: siguen siendo útiles para simular errores y casos límite.

---

## 11. Uso de IA generativa

Este documento y el trabajo técnico que describe se hicieron con ayuda de un asistente de IA generativa, tal y como exige el enunciado de la práctica.

- **Herramienta:** Claude (modelo de Anthropic), a través de su interfaz de chat.
- **Qué generó la IA:**
  - La propuesta de arquitectura (usar MSW para simular la API, estructura de `src/api`, `src/mocks` y `src/test`).
  - El código de `client.js`, `handlers.js`, `server.js`, `browser.js`, `setup.js`, `main.jsx` y los 5 tests de `client.test.js`.
  - La configuración de `.env.development` y las exclusiones de ESLint y SonarQube.
  - Este documento.
- **Cómo se comprobó:** el asistente reprodujo la configuración en un proyecto temporal, aparte del repositorio, con los tests de la home ya existentes. Allí se ejecutaron los tests con MSW (11 en total: 6 de la home y 5 del cliente HTTP, todos en verde), se comprobó la generación del service worker con `npx msw init` y se comprobó que ESLint muestra un aviso sobre `mockServiceWorker.js` (de ahí la exclusión). 
- **Cuestiones a validar por el equipo:** que las rutas de `handlers.js` coincidan con el contrato real de la API, y que las exclusiones de SonarQube se apliquen en la configuración real del CI.
- **Revisión humana:** el código fue revisado, ejecutado y validado por el equipo antes de su integración. Revisado por: Ricardo.
