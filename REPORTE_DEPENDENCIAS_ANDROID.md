# Reporte de Dependencias - Nombres Android Actualizados

**Fecha:** 22 de Julio, 2026  
**Convención:** `lib<nombre>.so` (Android-compatible)

> Este reporte muestra las dependencias de cada binario con sus **nombres Android actualizados**.  
> La columna "Nombre Antiguo" muestra el nombre original Linux; "Nombre Android" el nuevo nombre en jniLibs.

---

## Tabla Maestra de Renombrado

| Nombre Antiguo (Linux) | Nombre Android (jniLibs) | Categoría |
|---|---|---|
| `OpenSees` | `libOpenSees.so` | Ejecutable |
| `opensees.so` | `libopensees.so` | Módulo Python |
| `tclsh` | `libtclsh.so` | Ejecutable |
| `tclsh8.6` | `libtclsh8_6.so` | Ejecutable |
| `wish` | `libwish.so` | Ejecutable |
| `wish8.6` | `libwish8_6.so` | Ejecutable |
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
| `libaec.so` | `libaec.so` | Sin cambio |
| `libaec.so.0` | `libaec_so_0.so` | Versionada |
| `libaec.so.0.1.7` | `libaec_so_0_1_7.so` | Versionada |
| `libandroid-support.so` | `libandroid_support.so` | Guión → underscore |
| `libbrotlicommon.so` | `libbrotlicommon.so` | Sin cambio |
| `libbrotlidec.so` | `libbrotlidec.so` | Sin cambio |
| `libbz2.so` | `libbz2.so` | Sin cambio |
| `libbz2.so.1.0` | `libbz2_so_1_0.so` | Versionada |
| `libbz2.so.1.0.8` | `libbz2_so_1_0_8.so` | Versionada |
| `libc++_shared.so` | `libc++_shared.so` | Sin cambio |
| `libexpat.so` | `libexpat.so` | Sin cambio |
| `libexpat.so.1` | `libexpat_so_1.so` | Versionada |
| `libexpat.so.1.12.2` | `libexpat_so_1_12_2.so` | Versionada |
| `libfontconfig.so` | `libfontconfig.so` | Sin cambio |
| `libfreetype.so` | `libfreetype.so` | Sin cambio |
| `libhdf5.so` | `libhdf5.so` | Sin cambio |
| `libhdf5.so.1000` | `libhdf5_so_1000.so` | Versionada |
| `libhdf5.so.1000.0.0` | `libhdf5_so_1000_0_0.so` | Versionada |
| `libhdf5_cpp.so` | `libhdf5_cpp.so` | Sin cambio |
| `libhdf5_cpp.so.1000` | `libhdf5_cpp_so_1000.so` | Versionada |
| `libhdf5_cpp.so.1000.0.0` | `libhdf5_cpp_so_1000_0_0.so` | Versionada |
| `libhdf5_f90cstub.so` | `libhdf5_f90cstub.so` | Sin cambio |
| `libhdf5_f90cstub.so.1000` | `libhdf5_f90cstub_so_1000.so` | Versionada |
| `libhdf5_f90cstub.so.1000.0.0` | `libhdf5_f90cstub_so_1000_0_0.so` | Versionada |
| `libhdf5_fortran.so` | `libhdf5_fortran.so` | Sin cambio |
| `libhdf5_fortran.so.1000` | `libhdf5_fortran_so_1000.so` | Versionada |
| `libhdf5_fortran.so.1000.0.0` | `libhdf5_fortran_so_1000_0_0.so` | Versionada |
| `libhdf5_hl.so` | `libhdf5_hl.so` | Sin cambio |
| `libhdf5_hl.so.1000` | `libhdf5_hl_so_1000.so` | Versionada |
| `libhdf5_hl.so.1000.0.0` | `libhdf5_hl_so_1000_0_0.so` | Versionada |
| `libhdf5_hl_cpp.so` | `libhdf5_hl_cpp.so` | Sin cambio |
| `libhdf5_hl_cpp.so.1000` | `libhdf5_hl_cpp_so_1000.so` | Versionada |
| `libhdf5_hl_cpp.so.1000.0.0` | `libhdf5_hl_cpp_so_1000_0_0.so` | Versionada |
| `libhdf5_hl_f90cstub.so` | `libhdf5_hl_f90cstub.so` | Sin cambio |
| `libhdf5_hl_f90cstub.so.1000` | `libhdf5_hl_f90cstub_so_1000.so` | Versionada |
| `libhdf5_hl_f90cstub.so.1000.0.0` | `libhdf5_hl_f90cstub_so_1000_0_0.so` | Versionada |
| `libhdf5_hl_fortran.so` | `libhdf5_hl_fortran.so` | Sin cambio |
| `libhdf5_hl_fortran.so.1000` | `libhdf5_hl_fortran_so_1000.so` | Versionada |
| `libhdf5_hl_fortran.so.1000.0.0` | `libhdf5_hl_fortran_so_1000_0_0.so` | Versionada |
| `libhdf5_tools.so` | `libhdf5_tools.so` | Sin cambio |
| `libhdf5_tools.so.1000` | `libhdf5_tools_so_1000.so` | Versionada |
| `libhdf5_tools.so.1000.0.0` | `libhdf5_tools_so_1000_0_0.so` | Versionada |
| `libopenblas.so` | `libopenblas.so` | Sin cambio |
| `libopenblas.so.0` | `libopenblas_so_0.so` | Versionada |
| `libopenblasp-r0.3.34.dev.so` | `libopenblasp_r0_3_34_dev.so` | Guiones + puntos |
| `libpng16.so` | `libpng16.so` | Sin cambio |
| `libpython3.11.so` | `libpython3_11.so` | Punto en versión |
| `libpython3.11.so.1.0` | `libpython3_11_so_1_0.so` | Versionada |
| `libsz.so` | `libsz.so` | Sin cambio |
| `libsz.so.2` | `libsz_so_2.so` | Versionada |
| `libsz.so.2.0.1` | `libsz_so_2_0_1.so` | Versionada |
| `libtcl8.6.so` | `libtcl8_6.so` | Punto en versión |
| `libtk8.6.so` | `libtk8_6.so` | Punto en versión |
| `libX11.so` | `libX11.so` | Sin cambio |
| `libX11.so.6` | `libX11_so_6.so` | Versionada |
| `libXau.so` | `libXau.so` | Sin cambio |
| `libxcb.so` | `libxcb.so` | Sin cambio |
| `libXdmcp.so` | `libXdmcp.so` | Sin cambio |
| `libXext.so` | `libXext.so` | Sin cambio |
| `libXft.so` | `libXft.so` | Sin cambio |
| `libXrender.so` | `libXrender.so` | Sin cambio |
| `libXss.so` | `libXss.so` | Sin cambio |
| `libz.so` | `libz.so` | Sin cambio |
| `libz.so.1` | `libz_so_1.so` | Versionada |
| `libz.so.1.3.2` | `libz_so_1_3_2.so` | Versionada |

---

## Dependencias por Binario (con nombres Android)

### libOpenSees.so (OpenSees principal)
| Dep Android | Dep Original | Clase | InFolder |
|---|---|---|---|
| libtcl8_6.so | libtcl8.6.so | Externa | Sí |
| libz_so_1.so | libz.so.1 | Externa | Sí |
| libopenblas.so | libopenblas.so | Externa | Sí |
| libhdf5_so_1000.so | libhdf5.so.1000 | Externa | Sí |
| libc++_shared.so | libc++_shared.so | Externa | Sí |
| libdl.so | libdl.so | Sistema | No |
| libm.so | libm.so | Sistema | No |
| libc.so | libc.so | Sistema | No |

### libopensees.so (Módulo Python OpenSees)
| Dep Android | Dep Original | Clase | InFolder |
|---|---|---|---|
| libz_so_1.so | libz.so.1 | Externa | Sí |
| libpython3_11_so_1_0.so | libpython3.11.so.1.0 | Externa | Sí |
| libopenblas.so | libopenblas.so | Externa | Sí |
| libhdf5_so_1000.so | libhdf5.so.1000 | Externa | Sí |
| libc++_shared.so | libc++_shared.so | Externa | Sí |
| libdl.so | libdl.so | Sistema | No |
| libm.so | libm.so | Sistema | No |
| libc.so | libc.so | Sistema | No |

### Herramientas HDF5 (libh5clear.so, libh5copy.so, etc.)
| Dep Android | Dep Original | Clase | InFolder |
|---|---|---|---|
| libhdf5_tools_so_1000.so | libhdf5_tools.so.1000 | Externa | Sí |
| libhdf5_so_1000.so | libhdf5.so.1000 | Externa | Sí |
| libdl.so | libdl.so | Sistema | No |
| libc.so | libc.so | Sistema | No |

### libh5watch.so (HDF5 Watch - dependencia extra)
| Dep Android | Dep Original | Clase | InFolder |
|---|---|---|---|
| libhdf5_hl_so_1000.so | libhdf5_hl.so.1000 | Externa | Sí |
| libhdf5_tools_so_1000.so | libhdf5_tools.so.1000 | Externa | Sí |
| libhdf5_so_1000.so | libhdf5.so.1000 | Externa | Sí |
| libdl.so | libdl.so | Sistema | No |
| libc.so | libc.so | Sistema | No |

### libtclsh.so / libtclsh8_6.so
| Dep Android | Dep Original | Clase | InFolder |
|---|---|---|---|
| libtcl8_6.so | libtcl8.6.so | Externa | Sí |
| libz_so_1.so | libz.so.1 | Externa | Sí |
| libdl.so | libdl.so | Sistema | No |
| libc.so | libc.so | Sistema | No |
| libm.so | libm.so | Sistema | No |

### libwish.so / libwish8_6.so
| Dep Android | Dep Original | Clase | InFolder |
|---|---|---|---|
| libtk8_6.so | libtk8.6.so | Externa | Sí |
| libtcl8_6.so | libtcl8.6.so | Externa | Sí |
| libXft.so | libXft.so | Externa | Sí |
| libfontconfig.so | libfontconfig.so | Externa | Sí |
| libfreetype.so | libfreetype.so | Externa | Sí |
| libX11.so | libX11.so | Externa | Sí |
| libXss.so | libXss.so | Externa | Sí |
| libXext.so | libXext.so | Externa | Sí |
| libz_so_1.so | libz.so.1 | Externa | Sí |
| libdl.so | libdl.so | Sistema | No |
| libc.so | libc.so | Sistema | No |
| libm.so | libm.so | Sistema | No |

---

## Enlace Simbólico (AssetHelper.java)

Cada binario en jniLibs se enlaza simbólicamente a `usr/bin/` (ejecutables) o `usr/lib/` (librerías) con el nombre original Linux, para que el runtime Tcl/OpenSees pueda encontrarlos:

```
usr/bin/OpenSees  →  nativeLibDir/libOpenSees.so
usr/lib/libhdf5.so.1000  →  nativeLibDir/libhdf5_so_1000.so
...
```

Esto es manejado automáticamente por `AssetHelper.ensureNativeToolLinks()`.
