# Registro de Soluciones y Parches (OpenSees - Android/Termux)

Este archivo contiene soluciones rápidas ("hotfixes") para problemas encontrados durante la compilación o ejecución de OpenSees en entornos específicos como Android/Termux.

## 1. Error: `unable to find library -lquadmath`

Si durante la etapa de enlace (linking) aparece el error:
`ld.lld: error: unable to find library -lquadmath`

### Solución:
Eliminar la referencia a `libquadmath` de los archivos de configuración generados por CMake en la carpeta `build`:

```bash
# Ejecutar desde la raíz de la carpeta 'build'
find . -name "link.txt" -exec sed -i 's/-lquadmath//g' {} +
find . -name "build.make" -exec sed -i 's/-lquadmath//g' {} +
```

*Nota: Esta solución es necesaria debido a la falta de `libquadmath` en los repositorios estándar de Termux y no afecta la funcionalidad principal del programa si no se requiere alta precisión cuádruple.*

## 2. Error de Enlace en Tiempo de Ejecución: `cannot find "libz.so.1"`

Al intentar ejecutar la Consola OpenSees en Android, puede aparecer el siguiente error:
`CANNOT LINK EXECUTABLE ".../libOpenSees.so": cannot find "libz.so.1" from verneed[1] in DT_NEEDED list for ".../libtcl8_6.so"`

### Causa:
La librería `libtcl8_6.so` fue compilada con una dependencia explícita a `libz.so.1`. Sin embargo, en el entorno Android, la librería de compresión del sistema se llama simplemente `libz.so` (sin el sufijo de versión `.1`).

### Solución:
Se debe parchear el binario de `libtcl8_6.so` para cambiar la referencia de `libz.so.1` a `libz.so` usando la herramienta `patchelf`.

**Pasos:**
1. Instalar `patchelf` en Termux (si se está compilando allí):
   ```bash
   pkg install patchelf
   ```
2. Aplicar el parche a la librería Tcl:
   ```bash
   patchelf --replace-needed libz.so.1 libz.so libtcl8_6.so
   ```
3. Alternativamente, si se tiene control sobre el proceso de empaquetado del APK, asegurarse de que `libtcl8_6.so` se enlace contra la versión del NDK que usa `libz.so`.
