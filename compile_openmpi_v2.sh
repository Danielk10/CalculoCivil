#!/bin/bash
set -e

cd "$HOME"

export APP_PREFIX=/data/data/com.diamon.calculo/files/usr
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export VERSION=5.0.2

rm -rf "$HOME/openmpi-$VERSION" "$HOME/openmpi-${VERSION}.tar.bz2"

wget "https://www.open-mpi.org/software/ompi/v5.0/downloads/openmpi-${VERSION}.tar.bz2"
tar -xvf "openmpi-${VERSION}.tar.bz2"
cd "$HOME/openmpi-$VERSION"

export CC=clang
export CXX=clang++
export FC=gfortran

# FLAGS PARA CONFIGURAR: Sin -include stdlib.h
export CFLAGS="-fPIC -Oz"
export CXXFLAGS="-fPIC -Oz"
export FFLAGS="-fPIC -Oz"
export FCFLAGS="-fPIC -Oz"
export LDFLAGS="-Wl,-z,max-page-size=16384"

echo "=== Configurando OpenMPI (sin inyección) ==="
./configure \
  --prefix="$FAKE_USR" \
  --enable-shared \
  --disable-static \
  --enable-mpi-fortran=all \
  CC="$CC" \
  CXX="$CXX" \
  FC="$FC" \
  CFLAGS="$CFLAGS" \
  CXXFLAGS="$CXXFLAGS" \
  FFLAGS="$FFLAGS" \
  FCFLAGS="$FCFLAGS" \
  LDFLAGS="$LDFLAGS" \
  2>&1 | tee config.out

# FLAGS PARA COMPILAR: Con inyección
export CFLAGS="-fPIC -Oz -include stdlib.h"
export CXXFLAGS="-fPIC -Oz -include stdlib.h"

echo "=== Compilando OpenMPI (con inyección) ==="
# Usamos make configurando los flags adicionales
make -j"$(nproc)" all CFLAGS="$CFLAGS" CXXFLAGS="$CXXFLAGS" 2>&1 | tee make.out

echo "=== Instalando OpenMPI ==="
make install 2>&1 | tee install.out

echo "=== Verificación de wrappers ==="
ls -lh "$FAKE_USR/bin/mpicc"
ls -lh "$FAKE_USR/bin/mpicxx" || true
ls -lh "$FAKE_USR/bin/mpifort"

echo "=== OpenMPI completo compilado exitosamente ==="
echo "Instalado en: $FAKE_USR"
