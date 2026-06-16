# Plan de Optimización: Infraestructura Runtime (Structural and Seismic Research)

Este documento detalla las mejoras sugeridas para alinear la robustez técnica de **Structural and Seismic Research** con los estándares implementados en **Flash-EEPROM-Tool**.

## 1. Automatización de Enlaces (Mapeo Inteligente)
**Problema actual:** En `AssetHelper.java`, los enlaces simbólicos se crean manualmente línea por línea (ej. `libz.so.1` -> `libz_so_1.so`).
**Sugerencia:** Implementar la lógica de "Candidatos Nativos" de Flash-EEPROM-Tool.
- **Beneficio:** Permite que el código detecte automáticamente si el archivo en `jniLibs` usa puntos (`.so.1`) o guiones bajos (`_so_1.so`).
- **Código a adoptar:**
  ```java
  private static List<String> getNativeCandidates(String runtimeSoname) {
      Set<String> candidates = new LinkedHashSet<>();
      candidates.add(runtimeSoname);
      int soMarker = runtimeSoname.indexOf(".so.");
      if (soMarker > 0) {
          String base = runtimeSoname.substring(0, soMarker);
          String suffix = runtimeSoname.substring(soMarker + 4).replace('.', '_');
          candidates.add(base + "_" + suffix + ".so");
      }
      return new ArrayList<>(candidates);
  }
  ```

## 2. Validación de Integridad Post-Enlace
**Problema actual:** Si un `symlink` falla o una librería falta en el APK, la app solo lanza un error genérico al intentar ejecutar OpenSees.
**Sugerencia:** Implementar el método `ensurePresent(File file)` al final de la configuración.
- **Beneficio:** Permite registrar en el Logcat exactamente qué librería falta antes de que el motor de cálculo falle, facilitando el soporte técnico.

## 3. Unificación de Documentación (Matriz de Runtime)
**Problema actual:** La documentación actual es un reporte de dependencias básico.
**Sugerencia:** Evolucionar el `REPORTE_DEPENDENCIAS_BINARIOS.md` hacia una `MATRIZ_RUNTIME.md`.
- **Contenido:** Incluir no solo qué librerías hay, sino qué binario *necesita* a cuál (basado en la auditoría `readelf` que realizamos).

## 4. Gestión de Caché de Extracción
**Problema actual:** `AssetHelper` usa una clave de SharedPreferences básica.
**Sugerencia:** Usar una clave versionada (ej. `assets_extracted_v2`).
- **Beneficio:** Si actualizas los binarios en una nueva versión de la app, cambiar la clave forzará una re-extracción limpia, evitando que queden librerías viejas o corruptas en el almacenamiento interno del usuario.

---
**Veredicto de Consistencia:**
El patrón de diseño actual en **Structural and Seismic Research** es **Correcto y Válido**. Estas sugerencias son optimizaciones de "calidad de vida" y robustez que harán el mantenimiento a largo plazo mucho más sencillo, siguiendo el éxito ya comprobado en tus otros proyectos.
