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
