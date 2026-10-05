<div align="center">

```
██╗      ██████╗  ██████╗ ██████╗      ██████╗  ██████╗ ██╗     ███████╗██╗  ██╗ ██████╗
██║     ██╔═══██╗██╔═══██╗██╔══██╗     ██╔══██╗██╔═══██╗██║     ██╔════╝╚██╗██╔╝ ██╔══██╗
██║     ██║   ██║██║   ██║██████╔╝     ██████╔╝██║   ██║██║     █████╗   ╚███╔╝  ██████╔╝
██║     ██║   ██║██║   ██║██╔══██╗     ██╔══██╗██║   ██║██║     ██╔══╝   ██╔██╗  ██╔══██╗
███████╗╚██████╔╝╚██████╔╝██║  ██║     ██████╔╝╚██████╔╝███████╗███████╗██╔╝ ██╗ ██████╔╝
╚══════╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═╝     ╚═════╝  ╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝
```

# 🔥 FireFlow

### *Gestión de Grupos de Presión contra Incendios*

![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)
![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white&style=for-the-badge)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.03-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-350%20%E2%9C%94-00E676?style=for-the-badge)
![Modules](https://img.shields.io/badge/Modules-18-FF5252?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-FFC107?style=for-the-badge)

```
> INSERT_SCREENSHOT_OR_GIF_HERE
```

<br/>

*App Android profesional para la gestión integral de grupos de presión contra incendios.*
*Arquitectura limpia, sincronización offline-first y gráficas de rendimiento en tiempo real.*

</div>

---

## 👾 Acerca del Proyecto

```
╔══════════════════════════════════════════════════════════════╗
║  FireFlow resuelve un problema real en el sector de         ║
║  protección contra incendios: el control y seguimiento      ║
║  de grupos de presión (bombas de incendio) en instalaciones ║
║  industriales y comerciales.                               ║
║                                                            ║
║  Permite a técnicos y mantenedores:                        ║
║  • Registrar clientes, instalaciones y revisiones          ║
║  • Medir y visualizar curvas de presión vs. caudal         ║
║  • Generar informes PDF y exportar datos CSV               ║
║  • Trabajar 100% offline con sincronización al servidor    ║
║  • Documentar con fotos y checklists de mantenimiento      ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 🎬 Demo

> **Coloca tus GIFs en `docs/gifs/` y descomenta/reemplaza los bloques siguientes.**

<div align="center">

| | |
|:---:|:---:|
| 🔐 **Login seguro** | 📋 **Gestión de clientes** |
| ![Login](docs/gifs/login.gif) | ![Clientes](docs/gifs/clientes.gif) |
| *Lockout, cambio de contraseña, seed* | *Pull-to-refresh, búsqueda, estados* |
| | |
| 📈 **Curvas de presión** | 🧮 **Convertidor de unidades** |
| ![Curvas](docs/gifs/curvas.gif) | ![Convertidor](docs/gifs/convertidor.gif) |
| *Gráfico Vico, chips de motor, tabla* | *Conversión en vivo bidireccional* |
| | |
| 📄 **Informes y exportación** | 🌙 **Tema oscuro** |
| ![Acciones](docs/gifs/acciones.gif) | ![Dark](docs/gifs/dark-theme.gif) |
| *PDF, CSV, cámara, share* | *Tono completo adaptativo* |

</div>

---

## ⚡ Características

```
🕹️  MÓDULOS FUNCIONALES
─────────────────────────────────────────────────────────────
 🔐  Login seguro con lockout anti-brute-force
 🔑  Cambio de contraseña forzado en primer acceso
 👥  CRUD completo de clientes con geolocalización
 🔧  Gestión de grupos de presión y motores
 📅  Revisiones con checklists y estados de color
 📈  Gráficas de curvas presión/caudal (Vico 2.1)
 📄  Generación de informes PDF
 📊  Exportación de datos CSV
 📷  Captura y galería de fotos con CameraX
 🧮  Convertidor de unidades (caudal y presión)
 🔍  Búsqueda global en historial
 🔄  Sincronización offline-first con servidor Ktor
 🌙  Tema oscuro / claro adaptativo
 ♿  Accesibilidad y content descriptions
─────────────────────────────────────────────────────────────
```

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION                        │
│  ┌──────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │  Views   │  │  ViewModels  │  │  Navigation     │   │
│  │ Compose  │←→│    MVVM      │←→│  20 rutas       │   │
│  └──────────┘  └──────┬───────┘  └─────────────────┘   │
│                       │  inyección Hilt                 │
├───────────────────────┼────────────────────────────────┤
│                    DOMAIN (pure Kotlin)                │
│  ┌──────────┐  ┌──────┴───────┐  ┌─────────────────┐   │
│  │ Entities │←→│ Repositories │←→│    Use Cases     │   │
│  │  Models  │  │  Interfaces  │  │   Business       │   │
│  └──────────┘  └──────────────┘  └─────────────────┘   │
├────────────────────────────────────────────────────────┤
│                       DATA                             │
│  ┌──────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │  Room +  │  │   DataStore  │  │  Sync / Ktor    │   │
│  │ SQLCipher│  │  Prefs/Encr. │  │  Client API     │   │
│  └──────────┘  └──────────────┘  └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │   KTOR SERVER      │
                    │  PostgreSQL + JWT  │
                    │  Argon2id + CORS   │
                    └───────────────────┘
```

| Capa | Responsabilidad | Módulos |
|:-----|:----------------|:--------|
| **Presentation** | UI, navegación, estados | `app`, `common`, `feature_*` (11) |
| **Domain** | Entidades, casos de uso, contratos | `domain` |
| **Data** | Repositorios, sync, fuentes de datos | `data`, `database`, `security` |
| **Core** | Config, utils, constants | `core` |
| **Server** | API REST, auth, persistencia | `server/` (proyecto Gradle separado) |

---

## 🛠️ Tech Stack

```
╔══════════════════════════════════════════════════════════╗
║  UI                                                      ║
║  ├─ Jetpack Compose (BOM 2025.03.00)                     ║
║  ├─ Material3 1.3.1                                      ║
║  ├─ Vico 2.1.0 (gráficas cartesianas)                   ║
║  ├─ Coil 2.7.0 (carga de imágenes)                      ║
║  └─ CameraX 1.4.1 (captura de fotos)                    ║
║                                                          ║
║  DATA                                                    ║
║  ├─ Room 2.6.1 + SQLCipher 4.6.1 (DB cifrada)          ║
║  ├─ DataStore (preferencias)                            ║
║  ├─ EncryptedSharedPreferences (config servidor)       ║
║  └─ WorkManager 2.10.0 (sync en segundo plano)         ║
║                                                          ║
║  DI + ASYNC                                              ║
║  ├─ Hilt 2.53.1                                          ║
║  └─ Kotlin Coroutines + Flow                             ║
║                                                          ║
║  SERVER                                                  ║
║  ├─ Ktor 3.0.3 (client + server)                        ║
║  ├─ PostgreSQL (H2 para tests)                          ║
║  ├─ JWT (HS256) + Argon2id                               ║
║  └─ Flyway (migraciones)                                ║
║                                                          ║
║  TESTING                                                 ║
║  ├─ JUnit4 + Turbine (Flow testing)                     ║
║  ├─ MockK (mocking)                                     ║
║  ├─ Room in-memory + Robolectric                        ║
║  └─ Ktor Test Host (server integration)                 ║
║                                                          ║
║  BUILD                                                   ║
║  ├─ Kotlin 2.1.0 · AGP 8.13.2                           ║
║  ├─ minSdk 26 · targetSdk 35                            ║
║  └─ ProGuard/R8 optimizado                              ║
╚══════════════════════════════════════════════════════════╝
```

---

## 📊 Calidad

```
  ██████╗  █████╗ ████████╗ ██████╗██╗  ██╗
  ██╔══██╗██╔══██╗╚══██╔══╝██╔════╝██║  ██║
  ██████╔╝███████║   ██║   ██║     ███████║
  ██╔══██╗██╔══██║   ██║   ██║     ██╔══██║
  ██████╔╝██║  ██║   ██║   ╚██████╗██║  ██║
  ╚═════╝ ╚═╝  ╚═╝   ╚═╝    ╚═════╝╚═╝  ╚═╝
─────────────────────────────────────────────
  MÉTRICAS DEL PROYECTO
─────────────────────────────────────────────
  📦  18 módulos Gradle (Android)
  🖥️  1 servidor Ktor (proyecto separado)
  📝  217 archivos Kotlin
  📏  ~13.300 líneas de código fuente
  ✅  350 tests unitarios + instrumentados
     ├─  259 Android (unit + instrumented)
     └─   91 Server (Ktor + repositories)
  🔀  20 rutas de navegación
  🛡️  OWASP Top 10 checklist completo
─────────────────────────────────────────────
```

---

## 🚀 Setup

```bash
# 1. Clonar el repositorio
git clone git@github.com:DeepZixSoul/fireflow.git
cd fireflow

# 2. Añadir passphrase de la DB cifrada en local.properties
echo 'DB_PASSPHRASE=TuClaveSeguraAqui!' >> local.properties

# 3. Build del APK debug
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew assembleDebug

# 4. Ejecutar tests Android
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test

# 5. Ejecutar tests del servidor
cd server && ./gradlew test

# 6. Instalar en dispositivo/emulador
adb install app/build/outputs/apk/debug/app-debug.apk
```

> ⚠️ **Requisitos**: JDK 21, Android SDK 35, `local.properties` con `DB_PASSPHRASE`.

---

## 🔒 Seguridad

```
🛡️  MEDIDAS IMPLEMENTADAS
──────────────────────────────────────────────
 🔑  Argon2id (tCost=3, mCost=64MB) — servidor
 🔑  BCrypt (cost=10) — seed Android
 🔐  SQLCipher — base de datos cifrada en disco
 🔐  EncryptedSharedPreferences — config servidor
 🎫  JWT HS256 — expiración 15 min
 📦  BuildConfig.DB_PASSPHRASE — secrets fuera del código
 ⛔  Nada de secrets en .gitignore (local.properties, .env)
 🚫  .gitignore evita: build/, .kotlin/, APKs, SECURITY_AUDIT
 🌐  CORS restringido + rate limiting en auth
 📝  Logging sin PII ni passwords
──────────────────────────────────────────────
```

---

## 📁 Estructura

```
FireFlow/
├── app/                    # Módulo principal, navegación, DI
├── core/                   # Config, utils, constants
├── common/                 # Componentes UI compartidos, tema, tema
├── domain/                 # Entidades, repositorios (interfaces), casos de uso
├── data/                   # Implementación repos, sync, fuentes de datos
├── database/               # Room + SQLCipher, DAOs, migraciones
├── security/               # Hashing, sesiones, root detection
├── feature_login/          # Login + cambio contraseña
├── feature_clientes/       # CRUD clientes
├── feature_grupos/         # Grupos de presión + motores + curvas
├── feature_revisiones/     # Revisiones + checklists
├── feature_curvas/         # Gráficas de rendimiento
├── feature_informes/       # Generación PDF
├── feature_exportaciones/  # Exportación CSV
├── feature_fotografias/    # Cámara + galería
├── feature_historial/      # Búsqueda global
├── feature_conversiones/   # Convertidor unidades
├── feature_configuracion/  # Ajustes + sync
├── server/                 # Ktor + PostgreSQL (proyecto Gradle separado)
└── docs/gifs/              # GIFs para este README
```

---

## 📝 Licencia

Distribuido bajo la licencia **MIT** — ver [LICENSE](LICENSE) para más detalles.

---

<div align="center">

```
╔═══════════════════════════════════════════════╗
║   Hecho con 🔥 y Kotlin para la seguridad      ║
║   contra incendios.                            ║
║                                               ║
║   👾 Retro. ⚡ Rápido. 🛡️ Seguro.             ║
╚═══════════════════════════════════════════════╝
```

[![GitHub](https://img.shields.io/badge/GitHub-DeepZixSoul%2Ffireflow-181717?logo=github&style=for-the-badge)](https://github.com/DeepZixSoul/fireflow)

</div>
