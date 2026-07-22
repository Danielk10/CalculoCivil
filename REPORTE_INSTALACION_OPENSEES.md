# Reporte de Instalación de OpenSees en Cloud Shell

Este reporte documenta los pasos técnicos y el estado final de la instalación nativa de **OpenSees** y **OpenSeesPy** en el entorno de desarrollo actual (Cloud Shell), realizada de forma aislada y persistente.

## Resumen de Acciones Realizadas

1. **Limpieza del Entorno Node.js y Espacio en Disco:**
   - Se removió una versión duplicada y obsoleta de Node.js (v24).
   - Se reinstaló `gemini` sobre la última versión (v26.5.0).
   - Se limpiaron los cachés pesados del sistema (APT) y de nivel de usuario (`~/.cache`, `.gradle/caches`, `.npm`), liberando aproximadamente 2 GB de espacio.

2. **Preparación del Entorno de Python:**
   - Se requería que OpenSees no interviniera ni dependiera del Python del sistema por defecto.
   - Se instaló Python 3.11 desde el repositorio PPA (deadsnakes).
   - Se creó un entorno virtual puro en `~/opensees-env`.

3. **Proceso de Compilación Desde Código Fuente:**
   - Se utilizaron compiladores de C++ (gcc 13) y Fortran para construir OpenSees directamente desde el repositorio oficial.
   - Se deshabilitaron explícitamente los intérpretes paralelos (OpenSeesMP, OpenSeesSP) al no ser requeridos (y para evitar el peso/conflictos de librerías como MUMPS).
   - Se compilaron el ejecutable de Tcl (`OpenSees`) y la librería compartida de Python (`opensees.so`).

4. **Solución a la Persistencia del Cloud Shell:**
   - **El Reto:** Cloud Shell restaura todas las dependencias del sistema instaladas vía APT (como `libmkl`, `lapack`, `libeigen`, `tcl-dev`) cada vez que termina la sesión. Sin embargo, la carpeta `~` (Home) sí es persistente.
   - **La Solución:** Se reubicaron los binarios compilados de forma definitiva en el Home (`~/.local/bin/OpenSees` y `~/opensees-env/lib/python3.11/site-packages/opensees.so`).
   - Se eliminaron por completo las carpetas de código fuente (`~/OpenSees` y `~/.conan`) ahorrando casi 3 GB de espacio extra.

## Cómo Usar OpenSees y el Script de Restauración

Debido a que las librerías base compartidas del sistema Linux se pierden al reiniciar Cloud Shell, se ha creado un script automatizado para restaurarlas en segundos.

### 1. El script `preparar_opensees.sh`
Este script se encuentra en `~/preparar_opensees.sh` (y hay una copia en `~/Descargas`). Su función es descargar e instalar rápidamente vía APT las librerías efímeras (liblapack, mkl, eigen, tcl, tk) que los binarios compilados de OpenSees exigen para poder funcionar. 

**Debe ejecutarse (con source) cada vez que abras una nueva sesión de Cloud Shell y desees hacer análisis.**

```bash
# Ejecutar cada vez que inicies sesión:
source ~/preparar_opensees.sh
```

### 2. Ejecutando OpenSees en Tcl
Con las dependencias cargadas, el comando `OpenSees` se encuentra globalmente disponible (gracias a estar en `~/.local/bin`).

```bash
# Ejemplo:
OpenSees ~/Descargas/test_opensees.tcl
```

### 3. Ejecutando OpenSeesPy (Python)
Simplemente debes activar tu entorno permanente e importar la librería.

```bash
# Activar el entorno
source ~/opensees-env/bin/activate

# Correr scripts de prueba
python ~/Descargas/test_opensees.py
```

---
*Instalación completada y verificada exitosamente.*
