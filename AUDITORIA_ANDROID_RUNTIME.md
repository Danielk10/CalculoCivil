# Auditoría Android Runtime (CalculoCivil)

Este documento explica cómo se configuró el proyecto para que los binarios nativos de OpenSees cumplan con las políticas de Google Play.

## 1. Problema Original
Los binarios de `OpenSees`, `tclsh`, `wish`, así como las librerías dinámicas (`libhdf5.so`, `libopenblas.so`, etc.) se encontraban dentro de la carpeta `assets` o como archivos que no eran extraídos correctamente de forma nativa por Android.
Google Play prohíbe la carga de código ejecutable o librerías `.so` no declaradas en la carpeta `lib/` del APK.

## 2. Solución Implementada
Siguiendo la arquitectura de los proyectos `C-PIC` y `Flash-EEPROM-Tool`:

1. **Movimiento a jniLibs**: Todos los archivos ejecutables y `.so` se han movido a `app/src/main/jniLibs/arm64-v8a/`.
2. **Renombrado**: Para que el empaquetador de Android los trate como librerías válidas y los extraiga automáticamente en el directorio `nativeLibraryDir` (protegido por el SO), todos los binarios fueron renombrados para cumplir el formato `lib[nombre].so`.
   - Ejemplo: `OpenSees` -> `libOpenSees.so`
   - Ejemplo: `libhdf5.so.1000` -> `libhdf5_so_1000.so` (los puntos extra se reemplazaron por guiones bajos para evitar problemas).

3. **Extracción y Symlinks (AssetHelper.java)**:
   - Los archivos restantes (archivos TCL, configuración, documentación) que no son ejecutables se mantuvieron en `assets` y se extraen a `files/usr/` al iniciar la app.
   - En la clase `AssetHelper.java`, se crean **enlaces simbólicos (symlinks)** desde `files/usr/bin/OpenSees` hacia el binario real extraído por Android en `nativeLibraryDir/libOpenSees.so`.
   - De la misma forma, las librerías `.so` son enlazadas en `files/usr/lib/` para que `OpenSees` encuentre `libhdf5.so.1000` exactamente con el nombre que espera.

4. **Variables de Entorno (OpenSeesExecutor.java)**:
   - Se configuraron las variables `LD_LIBRARY_PATH` y `TCL_LIBRARY` al ejecutar el binario, para que resuelva dependencias locales de forma aislada.

## 3. Resultado
La app ahora:
- Cumple con Google Play.
- Aprovecha la extracción nativa (más rápido, sin duplicar memoria y seguro).
- Mantiene la estructura virtual `usr/bin` y `usr/lib` intacta a nivel lógico mediante symlinks, garantizando compatibilidad con OpenSees original.
