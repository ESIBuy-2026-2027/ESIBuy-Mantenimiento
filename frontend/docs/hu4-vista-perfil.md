# HU4 · Visión de perfil (Frontend)

Documento para entender la vista «Mi perfil»: quién la ve, qué muestra, qué contrato usa del backend y cómo probarla.

- **Fecha:** 27 de octubre de 2026
- **HU:** HU4 · Visión de perfil
- **Task:** AB#296 · Frontend: vista de perfil
- **Rama:** `feature/US4-T296-vista-perfil`
- **Metodología:** sin TDD; los tests se escribieron junto a la implementación
- **Depende de:** el endpoint del backend de la T297 (AB#297, PR #140), `docs/hu20-ficha-usuario.md` (campos de la ficha) y `docs/mocks-msw.md` (mocks)

---

## 1. Resumen

El cliente y el vendedor ven sus datos personales en `/profile`, en solo lectura. Llegan desde el enlace **Mi perfil** de la cabecera. Modificarlos es de la HU45.

| Criterio | Cómo se cumple |
|---|---|
| El usuario visualiza sus datos personales | `ProfilePage` pide `GET /api/perfil` y muestra los datos con la misma rejilla y los mismos formatos que la ficha del administrador (HU20 y HU26). |
| Solo sus datos | El frontend no envía ningún identificador: el backend saca al usuario de la sesión. |
| Solo cliente y vendedor | La ruta solo admite esos dos roles y el enlace solo les aparece a ellos. El backend responde 403 al administrador. |
| Datos sensibles | No se muestran la contraseña ni el estado de la cuenta (el backend tampoco los envía). |

---

## 2. Qué muestra

- **Cabecera de la tarjeta:** el avatar elegido en el registro como imagen (gato, perro o conejo), con su nombre accesible («Avatar 2»). Sin avatar, sus iniciales. Al lado, el nombre completo y el email.
- **Campos comunes:** nombre, apellidos, email, rol, DNI, teléfono y fecha de alta.
- **Cliente:** fecha de nacimiento y tipo de cliente (Estándar o Premium).
- **Vendedor:** nombre comercial y categoría principal, con su nombre (el backend envía el identificador y se traduce con `useCategories`).
- Un dato opcional que falta (p. ej. el teléfono) se muestra con un guion.
- **Estados:** spinner mientras carga; si falla, un aviso genérico con **Reintentar**.

---

## 3. Componentes y archivos

| Archivo | Cambio |
|---|---|
| `src/pages/ProfilePage.jsx` | Página nueva. Reutiliza `ReadOnlyFields` y `profileFieldsFor` (de la ficha del administrador), sin los campos `estado` e `avatar`, y añade la fecha de alta. |
| `src/features/account/AvatarSelector.jsx` | Nuevo `UserAvatar`: dibuja el avatar elegido con los mismos iconos y colores que el selector del registro. |
| `src/api/profile.js` | Nueva `getProfile()`. |
| `src/routes/paths.js` y `AppRoutes.jsx` | Ruta `/profile` (`PATHS.PROFILE`), protegida para `CLIENTE` y `VENDEDOR`. |
| `src/components/AppHeader.jsx` | Enlace **Mi perfil** para el cliente y el vendedor. |
| `src/mocks/profileHandlers.js` y `handlers.js` | Mock de `GET /api/perfil`. |

---

## 4. Contrato con el backend (T297, ya implementado)

```
GET /api/perfil
```

```json
{
  "id": "u-3", "nombre": "Carlos", "apellidos": "Cliente", "email": "cliente@esibuy.test", "rol": "CLIENTE",
  "fechaAlta": "2026-04-10T17:40:00Z", "dni": "87654321X", "telefono": "611222333", "avatar": "avatar1",
  "fechaNacimiento": "1998-05-14", "tipoCliente": "PREMIUM"
}
```

- El vendedor trae `nombreComercial` y `categoriaPrincipalId` en lugar de `fechaNacimiento` y `tipoCliente`.
- Los campos vacíos no llegan.
- `401` sin sesión y `403` para el administrador.

---

## 5. Mocks

`profileHandlers.js` responde como el backend con los datos del usuario de ejemplo en sesión:

| Usuario | Perfil |
|---|---|
| `cliente@esibuy.test` (Carlos) | Cliente Premium, avatar 1 |
| `vendedor@esibuy.test` (Vera) | Vendedora de Tecnología («Tienda de Vera»), avatar 2 |
| `vendedor-vacio@esibuy.test` (Vicente) | Vendedor de Hogar, sin teléfono ni avatar |
| `nuevo@esibuy.test` (Nora) | Clienta Estándar, sin teléfono ni avatar |

---

## 6. Tests

Los casos siguen la numeración del backend (HU4-CP01 a CP04).

| Caso | Qué se prueba | Archivo |
|---|---|---|
| HU4-CP05 | El cliente ve sus datos personales y los de cliente, en solo lectura (2 tests) | `ProfilePage.test.jsx` |
| HU4-CP06 | El vendedor ve sus datos y los de vendedor, con el nombre de su categoría; un dato que falta se ve con un guion (2 tests) | `ProfilePage.test.jsx` |
| HU4-CP07 | El avatar se ve como imagen y, sin avatar, salen las iniciales (4 tests) | `ProfilePage.test.jsx` y `AvatarSelector.test.jsx` |
| HU4-CP08 | No se muestran la contraseña ni el estado de la cuenta | `ProfilePage.test.jsx` |
| HU4-CP09 | Si el perfil no carga, se avisa sin detalles internos y se puede reintentar | `ProfilePage.test.jsx` |
| HU4-CP10 | Sin sesión se va al login y el administrador no entra (2 tests) | `ProfilePage.test.jsx` |
| HU4-CP11 | El enlace «Mi perfil» de la cabecera: lo tienen el cliente y el vendedor, no el administrador, y abre el perfil (3 tests) | `AppHeader.test.jsx` |
