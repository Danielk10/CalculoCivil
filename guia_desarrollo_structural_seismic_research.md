# Guía de Desarrollo para Structural and Seismic Research (Android Native C++)

Esta guía explica los pasos para compilar, instalar y desarrollar en el proyecto **Structural and Seismic Research** desde la línea de comandos en Linux.

## 1. Configuración Inicial del Entorno

Antes de compilar el proyecto, es necesario descargar e instalar el SDK y NDK de Android. Puedes hacerlo automáticamente ejecutando el script proporcionado:

```bash
chmod +x setup-sdk.sh
./setup-sdk.sh
```

Este script:
- Descarga las herramientas del SDK en `/tmp/android-sdk`.
- Instala la API 37 de Android, CMake 4.1.2 y el NDK.
- Genera el archivo [local.properties](file:///home/danielpdiamon/CalculoCivil/local.properties) apuntando a la ruta del SDK.

## 2. Compilación del Proyecto con Gradle

Una vez configurado el SDK, puedes compilar el proyecto usando el Gradle Wrapper:

*   **Compilar la aplicación en modo Debug (Generar APK):**
    ```bash
    ./gradlew assembleDebug
    ```
    El APK generado se guardará en:
    `app/build/outputs/apk/debug/app-debug.apk`

*   **Limpiar los archivos de compilación anteriores:**
    ```bash
    ./gradlew clean
    ```

*   **Compilar el código nativo C++ únicamente:**
    Para verificar que no hay errores de compilación en el código JNI/C++ sin empaquetar toda la app:
    ```bash
    ./gradlew :app:externalNativeBuildDebug
    ```

## 3. Instalación y Ejecución en Dispositivo / Emulador

Para instalar y depurar la aplicación en un dispositivo conectado a través de USB OTG o un emulador:

*   **Instalar el APK directamente:**
    ```bash
    ./gradlew installDebug
    ```

*   **Ver los logs en tiempo real (Logcat):**
    ```bash
    adb logcat -s "CalculoCivil"
    ```

*   **Verificar la salida de JNI:**
    Filtra los logs para ver mensajes de JNI o de la app:
    ```bash
    adb logcat *:S MainActivity:D
    ```

## 4. Estructura de Código Nativo

El código C++ nativo se encuentra en:
- **[CMakeLists.txt](file:///home/danielpdiamon/CalculoCivil/app/src/main/cpp/CMakeLists.txt)**: Configura la compilación de la librería dinámica `libcalculocivil.so`.
- **[native-lib.cpp](file:///home/danielpdiamon/CalculoCivil/app/src/main/cpp/native-lib.cpp)**: Contiene la implementación de la función nativa llamada desde Java (`stringFromJNI`).

Para añadir lógica de cálculo estructurado de **OpenSees**:
1. Añade los archivos fuente de OpenSees en el `CMakeLists.txt` de la carpeta [app/src/main/cpp](file:///home/danielpdiamon/CalculoCivil/app/src/main/cpp).
2. Agrega las rutas de inclusión para poder usar el código de OpenSees en el backend nativo.

---
*Guía del Desarrollador para Structural and Seismic Research*
