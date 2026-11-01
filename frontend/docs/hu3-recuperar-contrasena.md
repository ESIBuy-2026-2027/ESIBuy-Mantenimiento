# HU3 · Recuperación de contraseña (Frontend)

Documento para entender cómo recupera un usuario su contraseña desde el frontend y qué contrato sigue con el backend.

- **Fecha:** 1 de noviembre de 2026
- **HU:** HU3 · Recuperación de contraseña (AB#65)
- **Task:** AB#398 (formulario de recuperación de contraseña)
- **Rama:** `feature/US3-T398-formulario-recuperacion-contrasena`
- **Metodología:** TDD; el commit `test(front-HU3): ciclo rojo` va antes que el `feat(front-HU3): ciclo verde`
- **Depende de:** la lógica del backend (T400, AB#400, PR #172) y sus endpoints (T399, AB#399, PR #176)

---

## 1. Resumen

| Paso | Qué hace el usuario | Qué pasa |
|---|---|---|
| 0 | En el login pulsa **«¿Has olvidado tu contraseña?»** | Va a `/forgot-password`. Con la sesión abierta, esa ruta lleva a su inicio, como el login. |
| 1 | Escribe su correo y pulsa **Enviar código** | Se pide el código. El aviso es siempre el mismo, exista o no la cuenta: **«Si el email está registrado, te hemos enviado un código de 6 dígitos. Caduca en 10 minutos.»** |
| 2 | Escribe el código, la contraseña nueva y su repetición, y pulsa **Cambiar contraseña** | Si todo es correcto, vuelve al login con el aviso **«Contraseña cambiada. Ya puedes iniciar sesión.»** |
| 2 | Pulsa **Pedir otro código** | Se pide otro para el mismo correo. El anterior deja de servir (el backend guarda solo el último). |

Errores del paso 2:

| Respuesta | Qué se muestra |
|---|---|
| `401` (código incorrecto, caducado, ya usado o con 5 fallos) | «El código no es correcto o ha caducado. Revísalo o pide otro.» Se queda en el paso para volver a intentarlo. |
| `400` con el `code` de la política (`PASSWORD_WEAK`, `PASSWORD_CONTAINS_PERSONAL_DATA`...) | El mensaje en el campo de la contraseña nueva, con la misma traducción que el registro (`registroErrors.js`). |
| Cualquier otro | El aviso genérico de los formularios de cuenta, sin detalles internos. |

Decisiones:

- **Página aparte**, y no un paso más del login: el login ya tiene tres pasos y así cada flujo queda separado.
- **Se pide repetir la contraseña nueva**, como en el registro, aunque al backend solo se envía una.
- **Antes de enviar se comprueba** que el código tiene 6 dígitos y que la contraseña cumple los requisitos de la política y coincide con su repetición. Se reutilizan `validarPasswords` y la lista de requisitos del registro.
- Como en el registro, en los campos de la contraseña nueva no se puede pegar (T242).
- Los estilos de la tarjeta del login pasan a `authFormStyles.js` (`useAuthPageStyles`), para que las dos páginas no los dupliquen.

---

## 2. Contrato con el backend (T399, PR #176)

```
POST /api/auth/password-recovery
{ "email": "cliente@esibuy.test" }
→ 204 siempre (aunque el email no exista o falle el envío del correo)

POST /api/auth/password-recovery/confirm
{ "email": "cliente@esibuy.test", "code": "112233", "password": "Nueva-Clave-9!" }
→ 204 | 401 | 400 { "code": "PASSWORD_..." }
```

Las dos rutas son públicas, pero piden el token anti-CSRF. `api/client.js` lo envía en cada `POST`, como en el login.

En los mocks (`authHandlers.js`), el código válido es **112233** para cualquier cuenta del mock. Con otro código, o con un email que no está en el mock, responde 401.

---

## 3. Archivos

| Archivo | Cambio |
|---|---|
| `src/pages/ForgotPasswordPage.jsx` | Página nueva con los dos pasos. |
| `src/pages/LoginPage.jsx` | Enlace «¿Has olvidado tu contraseña?» y aviso de contraseña cambiada. Usa los estilos compartidos. |
| `src/features/auth/authFormStyles.js` | `useAuthPageStyles`: la tarjeta centrada de las páginas de acceso. |
| `src/api/auth.js` | `requestPasswordRecovery(email)` y `resetPassword({ email, code, password })`. |
| `src/mocks/authHandlers.js` | Los dos endpoints. |
| `src/routes/paths.js` y `AppRoutes.jsx` | Ruta `/forgot-password`. |

---

## 4. Tests

Los casos siguen la numeración del backend (HU3-CP01 a CP17).

| Caso | Qué se prueba | Archivo |
|---|---|---|
| HU3-CP18 | El login tiene el enlace a la recuperación | `LoginFlow.test.jsx` |
| HU3-CP19 | Sin email, o con uno mal formado, avisa y no pide código | `ForgotPasswordPage.test.jsx` |
| HU3-CP20 | Pide el código para el email escrito (sin espacios) y pasa al paso 2; con un email no registrado, el mismo aviso; si el servidor falla, lo avisa y no avanza (3 tests) | `ForgotPasswordPage.test.jsx` |
| HU3-CP21 | Comprueba el código y la contraseña antes de enviarlos, y muestra los requisitos (2 tests) | `ForgotPasswordPage.test.jsx` |
| HU3-CP22 | Con el código correcto envía email, código y contraseña, y vuelve al login con el aviso | `ForgotPasswordPage.test.jsx` |
| HU3-CP23 | Código incorrecto o caducado: aviso, y se puede volver a intentar o pedir otro código (2 tests) | `ForgotPasswordPage.test.jsx` |
| HU3-CP24 | Contraseña rechazada por la política: mensaje en el campo; otro error: aviso genérico (2 tests) | `ForgotPasswordPage.test.jsx` |
| HU3-CP25 | Se puede volver al login; con la sesión abierta lleva a su inicio (2 tests) | `ForgotPasswordPage.test.jsx` |

---

## 5. Prueba con el backend real (envío por Brevo)

Los tests usan mocks. Para comprobar que el correo llega de verdad:

1. Arrancar el backend desde `backend/`, con `MONGODB_URI` y `ESIBUY_PAGOS_CLAVE_CIFRADO` definidas: `./mvnw spring-boot:run`.
2. Arrancar el frontend sin mocks desde `frontend/`. En PowerShell: `$env:VITE_USE_MOCKS='false'; npm run dev`.
3. En `http://localhost:5173/login`, pulsar «¿Has olvidado tu contraseña?».
4. Pedir el código para una cuenta **activa** con un correo que se pueda leer (mirar también en spam).
5. Escribir el código y la contraseña nueva, y comprobar que se entra con ella (los tres pasos del login).
6. Comprobar también:
   - con un código incorrecto sale el aviso;
   - con un email que no existe sale el mismo aviso y no llega ningún correo.
