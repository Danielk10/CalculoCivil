# Structural and Seismic Research

**Structural and Seismic Research** es una aplicación de cálculo estructural para Android, basada en el potente motor de simulación **OpenSees** utilizando la interfaz de desarrollo nativo de Android (**NDK**) con integración **Java y C++ (JNI)**.

---

# README / LICENSE DECLARATION

## 🏛️ Proyecto basado en OpenSees - Declaración de Licencia y Uso

Este repositorio contiene mi **código Android independiente** (licenciado bajo **Apache 2.0**) que utiliza el núcleo de **OpenSees** como motor de cálculo estructural. Este proyecto está desarrollado **exclusivamente para investigación personal y uso interno**.

***

## ⚠️ Declaración de Cumplimiento con la Licencia de OpenSees

Declaro explícitamente que:

1.  **No soy una institución educativa, de investigación o sin fines de lucro** reconocido.
2.  Este proyecto es **para investigación personal y uso interno exclusivo**.
3.  **NO voy a publicar ni distribuir APKs** en GitHub, Google Play, ni en ningún otro lugar.
4.  **Solo licencio mi código Android independiente** (Apache 2.0). El código de OpenSees **se mantiene bajo su licencia original** de UC Berkeley.
5.  Este proyecto **no es comercial** y **no genera ingresos** (sin anuncios, sin venta, sin monetización).

Esto cumple con la **parte (b)** de la licencia oficial de OpenSees:

> *"use, reproduction and modification of this software by other entities for internal purposes only"*

**NO hay redistribución de binarios/APKs**, solo código fuente en este repositorio.

***

## 📄 Licencias de los Componentes y Dependencias

Este proyecto integra las siguientes librerías de código abierto. Todas tienen licencias permisivas (BSD/MIT/Apache), **excepto OpenSees** que tiene su licencia especial de UC Berkeley:

| Componente | Propósito | Licencia | Página Oficial | Repositorio Fuente |
| :--- | :--- | :--- | :--- | :--- |
| **OpenSees** | Núcleo de análisis estructural (FEA) | **UC Berkeley License** | [opensees.berkeley.edu](https://opensees.berkeley.edu) | [GitHub OpenSees](https://github.com/OpenSees/OpenSees) |
| **OpenBLAS** | Álgebra lineal optimizada (BLAS/LAPACK) | **BSD 3-Clause** | [www.openblas.net](https://www.openblas.net) | [GitHub OpenBLAS](https://github.com/OpenMathLib/OpenBLAS) |
| **Tcl/Tk** | Scripting, entorno de ejecución y comandos | **BSD-like** | [www.tcl-lang.org](https://www.tcl-lang.org) | [GitHub Tcl](https://github.com/tcltk/tcl) / [Tk](https://github.com/tcltk/tk) |
| **Eigen3** | Biblioteca de álgebra lineal en C++ | **MPL2** | [eigen.tuxfamily.org](https://eigen.tuxfamily.org) | [GitLab Eigen](https://gitlab.com/libeigen/eigen) |
| **HDF5** | Formato de almacenamiento científico E/S | **BSD-like** | [www.hdfgroup.org](https://www.hdfgroup.org) | [GitHub HDF5](https://github.com/HDFGroup/hdf5) |
| **libaec / SZIP** | Compresión de datos sin pérdida (Adaptive Entropy Coding) | **BSD-like** | [libaec.gitlab.io](https://libaec.gitlab.io) | [GitHub libaec](https://github.com/Deutsches-Klimarechenzentrum/libaec) |
| **Zlib** | Biblioteca de compresión estándar | **Zlib License** | [zlib.net](https://zlib.net) | [GitHub Zlib](https://github.com/madler/zlib) |
| **Python 3.11** | Intérprete y runtime para OpenSeesPy | **PSF License** | [www.python.org](https://www.python.org) | [GitHub Python](https://github.com/python/cpython) |
| **Bzip2** | Compresión de datos de bloques | **BSD-style** | [sourceware.org/bzip2](https://sourceware.org/bzip2/) | [Git Sourceware](https://sourceware.org/git/bzip2.git) |
| **Expat** | Parser XML en C | **MIT** | [libexpat.github.io](https://libexpat.github.io/) | [GitHub Expat](https://github.com/libexpat/libexpat) |
| **Brotli** | Algoritmo de compresión genérico | **MIT** | [github.com/google/brotli](https://github.com/google/brotli) | [GitHub Brotli](https://github.com/google/brotli) |
| **libpng** | Soporte de formato gráfico PNG | **libpng License** | [www.libpng.org](http://www.libpng.org/pub/png/libpng.html) | [SourceForge libpng](https://sourceforge.net/projects/libpng/) |
| **X11 / Xft / Xext** | Sistema de ventanas y soporte gráfico Tk | **MIT / X11** | [www.x.org](https://www.x.org) | [GitLab X.org](https://gitlab.freedesktop.org/xorg) |
| **Fontconfig / FreeType**| Gestión de fuentes tipográficas | **FreeType / MIT** | [freetype.org](https://freetype.org) | [FreeType Git](https://gitlab.freedesktop.org/freetype/freetype) |
| **BLAS (Netlib)** | Álgebra lineal básica | **Reference BLAS** | [netlib.org/blas](https://www.netlib.org/blas/) | [Netlib BLAS](https://www.netlib.org/blas/) |
| **ARPACK** | Problemas de autovalores dispersos | **Permisiva** | [netlib.org/arpack](https://www.netlib.org/arpack/) | [GitHub ARPACK-NG](https://github.com/opencollab/arpack-ng) |
| **SuperLU** | Solución de sistemas lineales dispersos | **BSD-like** | [superlu.cs.lbl.gov](http://superlu.cs.lbl.gov) | [GitHub SuperLU](https://github.com/xiaoyeli/superlu) |
| **UMFPACK** | Factorización LU dispersa (SuiteSparse) | **GPL** | [people.engr.tamu.edu](https://people.engr.tamu.edu/davis/suitesparse.html) | [GitHub SuiteSparse](https://github.com/DrTimothyAldenDavis/SuiteSparse) |
| **Mi código Android** | Aplicación Android NDK (Java/C++) | **Apache 2.0** | [apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0) | [Este Repositorio](https://github.com/Danielk10/CalculoCivil) |

***

## 🔒 Licencia de OpenSees

El código de OpenSees incluido en este repositorio se mantiene bajo la **licencia oficial de UC Berkeley**:

> **Copyright © The Regents of the University of California.**
> All rights reserved.
>
> The Regents grants permission, without fee and without a written license agreement, for:
>
> **(a)** use, reproduction, modification, and distribution of this software and its documentation by **educational, research, and non-profit entities for noncommercial purposes only**; and
>
> **(b)** use, reproduction and modification of this software by **other entities for internal purposes only**.
>
> The above copyright notice and this permission notice shall appear in all copies and modifications of the software and/or documentation.
>
> Permission to incorporate this software into products for commercial distribution may be obtained by contacting the **University of California Office of Technology Licensing**.

***

## 📌 Aviso de Copyright de UC Berkeley

```
Copyright © The Regents of the University of California.
All rights reserved.

OpenSees - Open System for Earthquake Engineering Simulation
Pacific Earthquake Engineering Research (PEER) Center
University of California, Berkeley
```

***

## 🏗️ Separación Arquitectónica

Para garantizar la integridad de las licencias, el proyecto se estructura bajo una separación clara:

1.  **Capa Nativa (C++/Fortran):** El núcleo de OpenSees se compila de forma independiente como una librería dinámica o estática (`.so` / `.a`). Esta parte del código es la que está sujeta a la Licencia de UC Berkeley. Todos los binarios cumplen estrictamente las convenciones de Android (`lib<nombre>.so`).
2.  **Capa de Aplicación Android (Java/Kotlin/JNI):** El código que interactúa con la interfaz de usuario, gestiona el ciclo de vida de la actividad y carga la librería nativa es **totalmente independiente** del código fuente de OpenSees. Este es el código licenciado bajo **Apache 2.0**.
3.  **Integración:** La comunicación entre ambas capas se realiza exclusivamente a través de la Interfaz Nativa de Java (JNI) y enlaces simbólicos de runtime manejados por `AssetHelper.java`. No existe una mezcla de propiedad intelectual que modifique la licencia del núcleo de OpenSees.

***

## 🚫 Lo que NO haré

Para evitar violación de la licencia de OpenSees, **NO**:

- ❌ Publicaré APKs en GitHub o Google Play.
- ❌ Distribuiré binarios compilados de OpenSees.
- ❌ Monetizaré este proyecto (sin anuncios, sin venta, sin ingresos).
- ❌ Usaré el nombre de UC Berkeley para promoción sin permiso.
- ❌ Venderé este producto como parte de software comercial.

***

## ✅ Lo que VOY a hacer

- ✅ Mantener este proyecto como **uso interno exclusivo** (sin redistribución).
- ✅ Documentar claramente que es **para investigación personal y uso no comercial**.
- ✅ Incluir el **texto completo de la licencia de OpenSees**.
- ✅ Mantener los **avisos de copyright de UC Berkeley**.
- ✅ Licenciar mi código Android independiente bajo **Apache 2.0**.

***

## 📞 Contacto para Licencia Comercial

Si en el futuro deseo distribuir APKs o monetizar este proyecto, contactaré a:

**University of California Office of Technology Licensing**  
2150 Shattuck Avenue #510, Berkeley, CA 94720-1620  
(510) 643-7201  
[otl.berkeley.edu](https://otl.berkeley.edu)

---

## 🛠️ Especificaciones Técnicas y Versiones

El proyecto está configurado con las siguientes versiones de Android y herramientas de compilación:

*   **SDK de Compilación (Compile SDK):** API 37 (Android 15+)
*   **SDK Objetivo (Target SDK):** API 37
*   **SDK Mínimo (Min SDK):** API 23 (Android 6.0 Marshmallow+)
*   **Versión de NDK:** `30.0.14904198`
*   **Versión de CMake:** `4.1.2`
*   **Versión de Gradle:** `9.5.1` (con Android Gradle Plugin 9.2.1)
*   **Alineación de Página ELF:** `16 KB` (Alineación requerida para Android 15+)
*   **Nombre de Paquete (Package Name):** `com.diamon.calculo`
*   **Nombre de la Librería Nativa:** `libcalculocivil.so` (Cargada como `System.loadLibrary("calculocivil")`)

---

## 📂 Estructura Principal del Proyecto

*   **[app/](app)**: Código fuente de la aplicación Android (Java, JNI, CMake).
*   **[app/src/main/jniLibs/arm64-v8a/](app/src/main/jniLibs/arm64-v8a)**: Librerías binarias nativas empaquetadas con el prefijo `lib` y extensión `.so`.
*   **[app/src/main/cpp/include/](app/src/main/cpp/include)**: Cabeceras C/C++ de desarrollo (Eigen3, HDF5, OpenBLAS, Tcl/Tk, libaec) integradas en CMake.
*   **[app/src/main/assets/](app/src/main/assets)**: Archivos de runtime ligero (scripts Tcl/Tk, pkgconfig).
*   **[fake_root/](fake_root)**: Estructura simulada original de archivos para referencia exacta de rutas.

---

## 📜 Scripts de Compilación Personalizados

El repositorio incluye los siguientes scripts de automatización de compilación:

*   **[`setup-sdk.sh`](setup-sdk.sh)**: Descarga e instala automáticamente el SDK de Android, NDK `30.0.14904198`, CMake `4.1.2`, licencias y genera `local.properties`.
*   **[`build_eigen_custom.sh`](build_eigen_custom.sh)**: Descarga Eigen 3.4.0, instala cabeceras e incluye la plantilla `eigen3.pc`.
*   **[`build_libaec_custom.sh`](build_libaec_custom.sh)**: Compila e instala `libaec.so` y `libsz.so` (SZIP) con alineación a 16KB.
*   **[`build_openblas_custom.sh`](build_openblas_custom.sh)**: Compila OpenBLAS optimizado para `ARMV8` con soporte LAPACK y alineación a 16KB.
*   **[`build_tcl_custom.sh`](build_tcl_custom.sh)**: Compila Tcl 8.6 (`libtcl8_6.so`), instala scripts runtime y ajusta `tclConfig.sh`.
*   **[`build_tk_custom.sh`](build_tk_custom.sh)**: Compila Tk 8.6 (`libtk8_6.so`) con integración X11 y cabeceras privadas.
*   **[`build_hdf5_custom.sh`](build_hdf5_custom.sh)**: Compila HDF5 (`libhdf5.so`) con soporte C++, Fortran, SZIP y Zlib.
*   **[`build_opensees_custom.sh`](build_opensees_custom.sh)**: Aplica parches de compatibilidad Clang/Tcl 8.6 (`TCL_Char`), compila `libOpenSees.so` y el módulo Python `opensees.so`.

---

## 🚀 Configuración y Construcción

Para configurar el entorno y compilar el APK:

### 1. Configurar el SDK y NDK de Android
```bash
chmod +x setup-sdk.sh
./setup-sdk.sh
```

### 2. Compilar el APK
```bash
./gradlew assembleDebug
```

El APK resultante se generará en: `/tmp/calculoestructural_build/outputs/apk/debug/app-debug.apk`

---

## 📚 Documentación y Reportes de Auditoría

*   **[REPORTE_RENOMBRADO_BINARIOS.md](REPORTE_RENOMBRADO_BINARIOS.md)**: Auditoría completa de renombrado de 59 binarios al formato `lib<nombre>.so` requerido por Android.
*   **[REPORTE_CAMBIOS_ASSETS.md](REPORTE_CAMBIOS_ASSETS.md)**: Detalle de optimización de assets (reducción de 135 MB a 3.5 MB, purga de `.a` y traslado de cabeceras al NDK).
*   **[REPORTE_DEPENDENCIAS_ANDROID.md](REPORTE_DEPENDENCIAS_ANDROID.md)**: Matriz de dependencias ELF entre librerías compartidas con nombres antiguos vs. nuevos nombres Android.
*   **[REPORTE_CRITICO_RUTAS.md](REPORTE_CRITICO_RUTAS.md)**: Corrección de rutas RUNPATH/RPATH hardcoded y eliminación de contaminación de Termux.
*   **[INSTALACION_OPENSEES.md](INSTALACION_OPENSEES.md)**: Guía detallada de parches de compatibilidad para compilación con Clang y Tcl 8.6.
*   **[REPORTE_ANALISIS_DEPENDENCIAS.md](REPORTE_ANALISIS_DEPENDENCIAS.md)**: Reporte inicial de dependencias nativas.

---

## Autor

**Daniel Diamon**  
Tinaquillo, Cojedes, Venezuela  
Desarrollador independiente
