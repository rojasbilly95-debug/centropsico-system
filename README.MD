# 🧠 CentroPsico — Plataforma Integral de Gestión y Atención Psicológica

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-red)
![Responsive](https://img.shields.io/badge/UI-Responsive-purple)
![Render](https://img.shields.io/badge/Deploy-Render-black)
![Aiven](https://img.shields.io/badge/Database-Aiven-blueviolet)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED)

Sistema web profesional desarrollado para la administración integral de un centro psicológico, permitiendo gestionar pacientes, psicólogos, citas, pagos, historia clínica, finanzas, reportes y notificaciones en tiempo real desde una sola plataforma moderna, segura y escalable.

Además del sistema interno de administración clínica, la plataforma incorpora un portal público moderno donde las personas pueden conocer los servicios psicológicos, revisar modalidades de atención y realizar pre-reservas inteligentes con validación de disponibilidad real.

El sistema busca centralizar la gestión clínica, mejorar la organización administrativa y optimizar la experiencia de atención desde el primer contacto hasta el seguimiento clínico.

---

# 🌍 Demo Online

🔗 https://centropsico-system.onrender.com

> Proyecto desplegado en Render + Aiven Cloud Database

---

# 🚀 Características principales

* Plataforma completamente responsive
* Compatible con móviles, tablets y desktop
* Portal público moderno
* Dashboard administrativo profesional
* Gestión clínica integral
* Sistema financiero integrado
* Historia clínica digital
* Seguridad JWT
* Notificaciones en tiempo real
* Arquitectura cloud moderna

---

# 🌐 Portal público y pre-reservas inteligentes

El sistema incorpora un portal web moderno orientado a pacientes y personas interesadas en recibir atención psicológica.

## Funcionalidades del portal

* Visualización de servicios psicológicos
* Modalidades de atención
* Diseño UX/UI profesional y emocional
* Información clara sobre terapias
* Consulta pública de disponibilidad real
* Visualización de horarios disponibles por psicólogo
* Selección dinámica de fecha y horario
* Pre-reservas inteligentes
* Registro de adelanto
* Validación administrativa del pago
* Conversión automática de pre-reserva en cita
* Asociación automática con paciente registrado
* Registro financiero automático del adelanto
* Responsive design profesional
* Experiencia mobile-first

---

# 🧠 Disponibilidad pública inteligente

La disponibilidad se calcula dinámicamente considerando:

* Servicio psicológico seleccionado
* Duración de atención
* Disponibilidad semanal del psicólogo
* Horarios ocupados
* Fecha seleccionada
* Cruces de citas
* Bloqueos automáticos

El sistema evita reservas inválidas y genera horarios disponibles reales automáticamente.

---

# 🔄 Flujo de atención

```text
Portal público
↓
Persona consulta disponibilidad real
↓
Selecciona servicio, fecha, psicólogo y horario
↓
Registra adelanto con método de pago
↓
Recepción valida el adelanto
↓
Sistema convierte la pre-reserva en cita
↓
El adelanto se registra como pago parcial
↓
Ingreso financiero automático
```

# 📌 Estados de pre-reserva

* NUEVO
* PAGO_EN_REVISION
* CONTACTADO
* PRE_RESERVADO
* AGENDADO
* DESCARTADO

---

# 👥 Gestión de usuarios y personas

## Pacientes

* Registro de pacientes
* Validación de DNI único
* Historial clínico digital
* Estado activo/inactivo
* Búsqueda dinámica
* Paginación profesional

## Psicólogos

* Registro de psicólogos
* Especialidades
* Disponibilidad semanal configurable
* Agenda personalizada
* Visualización exclusiva de sus citas

## Usuarios del sistema

### Roles implementados

* ADMIN
* RECEPCIONISTA
* PSICÓLOGO

### Funciones

* Crear usuarios
* Editar usuarios
* Activar/desactivar usuarios
* Seguridad por roles
* Restricción dinámica de módulos

---

# 📅 Gestión avanzada de citas

## Características

* Programación inteligente de citas
* Validación automática de conflictos horarios
* Generación dinámica de horarios disponibles
* Bloqueo automático de horarios ocupados
* Calendario interactivo FullCalendar
* Agenda tipo cronograma
* Visualización personalizada por rol

## Estados de citas

* PROGRAMADA
* ATENDIDA
* CANCELADA
* NO_ASISTIÓ
* REPROGRAMADA

---

# 🧠 Historia clínica digital

Sistema clínico profesional con:

* Evolución del paciente
* Diagnósticos
* Recomendaciones
* Observaciones clínicas
* Timeline clínico visual
* Registro cronológico

## Acceso restringido

Solo:

* ADMIN
* PSICÓLOGO

---

# 💳 Sistema de pagos profesional

## Funcionalidades

* Registro completo de pagos
* Adelantos desde portal público
* Conversión automática de adelanto en pago parcial
* Integración con finanzas
* Comprobante visual profesional
* Historial de pagos
* Actualización en tiempo real

## Métodos soportados

* Efectivo
* Yape
* Plin
* Transferencia
* Tarjeta

---

# 💰 Gestión financiera

## Ingresos

* Registro automático por pagos
* Registro manual
* Integración con citas
* Integración con pre-reservas

## Gastos

* Registro de gastos
* Categorías dinámicas
* Responsable
* Control mensual

---

# 📊 Dashboard inteligente

Indicadores dinámicos:

* Pacientes registrados
* Citas del día
* Ingresos
* Gastos
* Resultado financiero
* Agenda diaria
* Gráficas interactivas
* Resumen financiero

---

# 📈 Reportes

## Reporte mensual

Incluye:

* Total de citas
* Citas atendidas
* Canceladas
* No asistieron
* Ingresos
* Gastos
* Resultado financiero

---

# 🔔 Notificaciones en tiempo real

Implementado con WebSocket + STOMP.

## Eventos en tiempo real

### Pacientes

* Nuevo paciente registrado

### Citas

* Nueva cita
* Cita actualizada
* Estado actualizado

### Finanzas

* Nuevo gasto
* Nuevo ingreso

### Portal público

* Nueva pre-reserva
* Validación de adelantos
* Seguimiento administrativo

## Características visuales

* Campanita interactiva
* Contador dinámico
* Dropdown profesional
* Toasts en tiempo real
* Marcado de leído

---

# 🔐 Seguridad

Implementado con:

* Spring Security
* JWT Authentication
* Control de roles
* Validación de sesión
* Protección de endpoints REST
* Expiración de sesión

---

# 👤 Roles del sistema

## ADMIN

* Acceso total
* Finanzas
* Reportes
* Usuarios
* Psicólogos
* Servicios
* Dashboard global

## RECEPCIONISTA

* Gestión de pacientes
* Programación de citas
* Registro de pagos
* Atención de pre-reservas

## PSICÓLOGO

* Agenda personalizada
* Visualización de sus citas
* Historia clínica

---

# ☁️ Infraestructura Cloud

El sistema se encuentra desplegado utilizando arquitectura cloud moderna:

* Frontend + Backend desplegados en Render
* Base de datos MySQL Cloud en Aiven
* Variables de entorno seguras
* Integración continua con GitHub
* Deploy automático
* Dockerizado para producción

---

# 🐳 Docker

El sistema utiliza Docker para despliegue y portabilidad.

## Dockerfile

```dockerfile
FROM eclipse-temurin:21
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

# ⚙️ Tecnologías utilizadas

## Backend

* Java 21
* Spring Boot 3
* Spring Data JPA
* Hibernate 6
* Spring Security
* JWT
* WebSocket
* REST API

## Frontend

* HTML5
* CSS3
* JavaScript Vanilla
* FullCalendar
* Chart.js
* SweetAlert2
* Lucide Icons

## Base de datos

* MySQL
* Aiven Cloud

## Deploy

* Render
* Docker
* GitHub

---

# 🎨 UX/UI

* Diseño moderno
* Sidebar interactivo
* Dashboard profesional
* Modales flotantes
* Timeline clínico
* Calendario visual
* Tablas dinámicas
* Responsive Design
* Portal emocional y profesional
* Mobile-first design

---

# 📱 Responsive Design

El sistema fue adaptado completamente para dispositivos móviles y tablets.

## Características responsive

* Sidebar mobile interactivo
* Menú hamburguesa
* Formularios adaptativos
* Tablas responsive
* Dashboard responsive
* Portal mobile-first
* Modales adaptativos
* Compatibilidad con iPhone y Android

---

# 📂 Estructura del proyecto

## Backend

```text
src/main/java/com/centropsicologico/sistema

├── controller
├── service
├── service/impl
├── repository
├── entity
├── dto
├── enums
├── config
├── exception
```

## Frontend

```text
static/

├── components/
├── css/
├── js/
├── img/
├── index.html
├── login.html
└── portal.html
```

---

# ⚙️ Instalación local

## 1. Clonar repositorio

```bash
git clone https://github.com/rojasbilly95-debug/centropsico-system.git
```

## 2. Crear base de datos

```sql
CREATE DATABASE centro_psicologico;
```

## 3. Configurar application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/centro_psicologico
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

## 4. Ejecutar proyecto

```bash
mvn spring-boot:run
```

---

# 🔐 Variables de entorno

El proyecto utiliza variables de entorno para proteger credenciales sensibles.

## Render

```env
DB_PASSWORD=tu_password
```

---

# 🧪 Endpoints principales

## 🔐 Auth

```http
POST /api/auth/login
GET  /api/auth/validate
```

## 👥 Pacientes

```http
POST  /api/patients
GET   /api/patients
PUT   /api/patients/{id}
PATCH /api/patients/{id}/toggle-active
```

## 📅 Citas

```http
POST /api/appointments
GET  /api/appointments
GET  /api/appointments/my
PUT  /api/appointments/{id}
PUT  /api/appointments/{id}/status
```

## 🌐 Portal público

```http
POST /api/public/leads
GET  /api/public/plans
GET  /api/public/services
GET  /api/public/availability
```

## 🧠 Historia clínica

```http
POST   /api/clinical-history/patient/{id}
GET    /api/clinical-history/patient/{id}
DELETE /api/clinical-history/{id}
```

## 💰 Finanzas

```http
POST /api/incomes
POST /api/expenses
GET  /api/finances/summary
```

---

# 🧠 Metodología

Proyecto desarrollado utilizando:

* Scrum
* Desarrollo incremental
* Sprints
* Validación continua
* Modularización por funcionalidades

---

# 🎯 Problemas que resuelve

* Desorganización de citas
* Mala gestión financiera
* Doble reserva de horarios
* Falta de seguimiento clínico
* Gestión manual de pacientes
* Ausencia de reportes
* Mala comunicación con interesados
* Procesos manuales de atención

---

# 🛣️ Roadmap

* [ ] WhatsApp Business API
* [ ] Videollamadas psicológicas
* [ ] Portal del paciente
* [ ] IA para apoyo clínico
* [ ] Multi-sede
* [ ] Exportación PDF
* [ ] Dashboard analítico avanzado
* [ ] Integración Stripe/Yape

---

# 📸 Capturas del sistema

## Portal público

<img width="100%" src="docs/portal.png">

## Dashboard

<img width="100%" src="docs/dashboard.png">

## Calendario

<img width="100%" src="docs/calendar.png">

---

# 👨‍💻 Autor

Billy Rojas

Proyecto desarrollado como plataforma integral de gestión clínica y atención psicológica, orientada a optimizar la administración interna, el seguimiento de pacientes y la experiencia de atención desde el portal público hasta el agendamiento clínico.

---

# 📄 Licencia

Proyecto desarrollado con fines educativos, profesionales y de portafolio.
