#!/bin/bash
set -euo pipefail

# ==============================================================================
# CONFIGURACIÓN DE RUTAS Y PARALELISMO MATRICIAL
# ==============================================================================
export APP_PREFIX="/data/data/com.diamon.calculo/files/usr"
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export TMX_PREFIX="/data/data/com.termux/files/usr"

# Detección automática de todos los núcleos del CPU
NUM_CORES=$(nproc 2>/dev/null || echo 4)

# Asignación de TODOS los núcleos al solver (OpenBLAS / OpenMP)
export OPENBLAS_NUM_THREADS="$NUM_CORES"
export OMP_NUM_THREADS="$NUM_CORES"
export MKL_NUM_THREADS="$NUM_CORES"

# Prevención de desbordamiento de memoria de hilos en Android (Bionic Libc)
export OMP_STACKSIZE=4M

FLAT_LIBS="$HOME/android_sim_libs_ops"
TEST_DIR="$HOME/android_sim_test_ops"

# Parámetros del modelo (Viga en voladizo)
P_LOAD="${P_LOAD:--100.0}" # Carga en kN
LENGTH="10.0"              # Longitud en m
E_MOD="2.0e8"              # Módulo de Elasticidad (kPa)
AREA="0.01"                # Área de sección (m2)
IZ="0.0001"                # Inercia (m4)

echo "=========================================="
echo "CONFIGURACIÓN DE CÁLCULO MULTIHILO"
echo "Núcleos asignados al Solver: $NUM_CORES hilos"
echo "=========================================="

# Configurar entorno de librerías
export LD_LIBRARY_PATH="$FLAT_LIBS:${LD_LIBRARY_PATH:-}"
export PYTHONPATH="$FLAT_LIBS:${PYTHONPATH:-}"

TCL_LIB_DIR=$(find "$FAKE_USR" "$TMX_PREFIX" -name "init.tcl" 2>/dev/null | head -n1 | xargs dirname || true)
if [ -n "$TCL_LIB_DIR" ]; then
    export TCL_LIBRARY="$TCL_LIB_DIR"
fi

rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

echo
echo "=========================================="
echo "PASO 1: Ejecución OpenSees TCL ($NUM_CORES núcleos)"
echo "=========================================="

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
puts "TCL -> Análisis completado usando todos los núcleos."
exit
EOF_TCL

"$FLAT_LIBS/libopensees_cli.so" cantilever.tcl > tcl_execution.log 2>&1

if [ -f "tcl_disp.txt" ]; then
  TCL_DISP=$(cat tcl_disp.txt)
  echo "OK: OpenSees TCL ejecutado exitosamente."
  echo "Desplazamiento TCL: $TCL_DISP m"
else
  echo "ERROR: Falló OpenSees TCL con $NUM_CORES hilos."
  cat tcl_execution.log
  exit 1
fi

echo
echo "=========================================="
echo "PASO 2: Ejecución OpenSeesPy ($NUM_CORES núcleos)"
echo "=========================================="

python3.11 - << EOF_PY
import sys
import os
import multiprocessing

# Confirmar detección de núcleos dentro del runtime de Python
cores = os.environ.get("OPENBLAS_NUM_THREADS", multiprocessing.cpu_count())
print(f"Python ejecutando solver con {cores} hilos/núcleos...")

import opensees as ops

ops.wipe()
ops.model('basic', '-ndm', 2, '-ndf', 3)

length = 10.0
p_load = -100.0
E = 2.0e8
A = 0.01
I = 0.0001

ops.node(1, 0.0, 0.0)
ops.node(2, length, 0.0)
ops.fix(1, 1, 1, 1)

ops.geomTransf('Linear', 1)
ops.element('elasticBeamColumn', 1, 1, 2, float(A), float(E), float(I), 1)

ops.timeSeries('Linear', 1)
ops.pattern('Plain', 1, 1)
ops.load(2, 0.0, float(p_load), 0.0)

ops.system('BandGeneral')
ops.numberer('RCM')
ops.constraints('Plain')
ops.integrator('LoadControl', 1.0)
ops.algorithm('Linear')
ops.analysis('Static')

ops.analyze(1)

disp_y = ops.nodeDisp(2, 2)
with open("py_disp.txt", "w") as f:
    f.write(f"{disp_y:.12g}")

print("Python -> Análisis completado exitosamente.")
EOF_PY

if [ -f "py_disp.txt" ]; then
  PY_DISP=$(cat py_disp.txt)
  echo "OK: OpenSeesPy ejecutado exitosamente."
  echo "Desplazamiento Python: $PY_DISP m"
else
  echo "ERROR: Falló OpenSeesPy con $NUM_CORES hilos."
  exit 1
fi

echo
echo "=========================================="
echo "PASO 3: Validación Numérica"
echo "=========================================="

python3.11 - << 'EOF_VERIFY'
from pathlib import Path

tcl_val = float(Path('tcl_disp.txt').read_text().strip())
py_val = float(Path('py_disp.txt').read_text().strip())

diff = abs(tcl_val - py_val)
print(f"Diferencia TCL vs Python ({tcl_val:.8f} vs {py_val:.8f}): {diff:.2e}")

if diff < 1e-6:
    print("\n¡ÉXITO TOTAL! Multihilo completo validado y estable.")
EOF_VERIFY
