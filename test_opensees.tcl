puts "===================================="
puts "Probando OpenSees Tcl Nativo..."
puts "===================================="
wipe
model BasicBuilder -ndm 2 -ndf 2
node 1 0.0 0.0
node 2 0.0 10.0
fix 1 1 1
puts "Nodos creados exitosamente."
puts "¡El intérprete Tcl de OpenSees funciona perfectamente!"
