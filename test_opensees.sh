#!/bin/bash
set -euo pipefail

# ==============================================================================
# CONFIGURACIÓN DE RUTAS Y VARIABLES DE ENTORNO
# ==============================================================================
export APP_PREFIX="/data/data/com.diamon.calculo/files/usr"
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export TMX_PREFIX="/data/data/com.termux/files/usr"

# Prevención de Segfaults por hilos en Android ARM64
export OMP_NUM_THREADS=1
export OPENBLAS_NUM_THREADS=1
export MKL_NUM_THREADS=1

FLAT_LIBS="$HOME/android_sim_libs_ops"
TEST_DIR="$HOME/android_sim_test_ops"

# Parámetros del modelo (Viga en voladizo)
P_LOAD="${P_LOAD:--100.0}" # Carga en kN
LENGTH="10.0"              # Longitud en m
E_MOD="2.0e8"              # Módulo de Elasticidad (kPa)
AREA="0.01"                # Área de sección (m2)
IZ="0.0001"                # Inercia (m4)

# ==============================================================================
# FUNCIONES DE VERIFICACIÓN
# ==============================================================================
require_file() {
  [ -e "$1" ] || { echo "ERROR: no existe $1" >&2; exit 1; }
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "ERROR: falta comando $1" >&2; exit 1; }
}

require_cmd python3.11
require_cmd find
require_cmd cp
require_cmd grep
require_cmd ls

require_file "$FAKE_USR/lib"
require_file "$FAKE_USR/bin/OpenSees"

OPS_PY_SO=$(find "$FAKE_USR/lib" -name "opensees.so" 2>/dev/null | head -n1 || true)
if [ -z "$OPS_PY_SO" ]; then
  echo "ERROR: No se encontró 'opensees.so' en $FAKE_USR" >&2
  exit 1
fi

echo "=========================================="
echo "PASO 1: Aislando librerías y binarios (jniLibs)"
echo "=========================================="
rm -rf "$FLAT_LIBS"
mkdir -p "$FLAT_LIBS"

find "$FAKE_USR/lib" -maxdepth 1 -name "*.so*" -exec cp -L {} "$FLAT_LIBS/" \;
find "$TMX_PREFIX/lib" -maxdepth 1 -name "libopenblas*.so*" -exec cp -L {} "$FLAT_LIBS/" \; 2>/dev/null || true
find "$TMX_PREFIX/lib" -maxdepth 1 -name "libgfortran*.so*" -exec cp -L {} "$FLAT_LIBS/" \; 2>/dev/null || true

cp -L "$FAKE_USR/bin/OpenSees" "$FLAT_LIBS/libopensees_cli.so"
chmod +x "$FLAT_LIBS/libopensees_cli.so"
cp -L "$OPS_PY_SO" "$FLAT_LIBS/opensees.so"

TCL_LIB_DIR=$(find "$FAKE_USR" "$TMX_PREFIX" -name "init.tcl" 2>/dev/null | head -n1 | xargs dirname || true)
if [ -n "$TCL_LIB_DIR" ]; then
    export TCL_LIBRARY="$TCL_LIB_DIR"
    echo "TCL_LIBRARY configurado en: $TCL_LIBRARY"
fi

echo "Directorio de librerías planas: $FLAT_LIBS"
echo "Total de librerías empaquetadas: $(find "$FLAT_LIBS" -maxdepth 1 -type f | wc -l)"

export LD_LIBRARY_PATH="$FLAT_LIBS:${LD_LIBRARY_PATH:-}"
export PYTHONPATH="$FLAT_LIBS:${PYTHONPATH:-}"

echo
echo "=========================================="
echo "PASO 2: Prueba con OpenSees TCL (libopensees_cli.so)"
echo "=========================================="
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

cat > cantilever.tcl << EOF_TCL
wipe
model BasicBuilder -ndm 2 -ndf 3
node 1 0.0 0.0
node 2 $LENGTH 0.0
fix 1 1 1 1
geomTransf Linear 1
element elasticBeamColumn 1 1 2 $AREA $E_MOD $IZ 1
pattern Plain 1 Linear {
    load 2 0.0 $P_LOAD 0.0
}
system BandGeneral
numberer RCM
constraints Plain
integrator LoadControl 1.0
algorithm Linear
analysis Static
analyze 1

set disp [nodeDisp 2 2]
set fileId [open "tcl_disp.txt" "w"]
puts \$fileId \$disp
close \$fileId
puts "TCL -> Análisis completado exitosamente."
exit
EOF_TCL

"$FLAT_LIBS/libopensees_cli.so" cantilever.tcl > tcl_execution.log 2>&1

if [ -f "tcl_disp.txt" ]; then
  TCL_DISP=$(cat tcl_disp.txt)
  echo "OK: OpenSees TCL ejecutado."
  echo "Desplazamiento vertical en nodo libre (TCL): $TCL_DISP m"
else
  echo "ERROR: Falló la ejecución de OpenSees TCL."
  cat tcl_execution.log
  exit 1
fi

echo
echo "=========================================="
echo "PASO 3: Prueba con OpenSeesPy (opensees.so)"
echo "=========================================="

python3.11 - << 'EOF_PY'
import sys
import os

# Asegurar entorno monolítico de un solo hilo antes de importar ops
os.environ["OMP_NUM_THREADS"] = "1"
os.environ["OPENBLAS_NUM_THREADS"] = "1"

import opensees as ops

# Reset y definición de modelo
ops.wipe()
ops.model('basic', '-ndm', 2, '-ndf', 3)

# Parámetros del modelo
length = 10.0
p_load = -100.0
E = 2.0e8
A = 0.01
I = 0.0001

# Geometría y Nodos
ops.node(1, 0.0, 0.0)
ops.node(2, length, 0.0)
ops.fix(1, 1, 1, 1)

# Elemento Viga
ops.geomTransf('Linear', 1)
# Sintaxis explicita para elasticBeamColumn 2D: (eleTag, iNode, jNode, A, E, Iz, transfTag)
ops.element('elasticBeamColumn', 1, 1, 2, float(A), float(E), float(I), 1)

# Cargas
ops.timeSeries('Linear', 1)
ops.pattern('Plain', 1, 1)
ops.load(2, 0.0, float(p_load), 0.0)

# Configuración del Solucionador
ops.system('BandGeneral')
ops.numberer('RCM')
ops.constraints('Plain')
ops.integrator('LoadControl', 1.0)
ops.algorithm('Linear')
ops.analysis('Static')

# Resolver
ops.analyze(1)

# Extraer resultados
disp_y = ops.nodeDisp(2, 2)
with open("py_disp.txt", "w") as f:
    f.write(f"{disp_y:.12g}")

print("Python -> Análisis completado exitosamente.")
EOF_PY

if [ -f "py_disp.txt" ]; then
  PY_DISP=$(cat py_disp.txt)
  echo "OK: OpenSeesPy ejecutado."
  echo "Desplazamiento vertical en nodo libre (Python): $PY_DISP m"
else
  echo "ERROR: Falló la ejecución de OpenSeesPy."
  exit 1
fi

echo
echo "=========================================="
echo "PASO 4: Validación de consistencia numérica"
echo "=========================================="

python3.11 - << 'EOF_VERIFY'
from pathlib import Path

tcl_val = float(Path('tcl_disp.txt').read_text().strip())
py_val = float(Path('py_disp.txt').read_text().strip())

# Valor teórico Euler-Bernoulli
P, L, E, I = -100.0, 10.0, 2.0e8, 0.0001
exact_val = (P * (L**3)) / (3.0 * E * I)

diff = abs(tcl_val - py_val)
error_rel = abs((tcl_val - exact_val) / exact_val) * 100.0

print(f"Resultado Teórico (Euler-Bernoulli) : {exact_val:.8f} m")
print(f"Resultado OpenSees TCL              : {tcl_val:.8f} m")
print(f"Resultado OpenSeesPy (Python)       : {py_val:.8f} m")
print(f"Diferencia TCL vs Python            : {diff:.2e}")
print(f"Error relativo al teórico           : {error_rel:.4f}%")

if diff < 1e-6:
    print("\n¡ÉXITO TOTAL! Ambos motores coinciden perfectamente.")
EOF_VERIFY

echo
echo "=========================================="
echo "RESUMEN FINAL"
echo "=========================================="
ls -lh "$TEST_DIR"/tcl_disp.txt "$TEST_DIR"/py_disp.txt "$FLAT_LIBS"/libopensees_cli.so "$FLAT_LIBS"/opensees.so
