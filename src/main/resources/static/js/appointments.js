let appointmentsData = [];
let currentAppointmentPage = 1;
const appointmentsPerPage = 10;

async function createAppointment() {
    const patientId = document.getElementById("appointmentPatientId")?.value;
    const psychologistId = document.getElementById("appointmentPsychologistId")?.value;
    const serviceId = document.getElementById("appointmentServiceId")?.value;
    const dateValue = document.getElementById("appointmentDate")?.value;
    const startTimeValue = document.getElementById("appointmentStartTime")?.value;
    const endTimeValue = document.getElementById("appointmentEndTime")?.value;

    if (!patientId) {
        Swal.fire(
            "Paciente requerido",
            "Selecciona un paciente antes de guardar la cita.",
            "warning"
        );
        return;
    }

    if (!serviceId) {
        Swal.fire(
            "Servicio requerido",
            "Selecciona un servicio o especialidad.",
            "warning"
        );
        return;
    }

    if (!psychologistId) {
        Swal.fire(
            "Psicólogo requerido",
            "Selecciona uno de los psicólogos disponibles.",
            "warning"
        );
        return;
    }

    if (!dateValue) {
        Swal.fire(
            "Fecha requerida",
            "Selecciona una fecha correspondiente al horario del psicólogo.",
            "warning"
        );
        return;
    }

    if (!startTimeValue || !endTimeValue) {
        Swal.fire(
            "Horario requerido",
            "Selecciona uno de los horarios disponibles antes de guardar la cita.",
            "warning"
        );
        return;
    }

    if (!validateSelectedAppointmentTime()) {
        Swal.fire(
            "Horario inválido",
            "Selecciona un horario dentro de la disponibilidad del psicólogo.",
            "warning"
        );
        return;
    }

    const data = {
        patient: {
            id: Number(patientId)
        },
        psychologist: {
            id: Number(psychologistId)
        },
        service: {
            id: Number(serviceId)
        },
        date: dateValue,
        startTime: normalizeAppointmentTimeForBackend(startTimeValue),
        endTime: normalizeAppointmentTimeForBackend(endTimeValue),
        status: document.getElementById("appointmentStatus")?.value || "PROGRAMADA",
        reason: document.getElementById("appointmentReason")?.value || "",
        observation: document.getElementById("appointmentObservation")?.value || ""
    };

    try {
        const response = await authFetch(`${baseUrl}/appointments`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        let result = {};

        try {
            result = await response.json();
        } catch (error) {
            console.error("La respuesta de la cita no contiene JSON válido:", error);
        }

        if (!response.ok) {
            Swal.fire(
                "Error",
                result.message || "Error al guardar la cita.",
                "error"
            );
            return;
        }

        await Swal.fire(
            "Correcto",
            "Cita guardada correctamente.",
            "success"
        );

        clearAppointmentForm();
        await loadAppointments();

        if (appointmentsCalendar) {
            await loadFullCalendar();
        }

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

    } catch (error) {
        console.error("Error registrando cita:", error);

        Swal.fire(
            "Error",
            "Error de conexión con el servidor.",
            "error"
        );
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
            Swal.fire(
                "Error",
                data.message || "Error al listar citas.",
                "error"
            );
            return;
        }

        appointmentsData = Array.isArray(data) ? data : [];
        currentAppointmentPage = 1;

        renderAppointmentTable(
            getFilteredAppointments()
        );

    } catch (error) {
        console.error("Error listando citas:", error);

        Swal.fire(
            "Error",
            "Error al listar citas.",
            "error"
        );
    }
}

function renderAppointmentTable(data) {
    const tbody = document.getElementById("appointmentTableBody");

    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    const safeData = Array.isArray(data) ? data : [];
    const totalPages = Math.ceil(safeData.length / appointmentsPerPage) || 1;

    if (currentAppointmentPage > totalPages) {
        currentAppointmentPage = totalPages;
    }

    const start = (currentAppointmentPage - 1) * appointmentsPerPage;
    const end = start + appointmentsPerPage;
    const pageData = safeData.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="11" class="empty-state">
                    No existen citas para mostrar.
                </td>
            </tr>
        `;
    }

    pageData.forEach(appointment => {
        const statusLabel = {
            PROGRAMADA: "Programada",
            ATENDIDA: "Atendida",
            CANCELADA: "Cancelada",
            NO_ASISTIO: "No asistió",
            REPROGRAMADA: "Reprogramada"
        };

        const currentStatus = appointment.status ?? "PROGRAMADA";

        const appointmentJson = JSON.stringify(appointment)
            .replace(/'/g, "&apos;");

        const paymentInfo = getAppointmentPaymentInfo(appointment);

        const canManagePayments =
            currentUser.role === "ADMIN" ||
            currentUser.role === "RECEPCIONISTA";

        const isPsychologist =
            currentUser.role === "PSICOLOGO";

        const canUpdateStatus =
            currentUser.role === "ADMIN" ||
            currentUser.role === "RECEPCIONISTA" ||
            currentUser.role === "PSICOLOGO";

        const canPay =
            canManagePayments &&
            canRegisterAppointmentPayment(
                appointment,
                paymentInfo
            );

        const canShowReceipt =
            canManagePayments &&
            (
                paymentInfo.paidAmount > 0 ||
                paymentInfo.status === "PAGADO"
            );

        const patientName = appointment.patient
            ? `${appointment.patient.firstName ?? ""} ${appointment.patient.lastName ?? ""}`.trim()
            : "";

        const psychologistName = appointment.psychologist
            ? `${appointment.psychologist.firstName ?? ""} ${appointment.psychologist.lastName ?? ""}`.trim()
            : "";

        const serviceName =
            appointment.service?.name ?? "";

        tbody.innerHTML += `
            <tr>
                <td>${appointment.id ?? ""}</td>
                <td>${escapeAppointmentHtml(patientName)}</td>
                <td>${escapeAppointmentHtml(psychologistName)}</td>
                <td>${escapeAppointmentHtml(serviceName)}</td>
                <td>${appointment.date ?? ""}</td>
                <td>${formatAppointmentTime(appointment.startTime)}</td>
                <td>${formatAppointmentTime(appointment.endTime)}</td>
                <td>${statusLabel[currentStatus] ?? currentStatus}</td>

                ${!isPsychologist ? `
                    <td>
                        <span class="status-pill ${getPaymentStatusClass(paymentInfo.status)}">
                            ${formatPaymentStatus(paymentInfo.status)}
                        </span>
                    </td>

                    <td>
                        <strong>
                            S/ ${paymentInfo.paidAmount.toFixed(2)}
                        </strong>

                        <br>

                        <small>
                            Saldo: S/ ${paymentInfo.pendingAmount.toFixed(2)}
                        </small>
                    </td>
                ` : ""}

                <td>
                    ${canPay ? `
                        <button
                            class="btn-primary"
                            onclick="payAppointment(${appointment.id})"
                        >
                            Registrar pago
                        </button>
                    ` : ""}

                    ${canShowReceipt ? `
                        <button
                            class="btn-secondary"
                            onclick='showPaymentReceipt(${appointmentJson})'
                        >
                            ${
                                paymentInfo.status === "PAGADO"
                                    ? "Comprobante"
                                    : "Ver pago"
                            }
                        </button>
                    ` : ""}

                    ${
                        canManagePayments &&
                        !canPay &&
                        !canShowReceipt
                            ? `
                                <span class="badge badge-role">
                                    Sin pago
                                </span>
                            `
                            : ""
                    }

                    ${
                        currentStatus === "PROGRAMADA" &&
                        canUpdateStatus
                            ? `
                                <button
                                    class="btn-secondary"
                                    onclick="updateAppointmentStatus(${appointment.id}, 'ATENDIDA')"
                                >
                                    Atendida
                                </button>

                                <button
                                    class="btn-secondary"
                                    onclick="updateAppointmentStatus(${appointment.id}, 'NO_ASISTIO')"
                                >
                                    No asistió
                                </button>

                                ${
                                    currentUser.role === "ADMIN" ||
                                    currentUser.role === "RECEPCIONISTA"
                                        ? `
                                            <button
                                                class="btn-secondary"
                                                onclick="updateAppointmentStatus(${appointment.id}, 'CANCELADA')"
                                            >
                                                Cancelar
                                            </button>
                                        `
                                        : ""
                                }
                            `
                            : `
                                <span class="badge badge-role">
                                    ${statusLabel[currentStatus] ?? currentStatus}
                                </span>
                            `
                    }
                </td>
            </tr>
        `;
    });

    const pageInfo =
        document.getElementById("appointmentPageInfo");

    if (pageInfo) {
        pageInfo.textContent =
            `Página ${currentAppointmentPage} de ${totalPages}`;
    }
}

async function payAppointment(id) {
    const appointment =
        appointmentsData.find(
            item => item.id === id
        );

    if (!appointment) {
        Swal.fire(
            "Error",
            "No se encontró la cita seleccionada.",
            "error"
        );
        return;
    }

    const paymentInfo =
        getAppointmentPaymentInfo(appointment);

    if (
        appointment.status === "CANCELADA" ||
        appointment.status === "NO_ASISTIO"
    ) {
        Swal.fire({
            icon: "warning",
            title: "Pago no permitido",
            text: "No puedes registrar pagos en una cita cancelada o marcada como no asistió."
        });
        return;
    }

    if (
        paymentInfo.pendingAmount <= 0 ||
        paymentInfo.status === "PAGADO"
    ) {
        Swal.fire({
            icon: "info",
            title: "Cita pagada",
            text: "Esta cita ya no tiene saldo pendiente."
        });
        return;
    }

    if (paymentInfo.totalAmount <= 0) {
        Swal.fire({
            icon: "warning",
            title: "Monto no configurado",
            text: "La cita no tiene un monto total válido para registrar pago."
        });
        return;
    }

    closeAppointmentListIfOpen();

    const { value: formValues } = await Swal.fire({
        title: "Registrar pago de cita",

        html: `
            <div class="payment-modal-form">
                <div class="payment-summary-box">
                    <div>
                        <span>Total:</span>
                        <strong>
                            S/ ${paymentInfo.totalAmount.toFixed(2)}
                        </strong>
                    </div>

                    <div>
                        <span>Pagado:</span>
                        <strong>
                            S/ ${paymentInfo.paidAmount.toFixed(2)}
                        </strong>
                    </div>

                    <div>
                        <span>Saldo:</span>
                        <strong>
                            S/ ${paymentInfo.pendingAmount.toFixed(2)}
                        </strong>
                    </div>
                </div>

                <input
                    id="paymentAmount"
                    type="number"
                    min="0.01"
                    max="${paymentInfo.pendingAmount}"
                    step="0.01"
                    placeholder="Monto a pagar o adelanto"
                >

                <select id="paymentMethod">
                    <option value="">
                        Seleccione método
                    </option>

                    <option value="EFECTIVO">
                        Efectivo
                    </option>

                    <option value="YAPE">
                        Yape
                    </option>

                    <option value="PLIN">
                        Plin
                    </option>

                    <option value="TRANSFERENCIA">
                        Transferencia
                    </option>

                    <option value="TARJETA">
                        Tarjeta
                    </option>
                </select>

                <input
                    id="paymentOperationCode"
                    placeholder="Código de operación (opcional)"
                >

                <textarea
                    id="paymentObservation"
                    placeholder="Observación del pago (opcional)"
                ></textarea>
            </div>
        `,

        showCancelButton: true,
        confirmButtonText: "Registrar pago",
        cancelButtonText: "Cancelar",

        preConfirm: () => {
            const amount =
                Number(
                    document.getElementById("paymentAmount")?.value
                );

            const method =
                document.getElementById("paymentMethod")?.value;

            const operationCode =
                document.getElementById("paymentOperationCode")?.value || "";

            const observation =
                document.getElementById("paymentObservation")?.value || "";

            if (!amount || amount <= 0) {
                Swal.showValidationMessage(
                    "Ingresa un monto válido."
                );
                return false;
            }

            if (amount > paymentInfo.pendingAmount) {
                Swal.showValidationMessage(
                    "El monto no puede superar el saldo pendiente."
                );
                return false;
            }

            if (!method) {
                Swal.showValidationMessage(
                    "Selecciona un método de pago."
                );
                return false;
            }

            return {
                amount,
                method,
                operationCode,
                observation,
                registeredBy:
                    `${currentUser.firstName ?? ""} ${currentUser.lastName ?? ""}`.trim()
            };
        }
    });

    if (!formValues) return;

    try {
        const response = await authFetch(
            `${baseUrl}/appointments/${id}/pay`,
            {
                method: "PUT",
                body: JSON.stringify(formValues)
            }
        );

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire(
                "Error",
                result.message || "Error al registrar pago.",
                "error"
            );
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

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        if (typeof loadIncomes === "function") {
            await loadIncomes();
        }

    } catch (error) {
        console.error("Error registrando pago:", error);

        Swal.fire(
            "Error",
            "Error de conexión.",
            "error"
        );
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
        text: `La cita será marcada como ${labels[status] || status}.`,
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Sí",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(
            `${baseUrl}/appointments/${id}/status?status=${encodeURIComponent(status)}`,
            {
                method: "PUT"
            }
        );

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire(
                "Error",
                result.message || "Error al actualizar estado.",
                "error"
            );
            return;
        }

        await Swal.fire(
            "Correcto",
            "Estado actualizado.",
            "success"
        );

        await loadAppointments();

        const date =
            document.getElementById("agendaDate")?.value;

        if (
            date &&
            typeof loadAgenda === "function"
        ) {
            await loadAgenda();
        }

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        if (appointmentsCalendar) {
            await loadFullCalendar();
        }

    } catch (error) {
        console.error("Error actualizando estado:", error);

        Swal.fire(
            "Error",
            "Error de conexión.",
            "error"
        );
    }
}

async function loadAgenda() {
    const dateInput =
        document.getElementById("agendaDate");

    const container =
        document.getElementById("agendaResult");

    if (!dateInput || !container) {
        Swal.fire(
            "Error",
            "No se encontró la agenda diaria.",
            "error"
        );
        return;
    }

    const date = dateInput.value;

    if (!date) {
        Swal.fire(
            "Fecha requerida",
            "Selecciona una fecha para consultar la agenda.",
            "warning"
        );
        return;
    }

    try {
        container.innerHTML = `
            <div class="agenda-loading">
                Cargando agenda del día...
            </div>
        `;

        const url = currentUser.role === "PSICOLOGO"
            ? `${baseUrl}/appointments/my/by-date?date=${encodeURIComponent(date)}`
            : `${baseUrl}/appointments/by-date?date=${encodeURIComponent(date)}`;

        const response = await authFetch(url);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            container.innerHTML = `
                <div class="empty-state">
                    No se pudo cargar la agenda.
                </div>
            `;
            return;
        }

        container.innerHTML = "";

        if (!Array.isArray(data) || data.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    No hay citas programadas para esta fecha.
                </div>
            `;
            return;
        }

        data.sort(
            (first, second) =>
                String(first.startTime)
                    .localeCompare(
                        String(second.startTime)
                    )
        );

        data.forEach(appointment => {
            const patientName = appointment.patient
                ? `${appointment.patient.firstName ?? ""} ${appointment.patient.lastName ?? ""}`.trim()
                : appointment.patientName ?? "Paciente";

            const psychologistName = appointment.psychologist
                ? `${appointment.psychologist.firstName ?? ""} ${appointment.psychologist.lastName ?? ""}`.trim()
                : appointment.psychologistName ?? "Psicólogo";

            const serviceName = appointment.service
                ? appointment.service.name
                : appointment.serviceName ?? "Servicio";

            const status =
                formatAppointmentStatus(
                    appointment.status
                );

            container.innerHTML += `
                <div class="agenda-item agenda-item-improved">
                    <div class="agenda-time">
                        <strong>
                            ${formatAgendaTime(appointment.startTime)}
                            -
                            ${formatAgendaTime(appointment.endTime)}
                        </strong>

                        <span>
                            ${status}
                        </span>
                    </div>

                    <div class="agenda-info">
                        <strong>
                            ${escapeAppointmentHtml(patientName)}
                        </strong>

                        <span>
                            ${escapeAppointmentHtml(serviceName)}
                        </span>
                    </div>

                    <div class="agenda-psychologist">
                        ${escapeAppointmentHtml(psychologistName)}
                    </div>
                </div>
            `;
        });

    } catch (error) {
        console.error("Error al cargar agenda:", error);

        container.innerHTML = `
            <div class="empty-state">
                Error al cargar agenda.
            </div>
        `;
    }
}

function formatAgendaTime(time) {
    if (!time) {
        return "-";
    }

    return String(time).substring(0, 5);
}

function getTodayLocalDate() {
    const today = new Date();

    const year =
        today.getFullYear();

    const month =
        String(today.getMonth() + 1)
            .padStart(2, "0");

    const day =
        String(today.getDate())
            .padStart(2, "0");

    return `${year}-${month}-${day}`;
}

async function openAppointmentsAgenda(date = null) {
    showSectionById("appointments");

    const agendaDateInput =
        document.getElementById("agendaDate");

    if (!agendaDateInput) {
        Swal.fire(
            "Error",
            "No se encontró el campo de fecha de la agenda.",
            "error"
        );
        return;
    }

    const selectedDate =
        date || getTodayLocalDate();

    agendaDateInput.value =
        selectedDate;

    await loadAgenda();

    setTimeout(() => {
        const agendaBox =
            document.getElementById("agendaResult");

        if (agendaBox) {
            agendaBox.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });
        }
    }, 250);
}

function showPaymentReceipt(appointment) {
    const patientName = appointment.patient
        ? `${appointment.patient.firstName ?? ""} ${appointment.patient.lastName ?? ""}`.trim()
        : "-";

    const psychologistName = appointment.psychologist
        ? `${appointment.psychologist.firstName ?? ""} ${appointment.psychologist.lastName ?? ""}`.trim()
        : "-";

    const serviceName =
        appointment.service?.name ?? "-";

    const paymentInfo =
        getAppointmentPaymentInfo(appointment);

    const totalAmount =
        paymentInfo.totalAmount.toFixed(2);

    const paidAmount =
        paymentInfo.paidAmount.toFixed(2);

    const pendingAmount =
        paymentInfo.pendingAmount.toFixed(2);

    const paymentDate =
        appointment.paymentDateTime
            ? appointment.paymentDateTime
                .replace("T", " ")
                .substring(0, 16)
            : appointment.paymentDate ?? "-";

    Swal.fire({
        title: "Comprobante de pago",

        html: `
            <div
                id="paymentReceipt"
                class="payment-receipt"
            >
                <div class="receipt-header">
                    <div class="receipt-logo">
                        CP
                    </div>

                    <div class="receipt-title">
                        <h2>CentroPsico</h2>
                        <p>Comprobante de pago</p>
                    </div>

                    <span class="receipt-status">
                        ${formatPaymentStatus(paymentInfo.status)}
                    </span>
                </div>

                <div class="receipt-section">
                    <div class="receipt-section-title">
                        Datos de la cita
                    </div>

                    <div class="receipt-row">
                        <span>N.º de cita:</span>
                        <strong>${appointment.id ?? "-"}</strong>
                    </div>

                    <div class="receipt-row">
                        <span>Paciente:</span>
                        <strong>
                            ${escapeAppointmentHtml(patientName)}
                        </strong>
                    </div>

                    <div class="receipt-row">
                        <span>Psicólogo:</span>
                        <strong>
                            ${escapeAppointmentHtml(psychologistName)}
                        </strong>
                    </div>

                    <div class="receipt-row">
                        <span>Servicio:</span>
                        <strong>
                            ${escapeAppointmentHtml(serviceName)}
                        </strong>
                    </div>

                    <div class="receipt-row">
                        <span>Fecha cita:</span>
                        <strong>
                            ${appointment.date ?? "-"}
                            ${formatAppointmentTime(appointment.startTime)}
                        </strong>
                    </div>
                </div>

                <div class="receipt-section">
                    <div class="receipt-section-title">
                        Detalle de pago
                    </div>

                    <div class="receipt-row">
                        <span>Total:</span>
                        <strong>S/ ${totalAmount}</strong>
                    </div>

                    <div class="receipt-row">
                        <span>Pagado:</span>
                        <strong>S/ ${paidAmount}</strong>
                    </div>

                    <div class="receipt-row">
                        <span>Saldo:</span>
                        <strong>S/ ${pendingAmount}</strong>
                    </div>

                    <div class="receipt-row">
                        <span>Método:</span>
                        <strong>
                            ${appointment.paymentMethod ?? "-"}
                        </strong>
                    </div>

                    <div class="receipt-row">
                        <span>Código operación:</span>
                        <strong>
                            ${appointment.operationCode || "-"}
                        </strong>
                    </div>

                    <div class="receipt-row">
                        <span>Fecha pago:</span>
                        <strong>${paymentDate}</strong>
                    </div>

                    <div class="receipt-row">
                        <span>Registrado por:</span>
                        <strong>
                            ${escapeAppointmentHtml(
                                appointment.paymentRegisteredBy || "-"
                            )}
                        </strong>
                    </div>
                </div>

                <div class="receipt-note">
                    ${
                        escapeAppointmentHtml(
                            appointment.paymentObservation ||
                            "Sin observaciones"
                        )
                    }
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
    const receiptElement =
        document.getElementById("paymentReceipt");

    if (!receiptElement) {
        return;
    }

    const printWindow =
        window.open("", "_blank");

    if (!printWindow) {
        Swal.fire(
            "Ventana bloqueada",
            "Permite las ventanas emergentes para imprimir el comprobante.",
            "warning"
        );
        return;
    }

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
                ${receiptElement.innerHTML}

                <script>
                    window.print();
                <\/script>
            </body>
        </html>
    `);

    printWindow.document.close();
}

function clearAppointmentForm() {
    const patientSelect =
        document.getElementById("appointmentPatientId");

    const psychologistSelect =
        document.getElementById("appointmentPsychologistId");

    const serviceSelect =
        document.getElementById("appointmentServiceId");

    const dateInput =
        document.getElementById("appointmentDate");

    const statusSelect =
        document.getElementById("appointmentStatus");

    const reasonInput =
        document.getElementById("appointmentReason");

    const observationInput =
        document.getElementById("appointmentObservation");

    if (patientSelect) {
        patientSelect.value = "";
    }

    if (serviceSelect) {
        serviceSelect.value = "";
    }

    if (psychologistSelect) {
        psychologistSelect.value = "";

        psychologistSelect.innerHTML = `
            <option value="">
                Seleccione primero un servicio
            </option>
        `;

        psychologistSelect.disabled = true;
    }

    if (dateInput) {
        dateInput.value = "";
        dateInput.min = getTodayLocalDate();
        dateInput.disabled = true;
    }

    if (statusSelect) {
        statusSelect.value = "PROGRAMADA";
    }

    if (reasonInput) {
        reasonInput.value = "";
    }

    if (observationInput) {
        observationInput.value = "";
    }

    availablePsychologistsByService = [];
    selectedPsychologistSchedules = [];

    clearSelectedAppointmentSlot();
    clearAvailableAppointmentSlots();
    removeAppointmentAvailabilityInfo();

    renderPsychologistSchedulePanel(`
        <div class="slots-placeholder">
            Selecciona un servicio para ver los psicólogos
            y sus horarios.
        </div>
    `);
}

function filterAppointmentTable() {
    currentAppointmentPage = 1;

    renderAppointmentTable(
        getFilteredAppointments()
    );
}

function getFilteredAppointments() {
    const searchInput =
        document.querySelector(
            "#appointmentListModal .table-search"
        );

    const search =
        searchInput
            ? searchInput.value.toLowerCase().trim()
            : "";

    if (!search) {
        return appointmentsData;
    }

    return appointmentsData.filter(appointment => {
        const text = `
            ${appointment.id ?? ""}
            ${
                appointment.patient
                    ? `${appointment.patient.firstName ?? ""} ${appointment.patient.lastName ?? ""}`
                    : ""
            }
            ${
                appointment.psychologist
                    ? `${appointment.psychologist.firstName ?? ""} ${appointment.psychologist.lastName ?? ""}`
                    : ""
            }
            ${appointment.service?.name ?? ""}
            ${appointment.date ?? ""}
            ${appointment.startTime ?? ""}
            ${appointment.endTime ?? ""}
            ${appointment.status ?? ""}
            ${appointment.paid ? "Pagado" : "Pendiente"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function changeAppointmentPage(direction) {
    const filteredData =
        getFilteredAppointments();

    const totalPages =
        Math.ceil(
            filteredData.length /
            appointmentsPerPage
        ) || 1;

    currentAppointmentPage += direction;

    if (currentAppointmentPage < 1) {
        currentAppointmentPage = 1;
    }

    if (currentAppointmentPage > totalPages) {
        currentAppointmentPage = totalPages;
    }

    renderAppointmentTable(filteredData);
}

async function toggleAppointmentList() {
    const modal =
        document.getElementById("appointmentListModal");

    if (!modal) {
        return;
    }

    modal.classList.remove("hidden");

    await loadAppointments();
}

function closeAppointmentList() {
    const modal =
        document.getElementById("appointmentListModal");

    if (modal) {
        modal.classList.add("hidden");
    }
}

let appointmentsCalendar = null;

async function loadFullCalendar() {
    const calendarEl =
        document.getElementById("appointmentsCalendar");

    if (!calendarEl) {
        Swal.fire(
            "Error",
            "No se encontró el contenedor del calendario.",
            "error"
        );
        return;
    }

    const appointments =
        await getAppointmentsForCalendar();

    const events = appointments.map(appointment => {
        const patientName = appointment.patient
            ? `${appointment.patient.firstName ?? ""} ${appointment.patient.lastName ?? ""}`.trim()
            : "Paciente";

        const psychologistName = appointment.psychologist
            ? `${appointment.psychologist.firstName ?? ""} ${appointment.psychologist.lastName ?? ""}`.trim()
            : "Psicólogo";

        const serviceName =
            appointment.service?.name ?? "Servicio";

        return {
            id: appointment.id,
            title: `${patientName} - ${serviceName}`,
            start: `${appointment.date}T${appointment.startTime}`,
            end: `${appointment.date}T${appointment.endTime}`,

            extendedProps: {
                patientName,
                psychologistName,
                serviceName,
                status: appointment.status,
                paid: appointment.paid,
                paidAmount: appointment.paidAmount,
                observation: appointment.observation
            },

            className:
                getAppointmentEventClass(
                    appointment.status
                )
        };
    });

    if (appointmentsCalendar) {
        appointmentsCalendar.destroy();
    }

    appointmentsCalendar =
        new FullCalendar.Calendar(
            calendarEl,
            {
                initialView: "timeGridWeek",
                locale: "es",
                height: "auto",
                slotMinTime: "07:00:00",
                slotMaxTime: "22:00:00",
                allDaySlot: false,
                nowIndicator: true,
                selectable: false,
                editable: false,
                events,

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

                eventClick(info) {
                    showCalendarAppointmentDetail(
                        info.event
                    );
                }
            }
        );

    appointmentsCalendar.render();
}

async function getAppointmentsForCalendar() {
    try {
        const url = currentUser.role === "PSICOLOGO"
            ? `${baseUrl}/appointments/my`
            : `${baseUrl}/appointments`;

        const response =
            await authFetch(url);

        if (!response) {
            return [];
        }

        const data =
            await response.json();

        if (!response.ok) {
            Swal.fire(
                "Error",
                data.message ||
                "No se pudieron cargar las citas.",
                "error"
            );

            return [];
        }

        return Array.isArray(data)
            ? data
            : [];

    } catch (error) {
        console.error("Error cargando calendario:", error);

        Swal.fire(
            "Error",
            "Error al cargar calendario.",
            "error"
        );

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
    const props =
        event.extendedProps;

    Swal.fire({
        title: "Detalle de cita",

        html: `
            <div class="calendar-detail">
                <p>
                    <strong>Paciente:</strong>
                    ${escapeAppointmentHtml(props.patientName)}
                </p>

                <p>
                    <strong>Psicólogo:</strong>
                    ${escapeAppointmentHtml(props.psychologistName)}
                </p>

                <p>
                    <strong>Servicio:</strong>
                    ${escapeAppointmentHtml(props.serviceName)}
                </p>

                <p>
                    <strong>Estado:</strong>
                    ${formatAppointmentStatus(props.status)}
                </p>

                <p>
                    <strong>Inicio:</strong>
                    ${formatCalendarDateTime(event.start)}
                </p>

                <p>
                    <strong>Fin:</strong>
                    ${formatCalendarDateTime(event.end)}
                </p>

                ${
                    currentUser.role !== "PSICOLOGO"
                        ? `
                            <p>
                                <strong>Pago:</strong>
                                ${props.paid ? "Pagado" : "Pendiente"}
                            </p>

                            <p>
                                <strong>Monto:</strong>
                                ${
                                    props.paidAmount
                                        ? `S/ ${Number(props.paidAmount).toFixed(2)}`
                                        : "-"
                                }
                            </p>
                        `
                        : ""
                }

                <p>
                    <strong>Observación:</strong>
                    ${
                        escapeAppointmentHtml(
                            props.observation ||
                            "Sin observación"
                        )
                    }
                </p>
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
    if (!date) {
        return "-";
    }

    return date.toLocaleString(
        "es-PE",
        {
            dateStyle: "short",
            timeStyle: "short"
        }
    );
}

async function refreshAppointmentsRealtime() {
    await loadAppointments();

    if (typeof loadDashboard === "function") {
        await loadDashboard();
    }

    const appointmentSection =
        document.getElementById("appointments");

    if (
        appointmentSection &&
        appointmentSection.classList.contains("active")
    ) {
        const date =
            document.getElementById("agendaDate")?.value;

        if (
            date &&
            typeof loadAgenda === "function"
        ) {
            await loadAgenda();
        }

        if (
            typeof loadFullCalendar === "function" &&
            appointmentsCalendar
        ) {
            await loadFullCalendar();
        }
    }
}

/*
 * =========================================================
 * DISPONIBILIDAD AUTOMÁTICA PARA CITAS
 * =========================================================
 */

let availablePsychologistsByService = [];
let selectedPsychologistSchedules = [];

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

function initAppointmentAvailabilityEvents() {
    const serviceSelect =
        document.getElementById("appointmentServiceId");

    const psychologistSelect =
        document.getElementById("appointmentPsychologistId");

    const dateInput =
        document.getElementById("appointmentDate");

    const startInput =
        document.getElementById("appointmentStartTime");

    const endInput =
        document.getElementById("appointmentEndTime");

    if (
        !serviceSelect ||
        !psychologistSelect ||
        !dateInput
    ) {
        console.warn(
            "No se encontraron todos los campos del formulario de citas."
        );
        return;
    }

    if (
        serviceSelect.dataset.availabilityInitialized === "true"
    ) {
        return;
    }

    serviceSelect.dataset.availabilityInitialized = "true";

    psychologistSelect.disabled = true;
    dateInput.disabled = true;
    dateInput.min = getTodayLocalDate();

    psychologistSelect.innerHTML = `
        <option value="">
            Seleccione primero un servicio
        </option>
    `;

    if (startInput) {
        startInput.value = "";
        startInput.disabled = true;
        startInput.readOnly = true;
    }

    if (endInput) {
        endInput.value = "";
        endInput.disabled = true;
        endInput.readOnly = true;
    }

    serviceSelect.addEventListener(
        "change",
        handleAppointmentServiceChange
    );

    psychologistSelect.addEventListener(
        "change",
        handleAppointmentPsychologistChange
    );

    dateInput.addEventListener(
        "change",
        generateAvailableAppointmentSlots
    );

    renderPsychologistSchedulePanel(`
        <div class="slots-placeholder">
            Selecciona un servicio para ver los psicólogos
            y sus horarios.
        </div>
    `);

    clearAvailableAppointmentSlots();
}

async function handleAppointmentServiceChange() {
    const serviceId =
        document.getElementById("appointmentServiceId")?.value;

    resetPsychologistSelection();

    if (!serviceId) {
        showAppointmentAvailabilityInfo(
            "Selecciona un servicio para consultar los psicólogos y sus horarios.",
            "neutral"
        );
        return;
    }

    await loadPsychologistsByService(serviceId);
}

async function loadPsychologistsByService(serviceId) {
    const psychologistSelect =
        document.getElementById("appointmentPsychologistId");

    if (!psychologistSelect) {
        return;
    }

    psychologistSelect.disabled = true;

    psychologistSelect.innerHTML = `
        <option value="">
            Cargando psicólogos...
        </option>
    `;

    renderPsychologistSchedulePanel(`
        <div class="slots-placeholder">
            Consultando psicólogos y horarios disponibles...
        </div>
    `);

    try {
        const response = await authFetch(
            `${baseUrl}/appointments/psychologists-by-service?serviceId=${encodeURIComponent(serviceId)}`
        );

        if (!response) {
            psychologistSelect.innerHTML = `
                <option value="">
                    No se pudo realizar la consulta
                </option>
            `;
            return;
        }

        let data = [];

        try {
            data = await response.json();
        } catch (error) {
            console.error(
                "La respuesta de psicólogos no contiene JSON válido:",
                error
            );
        }

        if (!response.ok) {
            const message =
                data?.message ||
                "No se pudieron consultar los psicólogos.";

            psychologistSelect.innerHTML = `
                <option value="">
                    Error al cargar psicólogos
                </option>
            `;

            renderPsychologistSchedulePanel(`
                <div class="slots-error">
                    ${escapeAppointmentHtml(message)}
                </div>
            `);

            showAppointmentAvailabilityInfo(
                message,
                "error"
            );

            return;
        }

        availablePsychologistsByService =
            Array.isArray(data)
                ? data
                : [];

        if (
            availablePsychologistsByService.length === 0
        ) {
            psychologistSelect.innerHTML = `
                <option value="">
                    No hay psicólogos para este servicio
                </option>
            `;

            psychologistSelect.disabled = true;

            renderPsychologistSchedulePanel(`
                <div class="slots-error">
                    No existen psicólogos activos con horarios
                    registrados para el servicio seleccionado.
                </div>
            `);

            showAppointmentAvailabilityInfo(
                "No se encontraron psicólogos disponibles para este servicio.",
                "error"
            );

            return;
        }

        psychologistSelect.innerHTML = `
            <option value="">
                Seleccione un psicólogo
            </option>

            ${
                availablePsychologistsByService
                    .map(psychologist => `
                        <option value="${psychologist.psychologistId}">
                            ${
                                escapeAppointmentHtml(
                                    psychologist.psychologistName ||
                                    "Psicólogo"
                                )
                            }
                            ${
                                psychologist.specialty
                                    ? ` - ${escapeAppointmentHtml(psychologist.specialty)}`
                                    : ""
                            }
                        </option>
                    `)
                    .join("")
            }
        `;

        psychologistSelect.disabled = false;

        renderAllPsychologistSchedules(
            availablePsychologistsByService
        );

        showAppointmentAvailabilityInfo(
            "Selecciona un psicólogo para consultar sus días y horarios de atención.",
            "success"
        );

    } catch (error) {
        console.error(
            "Error consultando psicólogos por servicio:",
            error
        );

        psychologistSelect.innerHTML = `
            <option value="">
                Error de conexión
            </option>
        `;

        psychologistSelect.disabled = true;

        renderPsychologistSchedulePanel(`
            <div class="slots-error">
                No se pudo conectar con el servidor para
                consultar los psicólogos.
            </div>
        `);

        showAppointmentAvailabilityInfo(
            "No se pudieron cargar los psicólogos.",
            "error"
        );
    }
}

function renderAllPsychologistSchedules(psychologists) {
    if (
        !Array.isArray(psychologists) ||
        psychologists.length === 0
    ) {
        renderPsychologistSchedulePanel(`
            <div class="slots-placeholder">
                No existen psicólogos para mostrar.
            </div>
        `);

        return;
    }

    renderPsychologistSchedulePanel(`
        <div class="psychologist-schedule-title">
            Psicólogos y horarios de atención
        </div>

        <div class="psychologist-schedule-grid">
            ${
                psychologists
                    .map(psychologist =>
                        buildPsychologistScheduleCard(
                            psychologist
                        )
                    )
                    .join("")
            }
        </div>
    `);
}

function buildPsychologistScheduleCard(psychologist) {
const schedules =
    removeDuplicatePsychologistSchedules(
        Array.isArray(psychologist.schedules)
            ? psychologist.schedules
            : []
    );

    const scheduleHtml =
        schedules.length > 0
            ? schedules
                .map(schedule => `
                    <div class="psychologist-schedule-row">
                        <strong>
                            ${
                                escapeAppointmentHtml(
                                    schedule.dayLabel ||
                                    appointmentDayLabels[schedule.dayOfWeek] ||
                                    schedule.dayOfWeek ||
                                    ""
                                )
                            }:
                        </strong>

                        <span>
                            ${formatAppointmentTime(schedule.startTime)}
                            -
                            ${formatAppointmentTime(schedule.endTime)}
                        </span>
                    </div>
                `)
                .join("")
            : `
                <div class="slots-error">
                    Sin horarios registrados
                </div>
            `;

    return `
        <button
            type="button"
            class="psychologist-schedule-card"
            data-psychologist-id="${psychologist.psychologistId}"
            onclick="selectPsychologistFromScheduleCard(${psychologist.psychologistId})"
        >
            <div class="psychologist-schedule-card-header">
                <div class="psychologist-schedule-avatar">
                    ${
                        getPsychologistInitials(
                            psychologist.psychologistName
                        )
                    }
                </div>

                <div>
                    <strong>
                        ${
                            escapeAppointmentHtml(
                                psychologist.psychologistName ||
                                "Psicólogo"
                            )
                        }
                    </strong>

                    <span>
                        ${
                            escapeAppointmentHtml(
                                psychologist.specialty ||
                                "Especialidad no registrada"
                            )
                        }
                    </span>
                </div>
            </div>

            <div class="psychologist-schedule-card-body">
                ${scheduleHtml}
            </div>

            <div class="psychologist-schedule-card-action">
                Seleccionar psicólogo
            </div>
        </button>
    `;
}

function selectPsychologistFromScheduleCard(psychologistId) {
    const psychologistSelect =
        document.getElementById("appointmentPsychologistId");

    if (!psychologistSelect) {
        return;
    }

    psychologistSelect.value =
        String(psychologistId);

    psychologistSelect.dispatchEvent(
        new Event(
            "change",
            {
                bubbles: true
            }
        )
    );

    psychologistSelect.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

function handleAppointmentPsychologistChange() {
    const psychologistId =
        document.getElementById("appointmentPsychologistId")?.value;

    const dateInput =
        document.getElementById("appointmentDate");

    clearSelectedAppointmentSlot();
    clearAvailableAppointmentSlots();

    document
        .querySelectorAll(".psychologist-schedule-card")
        .forEach(card => {
            card.classList.remove("selected");
        });

    if (!psychologistId) {
        selectedPsychologistSchedules = [];

        if (dateInput) {
            dateInput.value = "";
            dateInput.disabled = true;
        }

        renderAllPsychologistSchedules(
            availablePsychologistsByService
        );

        showAppointmentAvailabilityInfo(
            "Selecciona un psicólogo para ver sus próximas fechas disponibles.",
            "neutral"
        );

        return;
    }

    const selectedPsychologist =
        availablePsychologistsByService.find(
            psychologist =>
                String(psychologist.psychologistId) ===
                String(psychologistId)
        );

    if (!selectedPsychologist) {
        selectedPsychologistSchedules = [];

        if (dateInput) {
            dateInput.value = "";
            dateInput.disabled = true;
        }

        showAppointmentAvailabilityInfo(
            "No se encontró la información del psicólogo seleccionado.",
            "error"
        );

        return;
    }

    /*
     * Elimina horarios duplicados que puedan venir del backend.
     */
    selectedPsychologistSchedules =
        removeDuplicatePsychologistSchedules(
            Array.isArray(selectedPsychologist.schedules)
                ? selectedPsychologist.schedules
                : []
        );

    selectedPsychologist.schedules =
        selectedPsychologistSchedules;

    const selectedCard =
        document.querySelector(
            `.psychologist-schedule-card[data-psychologist-id="${psychologistId}"]`
        );

    if (selectedCard) {
        selectedCard.classList.add("selected");
    }

    if (dateInput) {
        dateInput.disabled = false;
        dateInput.value = "";
        dateInput.min = getTodayLocalDate();
    }

    renderSelectedPsychologistSchedule(
        selectedPsychologist
    );

    showAppointmentAvailabilityInfo(
        `Selecciona una de las próximas fechas disponibles de ${selectedPsychologist.psychologistName}.`,
        "success"
    );
}

function renderSelectedPsychologistSchedule(psychologist) {
    const schedules =
        removeDuplicatePsychologistSchedules(
            Array.isArray(psychologist.schedules)
                ? psychologist.schedules
                : []
        );

    const availableDates =
        getUpcomingPsychologistDates(
            schedules,
            30,
            12
        );

    renderPsychologistSchedulePanel(`
        <div class="selected-psychologist-schedule">

            <div class="selected-psychologist-header">
                <div class="psychologist-schedule-avatar">
                    ${getPsychologistInitials(
                        psychologist.psychologistName
                    )}
                </div>

                <div>
                    <strong>
                        ${escapeAppointmentHtml(
                            psychologist.psychologistName ||
                            "Psicólogo"
                        )}
                    </strong>

                    <span>
                        ${escapeAppointmentHtml(
                            psychologist.specialty ||
                            "Especialidad no registrada"
                        )}
                    </span>
                </div>
            </div>

            <div class="psychologist-weekly-schedule">
                <strong class="schedule-section-title">
                    Horario semanal
                </strong>

                <div class="selected-psychologist-days">
                    ${
                        schedules.length > 0
                            ? schedules
                                .map(schedule => `
                                    <div class="selected-schedule-item">
                                        <span>
                                            ${escapeAppointmentHtml(
                                                schedule.dayLabel ||
                                                appointmentDayLabels[schedule.dayOfWeek] ||
                                                schedule.dayOfWeek ||
                                                ""
                                            )}
                                        </span>

                                        <strong>
                                            ${formatAppointmentTime(
                                                schedule.startTime
                                            )}
                                            -
                                            ${formatAppointmentTime(
                                                schedule.endTime
                                            )}
                                        </strong>
                                    </div>
                                `)
                                .join("")
                            : `
                                <div class="slots-error">
                                    Este psicólogo no tiene horarios registrados.
                                </div>
                            `
                    }
                </div>
            </div>

            <div class="psychologist-upcoming-dates">
                <strong class="schedule-section-title">
                    Próximas fechas disponibles
                </strong>

                <p class="selected-psychologist-help">
                    Selecciona una fecha. El sistema la colocará
                    automáticamente en el formulario y mostrará sus horas libres.
                </p>

                <div class="appointment-date-grid">
                    ${
                        availableDates.length > 0
                            ? availableDates
                                .map(dateItem => `
                                    <button
                                        type="button"
                                        class="appointment-date-card"
                                        data-date="${dateItem.date}"
                                        onclick="selectAvailableAppointmentDate('${dateItem.date}', this)"
                                    >
                                        <span class="appointment-date-day">
                                            ${escapeAppointmentHtml(
                                                dateItem.dayLabel
                                            )}
                                        </span>

                                        <strong>
                                            ${escapeAppointmentHtml(
                                                dateItem.formattedDate
                                            )}
                                        </strong>

                                        <small>
                                            ${escapeAppointmentHtml(
                                                dateItem.scheduleLabel
                                            )}
                                        </small>
                                    </button>
                                `)
                                .join("")
                            : `
                                <div class="slots-error">
                                    No se encontraron próximas fechas disponibles.
                                </div>
                            `
                    }
                </div>
            </div>

        </div>
    `);
}

function removeDuplicatePsychologistSchedules(schedules) {
    const uniqueSchedules = new Map();

    schedules.forEach(schedule => {
        const dayOfWeek =
            String(schedule.dayOfWeek || "").trim();

        const startTime =
            formatAppointmentTime(schedule.startTime);

        const endTime =
            formatAppointmentTime(schedule.endTime);

        if (!dayOfWeek || !startTime || !endTime) {
            return;
        }

        const key =
            `${dayOfWeek}-${startTime}-${endTime}`;

        if (!uniqueSchedules.has(key)) {
            uniqueSchedules.set(key, {
                ...schedule,
                dayOfWeek,
                startTime,
                endTime,
                dayLabel:
                    schedule.dayLabel ||
                    appointmentDayLabels[dayOfWeek] ||
                    dayOfWeek
            });
        }
    });

    return [...uniqueSchedules.values()];
}

function getUpcomingPsychologistDates(
    schedules,
    daysToSearch = 30,
    maximumDates = 12
) {
    const uniqueSchedules =
        removeDuplicatePsychologistSchedules(schedules);

    const availableDates = [];
    const today = new Date();

    today.setHours(0, 0, 0, 0);

    for (
        let dayOffset = 0;
        dayOffset <= daysToSearch;
        dayOffset++
    ) {
        const currentDate = new Date(today);

        currentDate.setDate(
            today.getDate() + dayOffset
        );

        const dayOfWeek =
            appointmentDayMap[currentDate.getDay()];

        const schedulesForDate =
            uniqueSchedules.filter(
                schedule =>
                    schedule.dayOfWeek === dayOfWeek
            );

        if (schedulesForDate.length === 0) {
            continue;
        }

        const localDate =
            formatDateToLocalIso(currentDate);

        /*
         * No agrega el día actual cuando todos sus horarios ya pasaron.
         */
        const hasFutureSchedule =
            schedulesForDate.some(schedule => {
                if (localDate !== getTodayLocalDate()) {
                    return true;
                }

                return !isAppointmentSlotInPast(
                    localDate,
                    formatAppointmentTime(
                        schedule.endTime
                    )
                );
            });

        if (!hasFutureSchedule) {
            continue;
        }

        const scheduleLabel =
            schedulesForDate
                .map(schedule =>
                    `${formatAppointmentTime(schedule.startTime)} - ${formatAppointmentTime(schedule.endTime)}`
                )
                .join(" / ");

        availableDates.push({
            date: localDate,

            dayLabel:
                appointmentDayLabels[dayOfWeek] ||
                dayOfWeek,

            formattedDate:
                currentDate.toLocaleDateString(
                    "es-PE",
                    {
                        day: "2-digit",
                        month: "short",
                        year: "numeric"
                    }
                ),

            scheduleLabel
        });

        if (
            availableDates.length >= maximumDates
        ) {
            break;
        }
    }

    return availableDates;
}

function formatDateToLocalIso(date) {
    const year =
        date.getFullYear();

    const month =
        String(date.getMonth() + 1)
            .padStart(2, "0");

    const day =
        String(date.getDate())
            .padStart(2, "0");

    return `${year}-${month}-${day}`;
}

async function selectAvailableAppointmentDate(
    dateValue,
    selectedButton = null
) {
    const dateInput =
        document.getElementById("appointmentDate");

    if (!dateInput) {
        return;
    }

    dateInput.disabled = false;
    dateInput.value = dateValue;

    document
        .querySelectorAll(".appointment-date-card")
        .forEach(button => {
            button.classList.remove("selected");
        });

    if (selectedButton) {
        selectedButton.classList.add("selected");
    }

    /*
     * Genera automáticamente las horas libres
     * después de seleccionar la fecha.
     */
    await generateAvailableAppointmentSlots();

    dateInput.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

function buildPsychologistAvailabilityMessage(psychologist) {
    const schedules =
        removeDuplicatePsychologistSchedules(
            Array.isArray(psychologist.schedules)
                ? psychologist.schedules
                : []
        );

    if (schedules.length === 0) {
        return "El psicólogo seleccionado no tiene horarios registrados.";
    }

    const ranges = schedules
        .map(schedule => {
            const day =
                schedule.dayLabel ||
                appointmentDayLabels[schedule.dayOfWeek] ||
                schedule.dayOfWeek ||
                "";

            return `${day}: ${formatAppointmentTime(schedule.startTime)} - ${formatAppointmentTime(schedule.endTime)}`;
        })
        .join(" | ");

    return `Horario de ${psychologist.psychologistName}: ${ranges}`;
}

async function generateAvailableAppointmentSlots() {
    const psychologistId =
        document.getElementById("appointmentPsychologistId")?.value;

    const serviceId =
        document.getElementById("appointmentServiceId")?.value;

    const dateValue =
        document.getElementById("appointmentDate")?.value;

    const slotsContainer =
        document.getElementById("appointmentSlotsContainer");

    clearSelectedAppointmentSlot();

    if (!slotsContainer) {
        console.warn(
            "No se encontró appointmentSlotsContainer."
        );
        return;
    }

    if (!serviceId) {
        slotsContainer.innerHTML = `
            <span class="slots-placeholder">
                Selecciona un servicio.
            </span>
        `;
        return;
    }

    if (!psychologistId) {
        slotsContainer.innerHTML = `
            <span class="slots-placeholder">
                Selecciona un psicólogo.
            </span>
        `;
        return;
    }

    if (!dateValue) {
        slotsContainer.innerHTML = `
            <span class="slots-placeholder">
                Selecciona una fecha correspondiente al horario
                semanal del psicólogo.
            </span>
        `;
        return;
    }

    const selectedDate =
        new Date(`${dateValue}T00:00:00`);

    if (
        Number.isNaN(
            selectedDate.getTime()
        )
    ) {
        slotsContainer.innerHTML = `
            <span class="slots-error">
                La fecha seleccionada no es válida.
            </span>
        `;
        return;
    }

    const today =
        getTodayLocalDate();

    if (dateValue < today) {
        slotsContainer.innerHTML = `
            <span class="slots-error">
                No puedes registrar una cita en una fecha pasada.
            </span>
        `;

        showAppointmentAvailabilityInfo(
            "Selecciona una fecha actual o futura.",
            "error"
        );

        return;
    }

    const selectedDay =
        appointmentDayMap[
            selectedDate.getDay()
        ];

    const daySchedules =
        selectedPsychologistSchedules.filter(
            schedule =>
                schedule.dayOfWeek === selectedDay
        );

    if (daySchedules.length === 0) {
        slotsContainer.innerHTML = `
            <span class="slots-error">
                El psicólogo seleccionado no atiende los
                ${appointmentDayLabels[selectedDay] || selectedDay}.
                Revisa su horario semanal y selecciona otro día.
            </span>
        `;

        showAppointmentAvailabilityInfo(
            `El psicólogo no atiende los ${appointmentDayLabels[selectedDay] || selectedDay}.`,
            "error"
        );

        return;
    }

    const duration =
        getSelectedServiceDuration();

    if (!duration || duration <= 0) {
        slotsContainer.innerHTML = `
            <span class="slots-error">
                No se pudo identificar la duración del servicio.
                Verifica que la opción del servicio incluya, por ejemplo,
                “60 min”.
            </span>
        `;

        return;
    }

    slotsContainer.innerHTML = `
        <div class="slots-loading">
            Consultando horarios libres...
        </div>
    `;

    try {
        const appointmentsResponse =
            await authFetch(
                currentUser.role === "PSICOLOGO"
                    ? `${baseUrl}/appointments/my`
                    : `${baseUrl}/appointments`
            );

        if (!appointmentsResponse) {
            slotsContainer.innerHTML = `
                <span class="slots-error">
                    No se pudieron consultar las citas existentes.
                </span>
            `;
            return;
        }

        const appointments =
            await appointmentsResponse.json();

        if (!appointmentsResponse.ok) {
            slotsContainer.innerHTML = `
                <span class="slots-error">
                    ${
                        escapeAppointmentHtml(
                            appointments?.message ||
                            "No se pudieron consultar los horarios ocupados."
                        )
                    }
                </span>
            `;
            return;
        }

        const occupiedAppointments =
            (
                Array.isArray(appointments)
                    ? appointments
                    : []
            )
            .filter(appointment =>
                String(appointment.psychologist?.id) ===
                    String(psychologistId) &&
                appointment.date === dateValue &&
                appointment.status !== "CANCELADA"
            );

        const slots = [];

        daySchedules.forEach(schedule => {
            const generatedSlots =
                buildTimeSlots(
                    formatAppointmentTime(schedule.startTime),
                    formatAppointmentTime(schedule.endTime),
                    duration
                );

            generatedSlots.forEach(slot => {
                const isOccupied =
                    occupiedAppointments.some(
                        appointment =>
                            slot.start <
                                formatAppointmentTime(
                                    appointment.endTime
                                ) &&
                            slot.end >
                                formatAppointmentTime(
                                    appointment.startTime
                                )
                    );

                const isPastSlot =
                    isAppointmentSlotInPast(
                        dateValue,
                        slot.start
                    );

                if (!isOccupied && !isPastSlot) {
                    slots.push(slot);
                }
            });
        });

        const uniqueSlots =
            removeDuplicateAppointmentSlots(slots)
                .sort(
                    (first, second) =>
                        first.start.localeCompare(
                            second.start
                        )
                );

        if (uniqueSlots.length === 0) {
            slotsContainer.innerHTML = `
                <span class="slots-error">
                    No hay horarios libres para esta fecha.
                    Selecciona otro día o elige otro psicólogo.
                </span>
            `;

            showAppointmentAvailabilityInfo(
                "El psicólogo no tiene horas libres en la fecha seleccionada.",
                "error"
            );

            return;
        }

        slotsContainer.innerHTML = `
            <div class="slots-title">
                Horarios libres para
                ${appointmentDayLabels[selectedDay] || selectedDay}
            </div>

            <div class="slots-grid">
                ${
                    uniqueSlots
                        .map(slot => `
                            <button
                                type="button"
                                class="slot-btn"
                                data-slot-start="${slot.start}"
                                data-slot-end="${slot.end}"
                                onclick="selectAppointmentSlot('${slot.start}', '${slot.end}', this)"
                            >
                                ${slot.start} - ${slot.end}
                            </button>
                        `)
                        .join("")
                }
            </div>

            <div class="slots-help">
                Selecciona una hora para completar automáticamente
                el inicio y fin de la cita.
            </div>
        `;

        showAppointmentAvailabilityInfo(
            `Se encontraron ${uniqueSlots.length} horario(s) libre(s) para la fecha seleccionada.`,
            "success"
        );

    } catch (error) {
        console.error(
            "Error generando horarios:",
            error
        );

        slotsContainer.innerHTML = `
            <span class="slots-error">
                Error al cargar los horarios disponibles.
            </span>
        `;

        showAppointmentAvailabilityInfo(
            "No se pudieron cargar los horarios libres.",
            "error"
        );
    }
}

function buildTimeSlots(
    startTime,
    endTime,
    durationMinutes
) {
    const slots = [];

    let current =
        timeToMinutes(startTime);

    const end =
        timeToMinutes(endTime);

    if (
        Number.isNaN(current) ||
        Number.isNaN(end) ||
        !durationMinutes ||
        durationMinutes <= 0
    ) {
        return slots;
    }

    while (
        current + durationMinutes <= end
    ) {
        const slotStart =
            minutesToTime(current);

        const slotEnd =
            minutesToTime(
                current + durationMinutes
            );

        slots.push({
            start: slotStart,
            end: slotEnd
        });

        current += durationMinutes;
    }

    return slots;
}

function selectAppointmentSlot(
    start,
    end,
    selectedButton = null
) {
    const startInput =
        document.getElementById("appointmentStartTime");

    const endInput =
        document.getElementById("appointmentEndTime");

    if (!startInput || !endInput) {
        return;
    }

    startInput.disabled = false;
    endInput.disabled = false;

    startInput.value = start;
    endInput.value = end;

    startInput.disabled = true;
    endInput.disabled = true;

    document
        .querySelectorAll(".slot-btn")
        .forEach(button => {
            button.classList.remove("selected");
        });

    if (selectedButton) {
        selectedButton.classList.add("selected");
    } else {
        const button =
            [
                ...document.querySelectorAll(".slot-btn")
            ]
            .find(item =>
                item.dataset.slotStart === start &&
                item.dataset.slotEnd === end
            );

        if (button) {
            button.classList.add("selected");
        }
    }

    showAppointmentAvailabilityInfo(
        `Horario seleccionado: ${start} - ${end}.`,
        "success"
    );
}

function validateSelectedAppointmentTime() {
    const psychologistId =
        document.getElementById("appointmentPsychologistId")?.value;

    const dateValue =
        document.getElementById("appointmentDate")?.value;

    const startTime =
        document.getElementById("appointmentStartTime")?.value;

    const endTime =
        document.getElementById("appointmentEndTime")?.value;

    if (
        !psychologistId ||
        !dateValue ||
        !startTime ||
        !endTime
    ) {
        return false;
    }

    if (startTime >= endTime) {
        showAppointmentAvailabilityInfo(
            "La hora de inicio debe ser menor que la hora de fin.",
            "error"
        );

        return false;
    }

    const selectedDate =
        new Date(`${dateValue}T00:00:00`);

    if (
        Number.isNaN(
            selectedDate.getTime()
        )
    ) {
        return false;
    }

    const selectedDay =
        appointmentDayMap[
            selectedDate.getDay()
        ];

    const belongsToAvailability =
        selectedPsychologistSchedules.some(
            schedule =>
                schedule.dayOfWeek === selectedDay &&
                startTime >=
                    formatAppointmentTime(
                        schedule.startTime
                    ) &&
                endTime <=
                    formatAppointmentTime(
                        schedule.endTime
                    )
        );

    if (!belongsToAvailability) {
        showAppointmentAvailabilityInfo(
            "El horario seleccionado no pertenece a la disponibilidad del psicólogo.",
            "error"
        );

        return false;
    }

    if (
        isAppointmentSlotInPast(
            dateValue,
            startTime
        )
    ) {
        showAppointmentAvailabilityInfo(
            "No puedes seleccionar una hora pasada.",
            "error"
        );

        return false;
    }

    return true;
}

function resetPsychologistSelection() {
    const psychologistSelect =
        document.getElementById("appointmentPsychologistId");

    const dateInput =
        document.getElementById("appointmentDate");

    availablePsychologistsByService = [];
    selectedPsychologistSchedules = [];

    if (psychologistSelect) {
        psychologistSelect.innerHTML = `
            <option value="">
                Seleccione primero un servicio
            </option>
        `;

        psychologistSelect.disabled = true;
    }

    if (dateInput) {
        dateInput.value = "";
        dateInput.disabled = true;
    }

    clearSelectedAppointmentSlot();
    clearAvailableAppointmentSlots();
    removeAppointmentAvailabilityInfo();
}

function clearSelectedAppointmentSlot() {
    const startInput =
        document.getElementById("appointmentStartTime");

    const endInput =
        document.getElementById("appointmentEndTime");

    if (startInput) {
        startInput.disabled = false;
        startInput.value = "";
        startInput.disabled = true;
        startInput.readOnly = true;
    }

    if (endInput) {
        endInput.disabled = false;
        endInput.value = "";
        endInput.disabled = true;
        endInput.readOnly = true;
    }

    document
        .querySelectorAll(".slot-btn")
        .forEach(button => {
            button.classList.remove("selected");
        });
}

function clearAvailableAppointmentSlots() {
    const slotsContainer =
        document.getElementById("appointmentSlotsContainer");

    if (!slotsContainer) {
        return;
    }

    slotsContainer.innerHTML = `
        <span class="slots-placeholder">
            Selecciona psicólogo y fecha para ver las horas libres.
        </span>
    `;
}

function getPsychologistSchedulePanel() {
    let panel =
        document.getElementById(
            "appointmentPsychologistSchedulePanel"
        );

    if (panel) {
        return panel;
    }

    panel =
        document.createElement("div");

    panel.id =
        "appointmentPsychologistSchedulePanel";

    panel.className =
        "appointment-psychologist-schedule-panel";

    const psychologistSelect =
        document.getElementById(
            "appointmentPsychologistId"
        );

    if (
        psychologistSelect &&
        psychologistSelect.parentElement
    ) {
        psychologistSelect
            .parentElement
            .insertAdjacentElement(
                "afterend",
                panel
            );

        return panel;
    }

    const slotsContainer =
        document.getElementById(
            "appointmentSlotsContainer"
        );

    if (
        slotsContainer &&
        slotsContainer.parentElement
    ) {
        slotsContainer
            .parentElement
            .insertBefore(
                panel,
                slotsContainer
            );
    }

    return panel;
}

function renderPsychologistSchedulePanel(html) {
    const panel =
        getPsychologistSchedulePanel();

    if (panel) {
        panel.innerHTML = html;
    }
}

function showAppointmentAvailabilityInfo(
    message,
    type = "neutral"
) {
    let box =
        document.getElementById(
            "appointmentAvailabilityInfo"
        );

    if (!box) {
        box =
            document.createElement("div");

        box.id =
            "appointmentAvailabilityInfo";

        box.className =
            "availability-info-box";

        const formActions =
            document.querySelector(
                "#appointments .form-actions"
            );

        if (
            formActions &&
            formActions.parentNode
        ) {
            formActions.parentNode.insertBefore(
                box,
                formActions
            );
        } else {
            const slotsContainer =
                document.getElementById(
                    "appointmentSlotsContainer"
                );

            if (
                slotsContainer &&
                slotsContainer.parentElement
            ) {
                slotsContainer
                    .parentElement
                    .appendChild(box);
            }
        }
    }

    box.textContent = message;

    box.classList.remove(
        "availability-info-success",
        "availability-info-error",
        "availability-info-neutral"
    );

    if (type === "success") {
        box.classList.add(
            "availability-info-success"
        );
    } else if (type === "error") {
        box.classList.add(
            "availability-info-error"
        );
    } else {
        box.classList.add(
            "availability-info-neutral"
        );
    }
}

function removeAppointmentAvailabilityInfo() {
    const box =
        document.getElementById(
            "appointmentAvailabilityInfo"
        );

    if (box) {
        box.remove();
    }
}

function formatAppointmentTime(time) {
    if (!time) {
        return "";
    }

    return String(time).substring(0, 5);
}

function getSelectedServiceDuration() {
    const select =
        document.getElementById(
            "appointmentServiceId"
        );

    if (!select) {
        return null;
    }

    const selectedOption =
        select.options[
            select.selectedIndex
        ];

    if (!selectedOption) {
        return null;
    }

    const dataDuration =
        selectedOption.dataset.durationMinutes ||
        selectedOption.dataset.duration;

    if (
        dataDuration &&
        Number(dataDuration) > 0
    ) {
        return Number(dataDuration);
    }

    const text =
        selectedOption.textContent || "";

    const match =
        text.match(/(\d+)\s*min/i);

    return match
        ? Number(match[1])
        : null;
}

function timeToMinutes(time) {
    if (
        !time ||
        !String(time).includes(":")
    ) {
        return Number.NaN;
    }

    const [hours, minutes] =
        String(time)
            .split(":")
            .map(Number);

    if (
        Number.isNaN(hours) ||
        Number.isNaN(minutes)
    ) {
        return Number.NaN;
    }

    return hours * 60 + minutes;
}

function minutesToTime(totalMinutes) {
    const hours =
        Math.floor(totalMinutes / 60);

    const minutes =
        totalMinutes % 60;

    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

function isAppointmentSlotInPast(
    dateValue,
    startTime
) {
    if (!dateValue || !startTime) {
        return false;
    }

    const slotDateTime =
        new Date(
            `${dateValue}T${startTime}:00`
        );

    if (
        Number.isNaN(
            slotDateTime.getTime()
        )
    ) {
        return false;
    }

    return slotDateTime.getTime() <= Date.now();
}

function removeDuplicateAppointmentSlots(slots) {
    const map =
        new Map();

    slots.forEach(slot => {
        const key =
            `${slot.start}-${slot.end}`;

        if (!map.has(key)) {
            map.set(key, slot);
        }
    });

    return [...map.values()];
}

function getPsychologistInitials(name) {
    if (!name) {
        return "PS";
    }

    const parts =
        name.trim().split(/\s+/);

    return parts
        .slice(0, 2)
        .map(part =>
            part.charAt(0).toUpperCase()
        )
        .join("");
}

function escapeAppointmentHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function normalizeAppointmentTimeForBackend(time) {
    if (!time) {
        return null;
    }

    const normalizedTime = String(time).trim();

    if (/^\d{2}:\d{2}:\d{2}$/.test(normalizedTime)) {
        return normalizedTime;
    }

    if (/^\d{2}:\d{2}$/.test(normalizedTime)) {
        return `${normalizedTime}:00`;
    }

    return normalizedTime;
}
function getAppointmentPaymentInfo(appointment) {
    const totalAmount =
        Number(
            appointment?.totalAmount ??
            appointment?.service?.price ??
            0
        );

    const paidAmount =
        Number(
            appointment?.paidAmount || 0
        );

    let pendingAmount;

    if (
        appointment?.pendingAmount !== null &&
        appointment?.pendingAmount !== undefined
    ) {
        pendingAmount =
            Number(
                appointment.pendingAmount || 0
            );
    } else {
        pendingAmount =
            totalAmount - paidAmount;
    }

    pendingAmount =
        Math.max(
            pendingAmount,
            0
        );

    let status =
        appointment?.paymentStatus ||
        "PENDIENTE";

    if (
        totalAmount > 0 &&
        pendingAmount <= 0 &&
        paidAmount > 0
    ) {
        status = "PAGADO";
    } else if (
        paidAmount > 0 &&
        pendingAmount > 0
    ) {
        status = "PARCIAL";
    } else if (
        paidAmount <= 0
    ) {
        status = "PENDIENTE";
    }

    return {
        totalAmount,
        paidAmount,
        pendingAmount,
        status
    };
}

function canRegisterAppointmentPayment(
    appointment,
    paymentInfo
) {
    if (!appointment) {
        return false;
    }

    if (
        appointment.status === "CANCELADA" ||
        appointment.status === "NO_ASISTIO"
    ) {
        return false;
    }

    if (!paymentInfo) {
        paymentInfo =
            getAppointmentPaymentInfo(
                appointment
            );
    }

    return (
        paymentInfo.totalAmount > 0 &&
        paymentInfo.pendingAmount > 0 &&
        paymentInfo.status !== "PAGADO"
    );
}

function closeAppointmentListIfOpen() {
    const modal =
        document.getElementById(
            "appointmentListModal"
        );

    if (!modal) {
        return;
    }

    if (
        !modal.classList.contains("hidden")
    ) {
        modal.classList.add("hidden");
    }
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
    if (status === "PAGADO") {
        return "active";
    }

    if (status === "PARCIAL") {
        return "partial";
    }

    return "inactive";
}

/*
 * =========================================================
 * INICIALIZAR MÓDULO DE DISPONIBILIDAD DE CITAS
 * =========================================================
 */

function startAppointmentAvailabilityModule() {
    const serviceSelect =
        document.getElementById("appointmentServiceId");

    if (!serviceSelect) {
        console.warn(
            "No se encontró el campo appointmentServiceId."
        );
        return;
    }

    /*
     * Esta función conecta los eventos:
     * servicio → psicólogo → fecha → horario.
     */
    initAppointmentAvailabilityEvents();

    console.log(
        "Módulo de disponibilidad de citas inicializado correctamente."
    );
}

/*
 * Ejecuta la inicialización cuando el HTML ya esté disponible.
 */
if (document.readyState === "loading") {
    document.addEventListener(
        "DOMContentLoaded",
        startAppointmentAvailabilityModule,
        {
            once: true
        }
    );
} else {
    startAppointmentAvailabilityModule();
}

/*
 * Segunda comprobación cuando toda la página termine de cargar.
 * No duplicará eventos porque tu función usa
 * dataset.availabilityInitialized.
 */
window.addEventListener(
    "load",
    startAppointmentAvailabilityModule,
    {
        once: true
    }
);