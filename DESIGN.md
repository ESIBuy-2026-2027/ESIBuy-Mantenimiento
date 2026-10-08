---
name: ESIBuy
description: Marketplace académico con tres roles, construido sobre Fluent UI v9 con acento naranja.
colors:
  brand-orange: "#EA580C"
  brand-orange-deep: "#C2410C"
  brand-orange-pressed: "#5C1F08"
  brand-wash: "#FFF4EA"
  ink: "#242424"
  ink-secondary: "#424242"
  ink-muted: "#616161"
  surface: "#FFFFFF"
  surface-subtle: "#F5F5F5"
  stroke: "#D1D1D1"
  stroke-soft: "#E0E0E0"
  focus-ring: "#000000"
  danger: "#BC2F32"
typography:
  display:
    fontFamily: "'Segoe UI', 'Segoe UI Web (West European)', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', sans-serif"
    fontSize: "40px"
    fontWeight: 600
    lineHeight: "52px"
  headline:
    fontFamily: "'Segoe UI', 'Segoe UI Web (West European)', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', sans-serif"
    fontSize: "28px"
    fontWeight: 600
    lineHeight: "36px"
  title:
    fontFamily: "'Segoe UI', 'Segoe UI Web (West European)', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', sans-serif"
    fontSize: "20px"
    fontWeight: 600
    lineHeight: "28px"
  body:
    fontFamily: "'Segoe UI', 'Segoe UI Web (West European)', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "20px"
  label:
    fontFamily: "'Segoe UI', 'Segoe UI Web (West European)', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', sans-serif"
    fontSize: "12px"
    fontWeight: 600
    lineHeight: "16px"
rounded:
  sm: "4px"
  md: "6px"
  lg: "8px"
  full: "9999px"
spacing:
  xxs: "2px"
  xs: "4px"
  s: "8px"
  m: "12px"
  l: "16px"
  xl: "20px"
  xxl: "24px"
  xxxl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.brand-orange}"
    textColor: "{colors.surface}"
    rounded: "{rounded.sm}"
    padding: "5px 12px"
    height: "32px"
  button-primary-hover:
    backgroundColor: "{colors.brand-orange-deep}"
  button-primary-active:
    backgroundColor: "{colors.brand-orange-pressed}"
  button-subtle:
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    padding: "5px 12px"
    height: "32px"
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.xl}"
  data-surface:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
  empty-state:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink-muted}"
    rounded: "{rounded.lg}"
    padding: "32px 16px"
  icon-medallion:
    backgroundColor: "{colors.brand-wash}"
    textColor: "{colors.brand-orange}"
    rounded: "{rounded.full}"
    size: "56px"
  pill:
    backgroundColor: "{colors.brand-wash}"
    textColor: "{colors.brand-orange-deep}"
    typography: "{typography.label}"
    rounded: "{rounded.full}"
    padding: "4px 12px"
---

# Design System: ESIBuy

## Overview

**Creative North Star: "El Mercado Práctico"**

ESIBuy se mira en los grandes marketplaces, con Amazon como referencia de patrones: el buscador manda, los listados son densos y fáciles de recorrer, y la información va por delante de la decoración. Esa referencia es de comportamiento, no de marca: no se copian su logotipo, sus colores ni su cabecera. El sistema visual propio es Fluent UI v9 tal cual, sobrio y nativo, con un único acento naranja que marca la acción principal y la identidad.

La interfaz es mayoritariamente blanca. Un velo naranja muy pálido entra desde arriba en el fondo de las páginas y se disuelve en blanco hacia la mitad, lo único atmosférico del sistema. Todo lo demás es estructura: tarjetas y tablas blancas con borde fino y sombra ligera sobre ese fondo, títulos en Segoe UI seminegrita, texto secundario en gris medio.

Los componentes se usan como Fluent los entrega. La personalidad está en los detalles: el "Buy" naranja del logotipo, los medallones circulares con icono naranja, y alguna microinteracción contenida en los puntos de navegación del panel de administración.

**Key Characteristics:**
- Fluent UI v9 nativo y sobrio; sin librerías de componentes paralelas.
- Un solo acento: naranja, reservado para la acción principal, el logotipo y los iconos destacados.
- Superficies blancas con sombra ligera estructural sobre un fondo con velo naranja.
- Listados de trabajo (tablas, filtros, paginación) densos y escaneables, al estilo de un marketplace.
- Movimiento corto (150–200 ms) y siempre con alternativa para movimiento reducido.

## Colors

Una paleta neutra de Fluent con un único acento naranja cálido, generado como rampa de marca en `frontend/src/theme/theme.js`.

### Primary
- **Naranja Mercado** (brand-orange): botón principal, logotipo ("Buy"), iconos de medallón, valores destacados y bordes de tarjeta en hover. Es el token `colorBrandBackground` / `colorBrandForeground1`.
- **Naranja Tostado** (brand-orange-deep): hover del botón principal y texto de las píldoras. Es el único naranja con contraste suficiente para texto pequeño sobre blanco.
- **Naranja Brasa** (brand-orange-pressed): estado pulsado del botón principal.
- **Velo Naranja** (brand-wash): fondo de medallones, píldoras, hover de filas de tabla y arranque del degradado de página.

### Neutral
- **Tinta** (ink): texto principal y títulos.
- **Tinta Secundaria** (ink-secondary): párrafos de apoyo largos, como la descripción de la home.
- **Tinta Apagada** (ink-muted): subtítulos de página, descripciones de tarjeta, resúmenes de paginación, pie.
- **Superficie** (surface): tarjetas, tablas, estados vacíos y final del degradado de página.
- **Superficie Suave** (surface-subtle): fondos secundarios de Fluent.
- **Trazo** (stroke): borde discontinuo de los estados vacíos.
- **Trazo Suave** (stroke-soft): borde de tarjetas, tablas y separador del pie.
- **Rojo Aviso** (danger): avisos puntuales en datos, como el stock agotado.

### Named Rules
**La Regla de la Voz Única.** El naranja es el único color de acento. Los colores de estado (éxito, aviso, error) llegan solo a través de los componentes de Fluent (`Badge`, `MessageBar`), nunca como colores nuevos inventados.

**La Regla del Naranja Legible.** El Naranja Mercado sobre blanco solo da un contraste de 3,6:1: vale para texto grande (logotipo, cifras de 24 px en negrita) e iconos, no para texto pequeño. El texto naranja pequeño usa Naranja Tostado.

## Typography

**Display Font:** Segoe UI (con -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue y sans-serif)
**Body Font:** la misma familia

**Character:** una sola familia de sistema, la de Fluent: neutral, legible y familiar, con jerarquía solo por tamaño y peso.

### Hierarchy
- **Display** (600, 40 px, 52 px; 32 px / 40 px en móvil): solo el titular de la home.
- **Headline** (600, 28 px, 36 px): título de cada página de trabajo (`Title1`), p. ej. "Panel de control" o "Mis productos".
- **Title** (600, 20 px, 28 px): títulos de estado vacío y de tarjeta (`Subtitle1`, `Text size={500}`). El título de sección de la home usa `Title2` (24 px / 32 px).
- **Body** (400, 14 px, 20 px): todo el contenido, celdas de tabla y campos. Los párrafos largos se limitan a 60ch.
- **Label** (600, 12 px, 16 px): píldoras y etiquetas pequeñas.

### Named Rules
**La Regla de los Componentes Tipográficos.** Los títulos se escriben con los componentes de Fluent (`Display`, `Title1`, `Title2`, `Subtitle1`, `Text`) y sus tokens, nunca con tamaños en píxeles sueltos.

## Layout

- **Contenedores centrados:** la cabecera y la home ocupan hasta 1200 px; las páginas de trabajo (panel de administración, listados), hasta 1000 px. Margen lateral de 16 px.
- **Páginas de trabajo:** columna flex con 20 px entre bloques. El orden es encabezado (título, subtítulo y acción principal a la derecha, que baja debajo en pantallas estrechas), luego filtros, luego tabla o estado vacío, y al final la paginación.
- **Filtros:** fila flexible que se envuelve. El buscador ocupa más espacio (`flex-grow: 2`, base 240 px) que los desplegables (`flex-grow: 1`, base 200 px).
- **Rejillas de tarjetas:** `auto-fit` con mínimo de 280 px en el panel y de 240 px en los pasos de la home, para que el reparto en columnas sea automático.
- **Home:** una columna en móvil y dos columnas (1,15fr / 0,85fr) a partir de 900 px, único punto de ruptura explícito.
- **Tablas:** dentro de un contenedor con desplazamiento horizontal, para que la página nunca desborde en móvil.

### Named Rules
**La Regla del Listado Primero.** En las páginas de trabajo, el listado es el protagonista: sin cabeceras decorativas ni tarjetas resumen antes de la tabla, salvo que la historia de usuario lo pida.

## Elevation & Depth

La sombra es estructural y ligera: separa las superficies blancas del fondo con velo naranja, no decora.

### Shadow Vocabulary
- **Reposo** (`0 0 2px rgba(0,0,0,0.12), 0 2px 4px rgba(0,0,0,0.14)`, `shadow4`): tablas y tarjetas de navegación en reposo.
- **Elevación al pasar el ratón** (`0 0 2px rgba(0,0,0,0.12), 0 8px 16px rgba(0,0,0,0.14)`, `shadow16`): tarjeta del panel en hover, junto a una subida de 4 px.
- **Panel destacado** (`0 0 8px rgba(0,0,0,0.12), 0 14px 28px rgba(0,0,0,0.14)`, `shadow28`): la tarjeta de login y el panel de características de la home; nunca en listados.

### Named Rules
**La Regla de la Sombra Justa.** Cada superficie lleva `shadow4` y borde suave. `shadow28` se reserva a un único panel protagonista por pantalla.

## Shapes

Esquinas suaves y consistentes: 8 px (`borderRadiusXLarge`) en todas las superficies contenedoras (tarjetas, tablas, estados vacíos, login) y 4 px en botones y campos, como trae Fluent. Los círculos completos se reservan a medallones de icono, números de paso y píldoras. Los bordes son de 1 px: sólidos y suaves en superficies, discontinuos en los estados vacíos para que se lean como "aquí todavía no hay nada".

## Components

### Buttons
- **Shape:** esquinas de 4 px, altura de 32 px (40 px en tamaño `large` para las llamadas a la acción de la home).
- **Primary:** Naranja Mercado con texto blanco; una sola acción principal por zona (p. ej. "Añadir producto", "Registrarse").
- **Hover / Focus:** pasa a Naranja Tostado y a Naranja Brasa al pulsar; el foco es el anillo de Fluent.
- **Subtle:** sin fondo, para acciones de navegación (cabecera, paginación). Los iconos de `@fluentui/react-icons` en 20 px van delante, o detrás cuando indican avance ("Siguiente", "Crear mi cuenta").

### Chips (Badges y píldoras)
- **Badge de Fluent:** estados de datos en tablas (`appearance="tint"`, `success` para disponible y `subtle` para no disponible) y categorías (`outline`, `brand`).
- **Píldora de marca:** Velo Naranja con texto Naranja Tostado e icono pequeño; solo como sello introductorio en la home.

### Cards / Containers
- **Corner Style:** 8 px.
- **Background:** Superficie blanca.
- **Shadow Strategy:** `shadow4` en reposo (ver Elevation & Depth).
- **Border:** 1 px Trazo Suave.
- **Internal Padding:** 20 px en las tarjetas de navegación y 24 px en el login.

### Inputs / Fields
- **Style:** `Field` de Fluent con etiqueta visible encima, más `SearchBox` o `Select`. Los desplegables de filtro empiezan con una opción "Todas/Todos" de valor vacío.
- **Focus:** la línea inferior de marca de Fluent.
- **Error / Disabled:** los mensajes de validación de `Field`; los errores de carga de página van en `MessageBar intent="error"` con una acción "Reintentar".

### Navigation
- **Cabecera:** logotipo textual "ESI**Buy**" (20 px en negrita, con "Buy" en naranja) a la izquierda y acciones de cuenta a la derecha (botón `subtle` más uno `primary`). Sin menú oscuro ni barra de categorías por ahora.
- **Paginación:** resumen "Mostrando X–Y de N" en Tinta Apagada a la izquierda; "Anterior / Página n de m / Siguiente" a la derecha. Se oculta cuando solo hay una página.

### Tabla de datos (componente distintivo)
`Table` de Fluent dentro de una superficie blanca con borde suave, esquinas de 8 px, `shadow4` y desplazamiento horizontal. La primera columna (el nombre) va en seminegrita con un ancho mínimo de 180 px. Las filas se tiñen de Velo Naranja al pasar el ratón (150 ms). Mientras se refresca, la tabla baja al 55 % de opacidad con `aria-busy`, sin vaciarse.

### Estado vacío (componente distintivo)
Superficie blanca con borde discontinuo, medallón circular de 72 px con el icono en naranja, título `Subtitle1`, descripción en Tinta Apagada (máximo 420 px) y, opcionalmente, una acción. Distingue entre "todavía no hay nada" (acción para crear) y "ningún resultado con estos filtros" (acción "Limpiar filtros").

### Tarjeta de sección del panel (componente distintivo)
Botón a ancho completo con forma de tarjeta: medallón de 56 px, título y descripción, y una flecha a la derecha. En hover sube 4 px, pasa a `shadow16` y el borde se vuelve naranja; el medallón se rellena de naranja y crece al 108 %, y la flecha avanza 6 px. Sin animación con movimiento reducido.

## Do's and Don'ts

### Do:
- **Do** construir con componentes y tokens de Fluent UI v9 (`makeStyles` + `tokens`) y reutilizar `EmptyState`, `Pagination` y `LinkButton` antes de crear otros.
- **Do** seguir los patrones de marketplace de la referencia: buscador visible y principal en los listados, filtros a la vista, tablas densas y paginación con resumen de resultados.
- **Do** envolver toda tabla en el contenedor con desplazamiento horizontal, borde suave, esquinas de 8 px y `shadow4`.
- **Do** dar a cada animación una duración de `durationFast` o `durationNormal` con `curveEasyEase` y su alternativa `prefers-reduced-motion`.
- **Do** usar Naranja Tostado (brand-orange-deep) para cualquier texto naranja de menos de 18 px.

### Don't:
- **Don't** copiar la marca de Amazon (logotipo, cabecera azul marino, amarillo-naranja, iconografía): es una referencia de patrones, no de identidad.
- **Don't** introducir otra librería de componentes ni colores de acento nuevos fuera de la rampa de marca y los colores de estado de Fluent.
- **Don't** poner texto blanco pequeño sobre Naranja Mercado fuera de los botones de Fluent, ni texto pequeño en Naranja Mercado sobre blanco.
- **Don't** usar `shadow28` en listados o tablas; es solo para el panel protagonista.
- **Don't** escribir tamaños de letra, radios o espaciados en píxeles sueltos cuando existe un token de Fluent equivalente.
