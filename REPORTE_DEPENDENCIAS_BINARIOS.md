# Dependencias de Binarios (CalculoCivil)

Este documento lista las dependencias y la matriz de renombrado entre la versión original y la versión de Android JNI.

## Ejecutables (usr/bin/)
| Nombre Original | Nombre JNI (arm64-v8a) | Enlazado en app como |
|-----------------|------------------------|----------------------|
| `OpenSees` | `libOpenSees.so` | `usr/bin/OpenSees` |
| `tclsh` | `libtclsh.so` | `usr/bin/tclsh` |
| `tclsh8.6` | `libtclsh8_6.so` | `usr/bin/tclsh8.6` |
| `wish` | `libwish.so` | `usr/bin/wish` |
| `wish8.6` | `libwish8_6.so` | `usr/bin/wish8.6` |
| `h5cc` | `libh5cc.so` | `usr/bin/h5cc` |

## Librerías Compartidas (usr/lib/)
| Nombre Original | Nombre JNI (arm64-v8a) | Enlazado en app como |
|-----------------|------------------------|----------------------|
| `libhdf5.so` | `libhdf5.so` | `usr/lib/libhdf5.so` |
| `libhdf5.so.1000`| `libhdf5_so_1000.so` | `usr/lib/libhdf5.so.1000` |
| `libhdf5_hl.so` | `libhdf5_hl.so` | `usr/lib/libhdf5_hl.so` |
| `libopenblas.so`| `libopenblas.so` | `usr/lib/libopenblas.so` |
| `libopenblas.so.0`| `libopenblas_so_0.so`| `usr/lib/libopenblas.so.0` |
| `libtcl8.6.so` | `libtcl8_6.so` | `usr/lib/libtcl8.6.so` |
| `libtk8.6.so` | `libtk8_6.so` | `usr/lib/libtk8.6.so` |

*Nota:* Si OpenSees depende de otra librería del sistema o no incluida aquí (ej. `libz.so`, `libc++_shared.so`), se asume que provee el sistema operativo Android o se debe copiar a `jniLibs` y mapear en `AssetHelper.java` siguiendo el mismo patrón.
