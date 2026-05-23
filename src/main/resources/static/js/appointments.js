let appointmentsData = [];
let currentAppointmentPage = 1;
const appointmentsPerPage = 10;

async function createAppointment() {

    const startTimeValue = document.getElementById("appointmentStartTime").value;
    const endTimeValue = document.getElementById("appointmentEndTime").value;

    if (!startTimeValue || !endTimeValue) {
        Swal.fire(
            "Horario requerido",
            "Selecciona uno de los horarios disponibles antes de guardar la cita.",
            "warning"
        );
        return;
    }
    if (!validateSelectedAppointmentTime()) {
        Swal.fire("Horario inválido", "Selecciona un horario dentro de la disponibilidad del psicólogo", "warning");
        return;
    }

    const startTime = document.getElementById("appointmentStartTime").value;
    const endTime = document.getElementById("appointmentEndTime").value;

    const data = {
        patient: { id: parseInt(document.getElementById("appointmentPatientId").value) },
        psychologist: { id: parseInt(document.getElementById("appointmentPsychologistId").value) },
        service: { id: parseInt(document.getElementById("appointmentServiceId").value) },
        date: document.getElementById("appointmentDate").value,
        startTime: startTime ? `${startTime}:00` : null,
        endTime: endTime ? `${endTime}:00` : null,
        status: document.getElementById("appointmentStatus").value,
        reason: document.getElementById("appointmentReason").value,
        observation: document.getElementById("appointmentObservation").value
    };

    try {
        const response = await authFetch(`${baseUrl}/appointments`, {
            method: "POST",
            body: JSON.stringify(data)
        });
        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire("Error", result.message || "Error al guardar cita", "error");
            return;
        }

        Swal.fire("Correcto", "Cita guardada correctamente", "success");
        clearAppointmentForm();
        loadAppointments();

        if (appointmentsCalendar) {
            await loadFullCalendar();
        }

    } catch (error) {
        Swal.fire("Error", "Error de conexión con el servidor", "error");
    }
}

async function loadAppointments() {
    try {
        const url = currentUser.role === "PSICOLOGO"
            ? `${baseUrl}/appointments/my`
            : `${baseUrl}/appointments`;

        const response = await authFetch(url);
        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            Swal.fire("Error", data.message || "Error al listar citas", "error");
            return;
        }

        appointmentsData = data;
        currentAppointmentPage = 1;
        renderAppointmentTable(getFilteredAppointments());

    } catch (error) {
        Swal.fire("Error", "Error al listar citas", "error");
    }
}

function renderAppointmentTable(data) {
    const tbody = document.getElementById("appointmentTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / appointmentsPerPage) || 1;

    if (currentAppointmentPage > totalPages) {
        currentAppointmentPage = totalPages;
    }

    const start = (currentAppointmentPage - 1) * appointmentsPerPage;
    const end = start + appointmentsPerPage;
    const pageData = data.slice(start, end);

    pageData.forEach(appointment => {
        const statusLabel = {
            PROGRAMADA: "Programada",
            ATENDIDA: "Atendida",
            CANCELADA: "Cancelada",
            NO_ASISTIO: "No asistió",
            REPROGRAMADA: "Reprogramada"
        };

        const currentStatus = appointment.status ?? "PROGRAMADA";
        const appointmentJson = JSON.stringify(appointment).replace(/'/g, "&apos;");

        tbody.innerHTML += `
            <tr>
                <td>${appointment.id ?? ""}</td>
                <td>${appointment.patient ? appointment.patient.firstName + " " + appointment.patient.lastName : ""}</td>
                <td>${appointment.psychologist ? appointment.psychologist.firstName + " " + appointment.psychologist.lastName : ""}</td>
                <td>${appointment.service ? appointment.service.name : ""}</td>
                <td>${appointment.date ?? ""}</td>
                <td>${appointment.startTime ?? ""}</td>
                <td>${appointment.endTime ?? ""}</td>
                <td>${statusLabel[currentStatus] ?? currentStatus}</td>
<td>
    <span class="status-pill ${getPaymentStatusClass(appointment.paymentStatus)}">
        ${formatPaymentStatus(appointment.paymentStatus)}
    </span>
</td>
<td>
    <strong>S/ ${Number(appointment.paidAmount || 0).toFixed(2)}</strong>
    <br>
    <small>Saldo: S/ ${Number(appointment.pendingAmount || 0).toFixed(2)}</small>
</td>
                <td>
${appointment.paymentStatus === "PAGADO"
                ? `<button class="btn-primary" onclick='showPaymentReceipt(${appointmentJson})'>Comprobante</button>`
                : `
        <button class="btn-primary" onclick="payAppointment(${appointment.id})">
            Registrar pago
        </button>
        ${Number(appointment.paidAmount || 0) > 0
                    ? `<button class="btn-secondary" onclick='showPaymentReceipt(${appointmentJson})'>Ver pago</button>`
                    : ""
                }
      `
            }

                    ${currentStatus === "PROGRAMADA"
                ? `
                                <button class="btn-secondary" onclick="updateAppointmentStatus(${appointment.id}, 'ATENDIDA')">Atendida</button>
                                <button class="btn-secondary" onclick="updateAppointmentStatus(${appointment.id}, 'CANCELADA')">Cancelar</button>
                                <button class="btn-secondary" onclick="updateAppointmentStatus(${appointment.id}, 'NO_ASISTIO')">No asistió</button>
                            `
                : `<span class="badge badge-role">${statusLabel[currentStatus] ?? currentStatus}</span>`
            }
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("appointmentPageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentAppointmentPage} de ${totalPages}`;
    }
}

async function payAppointment(id) {
    const appointment = appointmentsData.find(a => a.id === id);

    const totalAmount = Number(appointment?.totalAmount || appointment?.service?.price || 0);
    const paidAmount = Number(appointment?.paidAmount || 0);
    const pendingAmount = Number(appointment?.pendingAmount ?? (totalAmount - paidAmount));

    const { value: formValues } = await Swal.fire({
        title: "Registrar pago de cita",
        html: `
            <div class="payment-modal-form">
                <div class="payment-summary-box">
                    <div><span>Total:</span><strong>S/ ${totalAmount.toFixed(2)}</strong></div>
                    <div><span>Pagado:</span><strong>S/ ${paidAmount.toFixed(2)}</strong></div>
                    <div><span>Saldo:</span><strong>S/ ${pendingAmount.toFixed(2)}</strong></div>
                </div>

                <input id="paymentAmount" type="number" min="1" max="${pendingAmount}" step="0.01" placeholder="Monto a pagar o adelanto">

                <select id="paymentMethod">
                    <option value="">Seleccione método</option>
                    <option value="EFECTIVO">Efectivo</option>
                    <option value="YAPE">Yape</option>
                    <option value="PLIN">Plin</option>
                    <option value="TRANSFERENCIA">Transferencia</option>
                    <option value="TARJETA">Tarjeta</option>
                </select>

                <input id="paymentOperationCode" placeholder="Código de operación (opcional)">
                <textarea id="paymentObservation" placeholder="Observación del pago (opcional)"></textarea>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: "Registrar pago",
        cancelButtonText: "Cancelar",
        preConfirm: () => {
            const amount = Number(document.getElementById("paymentAmount").value);
            const method = document.getElementById("paymentMethod").value;
            const operationCode = document.getElementById("paymentOperationCode").value;
            const observation = document.getElementById("paymentObservation").value;

            if (!amount || amount <= 0) {
                Swal.showValidationMessage("Ingresa un monto válido");
                return false;
            }

            if (amount > pendingAmount) {
                Swal.showValidationMessage("El monto no puede superar el saldo pendiente");
                return false;
            }

            if (!method) {
                Swal.showValidationMessage("Selecciona un método de pago");
                return false;
            }

            return {
                amount,
                method,
                operationCode,
                observation,
                registeredBy: `${currentUser.firstName} ${currentUser.lastName}`
            };
        }
    });

    if (!formValues) return;

    try {
        const response = await authFetch(`${baseUrl}/appointments/${id}/pay`, {
            method: "PUT",
            body: JSON.stringify(formValues)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire("Error", result.message || "Error al registrar pago", "error");
            return;
        }

        await Swal.fire(
            "Pago registrado",
            result.paymentStatus === "PAGADO"
                ? "La cita quedó pagada completamente."
                : "Se registró el adelanto correctamente.",
            "success"
        );

        await loadAppointments();
        if (typeof loadDashboard === "function") await loadDashboard();
        if (typeof loadIncomes === "function") await loadIncomes();

    } catch (error) {
        Swal.fire("Error", "Error de conexión", "error");
    }
}

async function updateAppointmentStatus(id, status) {
    const labels = {
        ATENDIDA: "atendida",
        CANCELADA: "cancelada",
        NO_ASISTIO: "no asistió"
    };

    const confirm = await Swal.fire({
        title: "¿Actualizar estado?",
        text: `La cita será marcada como ${labels[status]}.`,
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Sí",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/appointments/${id}/status?status=${status}`, {
            method: "PUT"
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire("Error", result.message || "Error al actualizar estado", "error");
            return;
        }

        await Swal.fire("Correcto", "Estado actualizado", "success");

        loadAppointments();

        const date = document.getElementById("agendaDate")?.value;
        if (date && typeof loadAgenda === "function") loadAgenda();
        if (typeof loadDashboard === "function") loadDashboard();

    } catch (error) {
        Swal.fire("Error", "Error de conexión", "error");
    }
}

async function loadAgenda() {
    const date = document.getElementById("agendaDate").value;

    if (!date) {
        Swal.fire("Error", "Selecciona una fecha", "warning");
        return;
    }

    try {
        const url = currentUser.role === "PSICOLOGO"
            ? `${baseUrl}/appointments/my/by-date?date=${date}`
            : `${baseUrl}/appointments/by-date?date=${date}`;

        const response = await authFetch(url);
        if (!response) return;

        const data = await response.json();

        const container = document.getElementById("agendaResult");
        container.innerHTML = "";

        if (data.length === 0) {
            container.innerHTML = `<div class="empty-state">No hay citas</div>`;
            return;
        }

        data.sort((a, b) => a.startTime.localeCompare(b.startTime));

        data.forEach(app => {
            const patientName = app.patient
                ? `${app.patient.firstName ?? ""} ${app.patient.lastName ?? ""}`
                : app.patientName ?? "";

            const serviceName = app.service ? app.service.name : app.serviceName ?? "";

            container.innerHTML += `
                <div class="agenda-item">
                    <div>${app.startTime} - ${app.endTime}</div>
                    <div>${patientName} - ${serviceName}</div>
                </div>
            `;
        });

    } catch (error) {
        Swal.fire("Error", "Error al cargar agenda", "error");
    }
}

function showPaymentReceipt(appointment) {
    const patientName = appointment.patient
        ? `${appointment.patient.firstName ?? ""} ${appointment.patient.lastName ?? ""}`
        : "-";

    const psychologistName = appointment.psychologist
        ? `${appointment.psychologist.firstName ?? ""} ${appointment.psychologist.lastName ?? ""}`
        : "-";

    const serviceName = appointment.service ? appointment.service.name : "-";

    const totalAmount = Number(appointment.totalAmount || appointment.service?.price || 0).toFixed(2);
    const paidAmount = Number(appointment.paidAmount || 0).toFixed(2);
    const pendingAmount = Number(appointment.pendingAmount || 0).toFixed(2);

    const paymentDate = appointment.paymentDateTime
        ? appointment.paymentDateTime.replace("T", " ").substring(0, 16)
        : (appointment.paymentDate ?? "-");

    Swal.fire({
        title: "Comprobante de pago",
        html: `
            <div id="paymentReceipt" class="payment-receipt">
                <div class="receipt-header">
                    <div class="receipt-logo">CP</div>
                    <div class="receipt-title">
                        <h2>CentroPsico</h2>
                        <p>Comprobante de pago</p>
                    </div>
                    <span class="receipt-status">${formatPaymentStatus(appointment.paymentStatus)}</span>
                </div>

                <div class="receipt-section">
                    <div class="receipt-section-title">Datos de la cita</div>
                    <div class="receipt-row"><span>N° Cita:</span><strong>${appointment.id ?? "-"}</strong></div>
                    <div class="receipt-row"><span>Paciente:</span><strong>${patientName}</strong></div>
                    <div class="receipt-row"><span>Psicólogo:</span><strong>${psychologistName}</strong></div>
                    <div class="receipt-row"><span>Servicio:</span><strong>${serviceName}</strong></div>
                    <div class="receipt-row"><span>Fecha cita:</span><strong>${appointment.date ?? "-"} ${appointment.startTime ?? ""}</strong></div>
                </div>

                <div class="receipt-section">
                    <div class="receipt-section-title">Detalle de pago</div>
                    <div class="receipt-row"><span>Total:</span><strong>S/ ${totalAmount}</strong></div>
                    <div class="receipt-row"><span>Pagado:</span><strong>S/ ${paidAmount}</strong></div>
                    <div class="receipt-row"><span>Saldo:</span><strong>S/ ${pendingAmount}</strong></div>
                    <div class="receipt-row"><span>Método:</span><strong>${appointment.paymentMethod ?? "-"}</strong></div>
                    <div class="receipt-row"><span>Código operación:</span><strong>${appointment.operationCode || "-"}</strong></div>
                    <div class="receipt-row"><span>Fecha pago:</span><strong>${paymentDate}</strong></div>
                    <div class="receipt-row"><span>Registrado por:</span><strong>${appointment.paymentRegisteredBy || "-"}</strong></div>
                </div>

                <div class="receipt-note">
                    ${appointment.paymentObservation || "Sin observaciones"}
                </div>

                <div class="receipt-footer">
                    Documento generado por CentroPsico
                </div>
            </div>
        `,
        width: 600,
        showCancelButton: true,
        confirmButtonText: "Imprimir",
        cancelButtonText: "Cerrar",
        preConfirm: () => {
            printPaymentReceipt();
            return false;
        }
    });
}


function printPaymentReceipt() {
    const receipt = document.getElementById("paymentReceipt").innerHTML;
    const printWindow = window.open("", "_blank");

    printWindow.document.write(`
        <html>
            <head>
                <title>Comprobante de pago</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        padding: 30px;
                        color: #222;
                    }

                    .receipt-header {
                        text-align: center;
                        margin-bottom: 20px;
                    }

                    .receipt-header h2 {
                        margin: 0;
                        color: #0f3d66;
                    }

                    .receipt-row {
                        display: flex;
                        justify-content: space-between;
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                        font-size: 14px;
                    }

                    .receipt-note {
                        margin-top: 15px;
                        padding: 12px;
                        background: #f4f7fb;
                        border-radius: 8px;
                        font-size: 13px;
                    }
                </style>
            </head>
            <body>
                ${receipt}
                <script>
                    window.print();
                <\/script>
            </body>
        </html>
    `);

    printWindow.document.close();
}

function clearAppointmentForm() {
    document.getElementById("appointmentPatientId").value = "";
    document.getElementById("appointmentPsychologistId").value = "";
    document.getElementById("appointmentServiceId").value = "";
    document.getElementById("appointmentDate").value = "";
    document.getElementById("appointmentStartTime").value = "";
    document.getElementById("appointmentEndTime").value = "";
    document.getElementById("appointmentStatus").value = "PROGRAMADA";
    document.getElementById("appointmentReason").value = "";
    document.getElementById("appointmentObservation").value = "";
}

function filterAppointmentTable(value) {
    currentAppointmentPage = 1;
    renderAppointmentTable(getFilteredAppointments());
}

function getFilteredAppointments() {
    const searchInput = document.querySelector("#appointmentListModal .table-search");
    const search = searchInput ? searchInput.value.toLowerCase() : "";

    if (!search) return appointmentsData;

    return appointmentsData.filter(app => {
        const text = `
            ${app.id ?? ""}
            ${app.patient ? app.patient.firstName + " " + app.patient.lastName : ""}
            ${app.psychologist ? app.psychologist.firstName + " " + app.psychologist.lastName : ""}
            ${app.service ? app.service.name : ""}
            ${app.date ?? ""}
            ${app.startTime ?? ""}
            ${app.endTime ?? ""}
            ${app.status ?? ""}
            ${app.paid ? "Pagado" : "Pendiente"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function changeAppointmentPage(direction) {
    const filteredData = getFilteredAppointments();
    const totalPages = Math.ceil(filteredData.length / appointmentsPerPage) || 1;

    currentAppointmentPage += direction;

    if (currentAppointmentPage < 1) currentAppointmentPage = 1;
    if (currentAppointmentPage > totalPages) currentAppointmentPage = totalPages;

    renderAppointmentTable(filteredData);
}

async function toggleAppointmentList() {
    const modal = document.getElementById("appointmentListModal");
    modal.classList.remove("hidden");
    await loadAppointments();
}

function closeAppointmentList() {
    document.getElementById("appointmentListModal").classList.add("hidden");
}

let appointmentsCalendar = null;

async function loadFullCalendar() {
    const calendarEl = document.getElementById("appointmentsCalendar");

    if (!calendarEl) {
        Swal.fire("Error", "No se encontró el contenedor del calendario", "error");
        return;
    }

    const appointments = await getAppointmentsForCalendar();

    const events = appointments.map(app => {
        const patientName = app.patient
            ? `${app.patient.firstName ?? ""} ${app.patient.lastName ?? ""}`
            : "Paciente";

        const psychologistName = app.psychologist
            ? `${app.psychologist.firstName ?? ""} ${app.psychologist.lastName ?? ""}`
            : "Psicólogo";

        const serviceName = app.service ? app.service.name : "Servicio";

        return {
            id: app.id,
            title: `${patientName} - ${serviceName}`,
            start: `${app.date}T${app.startTime}`,
            end: `${app.date}T${app.endTime}`,
            extendedProps: {
                patientName,
                psychologistName,
                serviceName,
                status: app.status,
                paid: app.paid,
                paidAmount: app.paidAmount,
                observation: app.observation
            },
            className: getAppointmentEventClass(app.status)
        };
    });

    if (appointmentsCalendar) {
        appointmentsCalendar.destroy();
    }

    appointmentsCalendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "timeGridWeek",
        locale: "es",
        height: "auto",
        slotMinTime: "07:00:00",
        slotMaxTime: "22:00:00",
        allDaySlot: false,
        nowIndicator: true,
        selectable: false,
        editable: false,
        events: events,
        headerToolbar: {
            left: "prev,next today",
            center: "title",
            right: "dayGridMonth,timeGridWeek,timeGridDay,listWeek"
        },
        buttonText: {
            today: "Hoy",
            month: "Mes",
            week: "Semana",
            day: "Día",
            list: "Lista"
        },
        eventClick: function (info) {
            showCalendarAppointmentDetail(info.event);
        }
    });

    appointmentsCalendar.render();
}

async function getAppointmentsForCalendar() {
    try {
        const url = currentUser.role === "PSICOLOGO"
            ? `${baseUrl}/appointments/my`
            : `${baseUrl}/appointments`;

        const response = await authFetch(url);
        if (!response) return [];

        const data = await response.json();

        if (!response.ok) {
            Swal.fire("Error", data.message || "No se pudieron cargar las citas", "error");
            return [];
        }

        return data;

    } catch (error) {
        Swal.fire("Error", "Error al cargar calendario", "error");
        return [];
    }
}

function getAppointmentEventClass(status) {
    switch (status) {
        case "PROGRAMADA":
            return "calendar-event-programada";
        case "ATENDIDA":
            return "calendar-event-atendida";
        case "CANCELADA":
            return "calendar-event-cancelada";
        case "NO_ASISTIO":
            return "calendar-event-no-asistio";
        case "REPROGRAMADA":
            return "calendar-event-reprogramada";
        default:
            return "calendar-event-default";
    }
}

function showCalendarAppointmentDetail(event) {
    const props = event.extendedProps;

    Swal.fire({
        title: "Detalle de cita",
        html: `
            <div class="calendar-detail">
                <p><strong>Paciente:</strong> ${props.patientName}</p>
                <p><strong>Psicólogo:</strong> ${props.psychologistName}</p>
                <p><strong>Servicio:</strong> ${props.serviceName}</p>
                <p><strong>Estado:</strong> ${formatAppointmentStatus(props.status)}</p>
                <p><strong>Inicio:</strong> ${formatCalendarDateTime(event.start)}</p>
                <p><strong>Fin:</strong> ${formatCalendarDateTime(event.end)}</p>
                <p><strong>Pago:</strong> ${props.paid ? "Pagado" : "Pendiente"}</p>
                <p><strong>Monto:</strong> ${props.paidAmount ? "S/ " + Number(props.paidAmount).toFixed(2) : "-"}</p>
                <p><strong>Observación:</strong> ${props.observation || "Sin observación"}</p>
            </div>
        `,
        icon: "info",
        confirmButtonText: "Cerrar"
    });
}

function formatAppointmentStatus(status) {
    const labels = {
        PROGRAMADA: "Programada",
        ATENDIDA: "Atendida",
        CANCELADA: "Cancelada",
        NO_ASISTIO: "No asistió",
        REPROGRAMADA: "Reprogramada"
    };

    return labels[status] || status || "-";
}

function formatCalendarDateTime(date) {
    if (!date) return "-";

    return date.toLocaleString("es-PE", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

async function refreshAppointmentsRealtime() {
    await loadAppointments();

    if (typeof loadDashboard === "function") {
        await loadDashboard();
    }

    const appointmentSection = document.getElementById("appointments");

    if (appointmentSection && appointmentSection.classList.contains("active")) {
        const date = document.getElementById("agendaDate")?.value;

        if (date && typeof loadAgenda === "function") {
            await loadAgenda();
        }

        if (typeof loadFullCalendar === "function" && appointmentsCalendar) {
            await loadFullCalendar();
        }
    }
}

const appointmentDayMap = {
    0: "SUNDAY",
    1: "MONDAY",
    2: "TUESDAY",
    3: "WEDNESDAY",
    4: "THURSDAY",
    5: "FRIDAY",
    6: "SATURDAY"
};

const appointmentDayLabels = {
    MONDAY: "Lunes",
    TUESDAY: "Martes",
    WEDNESDAY: "Miércoles",
    THURSDAY: "Jueves",
    FRIDAY: "Viernes",
    SATURDAY: "Sábado",
    SUNDAY: "Domingo"
};

async function checkPsychologistAvailabilityForAppointment() {
    const psychologistId = document.getElementById("appointmentPsychologistId")?.value;
    const dateValue = document.getElementById("appointmentDate")?.value;
    const startInput = document.getElementById("appointmentStartTime");
    const endInput = document.getElementById("appointmentEndTime");

    removeAppointmentAvailabilityInfo();

    if (!psychologistId || !dateValue) {
        startInput.disabled = true;
        endInput.disabled = true;
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/psychologist-availabilities/psychologist/${psychologistId}`);
        if (!response) return;

        const availabilities = await response.json();

        const selectedDate = new Date(`${dateValue}T00:00:00`);
        const selectedDay = appointmentDayMap[selectedDate.getDay()];

        const validAvailabilities = availabilities.filter(a =>
            a.active === true && a.dayOfWeek === selectedDay
        );

        if (validAvailabilities.length === 0) {
            startInput.value = "";
            endInput.value = "";
            startInput.disabled = true;
            endInput.disabled = true;

            showAppointmentAvailabilityInfo(
                `Este psicólogo no atiende los ${appointmentDayLabels[selectedDay]}. Selecciona otra fecha o registra disponibilidad.`,
                "error"
            );
            return;
        }

        startInput.disabled = false;
        endInput.disabled = false;

        const rangesText = validAvailabilities
            .map(a => `${formatAppointmentTime(a.startTime)} - ${formatAppointmentTime(a.endTime)}`)
            .join(", ");

        showAppointmentAvailabilityInfo(
            `Horario disponible para ${appointmentDayLabels[selectedDay]}: ${rangesText}`,
            "success"
        );

        const firstRange = validAvailabilities[0];
        startInput.min = formatAppointmentTime(firstRange.startTime);
        startInput.max = formatAppointmentTime(firstRange.endTime);
        endInput.min = formatAppointmentTime(firstRange.startTime);
        endInput.max = formatAppointmentTime(firstRange.endTime);

    } catch (error) {
        console.error("Error validando disponibilidad:", error);
        showAppointmentAvailabilityInfo("No se pudo validar la disponibilidad del psicólogo.", "error");
    }
}

function validateSelectedAppointmentTime() {
    const psychologistId = document.getElementById("appointmentPsychologistId")?.value;
    const dateValue = document.getElementById("appointmentDate")?.value;
    const startInput = document.getElementById("appointmentStartTime");
    const endInput = document.getElementById("appointmentEndTime");

    if (!psychologistId || !dateValue || !startInput.value || !endInput.value) return true;

    const min = startInput.min;
    const max = startInput.max;

    if (min && max) {
        if (startInput.value < min || endInput.value > max || startInput.value >= endInput.value) {
            showAppointmentAvailabilityInfo(
                `La cita debe estar dentro del horario disponible: ${min} - ${max}`,
                "error"
            );
            return false;
        }
    }

    return true;
}

function showAppointmentAvailabilityInfo(message, type = "info") {
    let box = document.getElementById("appointmentAvailabilityInfo");

    if (!box) {
        box = document.createElement("div");
        box.id = "appointmentAvailabilityInfo";
        box.className = "availability-info-box";

        const formActions = document.querySelector("#appointments .form-actions");
        formActions.parentNode.insertBefore(box, formActions);
    }

    box.textContent = message;
    box.classList.remove("availability-info-success", "availability-info-error", "availability-info-neutral");

    if (type === "success") {
        box.classList.add("availability-info-success");
    } else if (type === "error") {
        box.classList.add("availability-info-error");
    } else {
        box.classList.add("availability-info-neutral");
    }
}

function removeAppointmentAvailabilityInfo() {
    const box = document.getElementById("appointmentAvailabilityInfo");
    if (box) box.remove();
}

function formatAppointmentTime(time) {
    if (!time) return "";
    return time.substring(0, 5);
}

function initAppointmentAvailabilityEvents() {

    const psychologistSelect = document.getElementById("appointmentPsychologistId");
    const serviceSelect = document.getElementById("appointmentServiceId");

    const dateInput = document.getElementById("appointmentDate");

    const startInput = document.getElementById("appointmentStartTime");
    const endInput = document.getElementById("appointmentEndTime");

    if (startInput) {
        startInput.disabled = true;
    }

    if (endInput) {
        endInput.disabled = true;
    }

    // =========================
    // GENERAR HORARIOS AUTOMÁTICOS
    // =========================

    if (psychologistSelect) {
        psychologistSelect.addEventListener(
            "change",
            generateAvailableAppointmentSlots
        );
    }

    if (serviceSelect) {
        serviceSelect.addEventListener(
            "change",
            generateAvailableAppointmentSlots
        );
    }

    if (dateInput) {
        dateInput.addEventListener(
            "change",
            generateAvailableAppointmentSlots
        );
    }

    // =========================
    // VALIDAR HORARIO SELECCIONADO
    // =========================

    if (startInput) {
        startInput.addEventListener(
            "change",
            validateSelectedAppointmentTime
        );
    }

    if (endInput) {
        endInput.addEventListener(
            "change",
            validateSelectedAppointmentTime
        );
    }
}

async function generateAvailableAppointmentSlots() {
    const psychologistId = document.getElementById("appointmentPsychologistId")?.value;
    const serviceSelect = document.getElementById("appointmentServiceId");
    const dateValue = document.getElementById("appointmentDate")?.value;
    const slotsContainer = document.getElementById("appointmentSlotsContainer");

    const startInput = document.getElementById("appointmentStartTime");
    const endInput = document.getElementById("appointmentEndTime");

    if (!slotsContainer) return;

    slotsContainer.innerHTML = "";

    startInput.value = "";
    endInput.value = "";
    startInput.disabled = true;
    endInput.disabled = true;

    if (!psychologistId || !serviceSelect.value || !dateValue) {
        slotsContainer.innerHTML = `
            <span class="slots-placeholder">
                Selecciona psicólogo, servicio y fecha para ver horarios disponibles.
            </span>
        `;
        return;
    }

    const duration = getSelectedServiceDuration();

    if (!duration || duration <= 0) {
        slotsContainer.innerHTML = `
            <span class="slots-error">
                No se pudo detectar la duración del servicio.
            </span>
        `;
        return;
    }

    try {
        const availabilityResponse = await authFetch(
            `${baseUrl}/psychologist-availabilities/psychologist/${psychologistId}`
        );

        const appointmentsResponse = await authFetch(`${baseUrl}/appointments`);

        if (!availabilityResponse || !appointmentsResponse) return;

        const availabilities = await availabilityResponse.json();
        const appointments = await appointmentsResponse.json();

        const selectedDate = new Date(`${dateValue}T00:00:00`);
        const selectedDay = appointmentDayMap[selectedDate.getDay()];

        const dayAvailabilities = availabilities.filter(a =>
            a.active === true && a.dayOfWeek === selectedDay
        );

        if (dayAvailabilities.length === 0) {
            slotsContainer.innerHTML = `
                <span class="slots-error">
                    Este psicólogo no atiende los ${appointmentDayLabels[selectedDay]}.
                </span>
            `;
            return;
        }

        const occupiedAppointments = appointments.filter(a =>
            a.psychologist?.id == psychologistId &&
            a.date === dateValue &&
            a.status !== "CANCELADA"
        );

        const slots = [];

        dayAvailabilities.forEach(availability => {
            const generated = buildTimeSlots(
                formatAppointmentTime(availability.startTime),
                formatAppointmentTime(availability.endTime),
                duration
            );

            generated.forEach(slot => {
                const isOccupied = occupiedAppointments.some(app =>
                    slot.start < formatAppointmentTime(app.endTime) &&
                    slot.end > formatAppointmentTime(app.startTime)
                );

                if (!isOccupied) {
                    slots.push(slot);
                }
            });
        });

        if (slots.length === 0) {
            slotsContainer.innerHTML = `
                <span class="slots-error">
                    No hay horarios libres para esta fecha.
                </span>
            `;
            return;
        }

        slotsContainer.innerHTML = `
            <div class="slots-title">Horarios disponibles</div>
            <div class="slots-grid">
                ${slots.map(slot => `
                    <button type="button"
                            class="slot-btn"
                            onclick="selectAppointmentSlot('${slot.start}', '${slot.end}')">
                        ${slot.start} - ${slot.end}
                    </button>
                `).join("")}
            </div>
        `;

    } catch (error) {
        console.error("Error generando horarios:", error);

        slotsContainer.innerHTML = `
            <span class="slots-error">
                Error al cargar horarios disponibles.
            </span>
        `;
    }
}

function buildTimeSlots(startTime, endTime, durationMinutes) {
    const slots = [];

    let current = timeToMinutes(startTime);
    const end = timeToMinutes(endTime);

    while (current + durationMinutes <= end) {
        const slotStart = minutesToTime(current);
        const slotEnd = minutesToTime(current + durationMinutes);

        slots.push({
            start: slotStart,
            end: slotEnd
        });

        current += durationMinutes;
    }

    return slots;
}

function selectAppointmentSlot(start, end) {
    document.getElementById("appointmentStartTime").value = start;
    document.getElementById("appointmentEndTime").value = end;

    document.querySelectorAll(".slot-btn").forEach(btn => {
        btn.classList.remove("selected");
    });

    const selectedButton = [...document.querySelectorAll(".slot-btn")]
        .find(btn => btn.textContent.includes(`${start} - ${end}`));

    if (selectedButton) {
        selectedButton.classList.add("selected");
    }
}

function getSelectedServiceDuration() {
    const select = document.getElementById("appointmentServiceId");
    const text = select.options[select.selectedIndex]?.textContent || "";

    const match = text.match(/(\d+)\s*min/i);

    return match ? parseInt(match[1]) : null;
}

function timeToMinutes(time) {
    const [hours, minutes] = time.split(":").map(Number);
    return hours * 60 + minutes;
}

function minutesToTime(totalMinutes) {
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

function formatPaymentStatus(status) {
    const labels = {
        PENDIENTE: "Pendiente",
        PARCIAL: "Adelanto",
        PAGADO: "Pagado"
    };

    return labels[status] || "Pendiente";
}

function getPaymentStatusClass(status) {
    if (status === "PAGADO") return "active";
    if (status === "PARCIAL") return "partial";
    return "inactive";
}