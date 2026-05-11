let availabilitiesData = [];
let currentAvailabilityPage = 1;
const availabilitiesPerPage = 10;

let editingAvailabilityId = null;

const dayLabels = {
    MONDAY: "Lunes",
    TUESDAY: "Martes",
    WEDNESDAY: "Miércoles",
    THURSDAY: "Jueves",
    FRIDAY: "Viernes",
    SATURDAY: "Sábado",
    SUNDAY: "Domingo"
};

function getAvailabilityFormData() {
    const startTime = document.getElementById("availabilityStartTime").value;
    const endTime = document.getElementById("availabilityEndTime").value;

    const selectedDays = [...document.querySelectorAll("input[name='availabilityDays']:checked")]
        .map(input => input.value);

    return {
        psychologistId: parseInt(document.getElementById("availabilityPsychologistId").value),
        days: selectedDays,
        startTime: startTime ? `${startTime}:00` : null,
        endTime: endTime ? `${endTime}:00` : null
    };
}

function validateAvailabilityForm(data) {
    if (!data.psychologistId || isNaN(data.psychologistId)) {
        return "Seleccione un psicólogo";
    }

    if (!data.days || data.days.length === 0) {
        return "Seleccione al menos un día de atención";
    }

    if (!data.startTime) {
        return "Ingrese la hora de inicio";
    }

    if (!data.endTime) {
        return "Ingrese la hora de fin";
    }

    if (data.startTime >= data.endTime) {
        return "La hora de inicio debe ser menor que la hora de fin";
    }

    return null;
}

async function saveAvailability() {
    if (editingAvailabilityId) {
        await updateAvailability();
    } else {
        await createAvailability();
    }
}

async function createAvailability() {
    const data = getAvailabilityFormData();
    const validationError = validateAvailabilityForm(data);

    if (validationError) {
        showAvailabilityMessage(validationError, "error");
        return;
    }

    try {
        let createdCount = 0;
        let errors = [];

        for (const day of data.days) {
            const payload = {
                psychologist: {
                    id: data.psychologistId
                },
                dayOfWeek: day,
                startTime: data.startTime,
                endTime: data.endTime,
                active: true
            };

            const response = await authFetch(`${baseUrl}/psychologist-availabilities`, {
                method: "POST",
                body: JSON.stringify(payload)
            });

            if (!response) continue;

            const result = await response.json();

            if (response.ok) {
                createdCount++;
            } else {
                errors.push(`${dayLabels[day] ?? day}: ${result.message || "No se pudo registrar"}`);
            }
        }

        if (createdCount > 0) {
            showAvailabilityMessage(
                `Se registraron ${createdCount} día(s) de disponibilidad correctamente`,
                "success"
            );
        }

        if (errors.length > 0) {
            Swal.fire({
                icon: "warning",
                title: "Algunos días no se registraron",
                html: errors.map(e => `<p>${e}</p>`).join("")
            });
        }

        clearAvailabilityForm();
        await loadAvailabilities();

    } catch (error) {
        showAvailabilityMessage("Error de conexión con el servidor", "error");
    }
}

async function updateAvailability() {
    const data = getAvailabilityFormData();
    const validationError = validateAvailabilityForm(data);

    if (validationError) {
        showAvailabilityMessage(validationError, "error");
        return;
    }

    const currentAvailability = availabilitiesData.find(a => a.id === editingAvailabilityId);
    data.active = currentAvailability ? currentAvailability.active : true;

    try {
        const response = await authFetch(`${baseUrl}/psychologist-availabilities/${editingAvailabilityId}`, {
            method: "PUT",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showAvailabilityMessage(result.message || "Error al actualizar disponibilidad", "error");
            return;
        }

        showAvailabilityMessage("Disponibilidad actualizada correctamente", "success");

        cancelAvailabilityEdit();
        await loadAvailabilities();

    } catch (error) {
        showAvailabilityMessage("Error de conexión con el servidor", "error");
    }
}

async function loadAvailabilities() {
    try {
        const response = await authFetch(`${baseUrl}/psychologist-availabilities`);
        if (!response) return;

        availabilitiesData = await response.json();
        currentAvailabilityPage = 1;

        renderAvailabilityTable(getFilteredAvailabilities());

    } catch (error) {
        showAvailabilityMessage("Error al listar disponibilidad", "error");
    }
}

function renderAvailabilityTable(data) {
    const tbody = document.getElementById("availabilityTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / availabilitiesPerPage) || 1;

    if (currentAvailabilityPage > totalPages) {
        currentAvailabilityPage = totalPages;
    }

    const start = (currentAvailabilityPage - 1) * availabilitiesPerPage;
    const end = start + availabilitiesPerPage;
    const pageData = data.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">
                    No se encontraron horarios registrados
                </td>
            </tr>
        `;
        return;
    }

    pageData.forEach(item => {
        const psychologistName = item.psychologist
            ? `${item.psychologist.firstName ?? ""} ${item.psychologist.lastName ?? ""}`
            : "";

        tbody.innerHTML += `
            <tr>
                <td>${item.id ?? ""}</td>
                <td>${psychologistName}</td>
                <td>${dayLabels[item.dayOfWeek] ?? item.dayOfWeek ?? ""}</td>
                <td>${formatShortTime(item.startTime)}</td>
                <td>${formatShortTime(item.endTime)}</td>
                <td>
                    <span class="status-pill ${item.active ? "active" : "inactive"}">
                        ${item.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td>
                    <button class="btn-secondary" onclick="startEditAvailability(${item.id})">
                        Editar
                    </button>

                    <button class="${item.active ? "btn-danger-soft" : "btn-secondary"}"
                            onclick="toggleAvailabilityStatus(${item.id}, ${item.active})">
                        ${item.active ? "Desactivar" : "Reactivar"}
                    </button>
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("availabilityPageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentAvailabilityPage} de ${totalPages}`;
    }
}

function getFilteredAvailabilities() {
    const input = document.querySelector("#availabilityListModal .table-search");
    const search = input ? input.value.toLowerCase() : "";

    if (!search) return availabilitiesData;

    return availabilitiesData.filter(item => {
        const psychologistName = item.psychologist
            ? `${item.psychologist.firstName ?? ""} ${item.psychologist.lastName ?? ""}`
            : "";

        const text = `
            ${item.id ?? ""}
            ${psychologistName}
            ${dayLabels[item.dayOfWeek] ?? item.dayOfWeek ?? ""}
            ${item.startTime ?? ""}
            ${item.endTime ?? ""}
            ${item.active ? "Activo" : "Inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterAvailabilityTable() {
    currentAvailabilityPage = 1;
    renderAvailabilityTable(getFilteredAvailabilities());
}

function changeAvailabilityPage(direction) {
    const filteredData = getFilteredAvailabilities();
    const totalPages = Math.ceil(filteredData.length / availabilitiesPerPage) || 1;

    currentAvailabilityPage += direction;

    if (currentAvailabilityPage < 1) currentAvailabilityPage = 1;
    if (currentAvailabilityPage > totalPages) currentAvailabilityPage = totalPages;

    renderAvailabilityTable(filteredData);
}

async function toggleAvailabilityList() {
    document.getElementById("availabilityListModal").classList.remove("hidden");
    await loadAvailabilities();
}

function closeAvailabilityList() {
    document.getElementById("availabilityListModal").classList.add("hidden");
}

async function loadAvailabilityPsychologistOptions() {
    try {
        const response = await authFetch(`${baseUrl}/psychologists/active`);
        if (!response) return;

        const data = await response.json();

        const select = document.getElementById("availabilityPsychologistId");
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
        console.error("Error cargando psicólogos para disponibilidad:", error);
    }
}

function startEditAvailability(id) {
    const availability = availabilitiesData.find(a => a.id === id);

    if (!availability) {
        showAvailabilityMessage("No se encontró la disponibilidad seleccionada", "error");
        return;
    }

    editingAvailabilityId = id;

    document.getElementById("availabilityPsychologistId").value = availability.psychologist?.id ?? "";
    document.querySelectorAll("input[name='availabilityDays']").forEach(input => {
    input.checked = input.value === availability.dayOfWeek;
});
    document.getElementById("availabilityStartTime").value = formatInputTime(availability.startTime);
    document.getElementById("availabilityEndTime").value = formatInputTime(availability.endTime);

    document.getElementById("availabilitySaveButton").textContent = "Actualizar disponibilidad";
    document.getElementById("availabilityCancelEditButton").style.display = "inline-block";

    closeAvailabilityList();

    document.getElementById("availability").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    showAvailabilityMessage("Editando disponibilidad seleccionada", "info");
}

function cancelAvailabilityEdit() {
    editingAvailabilityId = null;
    clearAvailabilityForm();

    document.getElementById("availabilitySaveButton").textContent = "Guardar disponibilidad";
    document.getElementById("availabilityCancelEditButton").style.display = "none";

    showAvailabilityMessage("Edición cancelada", "info");
}

async function toggleAvailabilityStatus(id, isActive) {
    const actionText = isActive ? "desactivar" : "reactivar";
    const confirmText = isActive ? "Sí, desactivar" : "Sí, reactivar";

    const confirm = await Swal.fire({
        title: `¿Deseas ${actionText} esta disponibilidad?`,
        text: isActive
            ? "El horario no se eliminará, solo quedará inactivo."
            : "El horario volverá a estar disponible para programación.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: confirmText,
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/psychologist-availabilities/${id}/toggle-active`, {
            method: "PATCH"
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showAvailabilityMessage(result.message || "No se pudo cambiar el estado", "error");
            return;
        }

        await Swal.fire({
            icon: "success",
            title: "Estado actualizado",
            text: `Disponibilidad ${result.active ? "reactivada" : "desactivada"} correctamente`,
            timer: 1500,
            showConfirmButton: false
        });

        await loadAvailabilities();

    } catch (error) {
        showAvailabilityMessage("Error de conexión con el servidor", "error");
    }
}

function clearAvailabilityForm() {
    document.getElementById("availabilityPsychologistId").value = "";

    document.querySelectorAll("input[name='availabilityDays']").forEach(input => {
        input.checked = false;
    });

    document.getElementById("availabilityStartTime").value = "";
    document.getElementById("availabilityEndTime").value = "";
}

function showAvailabilityMessage(message, type = "info") {
    const box = document.getElementById("availabilityResult");
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

function formatShortTime(time) {
    if (!time) return "";
    return time.substring(0, 5);
}

function formatInputTime(time) {
    if (!time) return "";
    return time.substring(0, 5);
}