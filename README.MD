# 🧠 CentroPsico — Plataforma Integral de Gestión y Atención Psicológica

Sistema web profesional desarrollado para la administración integral de un centro psicológico, permitiendo gestionar pacientes, psicólogos, citas, pagos, historia clínica, finanzas, reportes y notificaciones en tiempo real desde una sola plataforma moderna y segura.

Además del sistema interno de administración clínica, la plataforma incorpora un portal público moderno donde las personas pueden conocer los servicios psicológicos, revisar modalidades de atención y enviar pre-reservas para ser orientadas por el equipo del centro.

El sistema busca centralizar la gestión clínica, mejorar la organización administrativa y optimizar la experiencia de atención desde el primer contacto hasta el seguimiento clínico.

---

# 🚀 Características principales

# 🌐 Portal público y pre-reservas

El sistema incorpora un portal web moderno orientado a pacientes y personas interesadas en recibir atención psicológica.

## Funcionalidades del portal

- Visualización de servicios psicológicos
- Modalidades de atención
- Diseño UX/UI profesional y emocional
- Información clara sobre terapias
- Proceso guiado de pre-reserva
- Solicitudes de atención en línea
- Consulta pública de horarios disponibles
- Visualización de disponibilidad por psicólogo
- Selección de fecha y horario desde el portal
- Pre-reserva vinculada a disponibilidad real
- Seguimiento administrativo de interesados
- Experiencia responsive y moderna
- Cálculo automático de adelanto para separar horario
- Registro de método de pago y código de operación
- Pre-reserva con estado de pago en revisión
- Conversión de pre-reserva validada en cita
- Asociación de pre-reserva con paciente registrado
- Registro automático del adelanto como pago parcial
- Vinculación del adelanto con finanzas
---

## Disponibilidad pública inteligente

El portal permite consultar horarios disponibles reales según:

- Servicio psicológico seleccionado
- Duración de atención
- Disponibilidad del psicólogo
- Horarios ocupados
- Fecha elegida por el interesado

La disponibilidad se calcula dinámicamente evitando cruces de citas y horarios no disponibles.

---

## Flujo de atención

```text
Portal público
↓
Persona consulta disponibilidad real
↓
Selecciona servicio, fecha, psicólogo y horario
↓
Registra adelanto con método de pago y código de operación
↓
Recepción valida el adelanto
↓
Recepción crea o selecciona paciente
↓
Sistema convierte la pre-reserva en cita
↓
El adelanto se registra como pago parcial e ingreso financiero
````
## Estados de pre-reserva

- NUEVO
- PAGO_EN_REVISION
- CONTACTADO
- PRE_RESERVADO
- AGENDADO
- DESCARTADO

👥 Gestión de usuarios y personas
Pacientes
Registro de pacientes
Validación de DNI único
Historial clínico digital
Estado activo/inactivo
Búsqueda dinámica
Paginación profesional

Psicólogos
Registro de psicólogos
Especialidades
Disponibilidad semanal configurable
Agenda personalizada
Visualización exclusiva de sus citas

Usuarios del sistema

Gestión completa de usuarios con roles:

ADMIN
RECEPCIONISTA
PSICÓLOGO

Funciones
Crear usuarios
Editar usuarios
Activar/desactivar usuarios
Seguridad por roles

📅 Gestión avanzada de citas
Características
Programación inteligente de citas
Validación automática de conflictos horarios
Generación dinámica de horarios disponibles
Bloqueo de horarios ocupados
Agenda tipo cronograma
Calendario interactivo (FullCalendar)
Visualización personalizada por rol

🧠 Lógica de disponibilidad profesional

Cada psicólogo puede tener múltiples horarios semanales.

Ejemplo
Día	Horario
Lunes	08:00 - 13:00
Lunes	15:00 - 20:00
Miércoles	09:00 - 18:00
Viernes	08:00 - 14:00

Validaciones automáticas

El sistema:

Detecta cruces de horarios
Evita doble reserva
Genera horarios disponibles automáticamente
Calcula duración según el servicio
Oculta horarios ocupados
Restringe días no disponibles


📋 Estados de citas
PROGRAMADA
ATENDIDA
CANCELADA
NO_ASISTIÓ
REPROGRAMADA

🧠 Historia clínica digital

Sistema clínico profesional:

Evolución del paciente
Diagnósticos
Recomendaciones
Observaciones
Timeline clínico visual
Registro cronológico

Acceso restringido

Solo:

ADMIN
PSICÓLOGO

💳 Sistema de pagos profesional
- Registro completo de pagos
- Monto
- Método de pago
- Código de operación
- Observación
- Usuario responsable
- Fecha y hora
- Adelanto desde el portal público
- Validación interna de pagos de pre-reserva
- Comprobante visual de adelanto
- Conversión de adelanto validado en pago parcial de cita
- Registro automático del adelanto como ingreso financiero
- Cálculo de saldo pendiente después del adelanto

Métodos soportados
- Efectivo
- Yape
- Plin
- Transferencia
- Tarjeta

Funciones avanzadas
Generación automática de ingresos
Integración con finanzas
Comprobante visual profesional
Historial de pagos
Actualización en tiempo real

💰 Gestión financiera
Ingresos
Registro automático por pagos
Registro manual

Gastos
Registro de gastos
Categorías dinámicas
Responsable
Control mensual

📊 Dashboard inteligente

Indicadores dinámicos:

Pacientes registrados
Citas del día
Ingresos
Gastos
Resultado financiero
Agenda diaria
Gráficas interactivas
Resumen financiero

📈 Reportes
Reporte mensual

Incluye:

Total de citas
Citas atendidas
Canceladas
No asistieron
Ingresos
Gastos
Resultado final

🔔 Notificaciones en tiempo real

Implementado con WebSocket + STOMP.

Eventos en tiempo real
Pacientes
Nuevo paciente registrado
Citas
Nueva cita
Cita actualizada
Estado actualizado
Pago registrado
Finanzas
Nuevo gasto
Nuevo ingreso
Administración
Usuario creado/editado/desactivado
Servicio creado/editado/desactivado
Portal público
Nueva pre-reserva registrada
Seguimiento administrativo en tiempo real
Solicitudes de disponibilidad pública
Características visuales
Campanita interactiva
Contador dinámico
Dropdown profesional
Animaciones
Toasts en tiempo real
Marcado de leído
🔐 Seguridad

Implementado con:

Spring Security
JWT Authentication
Control de roles
Validación de sesión
Protección de endpoints REST
Expiración de sesión
👤 Roles del sistema
ADMIN
Acceso total
Finanzas
Reportes
Usuarios
Psicólogos
Servicios
Disponibilidad
Dashboard global
Seguimiento de pre-reservas
RECEPCIONISTA
Gestión de pacientes
Programación de citas
Registro de pagos
Atención de pre-reservas
Coordinación de horarios
PSICÓLOGO
Agenda personalizada
Visualización de sus citas
Historia clínica
🏗️ Arquitectura

Arquitectura multicapa profesional:

Controller → Service → Repository → Entity
⚙️ Tecnologías utilizadas
Backend
Java 17
Spring Boot
Spring Data JPA
Spring Security
JWT
WebSocket
REST API
Frontend
HTML5
CSS3
JavaScript Vanilla
FullCalendar
Chart.js
SweetAlert2
Lucide Icons
Base de datos
MySQL
🎨 UX/UI
Diseño moderno
Sidebar interactivo
Dashboard profesional
Modales flotantes
Timeline clínico
Calendario visual
Tablas dinámicas
Paginación
Responsive Design
Gráficas interactivas
Portal emocional y profesional
📂 Estructura del proyecto
Backend
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
Frontend
static/

├── components/
├── css/
├── js/
├── img/
├── index.html
├── login.html
└── portal.html

🧪 Endpoints principales
🔐 Auth
PUT  /api/leads/{id}/payment/validate
PUT  /api/leads/{id}/payment/reject
POST /api/leads/{id}/convert-to-appointment
POST /api/auth/login
GET  /api/auth/validate
👥 Pacientes
POST  /api/patients
GET   /api/patients
PUT   /api/patients/{id}
PATCH /api/patients/{id}/toggle-active

📅 Citas
POST /api/appointments
GET  /api/appointments
GET  /api/appointments/by-date
GET  /api/appointments/my
PUT  /api/appointments/{id}
PUT  /api/appointments/{id}/pay
PUT  /api/appointments/{id}/status
🌐 Portal público
POST /api/public/leads
GET  /api/public/plans
GET  /api/public/services
GET  /api/public/availability
🧠 Historia clínica
POST   /api/clinical-history/patient/{id}
GET    /api/clinical-history/patient/{id}
DELETE /api/clinical-history/{id}

💰 Finanzas
POST /api/incomes
POST /api/expenses
GET  /api/finances/summary
GET  /api/finances/expense-categories

🔔 Notificaciones
GET   /api/notifications/me
GET   /api/notifications/role/{role}
PATCH /api/notifications/{id}/read
PATCH /api/notifications/me/read-all
🧠 Metodología

Proyecto desarrollado utilizando:

Scrum
Desarrollo incremental
Sprints
Validación continua
Modularización por funcionalidades
🎯 Problemas que resuelve
Desorganización de citas
Mala gestión financiera
Doble reserva de horarios
Falta de seguimiento clínico
Gestión manual de pacientes
Falta de reportes
Ausencia de notificaciones
Mala comunicación con interesados
Procesos manuales de atención

🚀 Futuras mejoras y escalabilidad

WhatsApp automático para recepción
Pasarela de pagos online
Integración con WhatsApp Business
Recordatorios automáticos
Portal del paciente
Videoconsultas psicológicas
Historial clínico para pacientes
Multi-sede
Agenda inteligente
Exportación PDF
Auditoría completa
Correos automáticos
Dashboard analítico avanzado
IA para recomendaciones clínicas
Firma digital
👨‍💻 Autor

Billy Rojas

Proyecto desarrollado como plataforma integral de gestión clínica y atención psicológica, orientada a optimizar la administración interna, el seguimiento de pacientes y la experiencia de atención desde el portal público hasta el agendamiento clínico.