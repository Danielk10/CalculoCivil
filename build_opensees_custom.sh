#!/bin/bash
set -e
cd "$HOME"

echo "Descargando OpenSees oficial..."
rm -rf "$HOME/OpenSees"
git clone https://github.com/OpenSees/OpenSees.git --depth 1
cd "$HOME/OpenSees"

# ==============================================================================
# APLICANDO PARCHES DE COMPATIBILIDAD CLANG / TCL 8.6 A FUENTES DE OPENSEES
# ==============================================================================
echo "Aplicando parches de compatibilidad en el código fuente..."

# 1. Redefinir TCL_Char a 'const char' en OPS_Globals.h globalmente
find SRC DEVELOPER -name "OPS_Globals.h" -exec sed -i 's/typedef char TCL_Char;/typedef const char TCL_Char;/g' {} +
find SRC DEVELOPER -name "OPS_Globals.h" -exec sed -i 's/#define TCL_Char char/#define TCL_Char const char/g' {} +

# 2. Forzar la constante en TclReliabilityBuilder.cpp
if [ -f "SRC/reliability/tcl/TclReliabilityBuilder.cpp" ]; then
  sed -i '1i #ifndef TCL_Char\n#define TCL_Char const char\n#endif' SRC/reliability/tcl/TclReliabilityBuilder.cpp
fi

# 3. Corregir firmas 'char**' a 'const char**' o 'const TCL_Char**' en comandos Tcl de Reliability
find SRC/reliability/tcl -type f \( -name "*.h" -o -name "*.cpp" \) -exec sed -i 's/TCL_Char \*\*argv/const char \*\*argv/g' {} +
find SRC/reliability/tcl -type f \( -name "*.h" -o -name "*.cpp" \) -exec sed -i 's/char \*\*argv/const char \*\*argv/g' {} +

# 4. Ajustar calificadores 'const' para nombres de archivo y cadenas en Reliability
find SRC/reliability -type f \( -name "*.h" -o -name "*.cpp" \) -exec sed -i 's/TCL_Char \*fileName/const TCL_Char *fileName/g' {} +
find SRC/reliability -type f \( -name "*.h" -o -name "*.cpp" \) -exec sed -i 's/TCL_Char \*arrayName/const TCL_Char *arrayName/g' {} +

# ==============================================================================
# CONFIGURACIÓN DE COMPILACIÓN
# ==============================================================================
mkdir -p build && cd build && rm -rf ./*

export APP_PREFIX=/data/data/com.diamon.calculo/files/usr
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export TMX_PREFIX=/data/data/com.termux/files/usr

echo "Detectando rutas de Python 3.11..."
export PY_EXE=$(which python3.11)
export PY_INC=$(python3.11 -c "import sysconfig; print(sysconfig.get_path('include'))")
export PY_LIB=$(python3.11 -c "import sysconfig, os; print(os.path.join(sysconfig.get_config_var('LIBDIR'), sysconfig.get_config_var('LDLIBRARY')))")

mkdir -p "$FAKE_USR/include" "$FAKE_USR/lib"

export CC=clang
export CXX=clang++
export FC=gfortran

export COMMON_CFLAGS="-fPIC -fPIE -Oz -I$FAKE_USR/include -I$TMX_PREFIX/include -I$FAKE_USR/include/eigen3"
export COMMON_CXXFLAGS="-fPIC -fPIE -Oz -std=c++17 -Wno-error -Wno-c++11-narrowing -Wno-deprecated-declarations -Wno-inconsistent-missing-override -I$FAKE_USR/include -I$TMX_PREFIX/include -I$FAKE_USR/include/eigen3"
export COMMON_FFLAGS="-fPIC -fPIE -Oz -Wno-error"
export COMMON_LDFLAGS="-pie -Wl,-z,max-page-size=16384 -L$FAKE_USR/lib -L$TMX_PREFIX/lib"

export PKG_CONFIG_PATH="$FAKE_USR/lib/pkgconfig:$TMX_PREFIX/lib/pkgconfig:${PKG_CONFIG_PATH:-}"
export CMAKE_PREFIX_PATH="$FAKE_USR;$TMX_PREFIX"
export LD_LIBRARY_PATH="$FAKE_USR/lib:$TMX_PREFIX/lib:${LD_LIBRARY_PATH:-}"

export QUADMATH_LIB=$(find "$TMX_PREFIX" -name "libquadmath.*" 2>/dev/null | head -n1)

echo "Configurando OpenSees (Tcl + Python) con Clang/GFortran..."
cmake .. \
  -DOPS_FINAL_TARGET=OpenSeesPy \
  -DCMAKE_INSTALL_PREFIX="$APP_PREFIX" \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_C_COMPILER="$CC" \
  -DCMAKE_CXX_COMPILER="$CXX" \
  -DCMAKE_Fortran_COMPILER="$FC" \
  -DCMAKE_C_FLAGS="$COMMON_CFLAGS" \
  -DCMAKE_CXX_FLAGS="$COMMON_CXXFLAGS" \
  -DCMAKE_Fortran_FLAGS="$COMMON_FFLAGS" \
  -DCMAKE_EXE_LINKER_FLAGS="$COMMON_LDFLAGS" \
  -DCMAKE_SHARED_LINKER_FLAGS="$COMMON_LDFLAGS" \
  -DCMAKE_MODULE_LINKER_FLAGS="$COMMON_LDFLAGS" \
  -DCMAKE_PREFIX_PATH="$CMAKE_PREFIX_PATH" \
  -DCMAKE_BUILD_WITH_INSTALL_RPATH=TRUE \
  -DCMAKE_INSTALL_RPATH="$APP_PREFIX/lib" \
  -DTCL_INCLUDE_PATH="$FAKE_USR/include" \
  -DTCL_LIBRARY="$FAKE_USR/lib/libtcl8.6.so" \
  -DTK_INCLUDE_PATH="$FAKE_USR/include" \
  -DTK_LIBRARY="$FAKE_USR/lib/libtk8.6.so" \
  -DPYTHON_EXECUTABLE="$PY_EXE" \
  -DPYTHON_INCLUDE_DIR="$PY_INC" \
  -DPYTHON_LIBRARY="$PY_LIB" \
  -DPython_EXECUTABLE="$PY_EXE" \
  -DPython_INCLUDE_DIR="$PY_INC" \
  -DPython_LIBRARY="$PY_LIB" \
  -DPython3_EXECUTABLE="$PY_EXE" \
  -DPython3_INCLUDE_DIR="$PY_INC" \
  -DPython3_LIBRARY="$PY_LIB" \
  -DPython3_LIBRARY_DEBUG="$PY_LIB" \
  -DPython3_LIBRARY_RELEASE="$PY_LIB" \
  -DPython3_ROOT_DIR="$TMX_PREFIX" \
  -DPython3_FIND_STRATEGY=LOCATION \
  -DPython3_FIND_VIRTUALENV=STANDARD \
  -DPython3_FIND_IMPLEMENTATIONS=CPython \
  -DPython3_FIND_REGISTRY=NEVER \
  -DLAPACK_LIBRARIES="$FAKE_USR/lib/libopenblas.so" \
  -DLAPACK_FOUND=TRUE \
  -DZLIB_INCLUDE_DIR="$TMX_PREFIX/include" \
  -DZLIB_LIBRARY="$TMX_PREFIX/lib/libz.so" \
  -Dlibaec_FOUND=TRUE \
  -DHDF5_FOUND=TRUE \
  -DHDF5_INCLUDE_DIR="$FAKE_USR/include" \
  -DHDF5_LIBRARIES="$FAKE_USR/lib/libhdf5.so" \
  -DHDF5_VERSION=1.12.0 \
  -DEigen3_INCLUDE_DIR="$FAKE_USR/include/eigen3" \
  -DEIGEN3_INCLUDE_DIR="$FAKE_USR/include/eigen3"

echo "Ajustando enlaces a libquadmath en Makefile de CMake..."
if [ -n "$QUADMATH_LIB" ]; then
  find . \( -name "link.txt" -o -name "build.make" \) -exec sed -i "s|-lquadmath|$QUADMATH_LIB|g" {} +
else
  find . \( -name "link.txt" -o -name "build.make" \) -exec sed -i 's/-lquadmath//g' {} +
fi

echo "Compilando OpenSees (binario Tcl)..."
cmake --build . --target OpenSees --parallel "$(nproc)" -- -k

echo "Compilando OpenSeesPy (módulo Python 3.11)..."
cmake --build . --target OpenSeesPy --parallel "$(nproc)" -- -k

echo "Instalando OpenSees (Tcl) en fake_root..."
DESTDIR="$DESTDIR" cmake --install . --prefix "$APP_PREFIX" --component Runtime --strip 2>/dev/null || true
make install DESTDIR="$DESTDIR" || true

# Verificación de init.tcl
if [ ! -f "$FAKE_USR/lib/tcl8.6/init.tcl" ]; then
  echo "Aviso: init.tcl no encontrado en $FAKE_USR/lib/tcl8.6/. Tcl podría no funcionar."
fi

echo "Renombrando módulo Python..."
OPS_PY_SO=$(find . -iname "OpenSeesPy*.so" | head -n1)
if [ -n "$OPS_PY_SO" ]; then
  mkdir -p "$FAKE_USR/lib/python3.11/site-packages/"
  mv "$OPS_PY_SO" "$FAKE_USR/lib/python3.11/site-packages/opensees.so"
fi

echo "=== Verificando instalación ==="
ls -lh "$FAKE_USR/bin/OpenSees" 2>/dev/null || echo "Aviso: binario OpenSees (Tcl) no encontrado"
ls -lh "$FAKE_USR/lib/python3.11/site-packages/opensees.so" 2>/dev/null || echo "Aviso: módulo opensees.so no encontrado"
readelf -d "$FAKE_USR/lib/python3.11/site-packages/opensees.so" 2>/dev/null | grep NEEDED || true

echo "=== Alineación a 16KB de segmentos ELF ==="
readelf -l "$FAKE_USR/lib/python3.11/site-packages/opensees.so" 2>/dev/null | grep LOAD || true

echo "=== Proceso Completado ==="
