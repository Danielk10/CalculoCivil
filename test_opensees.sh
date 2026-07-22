#!/bin/bash
set -euo pipefail

# ==============================================================================
# CONFIGURACIÓN DE RUTAS Y VARIABLES
# ==============================================================================
export APP_PREFIX="/data/data/com.diamon.calculo/files/usr"
export DESTDIR="$HOME/fake_root"
export FAKE_USR="$DESTDIR$APP_PREFIX"
export TMX_PREFIX="/data/data/com.termux/files/usr"

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

require_cmd python3
require_cmd find
require_cmd cp
require_cmd grep
require_cmd ls

require_file "$FAKE_USR/lib"
require_file "$FAKE_USR/bin/OpenSees"

# Buscar dinámicamente opensees.so en fake_root
OPS_PY_SO=$(find "$FAKE_USR/lib" -name "opensees.so" 2>/dev/null | head -n1 || true)
if [ -z "$OPS_PY_SO" ]; then
  echo "ERROR: No se encontró el módulo Python 'opensees.so' en $FAKE_USR" >&2
  exit 1
fi

echo "=========================================="
echo "PASO 1: Aislando librerías y binarios (jniLibs)"
echo "=========================================="
rm -rf "$FLAT_LIBS"
mkdir -p "$FLAT_LIBS"

# Copiar todas las librerías dinámicas compartidas de fake_root y Termux
find "$FAKE_USR/lib" -maxdepth 1 -name "*.so*" -exec cp -L {} "$FLAT_LIBS/" \;
find "$TMX_PREFIX/lib" -maxdepth 1 -name "libopenblas*.so*" -exec cp -L {} "$FLAT_LIBS/" \; 2>/dev/null || true
find "$TMX_PREFIX/lib" -maxdepth 1 -name "libgfortran*.so*" -exec cp -L {} "$FLAT_LIBS/" \; 2>/dev/null || true

# Copiar ejecutable Tcl renombrado como librería de simulación CLI
cp -L "$FAKE_USR/bin/OpenSees" "$FLAT_LIBS/libopensees_cli.so"
chmod +x "$FLAT_LIBS/libopensees_cli.so"

# Copiar el módulo de Python
cp -L "$OPS_PY_SO" "$FLAT_LIBS/opensees.so"

# Localizar init.tcl para Tcl
TCL_LIB_DIR=$(find "$FAKE_USR" "$TMX_PREFIX" -name "init.tcl" 2>/dev/null | head -n1 | xargs dirname || true)
if [ -n "$TCL_LIB_DIR" ]; then
    export TCL_LIBRARY="$TCL_LIB_DIR"
    echo "TCL_LIBRARY configurado en: $TCL_LIBRARY"
fi

echo "Directorio de librerías planas: $FLAT_LIBS"
echo "Total de librerías empaquetadas: $(find "$FLAT_LIBS" -maxdepth 1 -type f | wc -l)"

# Configurar entorno aislado
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

# Nodos: 1 (Empotrado en 0,0), 2 (Libre en L,0)
node 1 0.0 0.0
node 2 $LENGTH 0.0

# Restricciones
fix 1 1 1 1

# Transformación Geométrica y Elemento
geomTransf Linear 1
element elasticBeamColumn 1 1 2 $AREA $E_MOD $IZ 1

# Carga Puntual en extremo
pattern Plain 1 Linear {
    load 2 0.0 $P_LOAD 0.0
}

# Configuración del Análisis
system BandGeneral
numberer RCM
constraints Plain
integrator LoadControl 1.0
algorithm Linear
analysis Static

# Resolver
analyze 1

# Exportar resultados
set disp [nodeDisp 2 2]
set fileId [open "tcl_disp.txt" "w"]
puts \$fileId \$disp
close \$fileId

puts "TCL -> Análisis completado exitosamente."
exit
EOF_TCL

# Ejecutar ejecutable Tcl en entorno aislado
"$FLAT_LIBS/libopensees_cli.so" cantilever.tcl > tcl_execution.log 2>&1

if [ -f "tcl_disp.txt" ]; then
  TCL_DISP=$(cat tcl_disp.txt)
  echo "OK: OpenSees TCL ejecutado."
  echo "Desplazamiento vertical en nodo libre (TCL): $TCL_DISP m"
else
  echo "ERROR: Falló la ejecución de OpenSees TCL. Revisa tcl_execution.log"
  cat tcl_execution.log
  exit 1
fi

echo
echo "=========================================="
echo "PASO 3: Prueba con OpenSeesPy (opensees.so)"
echo "=========================================="

python3 - << 'EOF_PY'
import sys
import opensees as ops

# 1. Definir el modelo con sintaxis explícita de OpenSeesPy
ops.model('basic', '-ndm', 2, '-ndf', 3)

# Parámetros
length = 10.0
p_load = -100.0
E = 2.0e8
A = 0.01
I = 0.0001

# Geometría y Condiciones de Borde
ops.node(1, 0.0, 0.0)
ops.node(2, length, 0.0)
ops.fix(1, 1, 1, 1)

# Transformación y Elemento
ops.geomTransf('Linear', 1)
ops.element('elasticBeamColumn', 1, 1, 2, A, E, I, 1)

# Cargas
ops.timeSeries('Linear', 1)
ops.pattern('Plain', 1, 1)
ops.load(2, 0.0, p_load, 0.0)

# Análisis
ops.system('BandGeneral')
ops.numberer('RCM')
ops.constraints('Plain')
ops.integrator('LoadControl', 1.0)
ops.algorithm('Linear')
ops.analysis('Static')

# Resolver
ops.analyze(1)

# Guardar resultado
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

python3 - << 'EOF_VERIFY'
import math
from pathlib import Path

tcl_val = float(Path('tcl_disp.txt').read_text().strip())
py_val = float(Path('py_disp.txt').read_text().strip())

# Valor analítico teórico v = (P * L^3) / (3 * E * I)
P = -100.0
L = 10.0
E = 2.0e8
I = 0.0001
exact_val = (P * (L**3)) / (3.0 * E * I)

diff = abs(tcl_val - py_val)
error_rel = abs((tcl_val - exact_val) / exact_val) * 100.0

print(f"Resultado Teórico (Fórmula Euler-Bernoulli) : {exact_val:.8f} m")
print(f"Resultado OpenSees TCL                     : {tcl_val:.8f} m")
print(f"Resultado OpenSeesPy (Python)              : {py_val:.8f} m")
print(f"Diferencia entre TCL y Python              : {diff:.2e}")
print(f"Error relativo respecto al teórico          : {error_rel:.4f}%")

if diff < 1e-6:
    print("\n¡ÉXITO TOTAL! Ambas interfaces generan resultados idénticos.")
else:
    print("\nADVERTENCIA: Hay discrepancias entre TCL y Python.")
EOF_VERIFY

echo
echo "=========================================="
echo "RESUMEN FINAL"
echo "=========================================="
ls -lh "$TEST_DIR"/tcl_disp.txt "$TEST_DIR"/py_disp.txt "$FLAT_LIBS"/libopensees_cli.so "$FLAT_LIBS"/opensees.so
