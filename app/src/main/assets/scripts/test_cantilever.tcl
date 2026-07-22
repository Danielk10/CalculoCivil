# test_cantilever.tcl - Cantilever Beam Static Analysis
# Pre-installed test script for Structural & Seismic Research
# =========================================================

wipe
model BasicBuilder -ndm 2 -ndf 3

# --- Node Definition ---
# Node 1: Fixed support at origin
# Node 2: Free end at L=10m
node 1 0.0 0.0
node 2 10.0 0.0

# --- Boundary Conditions ---
fix 1 1 1 1

# --- Material & Section ---
# Steel A992: E = 200 GPa = 2.0e8 kPa
# Section: A = 0.01 m2, Iz = 0.0001 m4
geomTransf Linear 1
element elasticBeamColumn 1 1 2 0.01 2.0e8 0.0001 1

# --- Applied Load ---
# P = -100 kN vertical at free end
pattern Plain 1 Linear {
    load 2 0.0 -100.0 0.0
}

# --- Analysis Configuration ---
system BandGeneral
numberer RCM
constraints Plain
integrator LoadControl 1.0
algorithm Linear
analysis Static
analyze 1

# --- Results Output ---
puts "================================================"
puts "  CANTILEVER BEAM - STATIC ANALYSIS RESULTS"
puts "================================================"
puts ""
puts "Node 1 (Fixed):  Ux = [nodeDisp 1 1]  Uy = [nodeDisp 1 2]"
puts "Node 2 (Free):   Ux = [nodeDisp 2 1]  Uy = [nodeDisp 2 2]"
puts ""
puts "Theoretical Uy = PL^3/(3EI) = -100*10^3/(3*2e8*1e-4)"
puts "Theoretical Uy = -0.016667 m"
puts ""
puts "Analysis completed successfully."
exit
