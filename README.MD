# 🧠 CentroPsico — Sistema Web de Gestión Integral para Centros Psicológicos

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.13-brightgreen)
![Spring Security](https://img.shields.io/badge/Security-Spring_Security-6DB33F)
![JWT](https://img.shields.io/badge/JWT-0.12.6-red)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Aiven](https://img.shields.io/badge/Cloud_DB-Aiven-blueviolet)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket_STOMP-purple)
![Brevo](https://img.shields.io/badge/Email-Brevo_API-0B996E)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED)
![Render](https://img.shields.io/badge/Deploy-Render-black)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)

**CentroPsico** es una plataforma web integral desarrollada para centralizar y automatizar los procesos administrativos, clínicos, operativos, comerciales y financieros de un centro psicológico.

El sistema permite gestionar usuarios, pacientes, psicólogos, servicios, disponibilidades, pre-reservas, citas, pagos, historias clínicas, ingresos, gastos, promociones, reportes, recordatorios, notificaciones y registros de auditoría.

También cuenta con un portal público desde el cual una persona puede consultar servicios, promociones y horarios disponibles, seleccionar un psicólogo, registrar una pre-reserva y enviar los datos de un adelanto para su posterior validación administrativa.

---

## 🌍 Demo en línea

**Aplicación desplegada:**

https://centropsico-system.onrender.com

### Accesos principales

```text
Portal público:
https://centropsico-system.onrender.com/portal.html

Inicio de sesión:
https://centropsico-system.onrender.com/login.html
```

> El backend y el frontend se despliegan conjuntamente en Render.  
> La información persistente se almacena en una base de datos MySQL administrada en Aiven Cloud.

---

## 📑 Contenido

- [Descripción del proyecto](#-descripción-del-proyecto)
- [Problema que resuelve](#-problema-que-resuelve)
- [Objetivo general](#-objetivo-general)
- [Flujo principal](#-flujo-principal-del-sistema)
- [Roles y permisos](#-roles-y-permisos)
- [Módulos principales](#-módulos-principales)
- [Reglas de negocio](#-reglas-de-negocio-principales)
- [Arquitectura](#-arquitectura-del-sistema)
- [Tecnologías](#-tecnologías-utilizadas)
- [Seguridad](#-seguridad)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Configuración](#-configuración)
- [Instalación local](#-instalación-local)
- [Docker](#-ejecución-con-docker)
- [Endpoints](#-endpoints-principales)
- [Pruebas](#-pruebas-recomendadas)
- [Limitaciones actuales](#-limitaciones-actuales)
- [Roadmap](#-roadmap)
- [Autor](#-autor)

---

# 📌 Descripción del proyecto

CentroPsico es un sistema web que controla el proceso completo de atención de un centro psicológico:

```text
Visitante
   ↓
Consulta servicios y promociones
   ↓
Selecciona psicólogo, fecha y horario
   ↓
Registra una pre-reserva con adelanto
   ↓
Administrador o recepcionista valida el pago
   ↓
Se registra o selecciona al paciente
   ↓
La pre-reserva se convierte en cita
   ↓
El psicólogo realiza la atención
   ↓
Se registra la historia clínica
   ↓
Se controla el pago pendiente
   ↓
Se actualizan finanzas, reportes y auditoría
```

La plataforma integra en una sola aplicación:

- Atención al público.
- Gestión administrativa.
- Agenda de psicólogos.
- Programación de citas.
- Seguimiento clínico.
- Control de pagos.
- Gestión financiera.
- Notificaciones.
- Auditoría de operaciones.
- Reportes para la toma de decisiones.

El sistema registra información clínica, pero no genera diagnósticos automáticos ni reemplaza la evaluación profesional del psicólogo.

---

# 🎯 Problema que resuelve

En un centro psicológico, la información puede estar distribuida entre documentos, hojas de cálculo, agendas físicas, mensajes y registros manuales.

Esta forma de trabajo puede producir:

- Cruces de horarios.
- Doble reserva de psicólogos.
- Pérdida o duplicación de información.
- Falta de seguimiento de solicitudes.
- Dificultad para controlar pagos.
- Desorden en ingresos y gastos.
- Acceso inadecuado a historias clínicas.
- Falta de trazabilidad de las operaciones.
- Dificultad para conocer el estado real de las citas.
- Escasa información para tomar decisiones.

CentroPsico centraliza estos procesos y aplica validaciones automáticas para mejorar la organización y reducir errores.

---

# ✅ Objetivo general

> Desarrollar un sistema web que permita optimizar la gestión administrativa, clínica, operativa y financiera de un centro psicológico mediante la centralización de pacientes, psicólogos, servicios, disponibilidades, pre-reservas, citas, pagos, historias clínicas, notificaciones, reportes y registros de auditoría.

---

# 🔄 Flujo principal del sistema

## Flujo desde el portal público

```text
Portal público
      ↓
Consulta de servicios y promociones
      ↓
Selección de modalidad
      ↓
Selección de psicólogo
      ↓
Consulta de disponibilidad
      ↓
Selección de fecha y horario
      ↓
Registro de datos personales
      ↓
Registro del adelanto
      ↓
Aceptación del tratamiento de datos
      ↓
Creación de pre-reserva
```

## Flujo administrativo

```text
Nueva pre-reserva
      ↓
Pago en revisión
      ↓
Validación o rechazo del adelanto
      ↓
Pre-reserva confirmada
      ↓
Selección del paciente
      ↓
Conversión a cita
      ↓
Registro del adelanto como ingreso
      ↓
Notificación al psicólogo
```

## Flujo clínico

```text
Cita programada
      ↓
Atención del paciente
      ↓
Estado: ATENDIDA o NO_ASISTIO
      ↓
Registro de historia clínica
      ↓
Seguimiento de evolución y recomendaciones
```

---

# 👥 Roles y permisos

El sistema utiliza tres roles principales:

```text
ADMIN
RECEPCIONISTA
PSICOLOGO
```

## Matriz general de acceso

| Módulo                        | ADMIN         | RECEPCIONISTA | PSICOLOGO       |
|------:                        |:-----:        |:------------: |:---------:      |
| Dashboard                     | ✅            | ✅           | ✅             |
| Solicitudes y pre-reservas    | ✅            | ✅           | ❌             |
| Pacientes generales           | ✅            | ✅           | ❌             |
| Psicólogos                    | ✅ Gestión    | ✅ Consulta  | ✅ Consulta    |
| Servicios                     | ✅ Gestión    | ✅ Consulta  | ✅ Consulta    |
| Disponibilidad                | ✅ Gestión    | ✅ Consulta  | ✅ Consulta    |
| Citas generales               | ✅            | ✅           | ❌             |
| Mis citas                     | ✅            | ✅           | ✅             |
| Registrar citas               | ✅            | ✅           | ❌             |
| Registrar pagos               | ✅            | ✅           | ❌             |
| Historias clínicas            | ✅            | ❌           | ✅ Restringido |
| Finanzas                      | ✅            | ❌           | ❌             |
| Reportes                      | ✅            | ❌           | ❌             |
| Promociones                   | ✅            | ❌           | ❌             |
| Usuarios                      | ✅            | ❌           | ❌             |
| Auditoría                     | ✅            | ❌           | ❌             |
| Notificaciones                | ✅            | ✅           | ✅             |
| Perfil                        | ✅            | ✅           | ✅             |

---

## 👨‍💼 ADMIN

El administrador dispone del acceso más amplio del sistema.

### Puede visualizar

```text
Dashboard
Solicitudes
Pacientes
Citas
Agenda
Calendario
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
Recordatorios
Perfil
```

### Puede realizar

- Registrar y administrar usuarios.
- Asignar roles.
- Activar o desactivar cuentas.
- Registrar pacientes.
- Registrar psicólogos.
- Administrar servicios psicológicos.
- Configurar disponibilidades.
- Gestionar todas las citas.
- Registrar pagos.
- Cancelar citas.
- Revisar pre-reservas.
- Validar o rechazar adelantos.
- Convertir pre-reservas en citas.
- Consultar historias clínicas.
- Registrar ingresos y gastos.
- Revisar movimientos financieros.
- Consultar reportes.
- Administrar promociones.
- Revisar registros de auditoría.
- Enviar notificaciones.
- Administrar su perfil y fotografía.

---

## 👩‍💼 RECEPCIONISTA

La recepcionista dispone de las funciones necesarias para la atención administrativa.

### Puede visualizar

```text
Dashboard
Solicitudes
Pacientes
Citas
Agenda diaria
Calendario
Notificaciones
Recordatorios
Perfil
```

### Puede realizar

- Revisar solicitudes y pre-reservas.
- Validar o rechazar adelantos.
- Registrar y actualizar pacientes.
- Consultar servicios y psicólogos.
- Consultar disponibilidades.
- Programar citas.
- Consultar la agenda.
- Consultar el calendario.
- Registrar pagos completos o parciales.
- Imprimir comprobantes.
- Cambiar estados permitidos de las citas.
- Convertir una pre-reserva en cita.
- Recibir notificaciones.
- Actualizar su perfil.

### No puede visualizar

```text
Historias clínicas
Finanzas generales
Reportes administrativos
Promociones administrativas
Auditoría
Usuarios
Gestión de psicólogos
Gestión de servicios
Gestión de disponibilidad
```

---

## 👨‍⚕️ PSICOLOGO

El psicólogo dispone de una experiencia centrada en la atención clínica.

### Puede visualizar

```text
Dashboard personal
Mis citas
Agenda diaria
Calendario
Historias clínicas autorizadas
Notificaciones
Recordatorios
Perfil
```

### Puede realizar

- Consultar únicamente sus citas asignadas.
- Consultar su agenda diaria.
- Consultar sus próximas citas.
- Visualizar su calendario.
- Marcar una cita como atendida.
- Marcar una cita como no asistió.
- Registrar información clínica.
- Consultar el historial clínico de pacientes asignados.
- Recibir notificaciones de nuevas citas.
- Actualizar su perfil y fotografía.

### Restricciones

- No puede consultar todas las citas del centro.
- No puede modificar citas de otros psicólogos.
- No puede registrar pagos.
- No puede visualizar montos.
- No puede acceder a finanzas.
- No puede acceder a pre-reservas.
- No puede gestionar usuarios.
- No puede acceder a historias clínicas de pacientes no asignados.

---

# 🧩 Módulos principales

## 1. Portal público

El portal público permite que una persona conozca los servicios del centro sin iniciar sesión.

### Funcionalidades

- Consulta de servicios.
- Consulta de planes.
- Consulta de promociones activas.
- Consulta de disponibilidad.
- Selección de modalidad presencial o virtual.
- Selección de psicólogo.
- Selección de fecha.
- Selección de horario.
- Registro de datos de contacto.
- Registro del método de pago.
- Registro del código de operación.
- Aceptación del tratamiento de datos.
- Envío de pre-reserva.

El portal registra el consentimiento junto con:

- Fecha de aceptación.
- Estado de aceptación.
- Versión de la política de privacidad.

---

## 2. Solicitudes y pre-reservas

Una solicitud registrada desde el portal se almacena como una pre-reserva.

### Información registrada

- Nombre completo.
- Correo.
- Teléfono o WhatsApp.
- Servicio seleccionado.
- Modalidad.
- Psicólogo seleccionado.
- Fecha preferida.
- Hora preferida.
- Precio del servicio.
- Porcentaje del adelanto.
- Monto del adelanto.
- Método de pago.
- Código de operación.
- Estado del pago.
- Mensaje adicional.
- Consentimiento.
- Fecha de registro.

### Estados de la pre-reserva

```text
NUEVO
PAGO_EN_REVISION
CONTACTADO
PRE_RESERVADO
AGENDADO
DESCARTADO
```

### Estados del adelanto

```text
PENDIENTE
PAGO_EN_REVISION
PAGO_VALIDADO
PAGO_RECHAZADO
```

La validación del pago es administrativa. El sistema registra el método y el código de operación, pero actualmente no se encuentra conectado directamente a una pasarela bancaria.

---

## 3. Conversión de pre-reserva en cita

Una pre-reserva solo puede convertirse en cita cuando su adelanto ha sido validado.

Durante la conversión, el sistema:

1. Verifica la existencia del paciente.
2. Verifica la existencia del psicólogo.
3. Verifica la existencia del servicio.
4. Comprueba que las entidades estén activas.
5. Obtiene la duración del servicio.
6. Calcula la hora final.
7. Verifica cruces de horario.
8. Crea la cita como `PROGRAMADA`.
9. Transfiere el adelanto.
10. Calcula el saldo pendiente.
11. Registra el adelanto como ingreso.
12. Cambia la pre-reserva a `AGENDADO`.
13. Notifica al administrador y a recepción.
14. Notifica al psicólogo.
15. Envía un correo mediante Brevo.
16. Registra la operación en auditoría.

---

## 4. Gestión de pacientes

### Funcionalidades

- Registrar pacientes.
- Consultar todos los pacientes.
- Consultar pacientes activos.
- Actualizar información.
- Validar DNI duplicado.
- Activar o desactivar pacientes.
- Aplicar eliminación lógica.
- Generar notificaciones.
- Registrar operaciones en auditoría.

### Datos principales

- Nombres.
- Apellidos.
- DNI.
- Fecha de nacimiento.
- Sexo.
- Teléfono.
- Correo.
- Dirección.
- Contacto de emergencia.
- Teléfono de emergencia.
- Estado.

El contacto y el teléfono de emergencia son opcionales.

---

## 5. Gestión de psicólogos

### Funcionalidades

- Registrar psicólogos.
- Actualizar información.
- Consultar profesionales activos.
- Activar o desactivar registros.
- Consultar cantidad de disponibilidades.
- Relacionar la fotografía con la cuenta de usuario.
- Generar notificaciones y auditorías.

### Datos principales

- Nombres.
- Apellidos.
- Especialidad.
- Teléfono.
- Correo.
- Estado.
- Cantidad de horarios registrados.
- Fotografía de perfil.

El correo permite relacionar al psicólogo con su cuenta de acceso.

---

## 6. Servicios psicológicos

Cada servicio contiene:

- Nombre.
- Descripción.
- Precio.
- Duración en minutos.
- Estado.

El servicio seleccionado determina:

- El precio de la cita.
- La duración de la atención.
- Los psicólogos relacionados.
- Los bloques horarios que se generan.
- El saldo total del pago.

---

## 7. Disponibilidad de psicólogos

El administrador registra los horarios semanales de los profesionales.

### Información utilizada

- Psicólogo.
- Día de la semana.
- Hora de inicio.
- Hora de fin.
- Estado.

### Validaciones

- La hora inicial debe ser menor que la hora final.
- El psicólogo debe estar activo.
- No se permiten disponibilidades superpuestas.
- No se muestran disponibilidades inactivas.
- No se permite programar fuera del horario registrado.

---

## 8. Gestión de citas

La cita es el proceso principal del sistema.

### Información registrada

- Paciente.
- Psicólogo.
- Servicio.
- Fecha.
- Hora de inicio.
- Hora de fin.
- Estado.
- Motivo de consulta.
- Observación.
- Monto total.
- Monto pagado.
- Saldo pendiente.
- Estado de pago.
- Método de pago.
- Código de operación.
- Fecha y hora del pago.
- Usuario que registró el pago.

### Estados de cita

```text
PROGRAMADA
ATENDIDA
CANCELADA
NO_ASISTIO
REPROGRAMADA
```

### Formas de consulta

- Tabla paginada.
- Buscador.
- Agenda diaria.
- Calendario mensual.
- Calendario semanal.
- Calendario diario.
- Vista de lista.

---

## 9. Disponibilidad automática para citas

Al crear una cita, el sistema sigue este orden:

```text
Seleccionar paciente
      ↓
Seleccionar servicio
      ↓
Consultar psicólogos relacionados
      ↓
Mostrar horarios semanales
      ↓
Seleccionar psicólogo
      ↓
Mostrar próximas fechas disponibles
      ↓
Seleccionar fecha
      ↓
Generar horas libres
      ↓
Seleccionar bloque horario
      ↓
Registrar cita
```

Los horarios se calculan considerando:

- Duración del servicio.
- Disponibilidad semanal.
- Fecha seleccionada.
- Citas ya registradas.
- Citas canceladas.
- Horarios pasados.
- Cruces de inicio y fin.

Las validaciones se realizan tanto en el frontend como en el backend.

---

## 10. Citas vencidas o pendientes de cierre

El dashboard identifica las citas que:

- Continúan como `PROGRAMADA`.
- Tienen una fecha anterior a la actual, o
- Ya superaron su hora final.

Estas citas se muestran como:

```text
Pendientes de cierre
```

Esto permite que el psicólogo o el personal administrativo actualice el resultado de la atención.

---

## 11. Pagos

El sistema permite registrar:

- Pago completo.
- Adelanto.
- Pago parcial.
- Saldo pendiente.

### Métodos disponibles

```text
EFECTIVO
YAPE
PLIN
TRANSFERENCIA
TARJETA
```

### Estados de pago

```text
PENDIENTE
PARCIAL
PAGADO
```

### Validaciones

- El monto debe ser mayor que cero.
- El monto no puede superar el saldo.
- Debe seleccionarse un método.
- No se permiten pagos en citas canceladas.
- No se permiten pagos en citas marcadas como no asistió.
- Una cita totalmente pagada no acepta nuevos pagos.

Cuando se registra un pago, se genera automáticamente un ingreso financiero.

---

## 12. Comprobante de pago

El sistema genera una vista imprimible con:

- Número de cita.
- Paciente.
- Psicólogo.
- Servicio.
- Fecha de atención.
- Monto total.
- Monto pagado.
- Saldo pendiente.
- Método de pago.
- Código de operación.
- Fecha del pago.
- Usuario responsable.
- Observación.

---

## 13. Historia clínica

La historia clínica almacena:

- Motivo.
- Diagnóstico.
- Evolución.
- Recomendaciones.
- Psicólogo responsable.
- Fecha y hora.
- Estado.

### Acceso

```text
ADMIN
PSICOLOGO
```

Para el rol psicólogo, el backend verifica que el paciente tenga una cita relacionada con el profesional autenticado.

La recepcionista no tiene acceso a la información clínica.

La eliminación es lógica: el registro permanece en la base de datos, pero se marca como inactivo.

---

## 14. Finanzas

El módulo financiero es exclusivo del administrador.

### Ingresos

Los ingresos pueden provenir de:

```text
MANUAL
CITA
ADELANTO
PAGO_FINAL
OTRO
```

Cada ingreso puede registrar:

- Descripción.
- Monto.
- Fecha.
- Método de pago.
- Origen.
- Referencia.
- Estado de revisión.
- Usuario revisor.
- Fecha de revisión.
- Observación.

### Gastos

Los gastos registran:

- Categoría.
- Descripción.
- Monto.
- Fecha.
- Responsable.
- Origen.
- Referencia.
- Estado.
- Información de revisión.

Cuando se registra un gasto igual o superior a **S/ 500**, el sistema genera una alerta para el administrador.

### Estados de revisión

```text
PENDIENTE
REVISADO
CONTABILIZADO
OBSERVADO
ANULADO
```

### Resumen financiero

```text
Total de ingresos
      −
Total de gastos
      =
Ganancia o pérdida
```

Los registros anulados se conservan mediante eliminación lógica.

---

## 15. Dashboard

El dashboard cambia según el rol autenticado.

### Periodos

```text
Día
Semana
Mes
Año
```

### Indicadores operativos

- Total de pacientes.
- Citas del periodo.
- Citas del día.
- Citas programadas.
- Citas atendidas.
- Citas canceladas.
- Inasistencias.
- Reprogramaciones.
- Citas pendientes de cierre.
- Próximas citas.
- Pagos pendientes.
- Pre-reservas pendientes.

### Indicadores financieros

Disponibles únicamente para el administrador:

- Total de ingresos.
- Total de gastos.
- Ganancia o pérdida.
- Evolución financiera por periodos.

El psicólogo visualiza solamente información relacionada con sus citas y pacientes asignados.

---

## 16. Reportes

El sistema incluye reportes relacionados con:

- Resumen mensual.
- Citas por estado.
- Pacientes atendidos.
- Inasistencias.
- Cancelaciones.
- Rendimiento por psicólogo.
- Ingresos.
- Gastos.
- Resultado financiero.

El frontend contiene soporte para visualización, impresión y exportación de información.

---

## 17. Promociones

El administrador puede:

- Registrar promociones.
- Actualizar promociones.
- Activarlas o desactivarlas.
- Definir su vigencia.
- Asociarlas a servicios.
- Publicarlas en el portal.
- Ocultar promociones inactivas o vencidas.

---

## 18. Notificaciones en tiempo real

El sistema utiliza:

```text
Spring WebSocket
SockJS
STOMP
```

Las notificaciones pueden dirigirse:

- A un rol.
- A un usuario específico.
- A un psicólogo específico.

### Eventos notificados

- Nuevo paciente.
- Nuevo psicólogo.
- Nueva disponibilidad.
- Nueva cita.
- Cita asignada.
- Actualización de cita.
- Cambio de estado.
- Pago registrado.
- Nueva pre-reserva.
- Adelanto validado.
- Adelanto rechazado.
- Conversión a cita.
- Ingreso registrado.
- Gasto registrado.
- Alerta de gasto alto.
- Mensaje administrativo.

---

## 19. Recordatorios internos

Los usuarios autenticados pueden consultar recordatorios relacionados con:

- Actividades pendientes.
- Citas.
- Tareas administrativas.
- Seguimientos internos.

Este módulo complementa las notificaciones automáticas.

---

## 20. Auditoría

El sistema registra operaciones administrativas, financieras, clínicas y de seguridad.

### Eventos registrados

- Inicio de sesión exitoso.
- Inicio de sesión fallido.
- Intento de acceso de usuario inactivo.
- Token inválido o expirado.
- Acceso no autorizado.
- Acceso denegado.
- Registro de entidades.
- Actualización de datos.
- Activación o desactivación.
- Registro de pagos.
- Validación de adelantos.
- Conversión de pre-reservas.
- Operaciones financieras.
- Actualización de perfil.

### Datos de seguridad

- Correo del usuario.
- Rol.
- Dirección IP.
- Navegador.
- Sistema operativo.
- Fecha y hora.
- Nivel del evento.
- Resultado de la operación.

### Niveles

```text
INFO
WARNING
CRITICAL
```

---

## 21. Perfil de usuario

Cada usuario autenticado puede:

- Consultar sus datos.
- Actualizar nombres y apellidos.
- Actualizar teléfono.
- Cambiar contraseña.
- Subir una fotografía.
- Eliminar su fotografía.
- Visualizar la imagen en el sidebar.

### Persistencia de fotografía

La fotografía se almacena en MySQL mediante Base64:

```text
Imagen seleccionada
      ↓
Procesamiento en el navegador
      ↓
Envío al backend
      ↓
Conversión o almacenamiento Base64
      ↓
Campo LONGTEXT
      ↓
MySQL Aiven
```

### Ventajas

- La imagen no depende del disco temporal de Render.
- No desaparece después de un redeploy.
- Se recupera al recargar la aplicación.
- Permanece relacionada con la cuenta de usuario.
- Se puede utilizar como fotografía del psicólogo mediante el correo.

---

# ⚖️ Reglas de negocio principales

## Citas

- El paciente debe existir y estar activo.
- El psicólogo debe existir y estar activo.
- El servicio debe existir y estar activo.
- No se permiten fechas pasadas.
- No se permiten horas pasadas para el día actual.
- La hora inicial debe ser menor que la hora final.
- El horario debe pertenecer a la disponibilidad del psicólogo.
- No se permiten citas superpuestas.
- Las citas nuevas se registran como `PROGRAMADA`.

## Psicólogo

- Solo puede modificar sus propias citas.
- Solo puede cambiar una cita programada a `ATENDIDA` o `NO_ASISTIO`.
- No puede modificar citas de otros profesionales.
- Solo puede acceder a pacientes relacionados con sus citas.

## Pagos

- El monto debe ser mayor que cero.
- No puede superar el saldo pendiente.
- Debe indicarse el método de pago.
- No se aceptan pagos en citas canceladas.
- No se aceptan pagos en citas con inasistencia.
- Los adelantos generan estado `PARCIAL`.
- El pago total genera estado `PAGADO`.

## Pre-reservas

- El visitante debe ingresar nombre y teléfono.
- Debe seleccionar un servicio.
- Debe seleccionar una modalidad.
- Debe seleccionar psicólogo, fecha y horario.
- Debe registrar método y código de operación.
- Debe aceptar el tratamiento de datos.
- Solo una pre-reserva validada puede convertirse en cita.
- Una pre-reserva agendada no puede convertirse nuevamente.

## Historia clínica

- Solo puede ser consultada por administrador o psicólogo autorizado.
- El psicólogo debe estar relacionado con una cita del paciente.
- La recepcionista no puede acceder.
- La eliminación se realiza de forma lógica.

---

# 🏗️ Arquitectura del sistema

CentroPsico utiliza una arquitectura web monolítica organizada por capas.

```text
┌──────────────────────────────────────────────┐
│                  USUARIO                     │
│ Navegador web: HTML, CSS y JavaScript        │
└──────────────────────┬───────────────────────┘
                       │ HTTP / JSON
                       ▼
┌──────────────────────────────────────────────┐
│             SPRING BOOT 3.5.13               │
│                                              │
│  Controller → Service → Repository           │
│                                              │
│  Spring Security + JWT                       │
│  WebSocket + STOMP                           │
│  Auditoría y reglas de negocio               │
└──────────────┬────────────────┬──────────────┘
               │                │
               ▼                ▼
┌─────────────────────┐  ┌─────────────────────┐
│ MySQL - Aiven Cloud │  │     Brevo API       │
│ Persistencia de     │  │ Envío de correos    │
│ información         │  │ y avisos             │
└─────────────────────┘  └─────────────────────┘
```

## Capas del backend

### Controller

Recibe las solicitudes HTTP y expone los endpoints REST.

### Service

Contiene las reglas de negocio, validaciones y procesos principales.

### Repository

Gestiona el acceso a la base de datos mediante Spring Data JPA.

### Entity

Representa las tablas y relaciones de MySQL.

### DTO

Transporta información específica entre frontend y backend.

### Config

Contiene las configuraciones de:

- Spring Security.
- JWT.
- WebSocket.
- CORS.
- Archivos.
- Inicialización.

### Exception

Centraliza errores de negocio y recursos no encontrados.

---

# 🛠️ Tecnologías utilizadas

## Backend

- Java 17.
- Spring Boot 3.5.13.
- Spring Web.
- Spring Security.
- Spring Data JPA.
- Hibernate.
- Spring Validation.
- Spring WebSocket.
- Maven.
- Lombok.
- JJWT 0.12.6.
- MySQL Connector/J.

## Frontend

- HTML5.
- CSS3.
- JavaScript Vanilla.
- Fetch API.
- SweetAlert2.
- FullCalendar.
- Chart.js.
- Lucide Icons.
- SockJS.
- STOMP.js.

## Infraestructura

- MySQL en Aiven Cloud.
- Render.
- Docker.
- GitHub.
- Brevo API.
- Maven Wrapper.

---

# 🔐 Seguridad

CentroPsico implementa seguridad mediante Spring Security y JWT.

## Características

- Inicio de sesión mediante correo y contraseña.
- Contraseñas cifradas con BCrypt.
- Factor BCrypt configurado en 12.
- Token JWT firmado.
- Tiempo de expiración configurable.
- Duración predeterminada de 30 minutos.
- Validación de usuario activo.
- Sesiones stateless.
- Control de acceso por roles.
- Protección de endpoints.
- CORS restringido.
- Respuestas uniformes `401` y `403`.
- Protección contra carga en iframes externos.
- Encabezados HTTP de seguridad.
- Auditoría de accesos.
- Registro de IP y navegador.
- Validación de rutas sensibles.

## Flujo de autenticación

```text
Correo y contraseña
        ↓
Validación del usuario
        ↓
Comparación de contraseña BCrypt
        ↓
Generación del token JWT
        ↓
Authorization: Bearer <token>
        ↓
JwtAuthenticationFilter
        ↓
Validación del token
        ↓
Carga del usuario y autoridad
        ↓
Acceso al endpoint
```

## Autoridades

```text
ADMIN
RECEPCIONISTA
PSICOLOGO
```

---

# 🗃️ Entidades principales

```text
User
Patient
Psychologist
ServiceEntity
PsychologistAvailability
Appointment
ClinicalHistory
Lead
Income
Expense
ExpenseCategory
Promotion
Notification
AuditLog
```

## Relaciones principales

```text
Patient 1 ───── N Appointment
Psychologist 1 ─ N Appointment
Service 1 ───── N Appointment

Psychologist 1 ─ N PsychologistAvailability

Patient 1 ───── N ClinicalHistory

Lead ─────────── Appointment
Appointment ──── Income

ExpenseCategory 1 ─ N Expense
```

---

# 📂 Estructura del proyecto

SISTEMA/
│
├── .github/
├── .mvn/
├── .vscode/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── centropsicologico/
│   │   │           └── sistema/
│   │   │               │
│   │   │               ├── config/
│   │   │               │   ├── DatabaseInitializer.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   ├── JwtUtil.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── UploadConfig.java
│   │   │               │   └── WebSocketConfig.java
│   │   │               │
│   │   │               ├── controller/
│   │   │               │   ├── AdminNotificationController.java
│   │   │               │   ├── AppointmentController.java
│   │   │               │   ├── AuditLogController.java
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── ClinicalHistoryController.java
│   │   │               │   ├── DashboardController.java
│   │   │               │   ├── FinanceController.java
│   │   │               │   ├── LeadController.java
│   │   │               │   ├── NotificationController.java
│   │   │               │   ├── PatientController.java
│   │   │               │   ├── ProfileController.java
│   │   │               │   ├── PromotionController.java
│   │   │               │   ├── PsychologistAvailabilityController.java
│   │   │               │   ├── PsychologistController.java
│   │   │               │   ├── PublicAvailabilityController.java
│   │   │               │   ├── PublicPortalController.java
│   │   │               │   ├── PublicPromotionController.java
│   │   │               │   ├── ReminderController.java
│   │   │               │   ├── ReportController.java
│   │   │               │   ├── ServiceController.java
│   │   │               │   └── UserController.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── AdminUserNotificationRequestDto.java
│   │   │               │   ├── AvailablePsychologistDto.java
│   │   │               │   ├── DashboardDto.java
│   │   │               │   ├── FinanceSummaryDto.java
│   │   │               │   ├── LeadConvertToAppointmentDto.java
│   │   │               │   ├── LeadRequestDto.java
│   │   │               │   ├── MonthlyReportDto.java
│   │   │               │   ├── PaymentRequestDto.java
│   │   │               │   ├── ProfileImageRequest.java
│   │   │               │   ├── PsychologistPerformanceReportDto.java
│   │   │               │   ├── PublicAvailabilityDto.java
│   │   │               │   └── ReminderDTO.java
│   │   │               │
│   │   │               ├── entity/
│   │   │               │   ├── Appointment.java
│   │   │               │   ├── AuditLog.java
│   │   │               │   ├── ClinicalHistory.java
│   │   │               │   ├── Expense.java
│   │   │               │   ├── ExpenseCategory.java
│   │   │               │   ├── Income.java
│   │   │               │   ├── Lead.java
│   │   │               │   ├── Notification.java
│   │   │               │   ├── Patient.java
│   │   │               │   ├── Promotion.java
│   │   │               │   ├── Psychologist.java
│   │   │               │   ├── PsychologistAvailability.java
│   │   │               │   ├── ServiceEntity.java
│   │   │               │   └── User.java
│   │   │               │
│   │   │               ├── enums/
│   │   │               │   └── AppointmentStatus.java
│   │   │               │
│   │   │               ├── exception/
│   │   │               │   ├── BusinessRuleException.java
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── ResourceNotFoundException.java
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   ├── AppointmentRepository.java
│   │   │               │   ├── AuditLogRepository.java
│   │   │               │   ├── ClinicalHistoryRepository.java
│   │   │               │   ├── ExpenseCategoryRepository.java
│   │   │               │   ├── ExpenseRepository.java
│   │   │               │   ├── IncomeRepository.java
│   │   │               │   ├── LeadRepository.java
│   │   │               │   ├── NotificationRepository.java
│   │   │               │   ├── PatientRepository.java
│   │   │               │   ├── PromotionRepository.java
│   │   │               │   ├── PsychologistAvailabilityRepository.java
│   │   │               │   ├── PsychologistRepository.java
│   │   │               │   ├── ServiceRepository.java
│   │   │               │   └── UserRepository.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   ├── impl/
│   │   │               │   │   ├── AppointmentServiceImpl.java
│   │   │               │   │   ├── AuditLogServiceImpl.java
│   │   │               │   │   ├── ClinicalHistoryServiceImpl.java
│   │   │               │   │   ├── CustomUserDetailsService.java
│   │   │               │   │   ├── EmailServiceImpl.java
│   │   │               │   │   ├── FinanceServiceImpl.java
│   │   │               │   │   ├── NotificationServiceImpl.java
│   │   │               │   │   ├── PatientServiceImpl.java
│   │   │               │   │   ├── PromotionServiceImpl.java
│   │   │               │   │   ├── PsychologistAvailabilityServiceImpl.java
│   │   │               │   │   ├── PsychologistServiceImpl.java
│   │   │               │   │   ├── ReminderServiceImpl.java
│   │   │               │   │   ├── ServiceEntityServiceImpl.java
│   │   │               │   │   └── UserServiceImpl.java
│   │   │               │   │
│   │   │               │   ├── AppointmentService.java
│   │   │               │   ├── AuditLogService.java
│   │   │               │   ├── ClinicalHistoryService.java
│   │   │               │   ├── EmailService.java
│   │   │               │   ├── FinanceService.java
│   │   │               │   ├── NotificationService.java
│   │   │               │   ├── PatientService.java
│   │   │               │   ├── PromotionService.java
│   │   │               │   ├── PsychologistAvailabilityService.java
│   │   │               │   ├── PsychologistService.java
│   │   │               │   ├── ReminderService.java
│   │   │               │   ├── ServiceEntityService.java
│   │   │               │   ├── UserService.java
│   │   │               │   └── WebSocketNotificationService.java
│   │   │               │
│   │   │               └── util/
│   │   │                   └── SistemaApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── components/
│   │       │   │   ├── header.html
│   │       │   │   ├── module-tabs.html
│   │       │   │   └── sidebar.html
│   │       │   │
│   │       │   ├── css/
│   │       │   │   ├── components.css
│   │       │   │   ├── dashboard.css
│   │       │   │   ├── layout.css
│   │       │   │   ├── modules.css
│   │       │   │   ├── portal.css
│   │       │   │   └── styles.css
│   │       │   │
│   │       │   ├── img/
│   │       │   │   ├── services/
│   │       │   │   ├── logo-dark.png
│   │       │   │   └── logo-white.png
│   │       │   │
│   │       │   ├── js/
│   │       │   │   ├── app.js
│   │       │   │   ├── appointments.js
│   │       │   │   ├── audit-logs.js
│   │       │   │   ├── auth.js
│   │       │   │   ├── clinical-history.js
│   │       │   │   ├── dashboard.js
│   │       │   │   ├── finances.js
│   │       │   │   ├── layout.js
│   │       │   │   ├── leads.js
│   │       │   │   ├── login.js
│   │       │   │   ├── notification.js
│   │       │   │   ├── patients.js
│   │       │   │   ├── portal.js
│   │       │   │   ├── profile.js
│   │       │   │   ├── promotions.js
│   │       │   │   ├── psychologist-availability.js
│   │       │   │   ├── psychologists.js
│   │       │   │   ├── reminders.js
│   │       │   │   ├── reports-export.js
│   │       │   │   ├── reports.js
│   │       │   │   ├── services.js
│   │       │   │   └── users.js
│   │       │   │
│   │       │   ├── index.html
│   │       │   ├── login.html
│   │       │   └── portal.html
│   │       │
│   │       ├── templates/
│   │       └── application.properties
│   │
│   └── test/
│
├── target/
├── uploads/
│
├── .dockerignore
├── .gitattributes
├── .gitignore
├── centro_psicologico.sql
├── Dockerfile
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.MD

# ⚙️ Configuración

## Base de datos

El proyecto utiliza MySQL alojado en Aiven Cloud.

La conexión exige SSL:

```properties
sslMode=REQUIRED
```

La contraseña se obtiene mediante una variable de entorno:

```properties
spring.datasource.password=${DB_PASSWORD}
```

## JPA e Hibernate

Configuración actual:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

> `ddl-auto=update` y `show-sql=true` son útiles durante el desarrollo. Para producción se recomienda utilizar migraciones y desactivar la impresión de SQL.

## Tamaño de archivos

Configuración actual:

```properties
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=3MB
```

## Correo mediante Brevo

```properties
brevo.api.base-url=https://api.brevo.com/v3
brevo.api.key=${BREVO_API_KEY}

app.mail.from=${MAIL_FROM}
app.mail.from-name=${MAIL_FROM_NAME:CentroPsico}
```

## CORS

```properties
app.security.allowed-origins=${APP_ALLOWED_ORIGINS:http://localhost:8080}
```

Se pueden configurar varios orígenes separados por comas.

## JWT

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=${JWT_EXPIRATION_MS:1800000}
```

El tiempo predeterminado es:

```text
1 800 000 ms = 30 minutos
```

---

# 🔑 Variables de entorno

| Variable | Obligatoria | Descripción |
|---|:---:|---|
| `DB_PASSWORD` | ✅ | Contraseña de MySQL Aiven |
| `BREVO_API_KEY` | ✅ | Clave de la API de Brevo |
| `MAIL_FROM` | ✅ | Correo remitente autorizado |
| `MAIL_FROM_NAME` | ❌ | Nombre visible del remitente |
| `APP_ALLOWED_ORIGINS` | ❌ | Dominios autorizados por CORS |
| `JWT_SECRET` | ✅ | Clave Base64 para firmar JWT |
| `JWT_EXPIRATION_MS` | ❌ | Duración del token en milisegundos |

## Ejemplo

```env
DB_PASSWORD=TU_PASSWORD_AIVEN
BREVO_API_KEY=TU_API_KEY_BREVO
MAIL_FROM=correo-verificado@dominio.com
MAIL_FROM_NAME=CentroPsico
APP_ALLOWED_ORIGINS=http://localhost:8080,https://centropsico-system.onrender.com
JWT_SECRET=CLAVE_BASE64_DE_AL_MENOS_32_BYTES
JWT_EXPIRATION_MS=1800000
```

> Nunca subas valores reales de contraseñas, claves API o secretos JWT al repositorio.

---

# 💻 Instalación local

## Requisitos

- Java 17.
- Maven 3.9 o Maven Wrapper.
- MySQL.
- Git.
- Navegador moderno.
- Docker opcional.

## 1. Clonar el repositorio

```bash
git clone https://github.com/rojasbilly95-debug/centropsico-system.git
cd centropsico-system
```

## 2. Crear una base de datos local

```sql
CREATE DATABASE centro_psicologico;
```

## 3. Configurar la conexión local

Para trabajar con MySQL local, actualiza temporalmente las propiedades:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/centro_psicologico?useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## 4. Configurar variables de entorno

### Windows PowerShell

```powershell
$env:DB_PASSWORD="TU_PASSWORD"
$env:BREVO_API_KEY="TU_API_KEY"
$env:MAIL_FROM="correo-verificado@dominio.com"
$env:MAIL_FROM_NAME="CentroPsico"
$env:APP_ALLOWED_ORIGINS="http://localhost:8080"
$env:JWT_SECRET="TU_CLAVE_BASE64"
$env:JWT_EXPIRATION_MS="1800000"
```

### Linux o macOS

```bash
export DB_PASSWORD="TU_PASSWORD"
export BREVO_API_KEY="TU_API_KEY"
export MAIL_FROM="correo-verificado@dominio.com"
export MAIL_FROM_NAME="CentroPsico"
export APP_ALLOWED_ORIGINS="http://localhost:8080"
export JWT_SECRET="TU_CLAVE_BASE64"
export JWT_EXPIRATION_MS="1800000"
```

## 5. Ejecutar con Maven Wrapper

### Windows

```bash
mvnw.cmd spring-boot:run
```

### Linux o macOS

```bash
./mvnw spring-boot:run
```

También puede ejecutarse con Maven instalado:

```bash
mvn spring-boot:run
```

## 6. Abrir la aplicación

```text
Portal:
http://localhost:8080/portal.html

Login:
http://localhost:8080/login.html
```

---

# 📦 Compilación

```bash
mvn clean package
```

El archivo generado se ubicará en:

```text
target/sistema-0.0.1-SNAPSHOT.jar
```

Ejecución:

```bash
java -jar target/sistema-0.0.1-SNAPSHOT.jar
```

---

# 🐳 Ejecución con Docker

El proyecto incluye un archivo `Dockerfile`.

## Construir la aplicación

```bash
mvn clean package
```

## Construir la imagen

```bash
docker build -t centropsico-system .
```

## Ejecutar el contenedor

```bash
docker run \
  -p 8080:8080 \
  -e DB_PASSWORD="TU_PASSWORD" \
  -e BREVO_API_KEY="TU_API_KEY" \
  -e MAIL_FROM="correo-verificado@dominio.com" \
  -e MAIL_FROM_NAME="CentroPsico" \
  -e APP_ALLOWED_ORIGINS="http://localhost:8080" \
  -e JWT_SECRET="TU_CLAVE_BASE64" \
  -e JWT_EXPIRATION_MS="1800000" \
  centropsico-system
```

---

# ☁️ Despliegue

```text
GitHub
   ↓
Render
   ├── Aplicación Spring Boot
   ├── API REST
   ├── Frontend estático
   ├── WebSocket
   └── Variables de entorno
           ↓
     MySQL Aiven Cloud
```

## Características

- Despliegue desde GitHub.
- Backend y frontend dentro de la misma aplicación.
- Base de datos externa y persistente.
- Conexión MySQL mediante SSL.
- Secretos configurados como variables de entorno.
- Envío de correos mediante Brevo.
- Fotografías persistentes en MySQL.
- Soporte de construcción mediante Docker.

---

# 🌐 Endpoints principales

## Autenticación

```http
POST /api/auth/login
GET  /api/auth/validate
POST /api/auth/logout
```

## Perfil

```http
GET    /api/profile/me
PUT    /api/profile/me
PUT    /api/profile/change-password
POST   /api/profile/photo
DELETE /api/profile/photo
```

## Usuarios

```http
POST   /api/users
GET    /api/users
GET    /api/users/{id}
PUT    /api/users/{id}
PATCH  /api/users/{id}/toggle-status
DELETE /api/users/{id}
```

## Pacientes

```http
POST   /api/patients
GET    /api/patients
GET    /api/patients/active
GET    /api/patients/{id}
PUT    /api/patients/{id}
PATCH  /api/patients/{id}/toggle-active
DELETE /api/patients/{id}
```

## Psicólogos

```http
POST   /api/psychologists
GET    /api/psychologists
GET    /api/psychologists/active
GET    /api/psychologists/{id}
PUT    /api/psychologists/{id}
PATCH  /api/psychologists/{id}/toggle-active
DELETE /api/psychologists/{id}
```

## Servicios

```http
POST   /api/services
GET    /api/services
GET    /api/services/{id}
PUT    /api/services/{id}
PATCH  /api/services/{id}/toggle-active
DELETE /api/services/{id}
```

## Disponibilidad

```http
POST   /api/psychologist-availabilities
GET    /api/psychologist-availabilities
GET    /api/psychologist-availabilities/active
GET    /api/psychologist-availabilities/{id}
GET    /api/psychologist-availabilities/psychologist/{psychologistId}
GET    /api/psychologist-availabilities/psychologist/{psychologistId}/count
PUT    /api/psychologist-availabilities/{id}
PATCH  /api/psychologist-availabilities/{id}/toggle-active
DELETE /api/psychologist-availabilities/{id}
```

## Citas

```http
POST   /api/appointments
GET    /api/appointments
GET    /api/appointments/{id}
GET    /api/appointments/by-date
GET    /api/appointments/my
GET    /api/appointments/my/by-date
GET    /api/appointments/psychologists-by-service
PUT    /api/appointments/{id}
PUT    /api/appointments/{id}/pay
PUT    /api/appointments/{id}/status
DELETE /api/appointments/{id}
```

## Historia clínica

```http
POST   /api/clinical-history/patient/{patientId}
GET    /api/clinical-history/patient/{patientId}
DELETE /api/clinical-history/{id}
```

## Portal público

```http
GET  /api/public/services
GET  /api/public/plans
GET  /api/public/promotions
GET  /api/public/availability
POST /api/public/leads
```

## Pre-reservas

```http
GET  /api/leads
GET  /api/leads/{id}
PUT  /api/leads/{id}/status
PUT  /api/leads/{id}/payment/validate
PUT  /api/leads/{id}/payment/reject
POST /api/leads/{id}/convert-to-appointment
```

## Finanzas

```http
POST   /api/finances/incomes
GET    /api/finances/incomes
GET    /api/finances/incomes/filter
PUT    /api/finances/incomes/{id}/review

POST   /api/finances/expense-categories
GET    /api/finances/expense-categories

POST   /api/finances/expenses
GET    /api/finances/expenses
GET    /api/finances/expenses/filter
PUT    /api/finances/expenses/{id}/review
DELETE /api/finances/expenses/{id}

GET    /api/finances/summary
```

## Dashboard

```http
GET /api/dashboard?period=day
GET /api/dashboard?period=week
GET /api/dashboard?period=month
GET /api/dashboard?period=year
```

## Reportes

```http
GET /api/reports/monthly
GET /api/reports/psychologist-performance
```

## Promociones

```http
POST   /api/promotions
GET    /api/promotions
GET    /api/promotions/{id}
PUT    /api/promotions/{id}
PATCH  /api/promotions/{id}/toggle-active
DELETE /api/promotions/{id}
```

## Notificaciones

```http
GET   /api/notifications/me
GET   /api/notifications/me/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/me/read-all
```

## Auditoría

```http
GET   /api/audit-logs/recent
GET   /api/audit-logs
GET   /api/audit-logs/page
GET   /api/audit-logs/module/{module}
PATCH /api/audit-logs/{id}/review
PATCH /api/audit-logs/{id}/pending
```

---

# 🧪 Pruebas recomendadas

## Autenticación y seguridad

- Iniciar sesión con credenciales correctas.
- Probar contraseña incorrecta.
- Probar usuario inactivo.
- Probar token expirado.
- Intentar acceder a rutas sin token.
- Intentar acceder con un rol sin permisos.
- Confirmar respuestas `401` y `403`.
- Revisar el registro de auditoría.

## Portal público

- Consultar servicios.
- Consultar promociones.
- Seleccionar psicólogo.
- Seleccionar fecha y horario.
- Registrar una pre-reserva.
- Verificar cálculo del adelanto.
- Verificar consentimiento.
- Confirmar notificación a administración.

## Pre-reservas

- Validar adelanto.
- Rechazar adelanto.
- Cambiar estado.
- Convertir una pre-reserva validada.
- Intentar convertir una pre-reserva no validada.
- Intentar convertir nuevamente una pre-reserva agendada.
- Verificar el ingreso generado.

## Citas

- Registrar cita.
- Intentar seleccionar una fecha pasada.
- Intentar registrar fuera de disponibilidad.
- Intentar crear un cruce.
- Consultar agenda diaria.
- Consultar calendario.
- Cambiar estado.
- Verificar citas pendientes de cierre.

## Pagos

- Registrar un adelanto.
- Registrar pago completo.
- Intentar superar el saldo.
- Intentar pagar una cita cancelada.
- Verificar el ingreso financiero.
- Imprimir comprobante.

## Historia clínica

- Acceder como administrador.
- Acceder como psicólogo asignado.
- Intentar acceder como recepcionista.
- Intentar acceder como psicólogo no asignado.
- Registrar evolución.
- Aplicar eliminación lógica.

## Finanzas

- Registrar ingreso.
- Registrar gasto.
- Registrar gasto superior a S/ 500.
- Revisar ingreso.
- Revisar gasto.
- Anular movimiento.
- Consultar resumen mensual.
- Verificar ganancia o pérdida.

## Perfil

- Actualizar datos.
- Cambiar contraseña.
- Subir fotografía.
- Recargar la aplicación.
- Cerrar e iniciar sesión.
- Realizar un redeploy.
- Confirmar persistencia de la fotografía.

---

# ⚠️ Limitaciones actuales

- La validación del adelanto es manual.
- No existe una integración directa con Yape, Plin o una pasarela bancaria.
- El sistema todavía no cuenta con un portal privado para pacientes.
- Las fotografías se almacenan dentro de MySQL como Base64.
- Hibernate utiliza `ddl-auto=update`.
- Las consultas SQL se muestran actualmente en los logs.
- El límite multipart actual es de 2 MB por archivo.
- El proyecto se encuentra orientado a una sola sede.
- La historia clínica depende del registro realizado por el profesional.
- El sistema no genera diagnósticos automáticos.

---

# 🛣️ Roadmap

## Implementado

- [x] Autenticación JWT.
- [x] Contraseñas BCrypt.
- [x] Control por roles.
- [x] Portal público.
- [x] Servicios y promociones.
- [x] Pre-reservas.
- [x] Consentimiento de datos.
- [x] Validación manual de adelantos.
- [x] Conversión de pre-reserva a cita.
- [x] Gestión de pacientes.
- [x] Gestión de psicólogos.
- [x] Disponibilidad semanal.
- [x] Generación automática de horarios.
- [x] Validación de cruces.
- [x] Agenda diaria.
- [x] Calendario FullCalendar.
- [x] Pagos parciales y completos.
- [x] Comprobantes imprimibles.
- [x] Historias clínicas.
- [x] Gestión financiera.
- [x] Dashboard por roles.
- [x] Reportes.
- [x] Promociones.
- [x] Notificaciones en tiempo real.
- [x] Recordatorios.
- [x] Auditoría.
- [x] Fotografías persistentes.
- [x] Despliegue en Render.
- [x] Base de datos en Aiven.

## Pendiente o futuro

- [ ] Consulta pública del estado de una pre-reserva.
- [ ] Portal privado para pacientes.
- [ ] Integración con pasarela de pago.
- [ ] Confirmaciones automáticas por WhatsApp.
- [ ] Videollamadas.
- [ ] Gestión de múltiples sedes.
- [ ] Migraciones mediante Flyway o Liquibase.
- [ ] Almacenamiento externo especializado para imágenes.
- [ ] Suite completa de pruebas automatizadas.
- [ ] Recuperación de contraseña por correo.
- [ ] Autenticación multifactor.
- [ ] Exportación avanzada de reportes.
- [ ] Copias de seguridad automatizadas.

---

# 📸 Capturas del sistema

Las capturas pueden almacenarse en una carpeta `docs/`:

```text
docs/
├── portal.png
├── login.png
├── dashboard-admin.png
├── dashboard-psicologo.png
├── solicitudes.png
├── pacientes.png
├── citas.png
├── calendario.png
├── historia-clinica.png
├── finanzas.png
├── reportes.png
├── auditoria.png
└── perfil.png
```

Ejemplo para mostrarlas:

```markdown
![Portal público](docs/portal.png)
![Dashboard administrador](docs/dashboard-admin.png)
![Gestión de citas](docs/citas.png)
![Módulo financiero](docs/finanzas.png)
```

---

# 👨‍💻 Autor

**Billy Rojas**

Proyecto desarrollado como una plataforma integral para optimizar la administración, la atención clínica, la programación de citas, el control financiero, la seguridad y la comunicación de un centro psicológico.

---

## 📄 Estado del proyecto

```text
Estado: Funcional y desplegado
Backend: Spring Boot 3.5.13
Java: 17
Base de datos: MySQL Aiven
Despliegue: Render
Autenticación: JWT
Correo: Brevo API
```