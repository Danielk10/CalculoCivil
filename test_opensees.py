import opensees

print("====================================")
print("Probando OpenSeesPy (Python)...")
print("====================================")

opensees.wipe()
opensees.model('basic', '-ndm', 2, '-ndf', 2)
opensees.node(1, 0.0, 0.0)
opensees.node(2, 0.0, 10.0)
opensees.fix(1, 1, 1)

print("Nodos creados exitosamente.")
print("¡El módulo de OpenSeesPy funciona perfectamente!")
