import subprocess
import os
import shutil

jni_dir = 'app/src/main/jniLibs/arm64-v8a'
libs_externas = 'app/src/main/jniLibs/libs_externas'
os.makedirs(libs_externas, exist_ok=True)

files = [f for f in os.listdir(jni_dir) if f.endswith('.so') or f == 'OpenSees']

with open('REPORTE_ANALISIS_DEPENDENCIAS.md', 'w') as r:
    r.write('# Reporte de Dependencias (Android ARM64)\n\n')
    
    for f in sorted(files):
        r.write(f'### {f}\n| Dep | Tipo | Disponible en JNI |\n|---|---|---|\n')
        
        try:
            # Usar ldd o readelf para obtener dependencias
            # ldd no siempre funciona en Android, usaremos readelf
            out = subprocess.check_output(['readelf', '-d', os.path.join(jni_dir, f)], text=True)
            for line in out.splitlines():
                if '(NEEDED)' in line:
                    d = line.split('[')[1].split(']')[0]
                    # Excluir librerías del sistema Android
                    is_system = d in ['libc.so', 'libm.so', 'libdl.so', 'liblog.so', 'libstdc++.so', 'libz.so']
                    
                    in_jni = os.path.exists(os.path.join(jni_dir, d))
                    
                    r.write(f'| {d} | {'Sistema' if is_system else 'Externa'} | {'Sí' if in_jni else 'No'} |\n')
                    
                    # Si no está en JNI y es externa, intentar buscarla para moverla
                    if not is_system and not in_jni:
                        # Buscar en el sistema o fake_root (aquí simplificado)
                        print(f"Buscando dependencia faltante: {d}")
        except Exception as e:
            r.write(f'| Error al analizar | - | - |\n')
        r.write('\n')

print("Reporte generado: REPORTE_ANALISIS_DEPENDENCIAS.md")
