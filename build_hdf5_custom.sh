#!/bin/bash
set -e

cd "$HOME" || exit 1

export APP_PREFIX=/data/data/com.diamon.calculo/files/usr
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export TMX_PREFIX=/data/data/com.termux/files/usr

mkdir -p "$FAKE_USR/lib" "$FAKE_USR/include"

export CC=clang
export CXX=clang++
export FC=gfortran

export COMMON_CPPFLAGS="-I$FAKE_USR/include -I$TMX_PREFIX/include"
export COMMON_CFLAGS="-fPIC -fPIE -Oz -ffile-prefix-map=$DESTDIR="
export COMMON_CXXFLAGS="-fPIC -fPIE -Oz -ffile-prefix-map=$DESTDIR="
export COMMON_FCFLAGS="-fPIC -fPIE -Oz -ffile-prefix-map=$DESTDIR="

# Banderas de enlace (Alineación a 16KB para Android)
export BASE_LDFLAGS="-Wl,-z,max-page-size=16384 -L$FAKE_USR/lib -L$TMX_PREFIX/lib"
export EXE_LDFLAGS="-pie $BASE_LDFLAGS"
export SHARED_LDFLAGS="$BASE_LDFLAGS"

export PKG_CONFIG_PATH="$FAKE_USR/lib/pkgconfig:$TMX_PREFIX/lib/pkgconfig:${PKG_CONFIG_PATH:-}"

# ==========================================
# Compilación e instalación de HDF5
# ==========================================
echo "=== Compilando HDF5 ==="
cd "$HOME"
rm -rf "$HOME/hdf5"
git clone https://github.com/HDFGroup/hdf5.git --depth 1
cd "$HOME/hdf5" || exit 1

mkdir -p build && cd build || exit 1
rm -rf ./*

cmake .. \
  -G "Unix Makefiles" \
  -DCMAKE_INSTALL_PREFIX="$APP_PREFIX" \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_CROSSCOMPILING=OFF \
  -DCMAKE_C_COMPILER="$CC" \
  -DCMAKE_CXX_COMPILER="$CXX" \
  -DCMAKE_Fortran_COMPILER="$FC" \
  -DCMAKE_C_FLAGS="$COMMON_CFLAGS $COMMON_CPPFLAGS" \
  -DCMAKE_CXX_FLAGS="$COMMON_CXXFLAGS $COMMON_CPPFLAGS" \
  -DCMAKE_Fortran_FLAGS="$COMMON_FCFLAGS $COMMON_CPPFLAGS" \
  -DCMAKE_EXE_LINKER_FLAGS="$EXE_LDFLAGS" \
  -DCMAKE_SHARED_LINKER_FLAGS="$SHARED_LDFLAGS" \
  -DCMAKE_PREFIX_PATH="$FAKE_USR;$TMX_PREFIX" \
  -DCMAKE_FIND_ROOT_PATH="$TMX_PREFIX;$FAKE_USR" \
  -DBUILD_SHARED_LIBS=ON \
  -DBUILD_TESTING=OFF \
  -DHDF5_BUILD_EXAMPLES=OFF \
  -DHDF5_BUILD_TOOLS=ON \
  -DHDF5_BUILD_UTILS=ON \
  -DHDF5_BUILD_CPP_LIB=ON \
  -DHDF5_BUILD_FORTRAN=ON \
  -DHDF5_BUILD_JAVA=OFF \
  -DHDF5_BUILD_HL_LIB=ON \
  -DHDF5_ENABLE_PARALLEL=OFF \
  -DHDF5_ENABLE_ZLIB_SUPPORT=ON \
  -DZLIB_INCLUDE_DIR="$TMX_PREFIX/include" \
  -DZLIB_LIBRARY_RELEASE="$TMX_PREFIX/lib/libz.so" \
  -DZLIB_LIBRARY="$TMX_PREFIX/lib/libz.so" \
  -DHDF5_ENABLE_SZIP_SUPPORT=ON \
  -DHDF5_ENABLE_SZIP_ENCODING=ON

cmake --build . --parallel "$(nproc)"
DESTDIR="$DESTDIR" cmake --install .

# ==========================================
# Verificación de la instalación
# ==========================================
echo "=== Verificando instalación de HDF5 ==="
ls -lh "$FAKE_USR/lib/libhdf5.so"
ls -lh "$FAKE_USR/lib/libhdf5_hl.so"

echo
echo "=== Verificando enlace de HDF5 ==="
readelf -d "$FAKE_USR/lib/libhdf5.so" | grep NEEDED

echo
echo "=== Verificando enlace de HDF5 con libsz ==="
readelf -d "$FAKE_USR/lib/libhdf5.so" | grep -E -i "libsz|libaec" || echo "Aviso: SZIP no aparece en las dependencias."

echo
echo "=== Alineación a 16KB de segmentos ELF (HDF5) ==="
readelf -l "$FAKE_USR/lib/libhdf5.so" | grep LOAD
