# Reporte de Cambios en Assets

**Fecha:** 22 de Julio, 2026  
**Ubicación:** `app/src/main/assets/data/data/com.diamon.calculo/files/usr/`

---

## Resumen de Impacto

| Métrica | Antes | Después |
|---|---|---|
| Tamaño total de assets | ~135 MB | ~4 MB |
| Espacio liberado | - | **~131 MB** |

---

## 1. Librerías Estáticas Eliminadas (~103 MB)

**Razón:** Las librerías estáticas (`.a`) son archivos de compilación/enlazado que solo se necesitan al momento de construir el binario. No tienen función en runtime en Android. Su inclusión en el APK es redundancia pura.

| Archivo | Tamaño | Razón de Eliminación |
|---|---|---|
| `lib/libopenblas.a` | 44 MB | Compilación estática, ya existe `.so` en jniLibs |
| `lib/libopenblasp-r0.3.34.dev.a` | 44 MB | Duplicado de compilación estática |
| `lib/libhdf5.a` | 13 MB | Compilación estática |
| `lib/libhdf5_cpp.a` | 1.2 MB | Compilación estática |
| `lib/libhdf5_fortran.a` | 470 KB | Compilación estática |
| `lib/libhdf5_hl.a` | 248 KB | Compilación estática |
| `lib/libhdf5_hl_cpp.a` | 16 KB | Compilación estática |
| `lib/libhdf5_hl_fortran.a` | 146 KB | Compilación estática |
| `lib/libhdf5_hl_f90cstub.a` | 39 KB | Compilación estática |
| `lib/libhdf5_f90cstub.a` | 159 KB | Compilación estática |
| `lib/libhdf5_tools.a` | 692 KB | Compilación estática |
| `lib/libaec.a` | 44 KB | Compilación estática |
| `lib/libsz.a` | 49 KB | Compilación estática |
| `lib/libtclstub8.6.a` | 8 KB | Stub de compilación |
| `lib/libtkstub8.6.a` | 5 KB | Stub de compilación |

## 2. Archivos de Configuración de Build Eliminados (~144 KB)

**Razón:** Los archivos CMake son configuraciones de compilación que solo sirven para `find_package()` en sistemas de build. No tienen utilidad en runtime Android.

| Directorio/Archivo | Razón |
|---|---|
| `lib/cmake/hdf5/` (completo) | Config CMake de HDF5 |
| `lib/cmake/openblas/` (completo) | Config CMake de OpenBLAS |
| `lib/cmake/libaec/` (completo) | Config CMake de libaec |
| `lib/libhdf5.settings` | Info de compilación HDF5 |

## 3. Módulos Fortran Eliminados

**Razón:** Los archivos `.mod` son módulos compilados de Fortran, necesarios solo para compilación, no para ejecución.

| Archivos | Razón |
|---|---|
| `include/*.mod` (28 archivos) | Módulos Fortran de compilación |
| `include/mod/` (directorio completo) | Módulos estáticos/compartidos |

## 4. Headers Movidos a NDK Includes (~20 MB)

**Razón:** Los headers de desarrollo (`.h`) no son necesarios en los assets del APK (no se extraen ni usan en runtime). Sin embargo, son valiosos para la compilación NDK, así que se movieron al directorio de includes del proyecto CMake.

**Origen:** `assets/.../usr/include/`  
**Destino:** `app/src/main/cpp/include/`

| Categoría | Archivos Movidos | Uso |
|---|---|---|
| Eigen3 | `eigen3/` (directorio completo, ~12 MB) | Biblioteca de álgebra lineal C++ |
| HDF5 | 110+ archivos `H5*.h`, `hdf5.h`, `hdf5_hl.h` | API HDF5 C/C++ |
| OpenBLAS | `cblas.h`, `lapack.h`, `lapacke*.h`, `f77blas.h`, `openblas_config*.h` | BLAS/LAPACK |
| Tcl/Tk | `tcl.h`, `tk.h`, `tcl*.h`, `tk*.h` | API Tcl/Tk |
| libaec/SZlib | `libaec.h`, `szlib.h` | Compresión AEC |
| Internos Tcl/Tk | `tclInt*.h`, `tkInt*.h`, `tclPort.h`, etc. | Headers privados Tcl/Tk |
| Fortran HDF5 | `H5config_f.inc`, `H5f90*.h` | Interfaz Fortran HDF5 |

## 5. Corrección de Rutas Termux

**Razón:** Algunos archivos de configuración aún contenían referencias a `/data/data/com.termux/files/usr` que es la ruta de Termux, no de la app.

| Archivo | Cambio Realizado |
|---|---|
| `lib/tclConfig.sh` | `com.termux` → `com.diamon.calculo` |
| `lib/tk8.6/tkConfig.sh` | `com.termux` → `com.diamon.calculo` |
| `lib/pkgconfig/tk.pc` | `com.termux` → `com.diamon.calculo` |
| `bin/h5cc` | `com.termux` → `com.diamon.calculo` |
| `bin/h5c++` | `com.termux` → `com.diamon.calculo` |
| `bin/h5fc` | `com.termux` → `com.diamon.calculo` |

## 6. Lo que se CONSERVÓ en Assets (Runtime)

Estos archivos son necesarios en runtime y se extraen al directorio interno de la app:

| Directorio/Archivo | Razón de Conservación |
|---|---|
| `lib/tcl8.6/` | Scripts Tcl runtime (init.tcl, auto.tcl, encodings, etc.) |
| `lib/tcl8/` | Scripts Tcl 8.5 platform |
| `lib/tk8.6/` | Scripts Tk runtime (widgets, dialogs, themes, etc.) |
| `lib/pkgconfig/` | Configuración de paquetes (.pc) |
| `lib/itcl4.3.2/` | Extensión [incr Tcl] + binario |
| `lib/sqlite3.43.0/` | Extensión SQLite + binario |
| `lib/tdbc*/` | Extensiones TDBC (MySQL, ODBC, PostgreSQL) |
| `lib/thread2.8.10/` | Extensión Thread + binario |
| `lib/tclConfig.sh` | Configuración Tcl (runtime) |
| `lib/tclooConfig.sh` | Configuración TclOO |
| `lib/libh5cc.so` | Script de compilación HDF5 (shell script) |
| `lib/hdf5/` | Directorio HDF5 (vacío, placeholder) |
| `bin/h5cc, h5c++, h5fc` | Scripts wrapper de compilación HDF5 |
| `share/` | Documentación y licencias |

---

## Actualización de CMakeLists.txt

Se actualizó `app/src/main/cpp/CMakeLists.txt` para incluir el nuevo directorio de headers:

```cmake
target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include)
```

Esto permite que el código C++ en el proyecto acceda a los headers de Eigen3, HDF5, OpenBLAS, Tcl/Tk y libaec directamente.
