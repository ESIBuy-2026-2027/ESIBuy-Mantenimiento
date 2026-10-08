# HU2 · Inicio de sesión seguro y redirección por rol (Frontend)

Documento para entender cómo funciona el login del frontend de ESIBuy: qué hace cada componente, cuál es el flujo, qué contrato espera del backend y cómo probarlo.

- **Fecha:** 7 de octubre de 2026
- **HU:** HU2 (inicio de sesión seguro) y la parte de redirección por rol de HU15 y HU18
- **Rama:** `feature/front/login`
- **Metodología:** esta HU **no se desarrolló con TDD**; los tests se añadieron después
- **Depende de:** `docs/mocks-msw.md` (MSW y cliente HTTP)

---

## 1. Resumen

El usuario inicia sesión en **tres pasos**:

| Paso | Factor | Qué introduce el usuario |
|---|---|---|
| 1 | Credenciales | Correo electrónico y contraseña |
| 2 | Código por correo (2FA) | Código de 6 dígitos que el backend envía a su correo |
| 3 | Microsoft Authenticator (3FA) | Código de 6 dígitos (TOTP) que genera la aplicación Microsoft Authenticator |

Al completar el tercer paso el backend abre la sesión y el frontend redirige al usuario según su rol. Una vez iniciada la sesión, la cabecera sustituye los botones "Iniciar sesión" y "Registrarse" por "Cerrar sesión".

> **Cambio respecto al diseño anterior.** El tercer factor era un captcha de Google. Se sustituye por un código TOTP de Microsoft Authenticator, que sí comprueba que quien accede posee un dispositivo registrado (un captcha solo comprueba que es una persona). Se elimina la dependencia `react-google-recaptcha`.

---

## 2. Flujo

```mermaid
sequenceDiagram
    actor U as Usuario
    participant F as Frontend
    participant B as Backend

    U->>F: Correo + contraseña
    F->>B: POST /api/auth/login
    B-->>F: 200 (y envía el código por correo)
    U->>F: Código de 6 dígitos
    F->>B: POST /api/auth/verify-email-code
    B-->>F: 200
    U->>F: Código de Microsoft Authenticator
    F->>B: POST /api/auth/verify-authenticator-code {code}
    B-->>F: 200 {usuario} + cookie de sesión httpOnly
    F->>F: Guarda el usuario en memoria y redirige según su rol
```

### Redirección por rol

| Rol (valor del backend) | Destino | Página |
|---|---|---|
| `ADMINISTRADOR` | `/admin` | `AdminPage` |
| `CLIENTE` | `/products` | `ProductsPage` |
| `VENDEDOR` | `/seller/products` | `SellerProductsPage` |
| Rol desconocido | `/` | Home |

Las tres páginas existen pero **no pintan nada**: son un componente que devuelve `null`, preparadas para que se implementen en sus historias (HU15, HU18 y el catálogo). Un archivo `.jsx` totalmente vacío no se puede importar como componente, por eso tienen esa única función.

### Reglas de acceso

| Ruta | Quién entra | Si no cumple |
|---|---|---|
| `/`, `/products` | Cualquiera | |
| `/login` | Solo sin sesión | Con sesión, redirige al destino de su rol |
| `/admin` | Administrador | Sin sesión → `/login`; otro rol → destino de su rol |
| `/seller/products` | Vendedor | Sin sesión → `/login`; otro rol → destino de su rol |

> Estas restricciones son **solo experiencia de usuario**. La barrera real de seguridad es el backend, que debe responder 401 o 403 igualmente.

### Cerrar sesión

1. El usuario pulsa "Cerrar sesión" en la cabecera.
2. El frontend navega a la home y llama a `POST /api/auth/logout`.
3. Aunque esa llamada falle, la sesión local se limpia (la cookie del servidor caducará por sí sola).
4. La cabecera vuelve a mostrar "Iniciar sesión" y "Registrarse".

---

## 3. Componentes y archivos

Todas las rutas son relativas a `frontend/src/`.

### Autenticación y sesión (`auth/`)

| Archivo | Qué hace |
|---|---|
| `AuthContext.js` | Crea el contexto de React que comparte la sesión. |
| `AuthProvider.jsx` | Al cargar la app consulta `GET /api/auth/me` para recuperar la sesión desde la cookie. Expone `user`, `loading`, `signIn` y `signOut`. El usuario vive **solo en memoria**. |
| `useAuth.js` | Hook para leer la sesión. Lanza un error si se usa fuera de `AuthProvider`. |
| `ProtectedRoute.jsx` | Protege rutas por rol (`allowedRoles`). Muestra un indicador mientras comprueba la sesión. |
| `roles.js` | Constantes de rol y función `getHomePath(rol)` con el destino de cada uno. Usa un `Map`, de modo que un rol inesperado nunca devuelve algo no previsto. |

### Formulario de login (`features/auth/`)

| Archivo | Qué hace |
|---|---|
| `LoginForm.jsx` | Orquesta los tres pasos, controla el estado de envío y los errores. Solo avanza de paso cuando el backend responde bien. |
| `CredentialsStep.jsx` | Paso 1. Valida que haya correo y contraseña (todos los errores a la vez) y **vacía la contraseña del estado en cuanto se envía**. |
| `CodeStep.jsx` | Pasos 2 y 3. Campo de código de 6 dígitos (se reutiliza para el correo y para Microsoft Authenticator): solo admite números y vacía el valor al enviarlo. |
| `loginErrors.js` | Traduce los errores del backend a mensajes fijos para el usuario. |
| `authFormStyles.js` | Estilos compartidos de los formularios. |
| `LoginFlow.test.jsx` | Tests del flujo completo, los errores y las rutas protegidas. |

### Estructura de la interfaz (`components/` y `pages/`)

| Archivo | Qué hace |
|---|---|
| `components/AppHeader.jsx` | Cabecera con el logo. Sin sesión muestra "Iniciar sesión" y "Registrarse"; con sesión, "Cerrar sesión". Mientras se comprueba la sesión no muestra ningún botón, para evitar parpadeos. |
| `components/AppLayout.jsx` | Marco común: cabecera y contenido de la ruta. **La página de login queda fuera**, por lo que no lleva cabecera. |
| `components/AppHeader.test.jsx` | Tests de la cabecera. |
| `pages/LoginPage.jsx` | Página de login; redirige si ya hay sesión. |
| `pages/AdminPage.jsx`, `ProductsPage.jsx`, `SellerProductsPage.jsx` | Páginas de destino, vacías a propósito. |
| `pages/Home.jsx` | Se le ha quitado la cabecera (ahora la pone el layout). |
| `routes/paths.js` | Todas las rutas en un solo sitio, para no repetir cadenas. |
| `routes/AppRoutes.jsx` | Definición de rutas y protecciones. |

### API y mocks

| Archivo | Qué hace |
|---|---|
| `api/auth.js` | Funciones `getCurrentUser`, `login`, `verifyEmailCode`, `verifyAuthenticatorCode` y `logout`. |
| `api/client.js` | Ahora el error (`ApiError`) incluye `code`, un identificador que envía el backend. Nunca se muestra el texto del error del servidor. |
| `mocks/authHandlers.js` | Simula el backend de autenticación (ver sección 6). |
| `test/renderApp.jsx`, `test/LocationProbe.jsx` | Utilidades de test para pintar la app completa y comprobar la ruta actual. |

---

## 4. Seguridad

| Medida | Cómo se aplica |
|---|---|
| Sin almacenamiento del navegador | No se usa `localStorage` ni `sessionStorage`. La sesión va en una **cookie httpOnly** que gestiona el backend y que JavaScript no puede leer. Hay un test que comprueba que tras un login completo ambos están vacíos. |
| Mensajes genéricos | Contraseña errónea y usuario inexistente dan exactamente el mismo mensaje. Excepciones acordadas: cuenta desactivada (HU23) y exceso de intentos. |
| No se muestran textos del servidor | Los mensajes salen de una tabla fija, así que un error interno nunca llega a la pantalla. |
| Higiene de datos sensibles | La contraseña y los códigos se vacían del estado al enviarse. Los campos usan `autocomplete` correcto (`current-password`, `one-time-code`). |
| El backend impone el orden | El frontend sigue los pasos por comodidad, pero no se fía de ellos. La contraseña solo se envía en el paso 1. |
| Sin redirección a URLs externas | Tras el login se va siempre al destino del rol; no se lee ninguna URL de la petición (evita el *open redirect*). |
| Código de Authenticator de un solo uso | El campo se vacía tras cada intento. El backend debe rechazar la reutilización de un código ya aceptado. |
| Sin datos de pruebas en producción | Se comprobó que el build de producción no contiene los mocks ni sus credenciales. |

---

## 5. Contrato esperado del backend

**Cookies:** la sesión debe ir en una cookie `HttpOnly`, `Secure` y con `SameSite`. El frontend envía siempre `credentials: 'include'`.

| Endpoint | Cuerpo | Respuesta correcta |
|---|---|---|
| `POST /api/auth/login` | `{ email, password }` | 200; envía el código al correo |
| `POST /api/auth/verify-email-code` | `{ code }` | 200 |
| `POST /api/auth/verify-authenticator-code` | `{ code }` | 200 con el usuario y cookie de sesión |
| `GET /api/auth/me` | | 200 con el usuario, o 401 sin sesión |
| `POST /api/auth/logout` | | 204 |

**Usuario devuelto:** `{ id, nombre, apellidos, email, rol, estado }`, con `rol` igual a `ADMINISTRADOR`, `VENDEDOR` o `CLIENTE`.

**Errores:**

| Estado | Significado para el frontend |
|---|---|
| 401 (u otro 4xx) | Dato incorrecto: mensaje genérico según el paso |
| 403 con `{ "code": "ACCOUNT_DISABLED" }` | Cuenta desactivada. Solo debe enviarse cuando la contraseña es correcta |
| 429 | Demasiados intentos |
| 5xx o sin respuesta | "No se ha podido conectar con el servidor" |

**Authenticator en el backend:** el backend valida el código TOTP (RFC 6238) con el secreto de cada usuario, que nunca llega al frontend. Queda pendiente definir cómo se **da de alta** el secreto (QR en el primer acceso o en el registro): el frontend aún no tiene esa pantalla.

**Puntos pendientes de acordar con backend:**

- Dónde guardan el estado de los pasos 1 y 2 (se recomienda una cookie temporal de caducidad corta, no algo accesible desde JavaScript).
- Mecanismo anti-CSRF (si usan un token en cabecera, hay que añadirlo a `api/client.js`).
- Si una cuenta pendiente de activación (HU1) debe mostrar un mensaje propio.
- Que los valores de `rol` coinciden con los indicados.

---

## 6. Configuración y pruebas manuales

### Variables de entorno

| Archivo | Variable | Uso |
|---|---|---|
| `.env.development` | `VITE_USE_MOCKS=true` | Activa los mocks en `npm run dev` |

### Usuarios del mock (contraseña `Mock-Clave-1!`)

| Correo | Rol | Notas |
|---|---|---|
| `admin@esibuy.test` | Administrador | |
| `vendedor@esibuy.test` | Vendedor | |
| `cliente@esibuy.test` | Cliente | |
| `desactivado@esibuy.test` | Cliente | Cuenta desactivada |

Código del correo: `654321`. Código de Microsoft Authenticator: `123456`. Tras 5 contraseñas erróneas, el mock responde 429. Al recargar la página la sesión simulada se pierde.

### Qué probar en el navegador

1. Con cada rol, completar los tres pasos y comprobar el destino (`/admin`, `/seller/products`, `/products`).
2. Comprobar que, con sesión iniciada, la cabecera muestra solo "Cerrar sesión" y que al pulsarlo vuelve la home con los botones de invitado.
3. Abrir `/admin` sin sesión (debe llevar al login) y con un cliente (debe llevar a `/products`).
4. En F12 → Application, comprobar que **Local Storage y Session Storage están vacíos**.
5. En F12 → Network, comprobar que la contraseña solo viaja en el cuerpo de `login`.
6. Comparar el mensaje de "contraseña errónea" con el de "usuario inexistente": deben ser idénticos.

### Tests automáticos

```bash
npx vitest run     # 51 tests
npm run lint
npm run build
```

---

## 7. Limitaciones y trabajo pendiente

- Falta el enlace "¿Has olvidado tu contraseña?" (no existe aún esa historia ni su ruta).
- La página de login no tiene enlace para volver a la home.
- Un usuario con sesión que visita la home sigue viendo la llamada "Crear mi cuenta".
- La ruta `/register` aún no existe (la desarrolla otra persona del equipo).
- Las rutas en inglés sustituyen a las que mencionaba HU15 (`/vendedor/productos`): `/seller/products`.
- El login real depende de que el backend cumpla el contrato de la sección 5.

---

## 8. Uso de IA generativa

Este documento y el trabajo técnico que describe se hicieron con ayuda de un asistente de IA generativa, como exige el enunciado.

- **Herramienta:** Claude (modelo de Anthropic), mediante su interfaz de chat.
- **Qué generó la IA:** la arquitectura del login, todo el código de las secciones anteriores, los tests, los mocks de autenticación y este documento. Las decisiones de producto las tomó el equipo: Microsoft Authenticator como tercer factor (en lugar del captcha inicial), las rutas en inglés, las páginas vacías y la cabecera con cierre de sesión.
- **Cómo se comprobó:** el asistente reprodujo el proyecto en un entorno temporal, aparte del repositorio, con los archivos reales del equipo. Allí se ejecutaron **40 tests (todos en verde)** con el captcha; tras sustituirlo por Authenticator la suite completa del frontend tiene **51 tests en verde**, ESLint sin errores y un build de producción correcto. También se comprobó que el build no contiene los mocks ni sus credenciales.
- **Qué NO se comprobó:** la app Microsoft Authenticator real, ni el flujo contra un backend real. Debe verificarse en el proyecto del equipo.
- **Cómo guió el proceso:** el asistente propuso el orden de commits y ramas siguiendo las convenciones del proyecto, y explicó cada paso para aplicarlo a mano.
- **Revisión humana:** el código fue revisado, ejecutado y validado por el equipo antes de su integración. Revisado por: Ricardo.
