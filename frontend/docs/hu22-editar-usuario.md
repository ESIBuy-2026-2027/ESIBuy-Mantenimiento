# HU22 · Modificar datos y rol de un usuario (Frontend)

Documento para entender cómo el administrador edita los datos de clientes, administradores y vendedores desde su ficha: qué se puede cambiar en cada caso, qué reglas aplican, qué hace cada parte del código, qué contrato espera del backend y cómo probarlo.

- **Fecha:** 10 de octubre de 2026
- **HU:** HU22 · Modificar datos de un usuario (administrador) (2 puntos)
- **Tasks:** AB#45 (formulario de edición), AB#46 (tests del formulario de edición) y AB#43 (el rol no se puede cambiar)
- **Ramas:** `feature/US22-T45-T46-edicion-usuario` (primera entrega: datos), `feature/US22-T45-T46-cambio-de-rol-y-endpoint-unico` (segunda: cambio de rol, endpoint único y administradores sin estado pendiente) `fix/US22-T43-rol-no-editable` (tercera, 11/10/2026: se quita el cambio de rol) y `fix/US22-T43-editar-vendedor` (cuarta, 11/10/2026: la edición del vendedor usa los datos del registro de vendedores)
- **Metodología:** sin TDD por decisión del equipo. Primero el código (commits `refactor`, `chore`, `feat` y `fix` con AB#45) y después los tests (commits `test` con AB#46).
- **Actualización del 11/10/2026:** el equipo ha decidido que **el rol de una cuenta no se puede cambiar** ("el que nace cliente muere cliente"). Se quitó el cambio de rol del frontend y del backend (AB#43); el resto de la HU22 sigue igual. Lo que este documento contaba del cambio de rol ya no aplica y se ha reescrito.
- **Actualización del 11/10/2026 (edición del vendedor):** con el registro de vendedores (T241) el backend ya guarda el nombre comercial, el DNI, el teléfono y la categoría del vendedor. El formulario del vendedor envía `dni` (antes `nif`, con otra validación), el teléfono y la imagen, y la lista de categorías llega de `GET /api/categorias`, que ya existe en el backend.
- **Depende de:** `docs/hu20-ficha-usuario.md` (ficha de clientes y administradores), `docs/hu26-supervision-vendedores.md` (ficha del vendedor), `docs/hu21-alta-admin-y-vendedores.md` (validaciones y piezas de cuenta), `docs/hu1-registro.md` (reglas del registro), `docs/mocks-msw.md` (mocks) y `docs/sonar-incidencias.md` (convención de `propTypes`)

---

> **Cambio del Sprint 2 (AB#249):** el formulario de edición se abre desde el lápiz de la fila de la tabla (`UserEditDialog`) y ya no desde la ficha, que es de solo lectura. Los campos, las validaciones y el contrato no cambian. Al guardar, el formulario se cierra y el aviso sale en la barra de la página (ver `docs/hu19-listado-usuarios.md`, sección 1.1). Lo que el texto de abajo dice sobre el *modo edición* de la ficha describe el diseño anterior.

---

## 1. Resumen

### Quién edita a quién

**Solo el administrador** edita datos de otros usuarios, y puede editar a **cualquier** cliente, administrador o vendedor, incluido él mismo. Ocurre desde la **ficha** de cada usuario (la que se abre con la lupa de los listados de Clientes, Administradores y Vendedores): la ficha tiene el botón **Editar**.

> La barrera real es el backend (RBAC): `/api/admin/**` solo para el rol administrador. El frontend solo oculta o muestra los botones.

### Qué se puede cambiar

Al pulsar **Editar**, la ficha pasa a **modo edición**: los campos que se pueden modificar se vuelven campos normales y **el resto se queda en solo lectura, más oscuro**, para que se vea qué se puede tocar.

| Tipo | Se puede modificar | No se puede modificar |
|---|---|---|
| **Cliente** | Nombre, apellidos, DNI, teléfono, fecha de nacimiento (siempre que sea **mayor de edad**), tipo de cliente (Estándar o Premium) e imagen de perfil | **Email**, **rol** y estado |
| **Administrador** | Nombre, apellidos, sede e imagen de perfil | **Email**, **rol**, **fecha de incorporación** (es la fecha de alta), estado y fecha de alta |
| **Vendedor** | Nombre, apellidos, nombre comercial (que siga siendo **único**), DNI, teléfono, **una sola categoría** e imagen de perfil | **Email** (por diseño del backend, de momento no se puede cambiar), **rol** y estado |

La **fecha de alta** solo se muestra en la ficha del **administrador**: la de clientes y vendedores no se guarda, así que su ficha no tiene ese campo.

Para los tres tipos:

- **La contraseña no se cambia desde aquí.** El campo no existe en el formulario. Solo se cambia con "He olvidado mi contraseña", igual para los tres tipos (esa pantalla todavía no existe en el frontend; ver la sección 9).
- **Los productos, los precios y los pedidos de un vendedor siguen siendo de solo lectura**, como en la HU26: mientras se edita, las pestañas Productos y Pedidos están deshabilitadas.
- **El estado** (activar o desactivar) sigue cambiándose con su propio botón y su confirmación (HU23 y HU24), no desde el formulario.

### El rol no se puede cambiar

Decisión del equipo (11/10/2026): **"el que nace cliente muere cliente"**. Una cuenta se identifica por su correo, que es único, y conserva para siempre el rol con el que se creó.

- En el formulario, el **Rol** es un campo de **solo lectura**, como el email. El formulario solo muestra y valida los campos del rol de la cuenta.
- El cuerpo de la petición **no lleva el rol**. Si llegara, el backend lo ignora (como el email).
- El **email nunca se cambia desde esta pantalla**, tampoco el de un vendedor (decisión del equipo por diseño del backend: de momento no se puede cambiar el correo de un vendedor). En todos los roles es un campo de solo lectura.
- Antes (segunda entrega) el rol era un desplegable con sus reglas: un administrador no podía cambiar el suyo ni degradar al último administrador, y una cuenta pendiente que pasaba a administrador quedaba activa. Todo eso se ha quitado.

### Los administradores no están pendientes

Un administrador siempre lo da de alta otro administrador, así que **nunca está en estado pendiente**:

- Los filtros de estado de los administradores (listado de Administradores y listado general de usuarios filtrando por Administrador) **no ofrecen "Pendiente"**. Al elegir Administrador en el listado general con el estado Pendiente seleccionado, ese filtro se limpia.
- Los datos de prueba ya no tienen ningún administrador pendiente.

---

## 2. Flujo

```mermaid
flowchart TD
    A[Listado de Clientes, Administradores o Vendedores] -- Lupa de una fila --> B[Ficha en solo lectura]
    B -- Editar --> C[Modo edición: campos modificables + campos en solo lectura]
    C -- Cancelar --> B
    C -- Guardar --> D{¿Datos válidos?}
    D -- No --> E[Todos los errores a la vez, sin llamar al servidor]
    E -- Corrige un campo --> F[Se revalida en vivo]
    F --> D
    D -- Sí --> G[PUT al backend]
    G -- 400 o 409 con un code de campo --> H[Error junto al campo, sigue en edición]
    G -- Otro error --> I[Mensaje genérico, conserva lo escrito]
    H --> C
    I --> C
    G -- 200 --> K[Vuelve a la ficha con los datos nuevos y el aviso de guardado]
    K --> N[El listado se recarga y muestra los cambios]
```

---

## 3. Componentes y archivos

Todas las rutas son relativas a `frontend/src/`.

### Definición de los campos (`features/admin/`)

| Archivo | Qué hace |
|---|---|
| `profileFields.js` | **Una sola definición de los campos de cada tipo de usuario** que sirve para los dos modos. Cada campo indica su etiqueta, cómo se muestra en lectura (`read`) y, si se puede modificar, sus reglas (`edit`: tipo, obligatorio, longitud máxima y validación). Lo que no tiene `edit` es de solo lectura. Aquí están las reglas "qué se puede cambiar" de la sección 1. El rol no tiene `edit`: siempre es de solo lectura. Exporta `profileFieldsFor`, `readFields`, `initialValues`, `validateProfile` y `profilePayload`, que trabajan siempre con el rol de la cuenta. |
| `ProfileEditor.jsx` | El formulario de edición. Pinta todos los campos de la ficha: los modificables como campos normales (texto, fecha, desplegable o selector de avatar) y el resto como solo lectura (el rol, entre ellos). Valida, muestra los errores, envía los cambios con la función `save` que recibe y avisa con `onSaved` (le pasa lo enviado) o `onCancel`. |
| `EditableProfile.jsx` | Envoltorio del formulario. **Solo para un vendedor carga las categorías** (para elegir la principal) y muestra un indicador de carga o un error con **Reintentar**; el resto de cuentas abren el formulario directamente. Lo usan las dos fichas. |
| `UserDetailDialog.jsx` | La ficha de clientes y administradores: botón **Editar**, aviso "Modo edición" y formulario. |
| `SellerDetailDialog.jsx` | La ficha del vendedor: **Editar** (solo en la pestaña Datos), pestañas deshabilitadas mientras se edita y, al guardar, vuelve a la ficha y avisa al listado. |
| `SellerDataForm.jsx` | Muestra los datos del vendedor en lectura con `readFields`. |
| `usersConfig.js` | Añade `statusLabelsForRole` (los estados que ofrece el filtro de cada rol: sin "Pendiente" para administradores). |

### Reglas compartidas, API y mocks

| Archivo | Qué hace |
|---|---|
| `features/account/registroErrors.js` | `erroresDelServidor(error)`: traduce el `code` de un 400 o 409 del backend al campo y su mensaje, igual que en el registro (fallos de la demo del Sprint 1; ver `docs/hu1-registro.md`, sección 1.2). |
| `features/account/accountRules.js` | `esDniValido`, `esTelefonoValido` y `esMayorDeEdad`, que antes estaban dentro del registro. `pages/Register.jsx` las usa desde aquí. |
| `api/users.js` | `updateUser(userId, datos)`: **una sola llamada para los tres tipos de usuario**. |
| `api/adminSellers.js` | Ya no tiene función de edición: los vendedores se editan con `updateUser`. |
| `mocks/usersHandlers.js` | Simula `PUT /api/admin/usuarios/{id}` para cualquier cuenta, también vendedores: valida según el rol de la cuenta, ignora el rol del cuerpo y comprueba la unicidad del nombre comercial. |
| `mocks/adminSellersHandlers.js` | Almacén de vendedores del mock, que el `PUT` de usuarios actualiza. Ya no tiene `PUT` propio. |
| `mocks/fechas.js` | Comprobación de mayoría de edad de los mocks. |

---

## 4. Validación

| Campo | Regla |
|---|---|
| Nombre y apellidos | Obligatorios, máximo 30 caracteres |
| DNI (cliente y vendedor) | Obligatorio, 8 números y una letra. Único entre las cuentas del mismo rol: un cliente y un vendedor pueden tener el mismo DNI |
| Teléfono (cliente y vendedor) | Opcional; si se rellena, 9 números |
| Fecha de nacimiento (cliente) | Obligatoria y de una persona **mayor de edad** (un día antes de cumplir los 18 ya no vale) |
| Tipo de cliente | Estándar o Premium |
| Sede (administrador) | Opcional, máximo 30 caracteres |
| Nombre comercial (vendedor) | Obligatorio, máximo 30 caracteres, único |
| Categoría (vendedor) | Obligatoria, **una sola**, de las categorías del sistema (campo `categoriaPrincipalId`) |

- **Se valida con los campos del rol de la cuenta.**
- **Todos los errores a la vez** al pulsar **Guardar**, sin llamar al servidor si hay alguno.
- **En vivo después del primer intento:** el error de un campo desaparece en cuanto se corrige.
- **El servidor también valida.** Si el nombre comercial ya es de otro vendedor, el mensaje sale junto a su campo. Cualquier otro fallo da el mensaje genérico "No se han podido guardar los cambios", sin detalles internos, y el formulario conserva lo escrito.
- **Doble clic:** mientras se guarda, los botones se deshabilitan y solo se envía una petición.

---

## 5. Seguridad

- **Solo administradores.** Las rutas están protegidas y los mocks responden 401 sin sesión y 403 con otro rol. La barrera real es el backend.
- **No viajan datos que no se pueden cambiar:** nunca se envían el email (de ningún rol), el rol, la fecha de incorporación, el estado ni la contraseña (`profilePayload` solo incluye los campos modificables del rol de la cuenta). Si el backend recibiera uno, debe ignorarlo o rechazarlo.
- **El rol no se puede cambiar:** el frontend no lo envía y el backend lo ignora aunque llegue.
- **No existe campo de contraseña** en ninguna ficha, ni en lectura ni en edición.
- **Los errores del servidor no se muestran tal cual,** solo mensajes fijos: junto al campo si el `code` lo indica, o uno genérico.

---

## 6. Contrato esperado del backend

**El frontend se ha desarrollado y probado con mocks (MSW).** Las rutas y los formatos los ha definido el frontend; **hay que acordarlos con el backend.**

### Un único endpoint para los tres tipos

`PUT /api/admin/usuarios/{id}`. Antes los vendedores tenían uno propio (`/api/admin/sellers/{id}`); ya no. En el backend los tres tipos de usuario viven en la misma colección, así que un solo endpoint basta. **El cuerpo no lleva el rol** y solo trae los campos modificables del rol de la cuenta:

```json
{ "nombre": "Carla", "apellidos": "Moreno Gil", "dni": "10004119A",
  "telefono": "620023757", "fechaNacimiento": "1978-04-13", "tipoCliente": "PREMIUM", "avatar": "avatar1" }
```

```json
{ "nombre": "Diego", "apellidos": "Delgado Iglesias", "sede": "Cuenca", "avatar": "avatar3" }
```

```json
{ "nombre": "Vera", "apellidos": "Vendedora",
  "nombreComercial": "TecnoVera", "dni": "12345601V", "telefono": "612345601",
  "categoriaPrincipalId": "cat-1", "avatar": "avatar2" }
```

El email y el rol no viajan nunca. El backend (AB#43) ignora el `rol` si llega en el cuerpo y valida los datos según el rol que ya tiene la cuenta.

### Respuestas

| Respuesta | Significado para el frontend |
|---|---|
| 200 + datos del usuario | Guardado. La ficha se recarga |
| 400 con un `code` de campo (`INVALID_DNI`, `INVALID_PHONE`, `UNDERAGE`...) | El motivo, junto a su campo (los mismos códigos que el registro, `docs/hu1-registro.md`, sección 5) |
| 400 con otro `code` (`VALIDATION_ERROR`...) | Mensaje genérico |
| 409 + `{ "code": "COMMERCIAL_NAME_ALREADY_REGISTERED" }` | "Este nombre comercial ya está registrado" (junto al campo) |
| 409 + `{ "code": "DNI_ALREADY_REGISTERED" }` | "Este DNI ya está registrado" (junto al campo; otra cuenta del mismo rol tiene ese DNI) |
| 401 / 403 / 404 / 5xx | Mensaje genérico |

### Qué debe imponer el backend

- Que solo el administrador pueda llamar a este endpoint.
- Que el **rol no cambie nunca**, aunque llegue en el cuerpo.
- Que el email (de cualquier rol) y la fecha de incorporación **no cambien nunca**, aunque lleguen en el cuerpo.
- Que la contraseña no se pueda cambiar por esta vía.
- La mayoría de edad de la fecha de nacimiento, la unicidad del nombre comercial y la del DNI dentro de cada rol.
- Que el vendedor guarde siempre sus datos de vendedor: el backend guarda el perfil completo, así que si falta el nombre comercial, el DNI o la categoría responde 400, y lo que no se envía (teléfono o imagen) se borra.
- Que el detalle del vendedor (`GET /api/admin/sellers/{id}`) incluya `categoriaPrincipalId` además de `categoriaPrincipal` (el nombre), porque el formulario elige la categoría por identificador. Lo hace desde AB#43, junto con `dni` y `telefono`.
- Que `GET /api/categorias` devuelva `[{ "id", "nombre" }]`. Existe desde AB#43 (módulo `categorias` del backend), es público y ordena por nombre.
- Que la ficha de un administrador (`GET /api/admin/usuarios/{id}`) traiga `fechaIncorporacion`. El backend la devuelve con el mismo instante que `fechaAlta` (AB#43), y el frontend la formatea igual que la fecha de alta.

---

## 7. Datos de prueba y cómo verlo en el navegador

Con `npm run dev` (mocks activos): `admin@esibuy.test`, contraseña `Mock-Clave-1!`, código de correo `654321` y código de Authenticator `123456`. Los cambios se olvidan al recargar la página.

| Prueba | Resultado |
|---|---|
| **Clientes** → lupa de `Carlos Moreno Gil` → **Editar** | Nombre, apellidos, DNI, teléfono, fecha de nacimiento, tipo e imagen se pueden cambiar; email, rol y estado, oscuros; no hay fecha de alta |
| Poner una fecha de nacimiento de hace 10 años → **Guardar** | "Debe ser mayor de edad" |
| **Administradores** → lupa de `Ada Admin` (la sesión) → **Editar** | Se pueden editar nombre, apellidos, sede e imagen; el rol es de solo lectura |
| **Administradores** → filtro de estado | Solo Activo y Desactivado (no hay Pendiente) |
| **Usuarios** → rol **Administrador** | El filtro de estado tampoco ofrece Pendiente |
| **Vendedores** → lupa de `TecnoVera` → **Editar** | Todos los datos menos el email y el rol se pueden cambiar; las pestañas Productos y Pedidos están deshabilitadas |
| Cambiar el nombre comercial a `Casa Vicente` | "Este nombre comercial ya está registrado" |
| Intentar escribir en el campo **Correo electrónico** | No se puede: es de solo lectura (fondo oscuro) |
| **Cancelar** | Vuelve a la ficha sin guardar nada |

### Tests automáticos

```bash
npx vitest run     # 571 tests tras la AB#43
npm run lint
npm run build
```

Esta rama pasa de 518 a 590 tests (74 nuevos y 2 menos, los de la función de edición propia de los vendedores, que ya no existe). Los nuevos son:

| Archivo | Qué cubre |
|---|---|
| `features/admin/profileFields.test.js` | El campo rol, los campos que ofrece cada rol destino, que el email no sea editable en ningún rol, valores iniciales con los campos de todos los roles, validación y cuerpo de la petición en un cambio de rol |
| `features/admin/ProfileEditor.test.jsx` | El desplegable de rol, su bloqueo y su ayuda, el cambio de campos al elegir otro rol, los datos exigidos al pasar a vendedor o a cliente, el aviso de cuenta pendiente que pasa a administrador, y los mensajes de "propio rol" y "último administrador" |
| `features/admin/EditableProfile.test.jsx` (nuevo) | Carga de las categorías, error con reintento y bloqueo del rol |
| `features/admin/UserDetailDialog.test.jsx` | Rol bloqueado en la propia ficha, ascenso de un cliente a administrador, paso a vendedor y a cliente, cuenta pendiente que queda activa, errores del backend y cancelar |
| `features/admin/SellerDetailDialog.test.jsx` | Rol en la ficha del vendedor, uso del endpoint único, y que la ficha se cierra al dejar de ser vendedor |
| `features/admin/usersConfig.test.js` (nuevo) | `effectiveStatus` y `statusLabelsForRole` |
| `features/admin/RoleUsersList.test.jsx` | El listado de administradores no ofrece ni muestra el estado pendiente |
| `pages/AdminUsersPage.test.jsx` | El filtro de estado del listado general no ofrece Pendiente para administradores y se limpia al elegirlos |
| `pages/AdminClientsPage.test.jsx`, `pages/AdminSellersPage.test.jsx` | Que una cuenta que cambia de rol sale del listado de su rol anterior |
| `api/users.test.js` | Las reglas del endpoint único en el mock: propio rol, rol inexistente, pendiente que pasa a administrador, vendedor que pasa a cliente y unicidad del nombre comercial |

**AB#43 (el rol no se puede cambiar, 571 tests):** se quitaron los tests del cambio de rol (desplegable, bloqueo del rol propio, paso a otro rol, cuenta pendiente que pasa a administrador, mensajes de "propio rol" y "último administrador", `effectiveStatus` y los de los listados al cambiar de rol). Se añadieron tests de que el rol se muestra en solo lectura en los tres tipos de cuenta y en las dos fichas, de que no viaja al guardar (ni aunque esté en los valores), de que las categorías solo se cargan para un vendedor y de que el mock ignora el rol del cuerpo.

Se ajustaron tests existentes de la HU19 (con los datos nuevos hay 4 cuentas pendientes, no 5, porque ya no hay ningún administrador pendiente).

**Fallos de la demo del Sprint 1:** un test nuevo en `features/admin/ProfileEditor.test.jsx`, `HU22-CP16: un DNI ya registrado se marca en el DNI` (antes salía el mensaje genérico). Es el único test del frontend de la HU22 que ya está en la matriz.

**AB#43 (edición del vendedor, 571 tests):** los tests del vendedor usan `dni` en lugar de `nif` (con la validación del DNI del cliente) y esperan el teléfono y la imagen en el formulario, en el cuerpo y en el mock. La ficha del vendedor tiene 10 campos en lugar de 8.

---

## 8. Decisiones y librerías

- **No se ha añadido ninguna librería nueva.**
- **Una sola definición de campos (`profileFields.js`)** para los dos modos y para los tres tipos de usuario: evita repetir etiquetas y reglas, y hace que "qué se puede editar" esté en un solo sitio. Cambiar una regla de edición es tocar una línea.
- **Un único endpoint de edición** (`PUT /api/admin/usuarios/{id}`) en lugar de uno para vendedores: es más simple, y es lo que encaja con el backend (una sola colección de usuarios). Si el backend decide otra cosa, el cambio en el frontend es `updateUser` y poco más.
- **El formulario edita en la propia ficha** (no en una página aparte), igual que la ficha y su estado: el administrador no pierde el listado ni su posición.
- **El rol no se puede cambiar** (decisión del equipo, 11/10/2026: "el que nace cliente muere cliente"). En la segunda entrega el formulario cambiaba de campos al elegir otro rol; se quitó junto con sus reglas en el frontend y en el backend (AB#43).
- **El email no se puede cambiar:** ni en clientes, ni en administradores, ni en vendedores. Al principio se permitía en los vendedores; el equipo decidió quitarlo porque, por diseño del backend, de momento no se puede cambiar el correo de un vendedor. Para volver a permitirlo bastaría con darle `edit` al campo en `profileFields.js`.
- **Un vendedor tiene una sola categoría** (decisión del equipo): el formulario sigue usando `categoriaPrincipalId`, un único identificador.
- **Cliente Premium:** el administrador puede pasar a un cliente de Estándar a Premium y al revés. Pasar a Premium en el registro lleva a un flujo de pago que aún no existe; no hay que confundirlos.
- **Fecha de alta solo en administradores:** la de clientes y vendedores no se guarda, así que se quitó de sus fichas. El listado general de usuarios (HU19) y el de clientes siguen teniendo la columna "Fecha de alta" de `UsersTable`; si tampoco debe mostrarse, hay que quitarla de la tabla y del contrato.
- **Los mensajes del servidor** son fijos y genéricos, como en el resto de pantallas.

## 9. Limitaciones y trabajo pendiente

- **"He olvidado mi contraseña"** es la única vía de cambio de contraseña para los tres tipos de usuario, pero **esa pantalla todavía no existe** (el login no tiene el enlace). Hasta entonces, nadie puede cambiar su contraseña.
- **Vendedores anteriores al registro de vendedores (T241):** no tienen nombre comercial, DNI ni categoría; la primera vez que se editan hay que rellenarlos.
- **Categorías:** si la colección `categorias` de MongoDB está vacía, el formulario del vendedor (y el del registro) no tiene categorías que elegir.
- **Matriz de trazabilidad:** los casos de la HU22 (`HU22-CPnn`) de la matriz solo trazan los tests del backend; los tests del frontend (AB#46), salvo el HU22-CP16 de los fallos de la demo, todavía no están en `docs/calidad/matriz-trazabilidad.md`.
- Aspecto visual sin revisar en un navegador real (ver la sección 10).

---

## 10. Uso de IA generativa

Este documento y el trabajo técnico que describe se hicieron con ayuda de un asistente de IA generativa, como exige el enunciado.

- **Herramienta:** Claude (modelo de Anthropic), mediante Claude Code.
- **Qué generó la IA:** la definición de campos, el formulario de edición, los cambios en las dos fichas, el cambio de rol y, después, su eliminación (AB#43), los mocks, los tests y este documento. Las decisiones de producto las tomó el equipo: quién puede editar a quién, qué campos se pueden modificar en cada tipo de usuario (incluida la mayoría de edad y la unicidad del nombre comercial), que la contraseña solo se cambie con "He olvidado mi contraseña", que los productos, precios y pedidos del vendedor sigan siendo de solo lectura, que el rol de una cuenta no se pueda cambiar (al principio se permitía), que los administradores no tengan estado pendiente y que el frontend use un único endpoint de edición.
- **Cómo se comprobó:** se ejecutaron los **590 tests (todos en verde)**, ESLint sin errores y un build de producción correcto. Tras la AB#43, **571 tests en verde**, ESLint sin errores y build correcto.
- **Qué NO se comprobó:** la pantalla en un navegador real ni su aspecto visual (por ejemplo, el contraste entre campos modificables y de solo lectura), ni el flujo contra un backend real. El contrato de la sección 6 debe acordarse con el backend.
- **Cómo guió el proceso:** el asistente siguió el flujo de ramas y commits del proyecto (refactor, mocks, código, tests y documentación en commits separados, con los AB# de las tasks).
- **Fallos de la demo del Sprint 1:** la IA escribió con TDD el test HU22-CP16 y el cambio para que la edición marque en su campo los errores del backend (con el módulo compartido con el registro), y actualizó este documento. El equipo decidió qué fallos se corregían. Suite completa: **591 tests en verde**, ESLint sin errores y build correcto.
- **Revisión humana:** el código fue revisado, ejecutado y validado por el equipo antes de su integración. Revisado por: Ricardo.
