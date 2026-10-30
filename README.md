# Mis Finanzas: control de ingresos y gastos

App Android para administrar ingresos y gastos en quetzales (Q), con control mes a mes,
alertas de gasto y un versículo bíblico cada vez que se registra un movimiento.

Proyecto del curso **Programación 2**, Universidad Mariano Gálvez de Guatemala.
Catedrático: Ing. Walter Cordova.

## Descargar

📦 [Descargar APK v1.0](https://github.com/RobertoLeiva15/ControlGastosApp/releases/tag/v1.0)

## Funciones

- **Login y registro** de usuarios, con contraseña cifrada (SHA-256)
- **Pantalla de bienvenida** animada
- **Inicio**: saldo del mes, ingresos, gastos y barra de porcentaje gastado
- **Alerta de colores**: verde, amarillo al 80 % de los ingresos y rojo si se gasta de más
- **Nuevo movimiento**: monto, categoría con íconos, descripción y fecha con calendario
- **Versículo** distinto al guardar cada movimiento (nunca repite el anterior)
- **Historial**: por mes, filtro por tipo, búsqueda, editar y eliminar movimientos
- **Resumen por categoría**: barras de colores, porcentajes y consejos
- **Meta de ahorro** mensual con barra de progreso
- **Modo claro y oscuro**
- **Persistencia** con Room (SQLite): los datos no se pierden al cerrar la app

## Arquitectura

La lógica de negocio está en una librería propia en Kotlin puro:
[control-gastos](https://github.com/RobertoLeiva15/control-gastos)

| Capa | Dónde | Qué hace |
|---|---|---|
| Pantallas | Esta app (Activities + View Binding) | Mostrar datos y recibir acciones |
| Servicio | Librería → `AdministradorFinanzas` | Validar, registrar y calcular totales |
| Datos | Interface `MovimientoRepository` | Contrato para guardar y leer |
| Persistencia | Esta app → `MovimientoRepositoryRoom` | Guarda en SQLite con Room |

Las pantallas solo conocen la interface del repositorio. Por eso se pudo cambiar
de una lista en memoria a Room sin modificar la lógica (inversión de dependencias).

## Tecnologías

Kotlin · Android (Views + View Binding) · Material Design 3 · Room 2.8.5 (SQLite) ·
KSP · Corrutinas · Maven · Gradle (Kotlin DSL) · minSdk 24

## Cómo ejecutarla

1. Clonar este repositorio y abrirlo en **Android Studio**.
2. Esperar a que Gradle sincronice.
3. Dar **Run ▶**.

La librería ya viene incluida como `.jar` en `app/libs`, así que no hace falta instalarla con Maven.

## Autor

**José Roberto Leiva Salazar**

**Carnet: 0905-25-11678**
