# 🧠 CentroPsico — Plataforma Integral de Gestión y Atención Psicológica

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-red)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket_STOMP-purple)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED)
![Render](https://img.shields.io/badge/Deploy-Render-black)
![Aiven](https://img.shields.io/badge/Database-Aiven-blueviolet)

Sistema web profesional desarrollado para la administración integral de un centro psicológico. Permite gestionar pacientes, psicólogos, disponibilidad, citas, pagos, pre-reservas, historia clínica, finanzas, reportes, auditoría, seguridad y notificaciones en tiempo real desde una sola plataforma moderna, segura y escalable.

Además del sistema interno, CentroPsico incorpora un portal público donde las personas pueden conocer los servicios psicológicos, consultar disponibilidad real, seleccionar horarios, registrar adelantos y enviar pre-reservas con consentimiento de tratamiento de datos personales.

---

## 🌍 Demo Online

🔗 https://centropsico-system.onrender.com

> Proyecto desplegado en Render con base de datos MySQL en Aiven.

---

## 🚀 Características principales

- Plataforma responsive para desktop, tablet y móvil.
- Sistema interno con control de acceso por roles.
- Portal público de servicios psicológicos y pre-reservas.
- Gestión de pacientes, psicólogos, disponibilidad y citas.
- Registro de pagos parciales o completos.
- Comprobantes de pago imprimibles.
- Conversión de pre-reservas en citas.
- Registro financiero automático de adelantos e ingresos.
- Historia clínica digital con acceso restringido.
- Dashboard administrativo y financiero.
- Reportes mensuales.
- Notificaciones en tiempo real con WebSocket + STOMP.
- Auditoría de movimientos, seguridad y autenticación.
- Paginación, filtros y marcado de revisión en auditoría.
- Seguridad con JWT y Spring Security.

---

## 🌐 Portal público y pre-reservas

El portal público permite a una persona interesada consultar información del centro, revisar servicios y enviar una pre-reserva de atención psicológica.

### Funcionalidades del portal

- Visualización de servicios psicológicos.
- Consulta pública de disponibilidad real.
- Selección de servicio, fecha, psicólogo y horario.
- Cálculo referencial de adelanto.
- Registro de método de pago y código de operación.
- Formulario de pre-reserva.
- Consentimiento obligatorio para tratamiento de datos personales.
- Registro de versión y fecha de consentimiento.
- Notificación automática al ADMIN y RECEPCIONISTA.
- Registro de auditoría por nueva pre-reserva.
- Diseño responsive y mobile-first.

### Datos registrados desde el portal

- Nombre completo.
- Correo electrónico.
- Teléfono o WhatsApp.
- Servicio de interés.
- Psicólogo y horario seleccionado.
- Modalidad de atención.
- Método de pago.
- Código de operación.
- Mensaje adicional.
- Consentimiento aceptado.
- Fecha y versión del consentimiento.

---

## 🔐 Privacidad y consentimiento

El sistema registra evidencia del consentimiento aceptado por la persona interesada antes de enviar la pre-reserva.

### Campos incorporados

```text
consentimiento_aceptado
fecha_consentimiento
version_consentimiento
```

### Versión actual

```text
PORTAL_PRIVACIDAD_V1
```

Esto permite demostrar que el interesado aceptó el uso de sus datos personales para gestionar la pre-reserva, validar la información registrada y permitir la comunicación administrativa correspondiente.

---

## 🧠 Disponibilidad pública inteligente

La disponibilidad pública se calcula dinámicamente considerando:

- Servicio seleccionado.
- Duración del servicio.
- Psicólogo disponible.
- Día de atención configurado.
- Horarios ocupados.
- Fecha seleccionada.
- Cruces de citas.
- Bloqueo automático de horarios no disponibles.

El sistema evita reservas inválidas y muestra horarios disponibles reales.

---

## 🔄 Flujo de atención

```text
Portal público
↓
Persona consulta servicios y disponibilidad
↓
Selecciona servicio, fecha, psicólogo y horario
↓
Registra datos personales y adelanto
↓
Acepta el tratamiento de datos personales
↓
Sistema genera una pre-reserva en estado PAGO_EN_REVISION
↓
ADMIN y RECEPCIONISTA reciben notificación
↓
Recepción valida o rechaza el adelanto
↓
Si el adelanto es validado, la pre-reserva pasa a PRE_RESERVADO
↓
Recepción asocia la pre-reserva con un paciente
↓
Sistema convierte la pre-reserva en cita
↓
El adelanto se registra como pago parcial o total
↓
Se genera ingreso financiero automático
↓
El psicólogo asignado recibe notificación de nueva cita
```

---

## 📌 Estados de pre-reserva

- NUEVO
- PAGO_EN_REVISION
- CONTACTADO
- PRE_RESERVADO
- AGENDADO
- DESCARTADO

---

## 👥 Gestión de usuarios y personas

### Pacientes

- Registro de pacientes.
- Validación de DNI único.
- Estado activo/inactivo.
- Búsqueda dinámica.
- Paginación.
- Asociación con citas e historia clínica.

### Psicólogos

- Registro de psicólogos.
- Especialidades profesionales.
- Activación y desactivación.
- Disponibilidad semanal configurable.
- Validación de cruces en disponibilidad.
- Agenda personalizada.
- Visualización exclusiva de sus citas.
- Notificación automática cuando se le asigna una cita.

### Usuarios del sistema

Roles implementados:

- ADMIN
- RECEPCIONISTA
- PSICÓLOGO

Funciones:

- Crear usuarios.
- Editar usuarios.
- Activar/desactivar usuarios.
- Control de acceso por rol.
- Restricción de módulos y endpoints.

---

## 📅 Gestión avanzada de citas

### Características

- Programación inteligente de citas.
- Validación automática de conflictos horarios.
- Generación dinámica de horarios disponibles.
- Bloqueo de horarios ocupados.
- Agenda tipo cronograma.
- Calendario interactivo con FullCalendar.
- Visualización personalizada por rol.
- Actualización de estados de cita.

### Estados de citas

- PROGRAMADA
- ATENDIDA
- CANCELADA
- NO_ASISTIO
- REPROGRAMADA

---

## 💳 Sistema de pagos

### Funcionalidades

- Registro de pagos de citas.
- Pagos parciales o completos.
- Validación de saldo pendiente.
- Bloqueo de registro de pago si la cita ya está pagada.
- Bloqueo de pago en citas canceladas o con estado no asistió.
- Conversión de adelantos de pre-reserva en pagos.
- Comprobante visual profesional.
- Impresión de comprobantes.
- Integración con ingresos financieros.
- Actualización de dashboard y finanzas.

### Métodos soportados

- Efectivo
- Yape
- Plin
- Transferencia
- Tarjeta

---

## 🧠 Historia clínica digital

Sistema clínico profesional con:

- Evolución del paciente.
- Diagnósticos.
- Recomendaciones.
- Observaciones clínicas.
- Timeline clínico visual.
- Registro cronológico.

Acceso restringido para:

- ADMIN
- PSICÓLOGO

---

## 💰 Gestión financiera

### Ingresos

- Registro automático por pagos.
- Registro manual.
- Integración con citas.
- Integración con pre-reservas.
- Actualización del dashboard financiero.

### Gastos

- Registro de gastos.
- Categorías dinámicas.
- Responsable.
- Control mensual.
- Alertas por gastos altos.

---

## 📊 Dashboard

Indicadores disponibles:

- Pacientes registrados.
- Citas del día.
- Ingresos.
- Gastos.
- Resultado financiero.
- Agenda diaria.
- Gráficas interactivas.
- Resumen financiero.
- Sincronización con eventos recibidos por WebSocket.

---

## 📈 Reportes

El sistema permite generar reportes mensuales con:

- Total de citas.
- Citas atendidas.
- Citas canceladas.
- Citas no asistidas.
- Ingresos.
- Gastos.
- Resultado financiero.

---

## 🔔 Notificaciones en tiempo real

El sistema incorpora notificaciones en tiempo real implementadas con WebSocket + STOMP.

Las notificaciones se almacenan en base de datos y se emiten hacia el frontend para actualizar la campanita, contador, toasts y módulos relacionados.

### Eventos implementados

- Nuevo paciente registrado.
- Paciente actualizado.
- Activación/desactivación de pacientes.
- Nuevo psicólogo registrado.
- Psicólogo actualizado.
- Activación/desactivación de psicólogos.
- Nuevo servicio registrado.
- Servicio actualizado.
- Activación/desactivación de servicios.
- Nueva disponibilidad registrada.
- Disponibilidad actualizada.
- Nueva cita registrada.
- Cambio de estado de cita.
- Cita asignada al psicólogo.
- Pago registrado en cita.
- Adelanto validado o rechazado.
- Ingreso registrado.
- Gasto registrado.
- Nueva pre-reserva.
- Conversión de pre-reserva en cita.
- Movimiento crítico de auditoría.

### Destinatarios

- ADMIN.
- RECEPCIONISTA.
- PSICÓLOGO asignado por correo.

### Características visuales

- Campanita interactiva.
- Contador dinámico de no leídas.
- Dropdown profesional.
- Toasts en tiempo real.
- Marcado individual como leído.
- Marcado masivo como leído.
- Recarga automática de módulos relacionados.

---

## 🧾 Auditoría y movimientos del sistema

El sistema incorpora un módulo de auditoría para registrar movimientos administrativos, operativos y de seguridad.

### Eventos auditados

- Inicio de sesión exitoso.
- Intento de inicio de sesión fallido.
- Usuario inactivo intentando ingresar.
- Token inválido.
- Acceso no autorizado.
- Acceso denegado por rol.
- Registro, edición y desactivación de entidades.
- Validación o rechazo de adelantos.
- Conversión de pre-reservas en citas.
- Registro de pagos.
- Movimientos financieros.

### Niveles de auditoría

```text
INFO      → acciones normales, login exitoso, cierre de sesión
WARNING   → login fallido, token inválido, accesos no autorizados
CRITICAL  → usuario inactivo, acceso denegado a módulos sensibles
```

### Funcionalidades del módulo Movimientos

- Filtro por módulo.
- Filtro por nivel.
- Filtro por revisado/pendiente.
- Búsqueda textual.
- Paginación.
- Marcado como revisado.
- Marcado como pendiente.
- Notificación al ADMIN para eventos críticos.

---

## 🔐 Seguridad

Implementado con:

- Spring Security.
- JWT Authentication.
- Control de roles.
- Validación de sesión.
- Protección de endpoints REST.
- Expiración de token.
- Auditoría de accesos.
- Bloqueo por permisos en backend.
- Ocultamiento de módulos según rol en frontend.

---

## 👤 Roles del sistema

### ADMIN

- Acceso total.
- Dashboard global.
- Usuarios.
- Pacientes.
- Psicólogos.
- Servicios.
- Disponibilidad.
- Citas.
- Pagos.
- Finanzas.
- Reportes.
- Movimientos.
- Notificaciones.

### RECEPCIONISTA

- Pacientes.
- Solicitudes/pre-reservas.
- Programación de citas.
- Registro de pagos.
- Consulta de disponibilidad.
- Notificaciones.

### PSICÓLOGO

- Agenda personalizada.
- Visualización de sus citas.
- Historia clínica.
- Notificaciones directas por cita asignada.

---

## ☁️ Infraestructura Cloud

- Frontend y backend desplegados en Render.
- Base de datos MySQL Cloud en Aiven.
- Variables de entorno para credenciales.
- Integración con GitHub.
- Deploy automático.
- Docker para producción.

---

## 🐳 Docker

```dockerfile
FROM eclipse-temurin:21
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## ⚙️ Tecnologías utilizadas

### Backend

- Java 21.
- Spring Boot 3.
- Spring Data JPA.
- Hibernate 6.
- Spring Security.
- JWT.
- WebSocket + STOMP.
- REST API.
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

### Deploy

- Render.
- Docker.
- GitHub.
- Aiven MySQL.

---

## 🎨 UX/UI

- Diseño moderno.
- Sidebar interactivo.
- Dashboard profesional.
- Modales flotantes.
- Timeline clínico.
- Calendario visual.
- Tablas dinámicas.
- Portal emocional y profesional.
- Responsive design.
- Mobile-first design.

---

## 📂 Estructura del proyecto

### Backend

```text
src/main/java/com/centropsicologico/sistema

├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── service
└── service/impl
```

### Frontend

```text
src/main/resources

├── static
│   ├── components
│   ├── css
│   ├── img
│   └── js
└── templates
    ├── login.html
    ├── index.html
    └── portal.html
```

---

## ⚙️ Instalación local

### 1. Clonar repositorio

```bash
git clone https://github.com/rojasbilly95-debug/centropsico-system.git
cd centropsico-system
```

### 2. Crear base de datos

```sql
CREATE DATABASE centro_psicologico;
```

### 3. Configurar `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/centro_psicologico
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. Ejecutar proyecto

```bash
mvn clean spring-boot:run
```

### 5. Abrir en navegador

```text
http://localhost:8080/login.html
http://localhost:8080/portal.html
```

---

## 🔐 Variables de entorno sugeridas

```env
DB_URL=jdbc:mysql://host:puerto/base
DB_USERNAME=usuario
DB_PASSWORD=password
JWT_SECRET=clave_segura
```

---

## 🧪 Endpoints principales

### Auth

```http
POST /api/auth/login
GET  /api/auth/validate
POST /api/auth/logout
```

### Pacientes

```http
POST  /api/patients
GET   /api/patients
PUT   /api/patients/{id}
PATCH /api/patients/{id}/toggle-active
```

### Citas

```http
POST /api/appointments
GET  /api/appointments
GET  /api/appointments/my
GET  /api/appointments/by-date
GET  /api/appointments/my/by-date
PUT  /api/appointments/{id}
PUT  /api/appointments/{id}/status
PUT  /api/appointments/{id}/pay
```

### Portal público

```http
POST /api/public/leads
GET  /api/public/plans
GET  /api/public/services
GET  /api/public/availability
```

### Pre-reservas

```http
GET  /api/leads
GET  /api/leads/{id}
PUT  /api/leads/{id}/status
PUT  /api/leads/{id}/payment/validate
PUT  /api/leads/{id}/payment/reject
POST /api/leads/{id}/convert-to-appointment
```

### Historia clínica

```http
POST   /api/clinical-history/patient/{id}
GET    /api/clinical-history/patient/{id}
DELETE /api/clinical-history/{id}
```

### Finanzas

```http
POST /api/finances/incomes
GET  /api/finances/incomes
POST /api/finances/expenses
GET  /api/finances/expenses
POST /api/finances/expense-categories
GET  /api/finances/expense-categories
GET  /api/finances/summary
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

### Psicólogos y disponibilidad

```http
POST  /api/psychologists
GET   /api/psychologists
PUT   /api/psychologists/{id}
PATCH /api/psychologists/{id}/toggle-active

POST  /api/psychologist-availabilities
GET   /api/psychologist-availabilities
GET   /api/psychologist-availabilities/psychologist/{id}
PUT   /api/psychologist-availabilities/{id}
PATCH /api/psychologist-availabilities/{id}/toggle-active
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

### Reportes

```http
GET /api/reports/monthly
```

---

## 🧠 Metodología

Proyecto desarrollado utilizando:

- Scrum.
- Desarrollo incremental.
- Sprints.
- Validación continua.
- Modularización por funcionalidades.

---

## 🎯 Problemas que resuelve

- Desorganización de citas.
- Doble reserva de horarios.
- Gestión manual de pacientes.
- Falta de seguimiento clínico.
- Mala gestión financiera.
- Ausencia de reportes.
- Mala comunicación con interesados.
- Falta de control sobre pagos y adelantos.
- Ausencia de trazabilidad sobre movimientos del sistema.
- Falta de control de accesos por rol.

---

## 🛣️ Roadmap

- [ ] Consulta pública de estado de pre-reserva.
- [ ] Botón flotante de WhatsApp.
- [ ] Preguntas frecuentes en portal público.
- [ ] Exportación PDF de comprobantes.
- [ ] Exportación Excel/PDF de reportes.
- [ ] Historial avanzado de pagos por cita.
- [ ] Dashboard analítico con filtros por rango de fechas.
- [ ] Limpieza automática de movimientos antiguos.
- [ ] Exportación de auditoría.
- [ ] Portal del paciente.
- [ ] Videollamadas psicológicas.
- [ ] Multi-sede.
- [ ] Integración con pasarelas de pago.

---

## 📸 Capturas del sistema

### Portal público

```text
docs/portal.png
```

### Dashboard

```text
docs/dashboard.png
```

### Calendario

```text
docs/calendar.png
```

> Agrega las imágenes reales en la carpeta `docs/` para que se visualicen en GitHub.

---

## 👨‍💻 Autor

Billy Rojas

Proyecto desarrollado como plataforma integral de gestión clínica y atención psicológica, orientada a optimizar la administración interna, el seguimiento de pacientes y la experiencia de atención desde el portal público hasta el agendamiento clínico.

---

## 📄 Licencia

Proyecto desarrollado con fines educativos, profesionales y de portafolio.
