#!/bin/bash
set -e

cd "$HOME"

# Corregido: Separación de prefix y DESTDIR
export APP_PREFIX=/data/data/com.diamon.calculo/files/usr
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export VERSION=5.0.2

rm -rf "$HOME/openmpi-$VERSION" "$HOME/openmpi-${VERSION}.tar.bz2"

wget "https://www.open-mpi.org/software/ompi/v5.0/downloads/openmpi-${VERSION}.tar.bz2"
tar -xvf "openmpi-${VERSION}.tar.bz2"
cd "$HOME/openmpi-${VERSION}"

export CC=clang
export CXX=clang++
export FC=gfortran

# FLAGS PARA CONFIGURAR: Sin inyecciones
CONF_CFLAGS="-fPIC -Oz"
CONF_CXXFLAGS="-fPIC -Oz"
CONF_FFLAGS="-fPIC -Oz"
CONF_FCFLAGS="-fPIC -Oz"
CONF_LDFLAGS="-Wl,-z,max-page-size=16384"

echo "=== Configurando OpenMPI ==="
./configure \
  --prefix="$APP_PREFIX" \
  --enable-shared \
  --disable-static \
  --enable-mpi-fortran=all \
  CC="$CC" \
  CXX="$CXX" \
  FC="$FC" \
  CFLAGS="$CONF_CFLAGS" \
  CXXFLAGS="$CONF_CXXFLAGS" \
  FFLAGS="$CONF_FFLAGS" \
  FCFLAGS="$CONF_FCFLAGS" \
  LDFLAGS="$CONF_LDFLAGS" \
  2>&1 | tee config.out

# FLAGS PARA COMPILAR/INSTALAR: Con inyección y GNU source
# Moviendo _GNU_SOURCE a CFLAGS/CXXFLAGS según recomendación
BUILD_CPPFLAGS="-include stdlib.h"
BUILD_CFLAGS="-fPIC -Oz -D_GNU_SOURCE"
BUILD_CXXFLAGS="-fPIC -Oz -D_GNU_SOURCE"

echo "=== Compilando e Instalando OpenMPI ==="
# Ejecutando en un solo bloque para consistencia y evitar recompilaciones
make -j"$(nproc)" all install DESTDIR="$DESTDIR" \
  CPPFLAGS="$BUILD_CPPFLAGS" \
  CFLAGS="$BUILD_CFLAGS" \
  CXXFLAGS="$BUILD_CXXFLAGS" \
  2>&1 | tee build_install.out

echo "=== Verificación de wrappers ==="
ls -lh "$FAKE_USR/bin/mpicc" || true
ls -lh "$FAKE_USR/bin/mpicxx" || true
ls -lh "$FAKE_USR/bin/mpifort" || true

echo "=== OpenMPI compilado exitosamente ==="
echo "Instalado en: $FAKE_USR"
