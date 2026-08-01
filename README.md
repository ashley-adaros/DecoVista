# DecoVista — Visualización y Planificación de Espacios 3D / AR

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2024.04.01-4285F4.svg?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![ARCore](https://img.shields.io/badge/ARCore-1.42.0-EA4335.svg?style=flat-square&logo=google)](https://developers.google.com/ar)
[![Room](https://img.shields.io/badge/Room_DB-2.6.1-3DDC84.svg?style=flat-square&logo=android)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_%2B_MVVM-blue.svg?style=flat-square)](#)

**DecoVista** es una aplicación móvil nativa para Android escrita en Kotlin que revoluciona el diseño de interiores y la planificación de espacios. Permite a los usuarios diseñar planos en un lienzo digital interactivo 2D y proyectar modelos de muebles tridimensionales en su entorno real a escala métrica exacta 1:1 mediante Realidad Aumentada (AR).

---

## 🚀 Características Principales

*   📐 **Plano Interactivo 2D**: Diseña y distribuye habitaciones en un Canvas interactivo y reactivo mapeado a escalas métricas de la vida real.
*   🕶️ **Proyección AR a Escala Real**: Utiliza ARCore y Filament (Sceneview) para visualizar muebles 3D (formato `.glb` / `.gltf`) en tu casa a escala estricta $1\text{m} = 1\text{ unidad virtual}$ bloqueando el escalado accidental.
*   ⚡ **Detección de Colisiones OBB (Oriented Bounding Box)**: Motor geométrico en tiempo real basado en el *Teorema del Eje Separador (SAT)* para predecir si dos muebles rotados colisionan o si sobresalen del espacio útil de la habitación.
*   📂 **Catálogo de Muebles Local**: Persistencia rápida y reactiva en base de datos local SQLite mediante Room, permitiendo el guardado de planos y diseños históricos con borrado relacional en cascada.

---

## 🛠️ Arquitectura y Buenas Prácticas

DecoVista está diseñada bajo los principios de **Clean Architecture** y **MVVM (Model-View-ViewModel)**. El proyecto está estructurado con una arquitectura modular híbrida orientada a optimizar la velocidad de compilación, el aislamiento de dependencias nativas complejas y la testabilidad unitaria:

```mermaid
graph TD
    subgraph app [":app"]
        app[Orquestador / Navigation / DI Entry]
    end

    subgraph features [":features (Funcionalidades)"]
        planner2d[planner2d : Plano interactivo]
        viewer3d[viewer3d : Visor AR 3D]
        catalog[catalog : Biblioteca de muebles]
    end

    subgraph core [":core (Módulos transversales)"]
        ar-engine[ar-engine : Abstracción ARCore/Filament]
        calculator[calculator : Lógica matemática pura Kotlin]
        database[database : SQLite Room DB]
        network[network : APIs REST]
        designsystem[designsystem : Componentes UI comunes]
    end

    app --> planner2d & viewer3d & catalog
    planner2d & viewer3d & catalog --> ar-engine & calculator & database & network & designsystem
```

### Principios de Ingeniería Clave:
1.  **Módulo Kotlin Puro (`:core:calculator`)**: Desacopla por completo todas las fórmulas de proyección geométrica y colisiones de los SDKs de Android (`Context`, `Canvas`, `View`). Esto permite realizar tests unitarios veloces (sin emuladores) en milisegundos.
2.  **Abstracción Gráfica (`:core:ar-engine`)**: Toda la complejidad de configurar los motores gráficos Filament / OpenGL y el ciclo de vida de la cámara con ARCore se encapsula, de forma que el módulo visual `:features:viewer3d` solo interactúa con representaciones declarativas y reactivas en Jetpack Compose.
3.  **Integridad Referencial Relacional**: El esquema local cuenta con restricciones estrictas de claves externas (`ForeignKey.CASCADE`) e índices optimizados en SQLite para asegurar la consistencia y el alto rendimiento en dispositivos de gama media.

---

## 📋 Requisitos del Sistema

*   **Android SDK**: API 26 (Android 8.0 Oreo) o superior (Requerido por ARCore).
*   **Hardware**: Dispositivo compatible con ARCore (Giroscopio, Acelerómetro y cámara con calibración AR).
*   **Herramientas**: Android Studio Jellyfish (o más nuevo) y JDK 17.

---

## 🛠️ Instalación y Configuración

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/DecoVista.git
   cd DecoVista
   ```

2. **Permisos y Configuración de ARCore:**
   Asegúrate de que la cámara está declarada en tu manifest local como requerida para filtrar dispositivos no compatibles en Google Play:
   ```xml
   <uses-feature android:name="android.hardware.camera.ar" android:required="true" />
   ```

3. **Compilación:**
   Sincroniza el proyecto con Gradle Files y ejecuta la aplicación:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Pruebas Unitarias
Para ejecutar las pruebas del motor de geometría y validar el algoritmo de colisión e indicadores de espacio:
```bash
./gradlew :core:calculator:testUnitTest
```

---

## 📄 Licencia
Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

## ✉️ Contacto
*   **Desarrollador**: Tu Nombre - [LinkedIn](https://linkedin.com/in/tu-perfil) - mail@tu-dominio.com
*   **Proyecto**: [https://github.com/tu-usuario/DecoVista](https://github.com/tu-usuario/DecoVista)
