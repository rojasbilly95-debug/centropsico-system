let psychologistsData = [];
let currentPsychologistPage = 1;
const psychologistsPerPage = 10;

let editingPsychologistId = null;

function getPsychologistFormData() {
    return {
        firstName: document.getElementById("psychologistFirstName").value.trim(),
        lastName: document.getElementById("psychologistLastName").value.trim(),
        specialty: document.getElementById("psychologistSpecialty").value.trim(),
        phone: document.getElementById("psychologistPhone").value.trim(),
        email: document.getElementById("psychologistEmail").value.trim(),
        active: true
    };
}

function validatePsychologistForm(data) {
    if (!data.firstName) return "Ingrese los nombres del psicólogo";
    if (!data.lastName) return "Ingrese los apellidos del psicólogo";
    if (!data.specialty) return "Ingrese la especialidad del psicólogo";
    return null;
}

async function savePsychologist() {
    if (editingPsychologistId) {
        await updatePsychologist();
    } else {
        await createPsychologist();
    }
}

async function createPsychologist() {
    const data = getPsychologistFormData();
    const validationError = validatePsychologistForm(data);

    if (validationError) {
        showPsychologistMessage(validationError, "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/psychologists`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showPsychologistMessage(result.message || "Error al guardar psicólogo", "error");
            return;
        }

        clearPsychologistForm();
        await loadPsychologists();
        await loadPsychologistOptions();

        const confirm = await Swal.fire({
            icon: "success",
            title: "Psicólogo registrado",
            text: "Ahora configura sus días y horarios de atención para que pueda recibir citas.",
            showCancelButton: true,
            confirmButtonText: "Configurar disponibilidad",
            cancelButtonText: "Después"
        });

        if (confirm.isConfirmed) {
            goToAvailabilityForPsychologist(result.id);
        }

    } catch (error) {
        showPsychologistMessage("Error de conexión con el servidor", "error");
    }
}

async function loadPsychologists() {
    try {
        const response = await authFetch(`${baseUrl}/psychologists`);
        if (!response) return;

        psychologistsData = await response.json();
        currentPsychologistPage = 1;

        renderPsychologistTable(getFilteredPsychologists());

        showPsychologistMessage("Psicólogos cargados correctamente", "success");

    } catch (error) {
        showPsychologistMessage("Error al listar psicólogos", "error");
    }
}

function renderPsychologistTable(data) {
    const tbody = document.getElementById("psychologistTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / psychologistsPerPage) || 1;

    if (currentPsychologistPage > totalPages) {
        currentPsychologistPage = totalPages;
    }

    const start = (currentPsychologistPage - 1) * psychologistsPerPage;
    const end = start + psychologistsPerPage;
    const pageData = data.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align:center;">
                    No se encontraron psicólogos
                </td>
            </tr>
        `;
        return;
    }

    pageData.forEach(psychologist => {

        const availabilityCount = psychologist.availabilityCount ?? 0;

        const availabilityBadge =
            availabilityCount > 0
                ? `
                <span class="status-pill active">
                    Disponible (${availabilityCount})
                </span>
            `
                : `
                <span class="status-pill inactive">
                    Sin horarios
                </span>
            `;

        tbody.innerHTML += `
        <tr>
            <td>${psychologist.id ?? ""}</td>

            <td>${psychologist.firstName ?? ""}</td>

            <td>${psychologist.lastName ?? ""}</td>

            <td>${psychologist.specialty ?? ""}</td>

            <td>${psychologist.phone ?? ""}</td>

            <td>${psychologist.email ?? ""}</td>

            <td>
                ${availabilityBadge}
            </td>

            <td>
                <span class="status-pill ${psychologist.active ? "active" : "inactive"}">
                    ${psychologist.active ? "Activo" : "Inactivo"}
                </span>
            </td>

            <td>

                <button class="btn-secondary"
                        onclick="startEditPsychologist(${psychologist.id})">
                    Editar
                </button>

                <button class="btn-primary"
                        onclick="goToAvailabilityForPsychologist(${psychologist.id})">
                    Horarios
                </button>

                <button class="${psychologist.active ? "btn-danger-soft" : "btn-secondary"}"
                        onclick="togglePsychologistStatus(${psychologist.id}, ${psychologist.active})">
                    ${psychologist.active ? "Desactivar" : "Reactivar"}
                </button>

                    ${
                        psychologist.active
                            ? `
                                <button 
                                    type="button"
                                    class="table-action-btn info"
                                    onclick="openPsychologistNotificationModal(${psychologist.id})">
                                    Notificar
                                </button>
                            `
                            : ""
                    }

            </td>
        </tr>
    `;
    });

    const pageInfo = document.getElementById("psychologistPageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentPsychologistPage} de ${totalPages}`;
    }
}

function getFilteredPsychologists() {
    const input = document.querySelector("#psychologistListModal .table-search");
    const search = input ? input.value.toLowerCase() : "";

    if (!search) return psychologistsData;

    return psychologistsData.filter(psychologist => {
        const text = `
            ${psychologist.id ?? ""}
            ${psychologist.firstName ?? ""}
            ${psychologist.lastName ?? ""}
            ${psychologist.specialty ?? ""}
            ${psychologist.phone ?? ""}
            ${psychologist.email ?? ""}
            ${psychologist.active ? "Activo" : "Inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterPsychologistTable() {
    currentPsychologistPage = 1;
    renderPsychologistTable(getFilteredPsychologists());
}

function changePsychologistPage(direction) {
    const filteredData = getFilteredPsychologists();
    const totalPages = Math.ceil(filteredData.length / psychologistsPerPage) || 1;

    currentPsychologistPage += direction;

    if (currentPsychologistPage < 1) currentPsychologistPage = 1;
    if (currentPsychologistPage > totalPages) currentPsychologistPage = totalPages;

    renderPsychologistTable(filteredData);
}

async function togglePsychologistList() {
    document.getElementById("psychologistListModal").classList.remove("hidden");
    await loadPsychologists();
}

function closePsychologistList() {
    document.getElementById("psychologistListModal").classList.add("hidden");
}

async function loadPsychologistOptions() {
    try {
        const response = await authFetch(`${baseUrl}/psychologists/active`);
        if (!response) return;

        const data = await response.json();

        const select = document.getElementById("appointmentPsychologistId");
        if (!select) return;

        select.innerHTML = `<option value="">Seleccione psicólogo</option>`;

        data.forEach(psychologist => {
            select.innerHTML += `
                <option value="${psychologist.id}">
                    ${psychologist.firstName} ${psychologist.lastName} - ${psychologist.specialty ?? ""}
                </option>
            `;
        });

    } catch (error) {
        console.error("Error cargando psicólogos:", error);
    }
}

function startEditPsychologist(id) {
    const psychologist = psychologistsData.find(p => p.id === id);

    if (!psychologist) {
        showPsychologistMessage("No se encontró el psicólogo seleccionado", "error");
        return;
    }

    editingPsychologistId = id;

    document.getElementById("psychologistFirstName").value = psychologist.firstName ?? "";
    document.getElementById("psychologistLastName").value = psychologist.lastName ?? "";
    document.getElementById("psychologistSpecialty").value = psychologist.specialty ?? "";
    document.getElementById("psychologistPhone").value = psychologist.phone ?? "";
    document.getElementById("psychologistEmail").value = psychologist.email ?? "";

    document.getElementById("psychologistSaveButton").textContent = "Actualizar psicólogo";
    document.getElementById("psychologistCancelEditButton").style.display = "inline-block";

    closePsychologistList();

    document.getElementById("psychologists").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    showPsychologistMessage(
        `Editando psicólogo: ${psychologist.firstName} ${psychologist.lastName}`,
        "info"
    );
}

function cancelPsychologistEdit() {
    editingPsychologistId = null;
    clearPsychologistForm();

    document.getElementById("psychologistSaveButton").textContent = "Guardar psicólogo";
    document.getElementById("psychologistCancelEditButton").style.display = "none";

    showPsychologistMessage("Edición cancelada", "info");
}

async function togglePsychologistStatus(id, isActive) {
    const actionText = isActive ? "desactivar" : "reactivar";
    const confirmText = isActive ? "Sí, desactivar" : "Sí, reactivar";

    const confirm = await Swal.fire({
        title: `¿Deseas ${actionText} este psicólogo?`,
        text: isActive
            ? "El psicólogo no se eliminará, solo quedará inactivo."
            : "El psicólogo volverá a estar disponible para nuevas citas.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: confirmText,
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/psychologists/${id}/toggle-active`, {
            method: "PATCH"
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showPsychologistMessage(result.message || "No se pudo cambiar el estado del psicólogo", "error");
            return;
        }

        await Swal.fire({
            icon: "success",
            title: "Estado actualizado",
            text: `Psicólogo ${result.active ? "reactivado" : "desactivado"} correctamente`,
            timer: 1600,
            showConfirmButton: false
        });

        await loadPsychologists();
        await loadPsychologistOptions();

    } catch (error) {
        showPsychologistMessage("Error de conexión con el servidor", "error");
    }
}

function clearPsychologistForm() {
    document.getElementById("psychologistFirstName").value = "";
    document.getElementById("psychologistLastName").value = "";
    document.getElementById("psychologistSpecialty").value = "";
    document.getElementById("psychologistPhone").value = "";
    document.getElementById("psychologistEmail").value = "";
}

function showPsychologistMessage(message, type = "info") {
    const box = document.getElementById("psychologistResult");
    if (!box) return;

    box.textContent = message;

    box.classList.remove("message-success", "message-error", "message-info");

    if (type === "success") {
        box.classList.add("message-success");
    } else if (type === "error") {
        box.classList.add("message-error");
    } else {
        box.classList.add("message-info");
    }
}

async function getPsychologistAvailabilityCount(psychologistId) {
    try {
        const response = await authFetch(`${baseUrl}/psychologist-availabilities/psychologist/${psychologistId}/count`);

        if (!response || !response.ok) return 0;

        return await response.json();

    } catch (error) {
        console.error("Error obteniendo disponibilidad:", error);
        return 0;
    }
}

function goToAvailabilityForPsychologist(psychologistId) {
    showSectionById("availability");

    setTimeout(() => {
        const select = document.getElementById("availabilityPsychologistId");

        if (select) {
            select.value = psychologistId;
        }

        const box = document.getElementById("availabilityResult");
        if (box) {
            box.textContent = "Configura la disponibilidad semanal del psicólogo recién registrado.";
            box.classList.remove("message-success", "message-error");
            box.classList.add("message-info");
        }
    }, 300);
}

async function openPsychologistNotificationModal(psychologistId) {
    if (!psychologistId) return;

    const psychologist = typeof psychologistsData !== "undefined"
        ? psychologistsData.find(item => Number(item.id) === Number(psychologistId))
        : null;

    const psychologistName = getPsychologistNotificationDisplayName(psychologist);
    const psychologistEmail = psychologist?.email || "";
    const psychologistSpecialty = psychologist?.specialty || "Especialidad no registrada";

    const result = await Swal.fire({
        title: "Enviar notificación",
        html: `
            <div class="admin-notify-modal">

                <div class="admin-notify-user-card">
                    <strong>${escapePsychologistNotificationHtml(psychologistName)}</strong>
                    <span>Psicólogo</span>
                    <small>${escapePsychologistNotificationHtml(psychologistSpecialty)}</small>
                    <small>${escapePsychologistNotificationHtml(psychologistEmail)}</small>
                </div>

                <label>Asunto</label>
                <input 
                    id="adminNotifyTitle" 
                    class="swal2-input admin-notify-input" 
                    maxlength="120"
                    placeholder="Ejemplo: Recordatorio de citas">

                <label>Mensaje</label>
                <textarea 
                    id="adminNotifyMessage" 
                    class="admin-notify-textarea"
                    maxlength="700"
                    placeholder="Escribe el mensaje que recibirá el psicólogo en su campana."></textarea>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: "Enviar notificación",
        cancelButtonText: "Cancelar",
        confirmButtonColor: "#0f3d66",
        width: 620,
        focusConfirm: false,
        preConfirm: () => {
            const title = document.getElementById("adminNotifyTitle").value.trim();
            const message = document.getElementById("adminNotifyMessage").value.trim();

            if (!title) {
                Swal.showValidationMessage("Ingresa el asunto de la notificación.");
                return false;
            }

            if (title.length < 4) {
                Swal.showValidationMessage("El asunto debe tener al menos 4 caracteres.");
                return false;
            }

            if (!message) {
                Swal.showValidationMessage("Ingresa el mensaje de la notificación.");
                return false;
            }

            if (message.length < 8) {
                Swal.showValidationMessage("El mensaje debe tener al menos 8 caracteres.");
                return false;
            }

            return {
                title,
                message
            };
        }
    });

    if (!result.isConfirmed || !result.value) return;

    try {
        const response = await authFetch(`${baseUrl}/admin-notifications/psychologists/${psychologistId}`, {
            method: "POST",
            body: JSON.stringify(result.value)
        });

        if (!response) return;

        let data = {};

        try {
            data = await response.json();
        } catch (error) {
            data = {};
        }

        if (!response.ok) {
            Swal.fire(
                "Error",
                data.message || "No se pudo enviar la notificación.",
                "error"
            );
            return;
        }

        Swal.fire({
            icon: "success",
            title: "Notificación enviada",
            text: data.message || "El psicólogo recibirá el mensaje en su campana.",
            timer: 1800,
            showConfirmButton: false
        });

        if (typeof loadNotifications === "function") {
            await loadNotifications(false);
        }

        if (typeof loadAuditLogs === "function") {
            await loadAuditLogs();
        }

    } catch (error) {
        console.error("Error enviando notificación al psicólogo:", error);

        Swal.fire(
            "Error",
            "Error de conexión con el servidor.",
            "error"
        );
    }
}

function getPsychologistNotificationDisplayName(psychologist) {
    if (!psychologist) return "Psicólogo seleccionado";

    const firstName = psychologist.firstName || psychologist.names || psychologist.name || "";
    const lastName = psychologist.lastName || psychologist.surnames || "";

    const fullName = `${firstName} ${lastName}`.trim();

    return fullName || psychologist.email || "Psicólogo seleccionado";
}

function escapePsychologistNotificationHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}