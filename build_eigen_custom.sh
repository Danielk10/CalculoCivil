#!/bin/bash
set -e

cd "$HOME" || exit 1

export APP_PREFIX=/data/data/com.diamon.calculo/files/usr
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"

mkdir -p "$FAKE_USR/include/eigen3" "$FAKE_USR/lib/pkgconfig"

rm -rf "$HOME/eigen-3.4.0" "$HOME/eigen-3.4.0.tar.gz"

echo "Descargando Eigen 3.4.0..."
wget https://gitlab.com/libeigen/eigen/-/archive/3.4.0/eigen-3.4.0.tar.gz

echo "Extrayendo Eigen..."
tar -xzf eigen-3.4.0.tar.gz

echo "Instalando cabeceras de Eigen..."
cp -r "$HOME/eigen-3.4.0/Eigen" "$FAKE_USR/include/eigen3/"
cp -r "$HOME/eigen-3.4.0/unsupported" "$FAKE_USR/include/eigen3/"

echo "Generando eigen3.pc..."
cat > "$FAKE_USR/lib/pkgconfig/eigen3.pc" <<EOF
prefix=$APP_PREFIX
includedir=\${prefix}/include/eigen3

Name: Eigen3
Description: A C++ template library for linear algebra
Version: 3.4.0
Cflags: -I\${includedir}
EOF

echo "=== Verificando instalación ==="
find "$FAKE_USR/include/eigen3" -maxdepth 1 \( -name 'Eigen' -o -name 'unsupported' \) | sort
ls -lh "$FAKE_USR/include/eigen3/Eigen/Dense" 2>/dev/null || echo "Aviso: Eigen/Dense no encontrado"
ls -lh "$FAKE_USR/lib/pkgconfig/eigen3.pc"

echo
echo "=== Contenido de eigen3.pc ==="
cat "$FAKE_USR/lib/pkgconfig/eigen3.pc"

echo
echo "Eigen 3.4.0 instalado correctamente en: $FAKE_USR/include/eigen3"
