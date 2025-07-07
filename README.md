![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-4285F4?style=for-the-badge&logo=android-studio&logoColor=white)
![XML Layouts](https://img.shields.io/badge/XML%20Layouts-FF9800?style=for-the-badge)
![Google Maps](https://img.shields.io/badge/Google%20Maps-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)
![View Binding](https://img.shields.io/badge/View%20Binding-0A7CFF?style=for-the-badge)
![Retrofit](https://img.shields.io/badge/Retrofit-2E8B57?style=for-the-badge)
![Coroutines](https://img.shields.io/badge/Kotlin%20Coroutines-0095D5?style=for-the-badge)
![Cisco](https://img.shields.io/badge/Cisco-Networking-blue?style=for-the-badge&logo=cisco&logoColor=white)
![Juniper](https://img.shields.io/badge/Juniper-Networking-005073?style=for-the-badge)



# UniTrackNet Android

**UniTrackNet** es una aplicación iOS de monitoreo y gestión de redes, diseñada para optimizar el control de enlaces y sesiones de routers utilizados por **UNINET**, especialmente aquellos que operan con los protocolos **BGP** y **OSPF** entre **Estados Unidos y México**.  
Está enfocada en la **detección temprana de fallas de red**, alertas en tiempo real y acciones proactivas desde una interfaz intuitiva para operadores.

---

## Objetivo del App

El objetivo principal de UniTrackNet iOS es:

- Proporcionar monitoreo en tiempo real de sesiones BGP/OSPF.
- Visualizar el estado de enlaces y sesiones activas en routers UNINET.
- Ofrecer acceso móvil al personal para una respuesta rápida y remota.
- Facilitar el monitoreo remoto desde cualquier ubicación, eliminando la dependencia de consolas físicas o herramientas de escritorio.
- Visualizar el estado actual de la red mediante paneles dinámicos y representaciones gráficas accesibles desde dispositivos
- Notificar de forma proactiva a los operadores ante degradaciones de servicio, desconexiones críticas o comportamientos fuera de lo esperado.
- Permitir el acceso seguro a la información de red a través de autenticación con credenciales y control de sesiones activas.
- Integrarse con sistemas existentes mediante interfaces seguras (API REST o WebSockets) adaptadas a la infraestructura interna de UNINET.
---

## Descripción del Logo

>El logo de UniTrackNet muestra una red de puntos conectados entre sí, representando cómo se comunican los routers en una red real.
>Las líneas grises simbolizan los enlaces entre dispositivos, mientras que los círculos verdes representan los nodos activos que están enviando o recibiendo información.

>Es un diseño simple pero moderno, pensado para transmitir conexión, tecnología y monitoreo constante. Además, los colores reflejan estabilidad y energía, dos cosas claves para el funcionamiento de una red confiable.

![unitracknet_icon_logo](https://github.com/user-attachments/assets/e04d1a55-741d-431e-ab16-ba1901af77b6)

## Justificación técnica

|                | Detalle                                                                                       |
|----------------|------------------------------------------------------------------------------------------------|
| **Dispositivo**| Teléfonos Android: portabilidad para el personal técnico.                                      |
| **Mínimo SDK** | **24 (Android 7.0 Nougat)** – mantiene compatibilidad amplia y permite API modernas.           |
| **Target SDK** | 35 (Android 14).                                                                               |
| **Orientación**| Solo **vertical (portrait)**: facilita el uso con una mano en campo.                           |
| **UI**         | **View Binding + XML** (no Jetpack Compose): interfaces programáticas y control granular.      |

---

## Credenciales de acceso

Estas credenciales te permiten acceder a una versión de prueba que simula el funcionamiento general del monitoreo de red:

```
Usuario: irjarqui
Contraseña: UnitrackNet
```

---

### Versión de producción

El acceso a la versión de producción está restringido por razones de seguridad y requiere lo siguiente:

- Conexión a través de **VPN corporativa**
- Códigos de acceso dinámicos generados al momento
- Credenciales individuales de acceso autorizadas

Si gustan en probar la versión de producción, por favor **contáctame directamente**.
 sobre todo por la cuestion de los codigos de acceso en tiempo real.


## Dependencias del proyecto

###  Navegación, red y parsing

- **Retrofit** – Cliente HTTP para conectar con APIs.
- **Gson Converter** – Conversión automática entre JSON y objetos Kotlin.
- **OkHttp Logging Interceptor** – Registro detallado de peticiones/respuestas HTTP.

###  UI, gráficos y diseño

- **Material Components (Material Design)** – Componentes visuales de Google.
- **Glide** – Carga eficiente de imágenes.
- **RoundedImageView** – Imágenes con esquinas redondeadas.
- **SpeedViewLib** – Gráfica de velocidad estilo velocímetro.
- **SwipeRefreshLayout** – Actualización de vistas mediante gestos.

###  Seguridad y autenticación

- **Security-Crypto** – Cifrado de preferencias compartidas.
- **Biometric API** – Soporte para autenticación biométrica (huella, rostro).
- **KeyStore** – Almacenamiento seguro de claves y tokens.

###  Geolocalización y mapas

- **Google Maps SDK** – Visualización de mapas.
- **Maps Utils** – Funciones adicionales para Google Maps.
- **Play Services Location** – Acceso a la ubicación con `FusedLocationProviderClient`.

###  Ciclo de vida y concurrencia

- **Kotlin Coroutines** – Programación asíncrona moderna.
- **Lifecycle KTX** – Corrutinas con scope de ciclo de vida.
- **WorkManager** – Tareas programadas en segundo plano.

###  Interfaz de usuario

- **XML Layouts** – Interfaces definidas en XML.
- **View Binding** – Acceso seguro y limpio a vistas.
- **SplashScreen API** – Pantalla de carga moderna.

## Autor

**Ivan Usiel Ramírez Jarquín**  
[GitHub: @IvanUsiel](https://github.com/IvanUsiel)  
Contacto directo: usiel_jarquin@outlook.com; irjarqui@uninet.com.mx;

---
