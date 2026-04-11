# Habitus — Habit Tracker

**Habitus** es una aplicación móvil nativa para Android que permite a los usuarios construir y mantener hábitos personales mediante seguimiento diario, rachas de consistencia y estadísticas de progreso.

Habitus esta desarrollada como proyecto integrador de la asignatura **Desarrollo de Aplicaciones Móviles**, desarrollado por estudiantes de **Ingeniería de Software**.

---

## Equipo de desarrollo

| Integrante | Rol |
|---|---|
| Edgar Torres | Backend |
| Arleth Caballero | Frontend |
| Elias Ochoa | Frontend |
| Roberto Perez | Backend |

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| IDE | Android Studio (Ladybug) |
| UI | XML Views + ViewBinding |
| Arquitectura | MVVM con capa de Repositorios |
| Base de datos | Firebase Firestore |
| Autenticación | Firebase Authentication |
| Tareas en segundo plano | WorkManager |
| Control de versiones | Git / GitHub |
| Tipografía | Playfair Display + DM Sans (Google Fonts) |

---

## Requisitos para ejecutar

- Android Studio Ladybug o superior
- JDK 21
- Android SDK API 24 o superior (minSdk 24)
- Archivo `google-services.json` en `/app/` (solicitar al equipo)
- Conexión a internet activa para Firebase

---

## Instalación

1. Clona el repositorio:
```bash
   git clone https://github.com/[usuario]/HabitTrackerApp.git
```
2. Abre el proyecto en Android Studio.
3. Coloca el archivo `google-services.json` dentro de la carpeta `/app/`.
4. Ejecuta *Sync Project with Gradle Files*.
5. Corre la app en un emulador o dispositivo físico con API 24+.

---

## Estructura del Proyecto
El código se organiza siguiendo las mejores prácticas de desarrollo en Android:
* `ui/`: Interfaces de usuario (Activities, Fragments, Adapters).
* `data/`: Modelos de datos, entidades de base de datos y repositorios.
* `logic/`: Casos de uso y lógica de procesamiento.
* `utils/`: Clases de utilidad y herramientas auxiliares.
* `worker/`: Tareas en segundo plano utilizando WorkManager.

---

## Funcionalidades implementadas

### Autenticación
- Registro con nombre, correo y contraseña
- Inicio de sesión con validación de formato
- Recuperación de contraseña por correo
- Cierre de sesión
- Redirección automática si hay sesión activa

### Onboarding
- Flujo de 3 páginas con ViewPager2 y dots indicadores
- Se muestra solo la primera vez; persiste el estado con SharedPreferences

### Hábitos
- Crear hábito con nombre, frecuencia (diaria o días específicos) y categoría
- Editar hábito existente con prellenado del formulario
- Eliminar hábito con gesto swipe (limpia también la subcolección de completaciones)
- Marcar como completado con guard anti-doble-completación por día
- Racha automática: se resetea a 0 si el usuario falta a un día programado
- Porcentaje de cumplimiento calculado sobre los últimos 30 días programados
- Historial visual de los últimos 7 días con estado real desde Firestore

### Categorías
- 5 categorías por defecto creadas automáticamente al registrarse
- Crear categorías personalizadas con nombre y selector de color
- Visualización separada (por defecto / personalizadas) con conteo de hábitos

### Perfil y estadísticas
- Nombre, correo y avatar con inicial del usuario
- Hábitos activos, racha máxima y total histórico de completaciones
- Navegación a Notificaciones, Apariencia, Privacidad y Acerca de

### Notificaciones
- Recordatorio diario programado con WorkManager
- Selector de hora: mañana (8:00), tarde (14:00), noche (20:00)
- Solicitud de permiso en Android 13+
- El recordatorio se cancela o reprograma según el estado del toggle

### Apariencia
- Tema claro, oscuro o seguir sistema
- Persiste entre sesiones con SharedPreferences
- Se aplica al inicio antes de inflar cualquier vista

---

## Estado del proyecto

| Área | Estado |
|---|---|
| Autenticación completa | ✔ |
| Onboarding | ✔ |
| CRUD de hábitos | ✔ |
| CRUD de categorías | ✔ |
| Historial real de 7 días | ✔ |
| Racha con reset automático | ✔ |
| Porcentaje de cumplimiento real (30 días) | ✔ |
| Estadísticas globales | ✔ |
| Notificaciones con WorkManager | ✔ |
| Modo oscuro/claro/sistema | ✔ |
| Firebase Security Rules | ✔ |
| Publicación en Play Store | ✘ Pendiente |

---

## En desarrollo...