#!/bin/bash
# preparar_opensees.sh

echo "==============================================="
echo "Preparando dependencias efímeras de OpenSees..."
echo "==============================================="

# Añadir el PPA de Python silenciosamente para que la descarga de 3.11 no falle
sudo add-apt-repository -y ppa:deadsnakes/ppa
sudo apt-get update

# Instalar las librerías matemáticas y Python
sudo apt-get install -y python3.11 python3.11-venv python3.11-dev liblapack-dev libopenmpi-dev libmkl-rt libmkl-blacs-openmpi-lp64 libscalapack-openmpi-dev tcl-dev tk-dev libeigen3-dev

echo "==============================================="
echo "¡Dependencias del sistema instaladas correctamente!"
echo "Ahora puedes activar tu entorno permanente y usar OpenSees:"
echo "source ~/opensees-env/bin/activate"
echo "python"
echo ">>> import opensees"
echo "==============================================="
