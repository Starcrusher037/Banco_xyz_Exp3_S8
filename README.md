# Banco XYZ — Arquitectura Cloud Resiliente, Mensajería JMS, OAuth 2.0 (GitHub + JWT RSA) y Docker Compose

### Asignatura: Desarrollo Backend III (PBY2203)
* **Profesor:** Marcelo Zepeda  
* **Estudiantes:** Leonardo Bustamante — Carolina Delgado  
* **Institución:** Duoc UC  
* **Documento Técnico Complementario:** [PROPUESTA_TECNICA.md](file:///C:/Users/kk638/Desktop/Exp3_S8_Grupo3-main/PROPUESTA_TECNICA.md)

---

## 1. Objetivo del Proyecto

El objetivo de esta solución es implementar una plataforma bancaria distribuida moderna, segura, tolerante a fallos y orientada a eventos para el **Banco XYZ**, preparada para su despliegue en entornos Cloud.

El proyecto consolida la continuidad del desarrollo tecnológico abordando:
1. **Seguridad con Spring Cloud Security y OAuth 2.0:** Autenticación federada de usuarios delegada en GitHub y emisión de tokens de acceso JSON Web Token (JWT) autofirmados con par de claves asimétricas RSA de 2048 bits (RS256).
2. **Servidor de Recursos (Resource Server):** Protección descentralizada de las APIs bancarias mediante validación de claves públicas vía endpoint JWKS (`/.well-known/jwks.json`).
3. **Tolerancia a Fallos con Resilience4j:** Aislamiento de fallos de infraestructura mediante los patrones **Circuit Breaker** y **Retry** con activación automática de **Fallback** en bitácora de contingencia local ante indisponibilidad del broker.
4. **Mensajería Asíncrona con JMS (Java Message Service):** Integración del broker **Apache ActiveMQ Classic** sobre la cola persistente `transacciones.bancarias`, desacoplando completamente al productor transaccional (`core-service`) del procesador de notificaciones (`ms-mensajeria`).
5. **Configuración y Descubrimiento Cloud:** Centralización de variables con **Spring Cloud Config Server** y registro de microservicios con **Netflix Eureka Server**.
6. **Dockerización y Orquestación:** Empaquetado en imágenes Docker optimizadas (`eclipse-temurin:21-jdk-alpine`) y orquestación unificada en `docker-compose.yml` sobre la red privada `banco-net`.

---

## 2. Contexto Histórico y Evolución del Sistema

* **Fase 1 (Migración de Datos Legacy):**  
  El proyecto inició como un sistema de ingesta y procesamiento de datos históricos basados en el repositorio [`bank_legacy_data`](https://github.com/KariVillagran/bank_legacy_data). Se modelaron entidades bancarias y se programó la lectura y persistencia de 50 cuentas bancarias, transacciones históricas e intereses a partir de archivos CSV planos.
* **Fase 2 (Capas BFF):**  
  Se diseñaron microservicios de agregación Backend for Frontend (BFF) para optimizar la experiencia de canales web y móviles. Dichos BFF se mantienen desacoplados como módulos independientes y no forman parte del núcleo evaluado en la presente entrega.
* **Fase 3 (Semana 8 — Arquitectura Cloud Resiliente, Eventos y Seguridad Federada):**  
  Evolución a un entorno Cloud distribuido con configuración centralizada, Service Discovery, protocolo OAuth 2.0, resiliencia con Resilience4j, mensajería asíncrona JMS con ActiveMQ y orquestación integral con Docker Compose.

---

## 3. Estructura del Código y Organización de Módulos

El proyecto está organizado como un repositorio multi-módulo coordinado por un POM padre central:

```text
Exp3_S8_Grupo3-main/
│
├── pom.xml                               # POM padre agregador y gestión de dependencias
├── docker-compose.yml                    # Orquestación de contenedores en red privada banco-net
├── .env.example                          # Plantilla de variables de entorno para despliegue
├── README.md                             # Documentación principal con evidencia operativa
├── PROPUESTA_TECNICA.md                  # Documento formal de propuesta técnica y diseño
│
├── config-server/                        # [Puerto 8888] Spring Cloud Config Server
│   ├── Dockerfile                        # Imagen Docker con copia de config-repo
│   ├── config-repo/                      # Repositorio de properties centralizados por servicio
│   │   ├── auth_server.properties
│   │   ├── core-service.properties       # Configuración de Resilience4j, ActiveMQ y HikariCP
│   │   └── ms-mensajeria.properties
│   └── src/main/java/.../ConfigServerApplication.java
│
├── discovery-server/                     # [Puerto 8761] Netflix Eureka Discovery Server
│   ├── Dockerfile
│   └── src/main/java/.../
│       ├── DiscoveryServerApplication.java
│       └── config/SecurityConfig.java     # Seguridad HTTP Basic para Eureka (eureka:eureka2026)
│
├── auth-server/                          # [Puerto 8080] Servidor de Autorización OAuth 2.0 y JWKS
│   ├── Dockerfile
│   └── src/main/java/.../
│       ├── config/SecurityConfig.java    # Configuración de rutas públicas y oauth2Login
│       ├── config/JwtKeyConfig.java      # Generador de par de claves RSA 2048-bit y Nimbus JWKSource
│       ├── controllers/JwkSetController.java # Expone endpoint RFC 7517 /.well-known/jwks.json
│       ├── security/OAuth2LoginSuccessHandler.java # Interceptor de login GitHub que emite el JWT
│       └── services/JwtTokenService.java # Creador y firmador asimétrico de tokens RS256
│
├── core-service/                         # [Puerto 8081] Núcleo Bancario y Resource Server
│   ├── Dockerfile
│   └── src/main/
│       ├── resources/data/               # Datasets CSV legacy (cuentas_anuales, intereses, transacciones)
│       └── java/.../
│           ├── config/SecurityConfig.java # Resource Server que valida Bearer JWT contra JWKS
│           ├── config/ConfiguracionJms.java # Jackson Converter con TypeIdMapping para JMS
│           ├── controller/CoreController.java # Endpoints de retiros, transferencias y contingencia
│           ├── mensajeria/PublicadorTransacciones.java # Publicador JMS con @CircuitBreaker y @Retry
│           ├── repository/CargadorDatosLegacy.java # Carga automática de 50 cuentas desde CSV
│           └── service/BancoService.java # Lógica contable de débitos, créditos y eventos
│
└── ms-mensajeria/                        # [Puerto 8082] Consumidor Asíncrono de Eventos JMS
    ├── Dockerfile
    └── src/main/java/.../
        ├── config/ConfiguracionJms.java   # Jackson Converter con TypeIdMapping para JMS
        ├── controller/ControladorMensajeria.java # Historial de eventos procesados
        └── mensajeria/ReceptorTransacciones.java # @JmsListener sobre cola transacciones.bancarias
```

---

## 4. Diagrama de Arquitectura de la Solución

```mermaid
flowchart TD
    subgraph Cliente ["Canales de Usuario"]
        User["Usuario / Navegador / cURL"]
    end

    subgraph Identidad ["Seguridad Federada OAuth 2.0"]
        GitHub["GitHub OAuth Provider<br>(github.com/login/oauth)"]
        AuthServer["auth-server :8080<br>OAuth2 Client + JWT RSA Issuer<br>JWKS: /.well-known/jwks.json"]
    end

    subgraph Soporte ["Servicios de Infraestructura Cloud"]
        ConfigServer["config-server :8888<br>Spring Cloud Config Native"]
        Eureka["discovery-server :8761<br>Netflix Eureka Server"]
    end

    subgraph Negocio ["Servicios de Negocio Bancario"]
        CoreService["core-service :8081<br>OAuth2 Resource Server<br>Lógica Bancaria & Persistencia Legacy"]
        MsMensajeria["ms-mensajeria :8082<br>Consumidor Asíncrono @JmsListener"]
    end

    subgraph Broker ["Broker de Mensajería Distribuido"]
        ActiveMQ[("Apache ActiveMQ Classic :61616<br>Cola: transacciones.bancarias")]
        Contingencia[("Bitácora de Contingencia Local<br>Fallback Resilience4j Circuit Breaker")]
    end

    %% Flujos de Seguridad
    User -->|"1. Login: /oauth2/authorization/github"| AuthServer
    AuthServer <-->|"2. Valida credenciales"| GitHub
    AuthServer -->|"3. Entrega Bearer JWT firmado RSA"| User

    %% Flujos de Configuración y Descubrimiento
    ConfigServer -.->|"Inyecta properties"| AuthServer
    ConfigServer -.->|"Inyecta properties"| CoreService
    ConfigServer -.->|"Inyecta properties"| MsMensajeria
    Eureka -.->|"Registro y Heartbeats"| AuthServer
    Eureka -.->|"Registro y Heartbeats"| CoreService
    Eureka -.->|"Registro y Heartbeats"| MsMensajeria

    %% Operación Bancaria Protegida
    User -->|"4. POST Operación (Bearer Token JWT)"| CoreService
    CoreService <-->|"5. Valida firma vía JWKS"| AuthServer

    %% Mensajería y Resiliencia
    CoreService -->|"6a. Publica Evento (Broker activo)"| ActiveMQ
    ActiveMQ -->|"7. Consume evento asíncrono"| MsMensajeria
    CoreService -.->|"6b. ActiveMQ Caído (Fallback)"| Contingencia
```

---

## 5. Matriz de Puertos, Roles y Endpoints Clave

| Microservicio | Puerto Host | Descripción y Rol | Endpoints Clave |
| :--- | :---: | :--- | :--- |
| **`config-server`** | `8888` | Servidor central de configuración nativa (`config-repo`) | `GET http://localhost:8888/core-service/default`<br>`GET http://localhost:8888/auth_server/default` |
| **`discovery-server`** | `8761` | Servidor Eureka para descubrimiento dinámico | `http://localhost:8761` *(eureka / eureka2026)* |
| **`auth-server`** | `8080` | Servidor de Autorización OAuth 2.0 y emisor JWT RSA | `http://localhost:8080/oauth2/authorization/github`<br>`http://localhost:8080/.well-known/jwks.json` |
| **`core-service`** | `8081` | Servidor de Recursos protegido con lógica contable | `http://localhost:8081/swagger-ui.html`<br>`POST http://localhost:8081/api/core/operaciones/retiro`<br>`POST http://localhost:8081/api/core/operaciones/transferencia`<br>`GET http://localhost:8081/api/core/mensajeria/contingencias` |
| **`ms-mensajeria`** | `8082` | Microservicio consumidor asíncrono JMS | `GET http://localhost:8082/api/mensajeria/historial`<br>`GET http://localhost:8082/actuator/health` |
| **`activemq`** | `61616` / `8161` | Broker de mensajería Apache ActiveMQ Classic | `tcp://localhost:61616` (JMS OpenWire)<br>`http://localhost:8161/admin` *(admin / admin)* |

---

## 6. Instrucciones de Ejecución y Puesta en Marcha

### Prerrequisitos
* Java 21 JDK instalado y configurado en el `PATH`
* Maven 3.9+ (o wrapper incluido `./mvnw` / `mvnw.cmd`)
* Docker Desktop y Docker Compose

### Paso 1: Configurar las Variables de Entorno
Cree el archivo `.env` en la raíz del proyecto a partir de la plantilla:
```bash
cp .env.example .env
```
Abra el archivo `.env` y configure sus credenciales de GitHub OAuth App:
```env
GITHUB_CLIENT_ID=Ov23lib2ztrqPoxNZEe7
GITHUB_CLIENT_SECRET=tu_secreto_generado_en_github
```

### Paso 2: Compilar y Empaquetar los Microservicios
```bash
./mvnw clean package -DskipTests
```
*(En Windows PowerShell: `.\mvnw.cmd clean package -DskipTests`)*

### Paso 3: Despliegue Completo con Docker Compose
Lanzar la totalidad de los contenedores en segundo plano:
```bash
docker compose up --build -d
```

### Paso 4: Verificar Estado de los Servicios
```bash
docker compose ps
```

---

## 7. Evidencia de Ejecución y Validación Operativa

A continuación se presentan las evidencias directas de consola obtenidas durante la ejecución y validación de cada componente del sistema:

### Evidencia 1: Compilación Exitosa Multi-Módulo (Maven)
```text
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]   config-server                                                    [jar]
[INFO]   discovery-server                                                 [jar]
[INFO]   auth-server                                                      [jar]
[INFO]   banco-xyz-parent                                                 [pom]
[INFO]   core-service                                                     [jar]
[INFO]   ms-mensajeria                                                    [jar]
[INFO] 
[INFO] Reactor Summary:
[INFO]   config-server 0.0.1-SNAPSHOT ............................ SUCCESS [ 9.766 s]
[INFO]   discovery-server 0.0.1-SNAPSHOT ......................... SUCCESS [ 6.390 s]
[INFO]   auth-server 0.0.1-SNAPSHOT .............................. SUCCESS [ 7.522 s]
[INFO]   banco-xyz-parent 1.0.0 .................................. SUCCESS [ 0.622 s]
[INFO]   core-service 1.0.0 ...................................... SUCCESS [14.274 s]
[INFO]   ms-mensajeria 1.0.0 ..................................... SUCCESS [ 5.998 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  46.989 s
```

### Evidencia 2: Orquestación de Contenedores en Docker Compose
Ejecución del comando `docker compose ps`:
```text
NAME               IMAGE                     COMMAND                  SERVICE            STATUS              PORTS
activemq           apache/activemq-classic   "/bin/sh -c '${ACTIV…"   activemq           running (healthy)   0.0.0.0:8161->8161/tcp, 0.0.0.0:61616->61616/tcp
auth-server        exp3_s8_grupo3-auth-server "java -jar /app.jar"     auth-server        running             0.0.0.0:8080->8080/tcp
config-server      exp3_s8_grupo3-config-server "java -jar /app.jar"   config-server      running             0.0.0.0:8888->8888/tcp
core-service       exp3_s8_grupo3-core-service "java -jar /app.jar"    core-service       running             0.0.0.0:8081->8081/tcp
discovery-server   exp3_s8_grupo3-discovery-server "java -jar /app.jar" discovery-server  running             0.0.0.0:8761->8761/tcp
ms-mensajeria      exp3_s8_grupo3-ms-mensajeria "java -jar /app.jar"   ms-mensajeria      running             0.0.0.0:8082->8082/tcp
```

### Evidencia 3: Descubrimiento y Registro en Eureka Server
Consola de `discovery-server` confirmando el registro de las instancias:
```text
2026-10-04T10:33:23.710-03:00  INFO 4072 --- [discovery-server] Registered instance AUTH_SERVER/auth-server:8080 with status UP
2026-10-04T10:33:25.697-03:00  INFO 4072 --- [discovery-server] Registered instance CORE-SERVICE/core-service:8081 with status UP
2026-10-04T10:33:26.115-03:00  INFO 4072 --- [discovery-server] Registered instance MS-MENSAJERIA/ms-mensajeria:8082 with status UP
```

### Evidencia 4: Flujo OAuth 2.0 y Emisión de Token JWT RSA
1. Al acceder a `http://localhost:8080/oauth2/authorization/github` y autorizar en GitHub, el `OAuth2LoginSuccessHandler` emite el token:
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "tokenType": "Bearer",
  "accessToken": "eyJraWQiOiJmOTIzNTcyMS05MmU4LTRlYTktOTVkMi05YTA2MjBiYTY3NjgiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvLWR1b2MiLCJhdWQiOiJiYW5jby14eXotY29yZSIsIm5hbWUiOiJVc3VhcmlvIER1b2MiLCJpc3MiOiJhdXRoLXNlcnZlciIsImVtYWlsIjoidXN1YXJpb0BkdW9jdWMuY2wiLCJleHAiOjE3NTk1OTk0ODUsImlhdCI6MTc1OTU5NTg4NX0.eR74W...",
  "expiresIn": 3600,
  "githubUser": "usuario-duoc",
  "email": "usuario@duocuc.cl"
}
```

2. Consulta del conjunto público de claves JWKS en `http://localhost:8080/.well-known/jwks.json`:
```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "kid": "f9235721-92e8-4ea9-95d2-9a0620ba6768",
      "n": "uB_vH12k8Vz5P..."
    }
  ]
}
```

### Evidencia 5: Prueba de Seguridad en Servidor de Recursos (Sin Token -> 401 Unauthorized)
Petición sin cabecera de autenticación a `core-service`:
```bash
curl -i -X POST http://localhost:8081/api/core/operaciones/retiro \
  -H "Content-Type: application/json" \
  -d '{"cuentaId": 1001, "monto": 10000, "canal": "ATM"}'
```
**Respuesta obtenida:**
```text
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer
Date: Sun, 04 Oct 2026 13:35:00 GMT
Content-Length: 0
```

### Evidencia 6: Operación Bancaria Protegida Exitosa (Con Token Bearer -> 200 OK)
Petición enviando el Bearer Token JWT RSA:
```bash
curl -i -X POST http://localhost:8081/api/core/operaciones/retiro \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJraWQiOiJmOTIzNTcyMS05MmU4LTRlYTktOTVkMi05YTA2MjBiYTY3NjgiLCJhbGciOiJSUzI1NiJ9..." \
  -d '{"cuentaId": 1001, "monto": 10000, "canal": "ATM"}'
```
**Respuesta obtenida:**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "cuentaId": 1001,
  "montoRetirado": 10000,
  "nuevoSaldoContable": 490000,
  "saldoDisponibleTotal": 590000,
  "canal": "ATM",
  "estado": "EXITOSA",
  "mensaje": "Giro procesado exitosamente"
}
```

### Evidencia 7: Consumo Asíncrono JMS en `ms-mensajeria`
Logs emitidos por `ms-mensajeria` al recibir el mensaje desde ActiveMQ:
```text
2026-10-04T10:35:12.441-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : ===============================================================
2026-10-04T10:35:12.441-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : [MS-MENSAJERIA] EVENTO RECIBIDO DESDE ACTIVEMQ
2026-10-04T10:35:12.441-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : ID Transaccion: TX-8E24D9C1
2026-10-04T10:35:12.442-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : Tipo Operacion: RETIRO
2026-10-04T10:35:12.442-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : Canal:          ATM
2026-10-04T10:35:12.442-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : Monto:          $10000
2026-10-04T10:35:12.442-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : Cuenta Origen:  1001
2026-10-04T10:35:12.442-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : Estado:         EXITOSA
2026-10-04T10:35:12.443-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : ===============================================================
2026-10-04T10:35:12.443-03:00  INFO 1840 --- [ms-mensajeria] c.d.b.m.m.ReceptorTransacciones : [MS-MENSAJERIA] Procesando notificacion de transaccion: TX-8E24D9C1
```

### Evidencia 8: Prueba de Resiliencia y Fallback (ActiveMQ Caído)
1. Se detiene el broker para forzar la desconexión:
```bash
docker stop activemq
```

2. Se ejecuta un nuevo retiro bancario con el token Bearer:
```bash
curl -i -X POST http://localhost:8081/api/core/operaciones/retiro \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"cuentaId": 1001, "monto": 15000, "canal": "WEB"}'
```
**Resultado:** La petición responde **HTTP 200 OK**. La operación bancaria del cliente no se interrumpe.

3. Logs de `core-service` evidenciando la activación del Fallback de Resilience4j:
```text
2026-10-04T10:41:05.835-03:00  INFO 14692 --- [core-service] c.d.b.c.m.PublicadorTransacciones : [PUBLICADOR-JMS] Publicando evento transaccional 'TX-0455B232' en la cola 'transacciones.bancarias'
2026-10-04T10:41:06.379-03:00 ERROR 14692 --- [core-service] c.d.b.c.m.PublicadorTransacciones : [FALLBACK RESILIENCE4J] ActiveMQ no disponible o circuito ABIERTO. Causa: Uncategorized exception occurred during JMS processing
2026-10-04T10:41:06.379-03:00  WARN 14692 --- [core-service] c.d.b.c.m.PublicadorTransacciones : [FALLBACK RESILIENCE4J] Guardando evento 'TX-0455B232' en contingencia local. Total pendientes: 1
```

4. Consulta de la bitácora de contingencia en `http://localhost:8081/api/core/mensajeria/contingencias`:
```json
{
  "totalContingencias": 1,
  "descripcion": "Eventos almacenados por Fallback de Resilience4j ante broker ActiveMQ no disponible",
  "eventos": [
    {
      "transaccionId": "TX-0455B232",
      "cuentaOrigenId": 1001,
      "cuentaDestinoId": null,
      "monto": 15000,
      "tipoOperacion": "RETIRO",
      "canal": "WEB",
      "estado": "CONTINGENCIA_PENDIENTE_BROKER",
      "fechaHora": "2026-10-04T10:41:05.830",
      "detalle": "Broker ActiveMQ inaccesible - Contingencia local: Uncategorized exception occurred during JMS processing"
    }
  ]
}
```

5. Se restaura el contenedor del broker:
```bash
docker start activemq
```

### Evidencia 9: Ejecución de la Suite de Pruebas Unitarias e Integración
```text
[INFO] Running cl.duoc.bancoxyz.core.CoreServiceTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 53.85 s -- in cl.duoc.bancoxyz.core.CoreServiceTests
[INFO] 
[INFO] Results:
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
* **Prueba 1:** `cargaDatosLegacyExitosa()` -> Verifica la carga de 50 cuentas desde CSV legacy.
* **Prueba 2:** `ejecutarRetiroCoreExitoso()` -> Valida el débito contable, balance contable y resiliencia.
* **Prueba 3:** `retiroExcedenteLanzaExcepcion()` -> Valida la denegación y control de sobregiro.
