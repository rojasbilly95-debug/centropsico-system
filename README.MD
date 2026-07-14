# 🧠 CentroPsico — Plataforma Integral de Gestión y Atención Psicológica

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-red)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket_STOMP-purple)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED)
![Render](https://img.shields.io/badge/Deploy-Render-black)
![Aiven](https://img.shields.io/badge/Database-Aiven-blueviolet)

CentroPsico es una plataforma web integral desarrollada para la gestión administrativa, clínica, financiera y operativa de un centro psicológico. El sistema permite centralizar pacientes, psicólogos, servicios, disponibilidad, citas, pagos, pre-reservas, promociones, reportes, auditoría, seguridad y notificaciones en tiempo real desde una sola aplicación moderna, segura y escalable.

Además del sistema interno, incluye un portal público donde las personas interesadas pueden revisar servicios psicológicos, consultar disponibilidad real, seleccionar horarios, registrar datos de contacto, indicar método de pago, aceptar el tratamiento de datos personales y enviar una pre-reserva para su posterior validación administrativa.

---

## 🌍 Demo Online

🔗 https://centropsico-system.onrender.com

> Proyecto desplegado en Render con base de datos MySQL en Aiven.

---

## 🚀 Funcionalidades principales

- Sistema interno con autenticación JWT.
- Control de acceso por roles: ADMIN, RECEPCIONISTA y PSICOLOGO.
- Portal público para servicios, promociones y pre-reservas.
- Gestión de pacientes activos e inactivos.
- Gestión de psicólogos, especialidades y disponibilidad semanal.
- Gestión de servicios psicológicos con duración y costo.
- Programación de citas con validación de horarios disponibles.
- Agenda diaria y calendario interactivo con FullCalendar.
- Vista personalizada de citas para psicólogos.
- Registro de pagos parciales o completos.
- Generación visual de comprobantes de pago.
- Registro automático de ingresos financieros desde pagos.
- Gestión de gastos, categorías y responsables.
- Eliminación lógica de gastos para mantener trazabilidad.
- Historial mensual de gastos.
- Dashboard con indicadores operativos y financieros.
- Reportes mensuales y reporte por psicólogo.
- Promociones visibles en el portal público.
- Notificaciones internas y en tiempo real con WebSocket + STOMP.
- Auditoría de movimientos administrativos y de seguridad.
- Perfil de usuario con foto y datos personales.
- Diseño responsive para escritorio, tablet y móvil.

---

## 🧩 Módulos del sistema

### Sistema interno

```text
Dashboard
Solicitudes / Pre-reservas
Pacientes
Citas
Finanzas
Reportes
Promociones
Movimientos / Auditoría
Administración
 ├── Usuarios
 ├── Psicólogos
 ├── Servicios
 └── Disponibilidad
Perfil de usuario
Notificaciones
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

## 👤 Roles del sistema

### ADMIN

Rol con acceso completo al sistema.

Puede realizar:

- Gestión de usuarios.
- Gestión de pacientes.
- Gestión de psicólogos.
- Gestión de servicios.
- Gestión de disponibilidad.
- Gestión de citas.
- Registro y control de pagos.
- Gestión de ingresos y gastos.
- Consulta de reportes.
- Gestión de promociones.
- Revisión de auditoría y movimientos.
- Envío de notificaciones directas.
- Acceso a dashboard completo.
- Acceso a historia clínica.

### RECEPCIONISTA

Rol operativo para la atención administrativa.

Puede realizar:

- Gestión de solicitudes y pre-reservas.
- Registro y edición de pacientes.
- Programación de citas.
- Consulta de disponibilidad.
- Registro de pagos.
- Cambio de estado de citas.
- Consulta de agenda diaria.
- Recepción de notificaciones.

No tiene acceso a:

- Finanzas generales.
- Reportes.
- Promociones.
- Auditoría.
- Administración de usuarios.
- Administración de psicólogos.
- Administración de servicios.

### PSICOLOGO

Rol clínico orientado a la atención del paciente.

Puede realizar:

- Ver su dashboard.
- Consultar solo sus citas asignadas.
- Revisar su agenda.
- Ver calendario de sus citas.
- Marcar citas como atendidas o no asistidas.
- Acceder a historia clínica.
- Recibir notificaciones de citas asignadas.

No tiene acceso a:

- Crear citas.
- Registrar pagos.
- Cancelar citas.
- Ver pacientes generales.
- Finanzas.
- Reportes.
- Promociones.
- Auditoría.
- Administración del sistema.

---

## 🔐 Seguridad y control de acceso

El sistema implementa seguridad con Spring Security y JWT.

Características:

- Autenticación mediante token JWT.
- Validación de sesión protegida.
- Control de endpoints por rol.
- Protección de rutas REST.
- Bloqueo de accesos no autorizados.
- Auditoría de accesos denegados.
- Separación entre rutas públicas y privadas.
- Ocultamiento de módulos en frontend según rol.
- Validación de usuario activo/inactivo.
- Registro de intentos de acceso no autorizados.

### Reglas principales por rol

```text
ADMIN:
Acceso completo.

RECEPCIONISTA:
Solicitudes, pacientes, citas y notificaciones.

PSICOLOGO:
Dashboard, citas asignadas, historia clínica y notificaciones.
```

### Control de citas

Las citas cuentan con restricciones específicas:

```text
ADMIN / RECEPCIONISTA:
- Crear citas.
- Ver citas generales.
- Registrar pagos.
- Cambiar estados.
- Cancelar citas.

PSICOLOGO:
- Ver solo sus citas.
- Ver su agenda por fecha.
- Marcar cita como atendida.
- Marcar cita como no asistió.
```

---

## 🌐 Portal público y pre-reservas

El portal público permite que una persona interesada consulte información del centro psicológico y registre una pre-reserva.

### Funcionalidades

- Visualización de servicios psicológicos.
- Visualización de promociones activas.
- Consulta pública de disponibilidad real.
- Selección de servicio, psicólogo, fecha y horario.
- Cálculo referencial de adelanto.
- Registro de método de pago.
- Registro de código de operación.
- Registro de mensaje adicional.
- Aceptación obligatoria del consentimiento de datos.
- Envío de pre-reserva al sistema interno.
- Notificación automática a ADMIN y RECEPCIONISTA.

### Datos registrados

- Nombre completo.
- Correo electrónico opcional.
- Teléfono o WhatsApp.
- Servicio de interés.
- Psicólogo preferido.
- Fecha y hora seleccionada.
- Modalidad de atención.
- Método de pago.
- Código de operación.
- Mensaje adicional.
- Consentimiento aceptado.
- Fecha y versión del consentimiento.

---

## 📌 Estados de pre-reserva

```text
NUEVO
PAGO_EN_REVISION
CONTACTADO
PRE_RESERVADO
AGENDADO
DESCARTADO
```

### Flujo de pre-reserva

```text
Portal público
↓
Persona revisa servicios, promociones y disponibilidad
↓
Selecciona servicio, psicólogo, fecha y horario
↓
Registra datos personales y datos de pago
↓
Acepta tratamiento de datos personales
↓
Sistema genera pre-reserva
↓
ADMIN y RECEPCIONISTA reciben notificación
↓
Recepción valida o rechaza adelanto
↓
Si procede, se asocia a un paciente
↓
La pre-reserva se convierte en cita
↓
El adelanto se registra como pago
↓
Se genera ingreso financiero
↓
El psicólogo asignado recibe notificación
```

---

## 🧠 Disponibilidad inteligente

La disponibilidad se calcula dinámicamente considerando:

- Psicólogo seleccionado.
- Servicio seleccionado.
- Duración del servicio.
- Día de atención configurado.
- Hora de inicio y hora de fin.
- Citas ya registradas.
- Cruces de horario.
- Estado de la cita.
- Horarios ocupados.
- Horarios libres.

Esto evita dobles reservas y permite mostrar horarios disponibles reales.

---

## 📅 Gestión de citas

### Funcionalidades

- Registro de citas.
- Validación automática de disponibilidad.
- Generación dinámica de horarios disponibles.
- Bloqueo de horarios ocupados.
- Agenda diaria.
- Calendario interactivo.
- Vista mensual, semanal, diaria y lista.
- Actualización de estados.
- Vista personalizada por rol.
- Notificación al psicólogo asignado.

### Estados de cita

```text
PROGRAMADA
ATENDIDA
CANCELADA
NO_ASISTIO
REPROGRAMADA
```

---

## 💳 Pagos y comprobantes

### Funcionalidades

- Registro de pagos por cita.
- Pagos parciales.
- Pagos completos.
- Validación de saldo pendiente.
- Bloqueo de pago en citas canceladas.
- Bloqueo de pago en citas con estado no asistió.
- Conversión de adelantos en pagos.
- Comprobante visual profesional.
- Impresión de comprobante.
- Registro automático en ingresos.
- Actualización de dashboard financiero.

### Métodos de pago

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

- Registro manual de ingresos.
- Registro automático desde pagos de citas.
- Registro de adelantos.
- Método de pago.
- Fecha de ingreso.
- Integración con dashboard.

### Gastos

- Registro de gastos.
- Categorías dinámicas.
- Responsable del gasto.
- Fecha del gasto.
- Filtros por año, mes, categoría, estado y responsable.
- Historial mensual.
- Eliminación lógica mediante estado inactivo.
- Auditoría de eliminación.

### Resumen financiero

El sistema permite consultar:

- Total de ingresos.
- Total de gastos.
- Ganancia o pérdida mensual.
- Historial de gastos por mes.
- Balance operativo.

---

## 📊 Dashboard

El dashboard muestra indicadores operativos y financieros según el rol.

### Indicadores

- Total de pacientes.
- Citas por periodo.
- Ingresos por periodo.
- Citas pendientes de pago.
- Citas por estado.
- Ingresos vs gastos.
- Agenda relevante.
- Recordatorios.
- Notificaciones.

### Periodos disponibles

```text
Hoy
Esta semana
Este mes
Este año
```

---

## 📈 Reportes

El sistema incluye reportes para análisis administrativo.

### Reporte mensual

Permite consultar:

- Total de citas.
- Citas atendidas.
- Citas canceladas.
- Citas no asistidas.
- Ingresos.
- Gastos.
- Resultado financiero.

### Reporte por psicólogo

Permite analizar:

- Psicólogo.
- Total de pacientes atendidos.
- Total de citas atendidas.
- Terapias o servicios realizados.
- Rendimiento por periodo.

---

## 🧠 Historia clínica digital

La historia clínica permite registrar información clínica del paciente.

Incluye:

- Evolución.
- Diagnóstico.
- Recomendaciones.
- Observaciones.
- Timeline clínico.
- Registro cronológico.

Acceso restringido a:

```text
ADMIN
PSICOLOGO
```

---

## 🔔 Notificaciones en tiempo real

El sistema utiliza WebSocket + STOMP para emitir notificaciones en tiempo real.

### Características

- Notificaciones persistidas en base de datos.
- Notificaciones por rol.
- Notificaciones por usuario.
- Campanita interactiva.
- Contador de no leídas.
- Vista previa del mensaje.
- Modal con mensaje completo.
- Marcado individual como leído.
- Marcado masivo como leído.
- Toasts en tiempo real.
- Recarga automática de módulos relacionados.

### Eventos notificados

- Nuevo paciente registrado.
- Paciente actualizado.
- Nuevo psicólogo registrado.
- Psicólogo actualizado.
- Nuevo servicio registrado.
- Servicio actualizado.
- Nueva disponibilidad registrada.
- Nueva cita registrada.
- Cita asignada a psicólogo.
- Cambio de estado de cita.
- Pago registrado.
- Adelanto validado.
- Adelanto rechazado.
- Ingreso registrado.
- Gasto registrado.
- Gasto eliminado lógicamente.
- Nueva pre-reserva.
- Conversión de pre-reserva en cita.
- Promoción registrada.
- Movimiento crítico de auditoría.
- Mensaje directo del administrador.

---

## 🧾 Auditoría y movimientos

El módulo de auditoría registra acciones administrativas, operativas y de seguridad.

### Eventos auditados

- Inicio de sesión exitoso.
- Intento de inicio de sesión fallido.
- Usuario inactivo intentando ingresar.
- Token inválido.
- Acceso no autorizado.
- Acceso denegado por rol.
- Registro de entidades.
- Actualización de entidades.
- Activación o desactivación.
- Validación de adelantos.
- Rechazo de adelantos.
- Conversión de pre-reservas en citas.
- Registro de pagos.
- Movimientos financieros.
- Eliminación lógica de gastos.
- Notificaciones directas.

### Niveles

```text
INFO      → acciones normales
WARNING   → intentos fallidos o accesos no autorizados
CRITICAL  → accesos denegados a módulos sensibles
```

### Funcionalidades

- Filtro por módulo.
- Filtro por nivel.
- Filtro por revisado.
- Búsqueda textual.
- Paginación.
- Marcar como revisado.
- Marcar como pendiente.

---

## 🎁 Promociones

El sistema permite al administrador registrar promociones visibles en el portal público.

### Funcionalidades

- Registrar promoción.
- Editar promoción.
- Activar o desactivar promoción.
- Definir porcentaje de descuento.
- Definir fecha de inicio.
- Definir fecha de fin.
- Mostrar promoción principal destacada.
- Mostrar promociones secundarias.
- Ocultar promociones vencidas o inactivas.

---

## 👤 Perfil de usuario

Cada usuario puede revisar y actualizar su perfil.

Incluye:

- Foto de perfil.
- Nombre de usuario.
- Rol.
- Correo.
- Teléfono.
- Cambio de contraseña.
- Visualización en sidebar.

---

## ☁️ Infraestructura Cloud

- Backend y frontend servidos desde Spring Boot.
- Despliegue en Render.
- Base de datos MySQL en Aiven.
- Integración con GitHub.
- Deploy automático desde la rama principal.
- Variables de entorno para credenciales sensibles.
- Docker para producción.

---

## ⚙️ Tecnologías utilizadas

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

### Base de datos y despliegue

- MySQL.
- Aiven MySQL.
- Render.
- Docker.
- GitHub.

---

## 📂 Estructura del proyecto

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

## 🧪 Endpoints principales

### Autenticación

```http
POST /api/auth/login
GET  /api/auth/validate
POST /api/auth/logout
```

### Perfil

```http
GET  /api/profile/me
PUT  /api/profile/me
PUT  /api/profile/change-password
POST /api/profile/photo
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
POST /api/appointments
GET  /api/appointments
GET  /api/appointments/{id}
GET  /api/appointments/by-date
GET  /api/appointments/my
GET  /api/appointments/my/by-date
PUT  /api/appointments/{id}
PUT  /api/appointments/{id}/status
PUT  /api/appointments/{id}/pay
DELETE /api/appointments/{id}
```

### Portal público

```http
GET  /api/public/services
GET  /api/public/promotions
GET  /api/public/availability
POST /api/public/leads
```

### Solicitudes / pre-reservas

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

### Notificaciones directas de ADMIN

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

### Historia clínica

```http
POST   /api/clinical-history/patient/{id}
GET    /api/clinical-history/patient/{id}
DELETE /api/clinical-history/{id}
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

### 1. Clonar repositorio

```bash
git clone https://github.com/rojasbilly95-debug/centropsico-system.git
cd centropsico-system
```

### 2. Crear base de datos local

```sql
CREATE DATABASE centro_psicologico;
```

### 3. Configurar `application.properties`

Ejemplo para entorno local:

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

Para producción se recomienda usar variables de entorno y no exponer credenciales en el repositorio.

```env
DB_URL=jdbc:mysql://host:puerto/base
DB_USERNAME=usuario
DB_PASSWORD=password
JWT_SECRET=clave_segura
```

---

## 🐳 Docker

Ejemplo de `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## 🧪 Pruebas por rol

### ADMIN

Debe validar:

- Login correcto.
- Visualización de todos los módulos.
- Registro de usuario.
- Registro de paciente.
- Registro de psicólogo.
- Registro de servicio.
- Registro de disponibilidad.
- Registro de cita.
- Registro de pago.
- Registro de gasto.
- Consulta de reportes.
- Registro de promoción.
- Revisión de movimientos.
- Recepción de notificaciones.

### RECEPCIONISTA

Debe validar:

- Login correcto.
- Visualización de dashboard.
- Visualización de solicitudes.
- Gestión de pacientes.
- Gestión de citas.
- Registro de pagos.
- Cambio de estado de citas.
- Recepción de notificaciones.
- Bloqueo visual y backend de módulos administrativos.

### PSICOLOGO

Debe validar:

- Login correcto.
- Visualización de dashboard.
- Visualización solo de sus citas.
- Agenda personalizada.
- Calendario personalizado.
- Cambio de estado a atendida o no asistió.
- Acceso a historia clínica.
- Recepción de notificaciones.
- Bloqueo de pagos, finanzas, reportes, pacientes generales y administración.

---

## 🎨 UX/UI

- Login profesional.
- Portal público responsive.
- Sidebar interactivo.
- Dashboard moderno.
- Modales flotantes.
- Calendario visual.
- Tablas dinámicas.
- Filtros de búsqueda.
- Campanita de notificaciones.
- Comprobantes visuales.
- Promociones destacadas.
- Diseño responsive.

---

## 🎯 Problemas que resuelve

- Desorganización de citas.
- Doble reserva de horarios.
- Gestión manual de pacientes.
- Falta de control de disponibilidad.
- Falta de seguimiento clínico.
- Falta de trazabilidad en pagos.
- Mala gestión financiera.
- Ausencia de reportes administrativos.
- Comunicación limitada con interesados.
- Falta de control sobre adelantos.
- Falta de auditoría.
- Falta de control de accesos por rol.
- Ausencia de notificaciones en tiempo real.

---

## 🛣️ Roadmap

- [ ] Consulta pública de estado de pre-reserva.
- [ ] Botón flotante de WhatsApp.
- [ ] Preguntas frecuentes en portal público.
- [ ] Exportación PDF de comprobantes.
- [ ] Exportación Excel/PDF de reportes.
- [ ] Historial avanzado de pagos por cita.
- [ ] Dashboard analítico con rango personalizado.
- [ ] Exportación de auditoría.
- [ ] Portal del paciente.
- [ ] Videollamadas psicológicas.
- [ ] Multi-sede.
- [ ] Integración con pasarela de pago.

---

## 📸 Capturas del sistema

Agrega capturas reales en una carpeta `docs/` para que se visualicen en GitHub.

```text
docs/portal.png
docs/login.png
docs/dashboard.png
docs/citas.png
docs/finanzas.png
docs/reportes.png
docs/auditoria.png
```

Ejemplo:

```md
![Portal público](docs/portal.png)
![Dashboard](docs/dashboard.png)
![Calendario de citas](docs/citas.png)
```

---

## 👨‍💻 Autor

**Billy Rojas**

Proyecto desarrollado como plataforma integral de gestión clínica y atención psicológica, orientada a optimizar la administración interna, el seguimiento de pacientes, el control financiero y la experiencia de atención desde el portal público hasta el agendamiento clínico.