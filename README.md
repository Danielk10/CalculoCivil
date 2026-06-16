# CalculoCivil

**CalculoCivil** es una aplicación de cálculo estructural para Android, basada en el potente motor de simulación **OpenSees** utilizando la interfaz de desarrollo nativo de Android (**NDK**) con integración **Java y C++ (JNI)**.

---

# README / LICENSE DECLARATION

## 🏛️ Proyecto basado en OpenSees - Declaración de Licencia y Uso

Este repositorio contiene mi **código Android independiente** (licenciado bajo **Apache 2.0**) que utiliza el núcleo de **OpenSees** como motor de cálculo estructural. Este proyecto está desarrollado **exclusivamente para investigación personal y uso interno**.

***

## ⚠️ Declaración de Cumplimiento con la Licencia de OpenSees

Declaro explícitamente que:

1.  **No soy una institución educativa, de investigación o sin fines de lucro** reconocido.
2.  Este proyecto es **para investigación personal y uso interno exclusivo**.
3.  **NO voy a publicar ni distribuir APKs** en GitHub, Google Play, ni en ningún otro lugar.
4.  **Solo licencio mi código Android independiente** (Apache 2.0). El código de OpenSees **se mantiene bajo su licencia original** de UC Berkeley.
5.  Este proyecto **no es comercial** y **no genera ingresos** (sin anuncios, sin venta, sin monetización).

Esto cumple con la **parte (b)** de la licencia oficial de OpenSees:

> *"use, reproduction and modification of this software by other entities for internal purposes only"*

**NO hay redistribución de binarios/APKs**, solo código fuente en este repositorio.

***

## 📄 Licencias de los Componentes

Este proyecto integra las siguientes librerías de código abierto. Todas tienen licencias permisivas (BSD/MIT/Apache), **excepto OpenSees** que tiene su licencia especial de UC Berkeley:

| Componente | Propósito | Licencia | Página Oficial |
| :--- | :--- | :--- | :--- |
| **OpenSees** | Núcleo de análisis estructural (FEA) | **UC Berkeley License** | [https://opensees.berkeley.edu](https://opensees.berkeley.edu) |
| **OpenBLAS** | Álgebra lineal optimizada (BLAS) | **BSD 3-Clause** | [https://www.openblas.net](https://www.openblas.net) |
| **Tcl/Tk** | Scripting y comandos | **BSD-like** | [https://www.tcl.tk](https://www.tcl.tk) |
| **BLAS (Netlib)** | Álgebra lineal básica | **Reference BLAS** | [https://www.netlib.org/blas/](https://www.netlib.org/blas/) |
| **ARPACK** | Problemas de autovalores | **Permisiva** | [https://www.netlib.org/arpack/](https://www.netlib.org/arpack/) |
| **SuperLU** | Solución de sistemas lineales dispersos | **BSD-like** | [http://superlu.cs.lbl.gov](http://superlu.cs.lbl.gov) |
| **UMFPACK** | Factorización LU dispersa | **GPL** | [http://www.cise.ufl.edu/research/sparse/umfpack](http://www.cise.ufl.edu/research/sparse/umfpack) |
| **HDF5** | I/O de datos científicos | **BSD-like** | [https://www.hdfgroup.org](https://www.hdfgroup.org) |
| **Mi código Android** | Aplicación Android NDK (Java/C++) | **Apache 2.0** | [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0) |

***

## 🔒 Licencia de OpenSees

El código de OpenSees incluido en este repositorio se mantiene bajo la **licencia oficial de UC Berkeley**:

> **Copyright © The Regents of the University of California.**
> All rights reserved.
>
> The Regents grants permission, without fee and without a written license agreement, for:
>
> **(a)** use, reproduction, modification, and distribution of this software and its documentation by **educational, research, and non-profit entities for noncommercial purposes only**; and
>
> **(b)** use, reproduction and modification of this software by **other entities for internal purposes only**.
>
> The above copyright notice and this permission notice shall appear in all copies and modifications of the software and/or documentation.
>
> Permission to incorporate this software into products for commercial distribution may be obtained by contacting the **University of California Office of Technology Licensing**.

***

## 📌 Aviso de Copyright de UC Berkeley

```
Copyright © The Regents of the University of California.
All rights reserved.

OpenSees - Open System for Earthquake Engineering Simulation
Pacific Earthquake Engineering Research (PEER) Center
University of California, Berkeley
```

***

## 🏗️ Separación Arquitectónica

Para garantizar la integridad de las licencias, el proyecto se estructura bajo una separación clara:

1.  **Capa Nativa (C++/Fortran):** El núcleo de OpenSees se compila de forma independiente como una librería dinámica o estática (`.so` / `.a`). Esta parte del código es la que está sujeta a la Licencia de UC Berkeley.
2.  **Capa de Aplicación Android (Java/Kotlin/JNI):** El código que interactúa con la interfaz de usuario, gestiona el ciclo de vida de la actividad y carga la librería nativa es **totalmente independiente** del código fuente de OpenSees. Este es el código licenciado bajo **Apache 2.0**.
3.  **Integración:** La comunicación entre ambas capas se realiza exclusivamente a través de la Interfaz Nativa de Java (JNI). No existe una mezcla de propiedad intelectual que modifique la licencia del núcleo de OpenSees.

***

## 🚫 Lo que NO haré

Para evitar violación de la licencia de OpenSees, **NO**:

- ❌ Publicaré APKs en GitHub o Google Play.
- ❌ Distribuiré binarios compilados de OpenSees.
- ❌ Monetizaré este proyecto (sin anuncios, sin venta, sin ingresos).
- ❌ Usaré el nombre de UC Berkeley para promoción sin permiso.
- ❌ Venderé este producto como parte de software comercial.

***

## ✅ Lo que VOY a hacer

- ✅ Mantener este proyecto como **uso interno exclusivo** (sin redistribución).
- ✅ Documentar claramente que es **para investigación personal y uso no comercial**.
- ✅ Incluir el **texto completo de la licencia de OpenSees**.
- ✅ Mantener los **avisos de copyright de UC Berkeley**.
- ✅ Licenciar mi código Android independiente bajo **Apache 2.0**.

***

## 📞 Contacto para Licencia Comercial

Si en el futuro deseo distribuir APKs o monetizar este proyecto, contactaré a:

**University of California Office of Technology Licensing**
2150 Shattuck Avenue #510, Berkeley, CA 94720-1620
(510) 643-7201
[https://otl.berkeley.edu](https://otl.berkeley.edu)

---

## 🛠️ Especificaciones Técnicas y Versiones

El proyecto está configurado con las siguientes versiones de Android y herramientas de compilación:

*   **SDK de Compilación (Compile SDK):** API 37 (Android 15+)
*   **SDK Objetivo (Target SDK):** API 37
*   **SDK Mínimo (Min SDK):** API 23 (Android 6.0 Marshmallow+)
*   **Versión de NDK:** `30.0.14904198`
*   **Versión de CMake:** `4.1.2`
*   **Versión de Gradle:** `9.5.1` (con Android Gradle Plugin 9.2.1)
*   **Nombre de Paquete (Package Name):** `com.diamon.calculo`
*   **Nombre de la Librería Nativa:** `libcalculocivil.so` (Cargada como `System.loadLibrary("calculocivil")`)

---

## 📂 Estructura Principal del Proyecto

*   **[app/](app)**: Código fuente de la aplicación Android.
*   **[OpenSees/](OpenSees)**: Repositorio y código fuente del motor OpenSees.
*   **[fake_root/](fake_root)**: Directorio original simulado de almacenamiento interno.

---

## 🚀 Configuración y Construcción

Para configurar el entorno de compilación, sigue estos pasos:

### 1. Configurar el SDK de Android
```bash
chmod +x setup-sdk.sh
./setup-sdk.sh
```

### 2. Compilar
```bash
./gradlew assembleDebug
```

---

## 📚 Documentación Adicional
- **[guia_desarrollo_calculocivil.md](guia_desarrollo_calculocivil.md)**
- **[resumen_proyecto_calculocivil.md](resumen_proyecto_calculocivil.md)**
- **[guia_uso_sdk.md](guia_uso_sdk.md)**

---

## Autor

**Daniel Diamon**
Tinaquillo, Cojedes, Venezuela
Desarrollador independiente
