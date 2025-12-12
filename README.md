#  UPSGlam 2.0 — Backend (Spring WebFlux)

Backend reactivo no bloqueante para la app de edición fotográfica **UPSGlam 2.0**, construido con **Spring WebFlux**, integrado con:

- 🔐 **Autenticación**: Firebase Authentication (email/password + Google Sign-In)  
- ☁️ **Almacenamiento y DB**: Supabase (Storage + PostgREST)  
- 🖼️ **Procesamiento de imágenes**: API externa en PyCUDA/Flask (`/procesar`)  
- ⚡ **Reactividad**: Flujo totalmente no bloqueante con `Mono` y `Flux`

> Diseñado para alta concurrencia, escalabilidad y baja latencia.

---

##  Arquitectura General

```text
UPSGlam Backend (Spring WebFlux)
│
├──  Auth Flow (Firebase)
│   ├── Registro (email + pass + foto opcional)
│   ├── Login (email/pass)
│   └── Google Sign-In (id_token → UID + metadata)
│
├── Image Processing
│   └── `/api/imagen/procesar` → reenvía a `http://localhost:5000/procesar` (PyCUDA)
│
├──  Posts & Media
│   └── `/posts/upload` → sube a Supabase Storage (`posts/`) y registra metadata en DB
│
├──  Perfiles
│   └── Sube fotos de perfil a Supabase Storage (`profiles/`)  
│       y guarda URL en tabla `user_profile_images`
│
└──  Integraciones externas
    ├── Firebase Admin SDK (validación de tokens)
    ├── Supabase (Storage + REST API)
    └── Flask + PyCUDA (GPU image filters)
```

---

##  Estructura del Proyecto

```text
src/main/java/com.upsglam/
│
├── config/
│   ├── FirebaseConfig.java        # Inicialización de Firebase Admin SDK
│   ├── SecurityConfig.java        # Deshabilita CSRF, permite todos los endpoints (modo dev)
│   ├── SupabaseConfig.java        # Configuración y WebClient para Supabase
│   ├── WebClientConfig.java       # WebClient para comunicación con Flask (`flaskWebClient`)
│   └── WebFluxCorsConfig.java     # CORS abierto (*), ideal para desarrollo
│
├── controller/
│   ├── AuthController.java        # /auth/register, /login, /google, /verify
│   ├── ImageController.java       # /api/imagen/procesar → delega a PyCUDA
│   └── PostController.java        # /posts/upload
│
├── service/
│   ├── FirebaseAuthService.java   # Registro/Login/Google/Firebase Token verify
│   ├── ImageProcessingClient.java # Cliente para API de PyCUDA (multipart/form-data)
│   ├── PostService.java           # Subida de posts a Supabase Storage
│   ├── SupabaseStorageService.java# Subida genérica (legacy — no usada en flujo actual)
│   └── UserProfileService.java    # Gestión fotos de perfil + DB
│
├── dto/
│   └── *.java                     # DTOs: LoginRequest, GoogleSignInRequest, etc.
│
└── UpsglamBackendApplication.java # Punto de entrada

```

---

## Configuración Requerida (application.yml)

```
app:
  firebase:
    service-account-path: "classpath:firebase-service-account.json"
    api-key: "AIzaSyA...tu_api_key_de_firebase"
    identitytoolkit-url: "https://identitytoolkit.googleapis.com/v1"

  supabase:
    url: "https://tu-proyecto.supabase.co"
    anon-key: "eyJh...tu_anon_key"
    service-role-key: "eyJh...tu_service_role_key"  # Se recomienda tener cuidado con las claves
    storage-bucket: "posts"
    profile-bucket: "profiles"

# Recursos externos
# └── src/main/resources/firebase-service-account.json ← archivo JSON de Firebase
# └── marcos/ ← debe existir en el **servidor Flask**

```

---


## Endpoints 

1. Autenticación

```

| Método | Endpoint        | Descripción                                           |
|--------|----------------|-------------------------------------------------------|
| POST   | /auth/register  | Registro con email, contraseña, displayName y foto opcional (multipart) |
| POST   | /auth/login     | Login con email + contraseña                          |
| POST   | /auth/google    | Login con Google (recibe idToken del frontend)       |
| GET    | /auth/verify    | Verifica token JWT en header `Authorization: Bearer <idToken>` |

```

2. ImageProcessingClient

```
   
| Método | Endpoint  | Descripción                                                                                                                   |
| ------ | --------- | ----------------------------------------------------------------------------------------------------------------------------- |
| POST   | /procesar | Envía una imagen y parámetros de filtro al microservicio Flask para procesamiento GPU y retorna la imagen procesada en bytes. |

```


4. Procesamiento de imagenes

```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET    | /rest/v1/posts | Obtiene todas las publicaciones con campos `id`, `user_uid`, `caption`, `public_url` y `created_at`. Orden descendente por fecha de creación. Requiere `apikey` y `Authorization: Bearer <service-role-key>` |

```

4. Publicaciones


```
| Método | Endpoint       | Descripción                                               |
|--------|----------------|-----------------------------------------------------------|
| POST   | /posts/upload  | Sube imagen + caption + uid. Retorna URL pública en Supabase. |

```


---

## Despliegue

**Requisitos**

- Java 17+
- Maven o Gradle
- Servidor Flask con PyCUDA corriendo en http://localhost:5000 
- Firebase: proyecto activo + archivo service-account.json en src/main/resources/
- Supabase: proyecto con buckets posts y profiles creados + tabla user_profile_images(user_uid UUID, profile_url TEXT)

---


