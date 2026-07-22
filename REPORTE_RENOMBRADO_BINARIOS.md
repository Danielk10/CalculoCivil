# Reporte de Renombrado de Binarios - Android Compatible

**Fecha:** 22 de Julio, 2026  
**Ubicación:** `app/src/main/jniLibs/arm64-v8a/`

## Reglas de Nomenclatura Android

Android requiere que todos los archivos en `jniLibs` cumplan:
1. Prefijo `lib` obligatorio
2. Extensión `.so` obligatoria
3. Sin puntos en versiones intermedias (usar `_` en vez de `.`)
4. Sin guiones (`-`) en el nombre (usar `_`)

---

## 1. Ejecutables Renombrados (23 archivos)

| Nombre Original | Nombre Android | Tipo |
|---|---|---|
| `OpenSees` | `libOpenSees.so` | Binario OpenSees principal |
| `opensees.so` | `libopensees.so` | Módulo Python OpenSees |
| `tclsh` | `libtclsh.so` | Intérprete Tcl |
| `tclsh8.6` | `libtclsh8_6.so` | Intérprete Tcl 8.6 |
| `wish` | `libwish.so` | Intérprete Tk |
| `wish8.6` | `libwish8_6.so` | Intérprete Tk 8.6 |
| `h5clear` | `libh5clear.so` | Herramienta HDF5 |
| `h5copy` | `libh5copy.so` | Herramienta HDF5 |
| `h5debug` | `libh5debug.so` | Herramienta HDF5 |
| `h5delete` | `libh5delete.so` | Herramienta HDF5 |
| `h5diff` | `libh5diff.so` | Herramienta HDF5 |
| `h5dump` | `libh5dump.so` | Herramienta HDF5 |
| `h5format_convert` | `libh5format_convert.so` | Herramienta HDF5 |
| `h5import` | `libh5import.so` | Herramienta HDF5 |
| `h5jam` | `libh5jam.so` | Herramienta HDF5 |
| `h5ls` | `libh5ls.so` | Herramienta HDF5 |
| `h5mkgrp` | `libh5mkgrp.so` | Herramienta HDF5 |
| `h5perf_serial` | `libh5perf_serial.so` | Herramienta HDF5 |
| `h5repack` | `libh5repack.so` | Herramienta HDF5 |
| `h5repart` | `libh5repart.so` | Herramienta HDF5 |
| `h5stat` | `libh5stat.so` | Herramienta HDF5 |
| `h5unjam` | `libh5unjam.so` | Herramienta HDF5 |
| `h5watch` | `libh5watch.so` | Herramienta HDF5 |

## 2. Librerías Versionadas Renombradas (35 archivos)

| Nombre Original | Nombre Android | Librería |
|---|---|---|
| `libaec.so.0` | `libaec_so_0.so` | AEC |
| `libaec.so.0.1.7` | `libaec_so_0_1_7.so` | AEC |
| `libbz2.so.1.0` | `libbz2_so_1_0.so` | Bzip2 |
| `libbz2.so.1.0.8` | `libbz2_so_1_0_8.so` | Bzip2 |
| `libexpat.so.1` | `libexpat_so_1.so` | Expat XML |
| `libexpat.so.1.12.2` | `libexpat_so_1_12_2.so` | Expat XML |
| `libhdf5.so.1000` | `libhdf5_so_1000.so` | HDF5 |
| `libhdf5.so.1000.0.0` | `libhdf5_so_1000_0_0.so` | HDF5 |
| `libhdf5_cpp.so.1000` | `libhdf5_cpp_so_1000.so` | HDF5 C++ |
| `libhdf5_cpp.so.1000.0.0` | `libhdf5_cpp_so_1000_0_0.so` | HDF5 C++ |
| `libhdf5_f90cstub.so.1000` | `libhdf5_f90cstub_so_1000.so` | HDF5 Fortran |
| `libhdf5_f90cstub.so.1000.0.0` | `libhdf5_f90cstub_so_1000_0_0.so` | HDF5 Fortran |
| `libhdf5_fortran.so.1000` | `libhdf5_fortran_so_1000.so` | HDF5 Fortran |
| `libhdf5_fortran.so.1000.0.0` | `libhdf5_fortran_so_1000_0_0.so` | HDF5 Fortran |
| `libhdf5_hl.so.1000` | `libhdf5_hl_so_1000.so` | HDF5 HL |
| `libhdf5_hl.so.1000.0.0` | `libhdf5_hl_so_1000_0_0.so` | HDF5 HL |
| `libhdf5_hl_cpp.so.1000` | `libhdf5_hl_cpp_so_1000.so` | HDF5 HL C++ |
| `libhdf5_hl_cpp.so.1000.0.0` | `libhdf5_hl_cpp_so_1000_0_0.so` | HDF5 HL C++ |
| `libhdf5_hl_f90cstub.so.1000` | `libhdf5_hl_f90cstub_so_1000.so` | HDF5 HL Fortran |
| `libhdf5_hl_f90cstub.so.1000.0.0` | `libhdf5_hl_f90cstub_so_1000_0_0.so` | HDF5 HL Fortran |
| `libhdf5_hl_fortran.so.1000` | `libhdf5_hl_fortran_so_1000.so` | HDF5 HL Fortran |
| `libhdf5_hl_fortran.so.1000.0.0` | `libhdf5_hl_fortran_so_1000_0_0.so` | HDF5 HL Fortran |
| `libhdf5_tools.so.1000` | `libhdf5_tools_so_1000.so` | HDF5 Tools |
| `libhdf5_tools.so.1000.0.0` | `libhdf5_tools_so_1000_0_0.so` | HDF5 Tools |
| `libopenblas.so.0` | `libopenblas_so_0.so` | OpenBLAS |
| `libopenblasp-r0.3.34.dev.so` | `libopenblasp_r0_3_34_dev.so` | OpenBLAS |
| `libpython3.11.so` | `libpython3_11.so` | Python |
| `libpython3.11.so.1.0` | `libpython3_11_so_1_0.so` | Python |
| `libsz.so.2` | `libsz_so_2.so` | SZlib |
| `libsz.so.2.0.1` | `libsz_so_2_0_1.so` | SZlib |
| `libtcl8.6.so` | `libtcl8_6.so` | Tcl |
| `libtk8.6.so` | `libtk8_6.so` | Tk |
| `libX11.so.6` | `libX11_so_6.so` | X11 |
| `libz.so.1` | `libz_so_1.so` | Zlib |
| `libz.so.1.3.2` | `libz_so_1_3_2.so` | Zlib |

## 3. Renombrado por Guiones (1 archivo)

| Nombre Original | Nombre Android | Librería |
|---|---|---|
| `libandroid-support.so` | `libandroid_support.so` | Android Support |

## 4. Sin Cambios (29 archivos ya correctos)

`libaec.so`, `libbrotlicommon.so`, `libbrotlidec.so`, `libbz2.so`, `libc++_shared.so`, `libexpat.so`, `libfontconfig.so`, `libfreetype.so`, `libhdf5.so`, `libhdf5_cpp.so`, `libhdf5_f90cstub.so`, `libhdf5_fortran.so`, `libhdf5_hl.so`, `libhdf5_hl_cpp.so`, `libhdf5_hl_f90cstub.so`, `libhdf5_hl_fortran.so`, `libhdf5_tools.so`, `libopenblas.so`, `libpng16.so`, `libsz.so`, `libX11.so`, `libXau.so`, `libxcb.so`, `libXdmcp.so`, `libXext.so`, `libXft.so`, `libXrender.so`, `libXss.so`, `libz.so`

---

## Resumen

| Acción | Cantidad |
|---|---|
| Ejecutables renombrados | 23 |
| Librerías versionadas renombradas | 35 |
| Renombrado por guiones | 1 |
| Sin cambios necesarios | 29 |
| **Total archivos en jniLibs** | **88** |
| Carpeta `respaldo_bimarios` | **Eliminada** |
