# Resumen del Proyecto CalculoCivil

Este documento resume la estructura y configuración actual del proyecto Android con soporte nativo C++ **CalculoCivil**.

## Ficha Técnica del Proyecto

| Parámetro | Valor |
| :--- | :--- |
| **Nombre del Proyecto** | CalculoCivil |
| **Package Name / Application ID** | `com.diamon.calculo` |
| **Librería Nativa** | `calculocivil` (compilada como `libcalculocivil.so`) |
| **Compile SDK** | 37 (Android 15+) |
| **Target SDK** | 37 |
| **Min SDK** | 23 (Android 6.0) |
| **C++ Standard** | CMake 4.1.2 |

## Componentes Principales del Proyecto

- **[app/src/main/java/com/diamon/calculo/MainActivity.java](file:///home/danielpdiamon/CalculoCivil/app/src/main/java/com/diamon/calculo/MainActivity.java)**: Actividad principal de la app que realiza la carga de la librería nativa y muestra texto generado desde C++.
- **[app/src/main/cpp/native-lib.cpp](file:///home/danielpdiamon/CalculoCivil/app/src/main/cpp/native-lib.cpp)**: Código fuente C++ que implementa el puente JNI.
- **[app/src/main/cpp/CMakeLists.txt](file:///home/danielpdiamon/CalculoCivil/app/src/main/cpp/CMakeLists.txt)**: Archivo de configuración de CMake que define las dependencias del compilador C/C++ y los enlaces de librerías.
- **[OpenSees/](file:///home/danielpdiamon/CalculoCivil/OpenSees)**: Repositorio del motor de simulación OpenSees, listo para ser integrado en la compilación nativa.

## Configuración y Construcción Rápida

- **Script de Instalación SDK**: [setup-sdk.sh](file:///home/danielpdiamon/CalculoCivil/setup-sdk.sh)
- **Guía de Uso del SDK**: [guia_uso_sdk.md](file:///home/danielpdiamon/CalculoCivil/guia_uso_sdk.md)
- **Guía del Proyecto**: [guia_desarrollo_calculocivil.md](file:///home/danielpdiamon/CalculoCivil/guia_desarrollo_calculocivil.md)

---
*Configurado y Adaptado para CalculoCivil*
