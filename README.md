# CalculoCivil

**CalculoCivil** es una aplicación de cálculo estructural para Android, basada en el potente motor de simulación **OpenSees** utilizando la interfaz de desarrollo nativo de Android (**NDK**) con integración **Java y C++ (JNI)**.

Este repositorio contiene la estructura completa del proyecto de Android Studio integrada con el código de OpenSees.

---

## 🛠️ Especificaciones Técnicas y Versiones

El proyecto está configurado con las siguientes versiones de Android y herramientas de compilación:

*   **SDK de Compilación (Compile SDK):** API 37 (Android 15+)
*   **SDK Objetivo (Target SDK):** API 37
*   **SDK Mínimo (Min SDK):** API 23 (Android 6.0 Marshmallow+)
*   **Versión de NDK:** `30.0.14904198`
*   **Versión de CMake:** `4.1.2`
*   **Versión de Gradle:** `9.5.1` (con Android Gradle Plugin 9.2.1)
*   **Nombre de Paquete (Package Name):** `com.diamon.calculo`
*   **Nombre de la Librería Nativa:** `libcalculocivil.so` (Cargada como `System.loadLibrary("calculocivil")`)

---

## 📂 Estructura Principal del Proyecto

*   **[app/](file:///home/danielpdiamon/CalculoCivil/app)**: Código fuente de la aplicación Android.
    *   **[app/src/main/java/](file:///home/danielpdiamon/CalculoCivil/app/src/main/java)**: Código fuente Java (`MainActivity.java`).
    *   **[app/src/main/cpp/](file:///home/danielpdiamon/CalculoCivil/app/src/main/cpp)**: Código nativo C++ (`native-lib.cpp`) y configuración de CMake (`CMakeLists.txt`).
    *   **[app/src/main/assets/](file:///home/danielpdiamon/CalculoCivil/app/src/main/assets)**: Directorio de assets del proyecto. Contiene una estructura interna simulada en la carpeta `data/` con librerías dinámicas precompiladas (como HDF5 y OpenBLAS) y scripts del lenguaje de scripts TCL (`tcl8.6`).
*   **[OpenSees/](file:///home/danielpdiamon/CalculoCivil/OpenSees)**: El repositorio y código fuente del motor OpenSees listo para ser integrado en la compilación nativa C++.
*   **[fake_root/](file:///home/danielpdiamon/CalculoCivil/fake_root)**: Directorio original simulado de almacenamiento interno que contiene las librerías nativas y archivos de configuración del motor de cálculo.

---

## 🚀 Configuración y Construcción

Para configurar el entorno de compilación y generar la aplicación, sigue estos pasos:

### 1. Configurar el SDK de Android
Ejecuta el script proporcionado en la raíz para descargar automáticamente las herramientas necesarias (SDK, NDK, CMake) en el directorio `/tmp/android-sdk` y generar tu archivo `local.properties`:
```bash
chmod +x setup-sdk.sh
./setup-sdk.sh
```

### 2. Compilar la Aplicación (APK)
Usa Gradle Wrapper para compilar el proyecto:
```bash
./gradlew assembleDebug
```
El APK se generará en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📚 Documentación Adicional
Para más detalles sobre el desarrollo y la configuración, consulta los siguientes archivos:
- **[guia_desarrollo_calculocivil.md](file:///home/danielpdiamon/CalculoCivil/guia_desarrollo_calculocivil.md)**: Guía detallada de compilación y comandos útiles para el desarrollador.
- **[resumen_proyecto_calculocivil.md](file:///home/danielpdiamon/CalculoCivil/resumen_proyecto_calculocivil.md)**: Ficha técnica y estructura completa del proyecto Android C++.
- **[guia_uso_sdk.md](file:///home/danielpdiamon/CalculoCivil/guia_uso_sdk.md)**: Comandos para gestionar componentes del SDK y dispositivos con ADB.
