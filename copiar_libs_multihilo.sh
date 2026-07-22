#!/bin/bash
set -euo pipefail

# ==============================================================================
# CONFIGURACIÓN DE RUTAS Y VARIABLES DE ENTORNO
# ==============================================================================
export APP_PREFIX="/data/data/com.diamon.calculo/files/usr"
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export TMX_PREFIX="/data/data/com.termux/files/usr"

# Nueva carpeta de destino en $HOME
TARGET_DIR="$HOME/android_sim_libs_fast"

# Detección automática del número total de hilos/núcleos del teléfono
CORES=$(nproc 2>/dev/null || getconf _NPROCESSORS_ONLN 2>/dev/null || echo 4)

echo "=========================================="
echo "Copiando librerías usando $CORES hilos simultáneos"
echo "Destino: $TARGET_DIR"
echo "=========================================="

# Crear la nueva carpeta totalmente limpia
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"

# 1. Copiar librerías de FAKE_USR/lib en paralelo
find "$FAKE_USR/lib" -maxdepth 1 -name "*.so*" -print0 | \
  xargs -0 -P "$CORES" cp -L -t "$TARGET_DIR/"

# 2. Copiar librerías dependientes de Termux (OpenBLAS, GFortran, etc.) en paralelo
find "$TMX_PREFIX/lib" -maxdepth 1 \( -name "libopenblas*.so*" -o -name "libgfortran*.so*" \) -print0 | \
  xargs -0 -P "$CORES" cp -L -t "$TARGET_DIR/" 2>/dev/null || true

# 3. Copiar el ejecutable OpenSees CLI y el módulo opensees.so
if [ -f "$FAKE_USR/bin/OpenSees" ]; then
  cp -L "$FAKE_USR/bin/OpenSees" "$TARGET_DIR/libopensees_cli.so"
  chmod +x "$TARGET_DIR/libopensees_cli.so"
fi

OPS_PY_SO=$(find "$FAKE_USR/lib" -name "opensees.so" 2>/dev/null | head -n1 || true)
if [ -n "$OPS_PY_SO" ] && [ -f "$OPS_PY_SO" ]; then
  cp -L "$OPS_PY_SO" "$TARGET_DIR/opensees.so"
fi

echo "=========================================="
echo "¡Copia multihilo completada exitosamente!"
echo "Total de archivos en $TARGET_DIR: $(find "$TARGET_DIR" -maxdepth 1 -type f | wc -l)"
echo "=========================================="
