# 🧠 CentroPsico — Plataforma Web de Gestión Psicológica

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Spring Security](https://img.shields.io/badge/Security-Spring_Security-6DB33F)
![JWT](https://img.shields.io/badge/Auth-JWT-red)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket_STOMP-purple)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED)
![Render](https://img.shields.io/badge/Deploy-Render-black)
![Aiven](https://img.shields.io/badge/Database-Aiven-blueviolet)

**CentroPsico** es una plataforma web integral para la gestión administrativa, clínica, financiera y operativa de un centro psicológico.

El sistema centraliza usuarios, pacientes, psicólogos, servicios, disponibilidad, citas, pagos, solicitudes, promociones, reportes, auditoría, notificaciones y perfiles dentro de una sola aplicación desarrollada con **Spring Boot, MySQL, HTML, CSS y JavaScript**.

También incluye un portal público desde el cual una persona puede revisar servicios y promociones, consultar disponibilidad y registrar una pre-reserva para su posterior validación.

---

## 🌍 Demo en línea

**Aplicación desplegada:**

https://centropsico-system.onrender.com

> El backend y el frontend se despliegan en Render.  
> La información persistente se almacena en MySQL Aiven.

---

## ✨ Últimas mejoras implementadas

- Separación de vistas y permisos para `ADMIN`, `RECEPCIONISTA` y `PSICOLOGO`.
- Sidebar dinámico según el rol autenticado.
- Vista **Citas** para administración y recepción.
- Vista **Mis citas** para psicólogos.
- Ocultamiento de pagos y montos para el rol psicólogo.
- Historia clínica abierta desde las citas mediante un modal.
- Eliminación del acceso independiente a “Historia clínica” desde el sidebar.
- Persistencia de la foto de perfil en MySQL Aiven.
- Eliminación de la dependencia de `/uploads/profiles` para fotos de usuario.
- Conversión de la imagen a Base64 antes de almacenarla.
- Optimización automática de fotografías grandes desde el navegador.
- Recuperación de la foto mediante `GET /api/profile/me` después de recargar o redeplegar.
- Actualización del nombre, teléfono y contraseña desde el perfil.
- Notificaciones y auditoría de operaciones de seguridad.

---

## 🚀 Funcionalidades principales

- Autenticación mediante JWT.
- Autorización por roles con Spring Security.
- Dashboard adaptado al rol autenticado.
- Gestión de solicitudes y pre-reservas.
- Gestión de pacientes.
- Gestión de psicólogos.
- Gestión de servicios psicológicos.
- Configuración de disponibilidad semanal.
- Programación de citas.
- Validación de horarios disponibles.
- Agenda diaria.
- Calendario interactivo con FullCalendar.
- Vista de citas asignadas para psicólogos.
- Registro y seguimiento de pagos.
- Registro automático de ingresos.
- Gestión de gastos y categorías.
- Reportes mensuales y por psicólogo.
- Promociones para el portal público.
- Historia clínica digital.
- Notificaciones persistentes y en tiempo real.
- Auditoría de movimientos y accesos.
- Perfil de usuario con foto persistente.
- Diseño responsive para escritorio, tablet y móvil.

---

## 👤 Roles y permisos

### ADMIN

El administrador tiene acceso completo a los módulos administrativos, operativos y financieros.

**Puede visualizar:**

```text
Dashboard
Solicitudes
Pacientes
Citas
Finanzas
Reportes
Promociones
Movimientos
Administración
 ├── Usuarios
 ├── Psicólogos
 ├── Servicios
 └── Disponibilidad
Notificaciones
Perfil
```

**Puede realizar:**

- Gestionar usuarios y roles.
- Registrar y actualizar pacientes.
- Registrar psicólogos.
- Administrar servicios.
- Configurar disponibilidades.
- Registrar, actualizar y eliminar citas.
- Registrar pagos.
- Consultar finanzas.
- Generar reportes.
- Administrar promociones.
- Revisar movimientos de auditoría.
- Enviar notificaciones.
- Consultar historias clínicas desde las citas.
- Actualizar su perfil y foto.

---

### RECEPCIONISTA

La recepción dispone de los módulos necesarios para la atención administrativa.

**Puede visualizar:**

```text
Dashboard
Solicitudes
Pacientes
Citas
Notificaciones
Perfil
```

**Puede realizar:**

- Revisar solicitudes y pre-reservas.
- Registrar y actualizar pacientes.
- Registrar citas.
- Consultar agenda y calendario.
- Registrar pagos.
- Actualizar estados permitidos de una cita.
- Recibir notificaciones.
- Actualizar su perfil y foto.

**No puede visualizar:**

```text
Finanzas
Reportes
Promociones
Movimientos
Usuarios
Psicólogos
Servicios
Disponibilidad
Historia clínica
```

---

### PSICOLOGO

El psicólogo dispone de una experiencia orientada a la atención clínica.

**Puede visualizar:**

```text
Dashboard
Mis citas
Agenda diaria
Calendario
Historia clínica de pacientes asignados
Notificaciones
Perfil
```

**Puede realizar:**

- Consultar sus citas asignadas.
- Revisar su agenda diaria.
- Visualizar su calendario.
- Actualizar estados clínicos permitidos.
- Abrir la historia clínica desde una cita.
- Registrar información clínica de pacientes asignados.
- Recibir notificaciones.
- Actualizar su perfil y foto.

**No puede visualizar:**

```text
Registrar cita
Pacientes generales
Solicitudes
Finanzas
Reportes
Promociones
Movimientos
Administración
Pagos
Montos
Usuarios
```

---

## 🧠 Historia clínica

La historia clínica **no aparece como una sección independiente en el sidebar**.

El acceso se realiza mediante el flujo:

```text
Citas / Mis citas
        ↓
Seleccionar paciente o cita
        ↓
Abrir historia clínica
        ↓
Modal clinicalHistoryModal
```

El acceso se restringe a:

```text
ADMIN
PSICOLOGO
```

Para el rol psicólogo, el backend debe comprobar que el paciente solicitado esté relacionado con una cita asignada al profesional autenticado.

### Información clínica

- Evolución.
- Diagnóstico.
- Recomendaciones.
- Observaciones.
- Registro cronológico.
- Historial de atenciones.

---

## 🖼️ Foto de perfil persistente

Anteriormente, las fotografías se almacenaban en:

```text
/uploads/profiles
```

Ese almacenamiento era temporal en Render, por lo que las imágenes podían desaparecer después de un redeploy.

La implementación actual utiliza:

```text
Imagen seleccionada
        ↓
Optimización automática en profile.js
        ↓
POST /api/profile/photo
        ↓
Conversión a Base64
        ↓
Campo profile_image_base64
        ↓
MySQL Aiven
```

### Orden de visualización

El frontend intenta mostrar la imagen en este orden:

```text
1. profileImageBase64
2. profileImageUrl como respaldo
3. Iniciales del usuario
```

### Campo de base de datos

```sql
ALTER TABLE usuario
ADD COLUMN profile_image_base64 LONGTEXT;
```

### Entidad Java

```java
@Lob
@Column(name = "profile_image_base64", columnDefinition = "LONGTEXT")
private String profileImageBase64;
```

### Ventajas

- La foto no depende del disco temporal de Render.
- Se conserva después de un `git push`.
- Se conserva después de un redeploy.
- Se recupera al iniciar sesión o recargar.
- No se almacena el Base64 pesado en `localStorage`.
- El usuario puede seleccionar fotografías grandes y el navegador las optimiza antes de enviarlas.

---

## 🧩 Módulos del sistema

### Sistema interno

```text
Dashboard
Solicitudes / Pre-reservas
Pacientes
Citas / Mis citas
Agenda diaria
Calendario
Finanzas
Reportes
Promociones
Movimientos / Auditoría
Administración
 ├── Usuarios
 ├── Psicólogos
 ├── Servicios
 └── Disponibilidad
Notificaciones
Perfil
```

### Portal público

```text
Inicio
Servicios
Promociones
Disponibilidad
Formulario de pre-reserva
Consentimiento de tratamiento de datos
Acceso al sistema interno
```

---

## 🔐 Seguridad

CentroPsico implementa seguridad mediante **Spring Security y JWT**.

### Características

- Autenticación mediante token JWT.
- Sesiones stateless.
- Validación de usuarios activos.
- Control de endpoints por rol.
- Protección de rutas REST.
- Separación entre endpoints públicos y privados.
- Ocultamiento de componentes según el rol.
- Auditoría de accesos no autorizados.
- Auditoría de accesos denegados.
- Registro de eventos críticos.
- Identificación del usuario autenticado mediante correo.

### Flujo de autenticación

```text
Login
  ↓
Generación de JWT
  ↓
Token almacenado en el navegador
  ↓
Authorization: Bearer <token>
  ↓
JwtAuthenticationFilter
  ↓
Usuario autenticado en Spring Security
  ↓
Acceso al endpoint según autoridad
```

### Autoridades

```text
ADMIN
RECEPCIONISTA
PSICOLOGO
```

---

## 🌐 Portal público y pre-reservas

El portal público permite que una persona interesada consulte información del centro y registre una solicitud de atención.

### Funcionalidades

- Visualización de servicios.
- Visualización de promociones activas.
- Consulta de disponibilidad.
- Selección de psicólogo.
- Selección de fecha y horario.
- Selección de modalidad.
- Registro de información de contacto.
- Registro de método de pago.
- Registro de código de operación.
- Consentimiento de tratamiento de datos.
- Envío de la pre-reserva.
- Notificación a administración y recepción.

### Estados de pre-reserva

```text
NUEVO
PAGO_EN_REVISION
CONTACTADO
PRE_RESERVADO
AGENDADO
DESCARTADO
```

### Flujo

```text
Portal público
        ↓
Selección de servicio y psicólogo
        ↓
Consulta de disponibilidad
        ↓
Registro de datos personales
        ↓
Aceptación del consentimiento
        ↓
Creación de pre-reserva
        ↓
Validación administrativa
        ↓
Conversión a cita
        ↓
Registro del adelanto
        ↓
Notificación al psicólogo
```

---

## 📅 Gestión de citas

### Funcionalidades

- Registro de citas.
- Validación de disponibilidad.
- Generación dinámica de horarios.
- Bloqueo de cruces.
- Agenda diaria.
- Calendario mensual, semanal y diario.
- Vista de lista.
- Actualización de estados.
- Registro de pagos.
- Notificación al psicólogo.
- Apertura de historia clínica desde la cita.

### Estados

```text
PROGRAMADA
ATENDIDA
CANCELADA
NO_ASISTIO
REPROGRAMADA
```

### Reglas principales

```text
ADMIN / RECEPCIONISTA
- Registrar citas.
- Consultar citas generales.
- Actualizar citas.
- Registrar pagos.
- Cambiar estados permitidos.

PSICOLOGO
- Consultar solo sus citas.
- Consultar su agenda.
- Consultar su calendario.
- Cambiar estados clínicos permitidos.
- Abrir historia clínica de pacientes asignados.

ADMIN
- Eliminar citas.
```

---

## 🧠 Disponibilidad de psicólogos

La disponibilidad se calcula considerando:

- Psicólogo.
- Servicio.
- Duración del servicio.
- Día configurado.
- Hora de inicio.
- Hora de fin.
- Citas registradas.
- Cruces de horario.
- Estado de la cita.
- Horarios ocupados y libres.

Esto evita dobles reservas y permite presentar horarios reales.

---

## 💳 Pagos

### Funcionalidades

- Registro de pagos por cita.
- Pagos parciales.
- Pagos completos.
- Validación del saldo pendiente.
- Conversión de adelantos en pagos.
- Generación visual de comprobantes.
- Registro automático del ingreso.
- Actualización de indicadores financieros.

### Métodos

```text
EFECTIVO
YAPE
PLIN
TRANSFERENCIA
TARJETA
```

---

## 💰 Finanzas

### Ingresos

- Registro manual.
- Registro automático desde citas.
- Registro de adelantos.
- Método de pago.
- Fecha del ingreso.
- Integración con el dashboard.

### Gastos

- Registro de gastos.
- Categorías.
- Responsable.
- Fecha.
- Filtros avanzados.
- Historial mensual.
- Eliminación lógica.
- Auditoría.

### Resumen

- Total de ingresos.
- Total de gastos.
- Ganancia o pérdida.
- Balance por periodo.

---

## 📊 Dashboard

El dashboard presenta indicadores operativos y financieros según el rol.

### Indicadores

- Total de pacientes.
- Citas por periodo.
- Citas por estado.
- Ingresos.
- Gastos.
- Pagos pendientes.
- Agenda relevante.
- Recordatorios.
- Notificaciones.

### Periodos

```text
Hoy
Esta semana
Este mes
Este año
```

---

## 📈 Reportes

### Reporte mensual

- Total de citas.
- Citas atendidas.
- Citas canceladas.
- Citas no asistidas.
- Ingresos.
- Gastos.
- Resultado financiero.

### Reporte por psicólogo

- Profesional.
- Pacientes atendidos.
- Citas atendidas.
- Servicios realizados.
- Rendimiento por periodo.

### Exportación

El proyecto contiene soporte para reportes exportables mediante:

- CSV.
- Vista imprimible.
- Generación de PDF desde el navegador.

---

## 🔔 Notificaciones

El sistema utiliza **WebSocket, SockJS y STOMP** para emitir notificaciones en tiempo real.

### Características

- Persistencia en base de datos.
- Notificaciones por rol.
- Notificaciones por usuario.
- Contador de no leídas.
- Marcado individual.
- Marcado masivo.
- Mensajes emergentes.
- Actualización de módulos relacionados.

### Eventos

- Nuevo paciente.
- Nuevo psicólogo.
- Nuevo servicio.
- Nueva disponibilidad.
- Nueva cita.
- Cita asignada.
- Cambio de estado.
- Pago registrado.
- Adelanto validado o rechazado.
- Ingreso registrado.
- Gasto registrado.
- Nueva pre-reserva.
- Conversión a cita.
- Nueva promoción.
- Movimiento crítico.
- Mensaje directo del administrador.

---

## 🧾 Auditoría

El módulo de auditoría registra eventos administrativos, operativos y de seguridad.

### Eventos

- Inicio de sesión exitoso.
- Inicio de sesión fallido.
- Usuario inactivo.
- Acceso no autorizado.
- Acceso denegado.
- Registro de entidades.
- Actualización de entidades.
- Activación o desactivación.
- Operaciones financieras.
- Registro de pagos.
- Validación de adelantos.
- Actualización de perfil.
- Actualización de foto.
- Cambio de contraseña.

### Niveles

```text
INFO
WARNING
CRITICAL
```

---

## 🎁 Promociones

El administrador puede:

- Registrar promociones.
- Editar promociones.
- Activar o desactivar promociones.
- Definir descuentos.
- Configurar fechas de vigencia.
- Mostrar promociones en el portal público.
- Ocultar promociones vencidas o inactivas.

---

## 👤 Perfil de usuario

Cada usuario autenticado puede:

- Consultar sus datos.
- Actualizar nombres.
- Actualizar apellidos.
- Actualizar teléfono.
- Consultar correo y rol.
- Cambiar contraseña.
- Subir una foto.
- Eliminar su foto mediante endpoint.
- Visualizar su foto en el sidebar.

### Endpoints

```http
GET    /api/profile/me
PUT    /api/profile/me
PUT    /api/profile/change-password
POST   /api/profile/photo
DELETE /api/profile/photo
```

---

## ☁️ Infraestructura

```text
GitHub
  ↓
Render
  ├── Aplicación Spring Boot
  ├── Frontend estático
  └── API REST
        ↓
Aiven MySQL
```

### Características

- Deploy automático desde GitHub.
- Backend y frontend servidos por Spring Boot.
- Base de datos administrada en Aiven.
- Variables de entorno para credenciales.
- Docker para producción.
- Persistencia de datos fuera del sistema de archivos de Render.

---

## ⚙️ Tecnologías

### Backend

- Java 21.
- Spring Boot 3.
- Spring Security.
- JWT.
- Spring Data JPA.
- Hibernate.
- REST API.
- WebSocket.
- STOMP.
- MySQL.

### Frontend

- HTML5.
- CSS3.
- JavaScript Vanilla.
- FullCalendar.
- Chart.js.
- SweetAlert2.
- Lucide Icons.
- SockJS.
- STOMP.js.

### Cloud y herramientas

- Aiven MySQL.
- Render.
- Docker.
- GitHub.
- Maven.
- MySQL Workbench.
- Visual Studio Code.

---

## 📂 Estructura del proyecto

```text
src/
└── main/
    ├── java/
    │   └── com/centropsicologico/sistema/
    │       ├── config/
    │       ├── controller/
    │       ├── dto/
    │       ├── entity/
    │       ├── enums/
    │       ├── exception/
    │       ├── repository/
    │       ├── service/
    │       ├── service/impl/
    │       ├── util/
    │       └── SistemaApplication.java
    │
    └── resources/
        ├── static/
        │   ├── components/
        │   │   ├── header.html
        │   │   ├── module-tabs.html
        │   │   └── sidebar.html
        │   ├── css/
        │   ├── img/
        │   ├── js/
        │   ├── index.html
        │   ├── login.html
        │   └── portal.html
        └── application.properties
```

---

## 🧪 Endpoints principales

### Autenticación

```http
POST /api/auth/login
GET  /api/auth/validate
POST /api/auth/logout
```

### Perfil

```http
GET    /api/profile/me
PUT    /api/profile/me
PUT    /api/profile/change-password
POST   /api/profile/photo
DELETE /api/profile/photo
```

### Pacientes

```http
POST  /api/patients
GET   /api/patients
GET   /api/patients/{id}
PUT   /api/patients/{id}
PATCH /api/patients/{id}/toggle-active
```

### Psicólogos

```http
POST  /api/psychologists
GET   /api/psychologists
GET   /api/psychologists/{id}
PUT   /api/psychologists/{id}
PATCH /api/psychologists/{id}/toggle-active
```

### Servicios

```http
POST  /api/services
GET   /api/services
GET   /api/services/{id}
PUT   /api/services/{id}
PATCH /api/services/{id}/toggle-active
```

### Disponibilidad

```http
POST  /api/psychologist-availabilities
GET   /api/psychologist-availabilities
GET   /api/psychologist-availabilities/psychologist/{id}
PUT   /api/psychologist-availabilities/{id}
PATCH /api/psychologist-availabilities/{id}/toggle-active
```

### Citas

```http
POST   /api/appointments
GET    /api/appointments
GET    /api/appointments/{id}
GET    /api/appointments/by-date
GET    /api/appointments/my
GET    /api/appointments/my/by-date
PUT    /api/appointments/{id}
PUT    /api/appointments/{id}/status
PUT    /api/appointments/{id}/pay
DELETE /api/appointments/{id}
```

### Historia clínica

```http
POST   /api/clinical-history/patient/{id}
GET    /api/clinical-history/patient/{id}
DELETE /api/clinical-history/{id}
```

### Portal público

```http
GET  /api/public/services
GET  /api/public/promotions
GET  /api/public/availability
POST /api/public/leads
```

### Solicitudes

```http
GET  /api/leads
GET  /api/leads/{id}
PUT  /api/leads/{id}/status
PUT  /api/leads/{id}/payment/validate
PUT  /api/leads/{id}/payment/reject
POST /api/leads/{id}/convert-to-appointment
```

### Finanzas

```http
POST   /api/finances/incomes
GET    /api/finances/incomes
POST   /api/finances/expenses
GET    /api/finances/expenses
GET    /api/finances/expenses/filter
DELETE /api/finances/expenses/{id}
POST   /api/finances/expense-categories
GET    /api/finances/expense-categories
GET    /api/finances/summary
```

### Reportes

```http
GET /api/reports/monthly
GET /api/reports/psychologist-performance
```

### Notificaciones

```http
GET   /api/notifications/role/{role}
GET   /api/notifications/me
GET   /api/notifications/role/{role}/unread-count
GET   /api/notifications/me/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/role/{role}/read-all
PATCH /api/notifications/me/read-all
```

### Notificaciones directas

```http
POST /api/admin-notifications/users/{userId}
POST /api/admin-notifications/psychologists/{psychologistId}
```

### Promociones

```http
POST  /api/promotions
GET   /api/promotions
GET   /api/promotions/{id}
PUT   /api/promotions/{id}
PATCH /api/promotions/{id}/toggle-active
```

### Auditoría

```http
GET   /api/audit-logs/recent
GET   /api/audit-logs
GET   /api/audit-logs/page
GET   /api/audit-logs/module/{module}
PATCH /api/audit-logs/{id}/review
PATCH /api/audit-logs/{id}/pending
```

---

## ⚙️ Instalación local

### 1. Clonar el repositorio

```bash
git clone https://github.com/rojasbilly95-debug/centropsico-system.git
cd centropsico-system
```

### 2. Crear la base de datos

```sql
CREATE DATABASE centro_psicologico;
```

### 3. Configurar credenciales

Ejemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/centro_psicologico
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### 4. Configurar variables

```env
DB_PASSWORD=TU_PASSWORD
JWT_SECRET=TU_CLAVE_JWT
```

### 5. Ejecutar

```bash
mvn clean spring-boot:run
```

### 6. Abrir

```text
http://localhost:8080/login.html
http://localhost:8080/portal.html
```

---

## 🐳 Docker

```dockerfile
FROM eclipse-temurin:21
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Construcción:

```bash
mvn clean package
docker build -t centropsico-system .
docker run -p 8080:8080 centropsico-system
```

---

## 🧪 Pruebas recomendadas

### Perfil

- Abrir el perfil desde el sidebar.
- Actualizar nombres y teléfono.
- Cambiar contraseña.
- Seleccionar una fotografía grande.
- Confirmar que el navegador la optimiza.
- Confirmar `POST /api/profile/photo → 200`.
- Verificar `profile_image_base64` en MySQL.
- Recargar con `Ctrl + F5`.
- Cerrar e iniciar sesión.
- Realizar un redeploy.
- Confirmar que la foto permanece.

### ADMIN

- Visualizar todos los módulos.
- Gestionar usuarios.
- Gestionar pacientes.
- Gestionar psicólogos.
- Gestionar servicios.
- Configurar disponibilidad.
- Registrar citas.
- Registrar pagos.
- Consultar finanzas.
- Generar reportes.
- Gestionar promociones.
- Revisar movimientos.

### RECEPCIONISTA

- Visualizar dashboard, solicitudes, pacientes y citas.
- Registrar pacientes.
- Registrar citas.
- Registrar pagos.
- Recibir notificaciones.
- Confirmar bloqueo de módulos administrativos.

### PSICOLOGO

- Visualizar Mis citas.
- Consultar agenda.
- Consultar calendario.
- Actualizar estados permitidos.
- Abrir historia clínica desde una cita.
- Confirmar ocultamiento de pagos y montos.
- Confirmar bloqueo de pacientes generales y administración.

---

## 🎨 UX/UI

- Login profesional.
- Portal público responsive.
- Sidebar interactivo y contraíble.
- Tema visual según el rol.
- Dashboard moderno.
- Modales con SweetAlert2.
- Calendario interactivo.
- Tablas dinámicas.
- Filtros.
- Notificaciones.
- Comprobantes.
- Visualización de foto de perfil.
- Diseño responsive.

---

## 🎯 Problemas que resuelve

- Desorganización de citas.
- Dobles reservas.
- Gestión manual de pacientes.
- Falta de control de disponibilidad.
- Falta de seguimiento clínico.
- Falta de trazabilidad de pagos.
- Falta de control financiero.
- Ausencia de reportes.
- Falta de comunicación con interesados.
- Falta de auditoría.
- Falta de separación de permisos.
- Pérdida de fotos después de redeploys.
- Ausencia de notificaciones en tiempo real.

---

## 🛣️ Roadmap

- [x] Autenticación JWT.
- [x] Control por roles.
- [x] Portal público.
- [x] Gestión de citas.
- [x] Calendario.
- [x] Finanzas.
- [x] Reportes.
- [x] Auditoría.
- [x] Notificaciones en tiempo real.
- [x] Perfil de usuario.
- [x] Foto persistente en MySQL.
- [x] Optimización automática de fotografías.
- [x] Vista de citas personalizada para psicólogos.
- [ ] Consulta pública del estado de una pre-reserva.
- [ ] Botón flotante de WhatsApp.
- [ ] Portal del paciente.
- [ ] Videollamadas.
- [ ] Multi-sede.
- [ ] Integración con pasarela de pago.
- [ ] Almacenamiento externo especializado para imágenes.

---

## 📸 Capturas del sistema

Puedes agregar las capturas en una carpeta `docs/`:

```text
docs/portal.png
docs/login.png
docs/dashboard.png
docs/citas.png
docs/finanzas.png
docs/reportes.png
docs/auditoria.png
docs/perfil.png
```

Ejemplo:

```md
![Portal público](docs/portal.png)
![Dashboard](docs/dashboard.png)
![Perfil](docs/perfil.png)
```

---

## 👨‍💻 Autor

**Billy Rojas**

Proyecto desarrollado como una plataforma integral de gestión psicológica orientada a optimizar la administración, la atención clínica, el control financiero, la seguridad, la comunicación interna y la experiencia del usuario.