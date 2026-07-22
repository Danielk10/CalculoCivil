# test_portal_frame.py - Portal Frame Modal Analysis
# Pre-installed test script for Structural & Seismic Research
# ===========================================================

import opensees as ops
import math

ops.wipe()
ops.model('basic', '-ndm', 2, '-ndf', 3)

# --- Node Definition ---
# 2D Portal Frame: 5m span, 3m height
ops.node(1, 0.0, 0.0)   # Base left
ops.node(2, 5.0, 0.0)   # Base right
ops.node(3, 5.0, 3.0)   # Top right
ops.node(4, 0.0, 3.0)   # Top left

# --- Boundary Conditions ---
ops.fix(1, 1, 1, 1)  # Fixed base left
ops.fix(2, 1, 1, 1)  # Fixed base right

# --- Material & Section ---
# Concrete columns: E = 30 GPa = 3.0e7 kPa
# Section: 300x300mm -> A = 0.09 m2, Iz = 6.75e-4 m4
ops.geomTransf('Linear', 1)

# Columns
ops.element('elasticBeamColumn', 1, 1, 4, 0.09, 3.0e7, 6.75e-4, 1)
ops.element('elasticBeamColumn', 2, 2, 3, 0.09, 3.0e7, 6.75e-4, 1)

# Beam
ops.element('elasticBeamColumn', 3, 4, 3, 0.09, 3.0e7, 6.75e-4, 1)

# --- Eigenvalue Analysis ---
numModes = 3
eigenValues = ops.eigen(numModes)

# --- Output Results ---
print("=" * 52)
print("  PORTAL FRAME - MODAL ANALYSIS RESULTS")
print("=" * 52)
print("")
print(f"{'Mode':>6}  {'Period T (s)':>14}  {'Freq f (Hz)':>14}  {'omega (rad/s)':>14}")
print("-" * 52)

for i, ev in enumerate(eigenValues):
    omega = math.sqrt(ev)
    T = 2.0 * math.pi / omega
    f = 1.0 / T
    print(f"{i+1:>6}  {T:>14.6f}  {f:>14.4f}  {omega:>14.4f}")

print("")
print("Analysis completed successfully.")
