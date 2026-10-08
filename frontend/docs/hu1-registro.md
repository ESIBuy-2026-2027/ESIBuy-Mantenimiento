# HU1 · Registro de cliente y vendedor (Frontend)

Documento para entender la pantalla de registro: qué pide a cada tipo de usuario, cómo valida los datos, qué hace cada parte del código, qué contrato espera del backend y cómo probarlo.

- **Fecha:** 8 de octubre de 2026
- **HU:** HU1 · Registro de usuario
- **Tasks:** AB#17 y AB#18
- **Rama:** `feature/front/registro`
- **Metodología:** TDD. Cada ciclo tiene su commit de rojo (`test(HU1)`) y su commit de verde (`feat(HU1)`), y los refactors van en commits `refactor(HU1)` aparte.
- **Depende de:** `docs/Contexto_frontend.md` (sección 6.3, requisitos del registro) y `docs/mocks-msw.md` (mocks)

---

## 1. Resumen

En `/register` el usuario elige si quiere una cuenta de **cliente** o de **vendedor** y rellena el formulario correspondiente. Al pulsar **Registrarse** se validan todos los campos y se muestran **todos los errores a la vez**, no campo a campo. Si no hay errores, se envían los datos y aparece la pantalla **Cuenta creada**.

| Campo | Cliente | Vendedor | Reglas |
|---|---|---|---|
| Nombre | Obligatorio | Obligatorio | Máximo 30 caracteres |
| Apellidos | Obligatorio | Obligatorio | Máximo 30 caracteres |
| Fecha de nacimiento | Obligatorio | — | Mayor de 18 años |
| Nombre comercial | — | Obligatorio | Máximo 30 caracteres. Único |
| Categorías | — | Obligatorio | Al menos una; se pueden marcar varias |
| DNI | Obligatorio | Obligatorio | 8 números y una letra |
| Email | Obligatorio | Obligatorio | Formato válido, máximo 100 caracteres. Único |
| Teléfono | Opcional | Opcional | Si se rellena: 9 números, sin letras |
| Password | Obligatorio | Obligatorio | Mínimo 8 caracteres, con mayúscula, minúscula, número y símbolo. Máximo 64 |
| Repetir password | Obligatorio | Obligatorio | Igual que el password |
| Tipo de cliente | Obligatorio | — | Normal (por defecto) o Premium |
| Imagen de perfil | Opcional | Opcional | Uno de los tres avatares |
| Política de privacidad | Obligatorio | Obligatorio | Casilla "Acepto la política de privacidad" (RGPD) |

Los campos obligatorios llevan el asterisco rojo de Fluent UI.

> Las validaciones del frontend son **experiencia de usuario**. La unicidad del email y del nombre comercial solo la puede garantizar el backend, y el backend debe volver a validar todos los datos.

---

## 2. Flujo

```mermaid
flowchart TD
    A[Usuario entra en /register] --> B[Elige Cliente o Vendedor]
    B -- Cliente --> C[Formulario de cliente]
    B -- Vendedor --> D[Carga las categorías y muestra el formulario de vendedor]
    C -- Registrarse --> E{¿Errores de validación?}
    D -- Registrarse --> E
    E -- Sí --> F[Muestra todos los errores a la vez y no envía nada]
    F --> E
    E -- No --> G[Envía los datos al backend]
    G --> H{¿Respuesta?}
    H -- 201 --> I[Pantalla Cuenta creada]
    H -- 409 --> J[Marca el email y/o el nombre comercial como ya registrados]
    J --> E
```

Mientras se escribe el password, la lista de requisitos marca con un tick verde cada uno que se cumple. Cada campo de password tiene un botón con un ojo para mostrarlo u ocultarlo.

---

## 3. Componentes y archivos

Todas las rutas son relativas a `frontend/src/`.

### Página

| Archivo | Qué hace |
|---|---|
| `pages/Register.jsx` | Toda la pantalla: elección del tipo de cuenta, los dos formularios, la validación, el envío y la confirmación. |
| `routes/AppRoutes.jsx` | Ruta `REGISTER` (`/register`) dentro de `AppLayout`, accesible sin sesión. |

### Organización de `Register.jsx`

| Parte | Qué hace |
|---|---|
| `OBLIGATORIOS_CLIENTE` / `OBLIGATORIOS_VENDEDOR` | Campos obligatorios de cada formulario. |
| `REQUISITOS_PASSWORD` | Las cinco reglas del password (texto y comprobación). La misma lista sirve para pintar los ticks y para validar al enviar. Admite letras con acento y la ñ. |
| `LONGITUD_MAXIMA` | Máximo de caracteres de cada campo. Se aplica con `maxLength`, así que no se puede escribir de más. |
| `AVATARES` | Los tres avatares: un `Avatar` de Fluent con un icono de animal (gato, perro y conejo) y su color. |
| `ERRORES_VENDEDOR_YA_REGISTRADO` | Traduce cada código 409 del backend a los campos que hay que marcar. |
| `validarCamposComunes()` | Valida lo que comparten los dos formularios: obligatorios, DNI, email, teléfono, requisitos del password, que los dos passwords coincidan y la política de privacidad. |
| `enviarCliente()` / `enviarVendedor()` | Añaden su validación propia (mayoría de edad o categorías), envían los datos y gestionan el 409. |
| `campo()`, `campoPassword()`, `camposComunes`, `selectorAvatar`, `casillaPrivacidad` | Piezas de la interfaz que comparten los dos formularios, para no repetir código. |
| `pantalla()` | Tarjeta centrada con su título, común a las cuatro pantallas (elección, cliente, vendedor y confirmación). |

### API y mocks

| Archivo | Qué hace |
|---|---|
| `api/auth.js` | `registrarCliente(datos)` y `registrarVendedor(datos)`. |
| `api/products.js` | `getCategories()`, compartida con el listado de productos del vendedor (HU32). |
| `mocks/registroHandlers.js` | Simula los dos endpoints de registro: crea la cuenta o responde 409 si el email o el nombre comercial ya existen. Nunca guarda el password. |
| `mocks/authHandlers.js` | Exporta `USUARIOS` para que el mock del registro sepa qué emails ya existen. |
| `mocks/handlers.js` | Registra los handlers del registro. |

---

## 4. Seguridad

- **Política de password** visible desde el principio, con un tick por requisito cumplido. Al enviar se rechaza cualquier password que no cumpla las cinco reglas.
- **El password nunca se guarda en el navegador** (ni en `localStorage` ni en `sessionStorage`). El mock tampoco lo guarda: solo recuerda emails y nombres comerciales para detectar duplicados.
- **Límite de longitud** en los campos de texto, para no enviar entradas desproporcionadas.
- **Consentimiento RGPD** obligatorio para registrarse.
- **Mensajes de duplicado** solo para el email y el nombre comercial, que son los datos que el usuario necesita corregir.

---

## 5. Contrato esperado del backend

**El frontend se ha desarrollado y probado con mocks (MSW)**, porque el backend aún no estaba disponible. Las rutas, el cuerpo de la petición y los códigos de error de esta sección los ha definido el frontend, así que **puede haber diferencias con el backend real**. Se ajustarán cuando se unan frontend y backend.

`POST /api/auth/register/cliente` y `POST /api/auth/register/vendedor`

Cuerpo (JSON): el objeto completo del formulario.

```json
{
  "nombre": "Luis",
  "apellidos": "Pérez",
  "nombreComercial": "Tienda Luis",
  "categorias": ["cat-1"],
  "fechaNacimiento": "",
  "dni": "12345678Z",
  "email": "luis@correo.com",
  "telefono": "612345678",
  "password": "Password123!",
  "repeticionPassword": "Password123!",
  "tipoCliente": "normal",
  "avatar": "avatar2",
  "aceptaPrivacidad": true
}
```

| Respuesta | Significado para el frontend |
|---|---|
| 201 | Cuenta creada: se muestra la confirmación |
| 409 + `{ "code": "EMAIL_ALREADY_REGISTERED" }` | "Este email ya está registrado" |
| 409 + `{ "code": "COMMERCIAL_NAME_ALREADY_REGISTERED" }` | "Este nombre comercial ya está registrado" (solo vendedor) |
| 409 + `{ "code": "EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED" }` | Los dos mensajes (solo vendedor) |

- En el cliente, cualquier 409 marca el email.
- `categorias` envía los **identificadores** que devuelve `GET /api/categorias`.
- Se envían también campos que el backend no necesita (`repeticionPassword`, y los del otro tipo de usuario vacíos). Se filtrarán al unir con el backend.

---

## 6. Datos de prueba y cómo verlo en el navegador

Con `npm run dev` (mocks activos con `VITE_USE_MOCKS=true` en `.env.development`):

| Prueba | Resultado |
|---|---|
| Cliente o vendedor con un email nuevo | Pantalla **Cuenta creada** |
| Email `cliente@esibuy.test`, `vendedor@esibuy.test` o cualquier otro usuario del mock de login | "Este email ya está registrado" |
| Vendedor con nombre comercial `Tienda Vera` | "Este nombre comercial ya está registrado" |
| Las dos cosas a la vez | Los dos mensajes |
| Registrar dos veces el mismo email nuevo | La segunda vez, "ya está registrado" (el mock lo recuerda hasta recargar la página) |

Pruebas manuales sugeridas:

1. Pulsar **Registrarse** con el formulario vacío y comprobar que aparecen todos los errores a la vez.
2. Escribir un password poco a poco y ver cómo se marcan los requisitos.
3. Pulsar el ojo de cada password para mostrarlo y ocultarlo.
4. Como vendedor, marcar varias categorías.
5. Intentar escribir más de 30 caracteres en el nombre.

### Tests automáticos

```bash
npx vitest run     # 129 tests (47 son de esta HU)
npm run lint
npm run build
```

| Archivo | Qué cubre |
|---|---|
| `pages/Register.test.jsx` | 47 tests: elección del tipo de cuenta, campos y obligatoriedad de cada formulario, categorías, avatares, validaciones (DNI, email, teléfono, mayoría de edad, requisitos y coincidencia del password, longitud máxima, política de privacidad), mostrar u ocultar el password, envío, confirmación y errores 409. |

Los tests sustituyen `api/auth` y `api/products` con `vi.mock`, así que no dependen de los mocks de MSW.

---

## 7. Decisiones

- **Convención de nombres:** en la interfaz se usan "Email" y "Password", acordado en una reunión del equipo para todo el proyecto.
- **Categorías con casillas** (`Checkbox` dentro de un `fieldset`) y no con un desplegable. No se usa `Field` porque está pensado para un solo control y daba el mismo `id` a todas las casillas. Se puede cambiar a un desplegable múltiple si el equipo lo prefiere.
- **Límite de longitud con `maxLength`:** se impide escribir de más en lugar de dejarlo y mostrar un error después.
- **Códigos de error 409** con el campo `code`, siguiendo el patrón del login (`ACCOUNT_DISABLED`) y sin tocar `api/client.js`.
- **Avatares con iconos de Fluent UI**, sin imágenes externas. Para cambiarlos basta con editar la lista `AVATARES`.
- **No se ha añadido ninguna librería nueva.** Todo sale de `@fluentui/react-components`, `@fluentui/react-icons` y `react-router-dom`.

## 8. Limitaciones y trabajo pendiente

- **Cliente Premium:** no redirige al pago. Es un punto abierto de `Contexto_frontend.md` y depende de las HU de pagos, fuera de este sprint.
- **Subir una imagen de perfil propia:** no está hecho; de momento solo se elige uno de los tres avatares.
- **Fallo al cargar las categorías:** no se muestra ningún mensaje. `DESIGN.md` recomienda un `MessageBar` de error con **Reintentar**.
- **Diferencias menores con `DESIGN.md`:** el error de categorías es un texto propio en lugar del mensaje de `Field`, y los ticks del password usan el verde de Fluent aplicado directamente.
- **Unión con el backend:** ajustar rutas, cuerpo y códigos de error (sección 5).

---

## 9. Uso de IA generativa

Este documento y el trabajo técnico que describe se hicieron con ayuda de un asistente de IA generativa, como exige el enunciado.

- **Herramienta:** Claude (modelo de Anthropic), mediante Claude Code.
- **Qué generó la IA:** los tests y el código de cada ciclo de TDD, los refactors, los mocks del registro y este documento.
- **Qué hizo el equipo:** definió los requisitos de cada campo (obligatoriedad, longitudes, formato del teléfono, reglas del password), decidió la convención Email/Password, el formato de las categorías, los códigos de error y el estilo. Aprobó cada ciclo antes de pasar al siguiente e hizo cada commit.
- **Cómo se comprobó:** en cada ciclo se ejecutaron los tests (129 en verde al final), ESLint sin errores y el build de producción. La pantalla se revisó en el navegador con los mocks.
- **Qué NO se comprobó:** el flujo contra un backend real. El contrato de la sección 5 debe acordarse con el backend.
- **Revisión humana:** el código fue revisado, ejecutado y validado por el equipo antes de su integración. Revisado por: Alejandro.
