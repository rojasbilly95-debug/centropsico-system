let patientsData = [];
let currentPatientPage = 1;
const patientsPerPage = 10;

let editingPatientId = null;

function getPatientFormData() {
    return {
        firstName: document.getElementById("patientFirstName").value.trim(),
        lastName: document.getElementById("patientLastName").value.trim(),
        dni: document.getElementById("patientDni").value.trim(),
        birthDate: document.getElementById("patientBirthDate").value || null,
        gender: document.getElementById("patientGender").value,
        phone: document.getElementById("patientPhone").value.trim(),
        email: document.getElementById("patientEmail").value.trim(),
        address: document.getElementById("patientAddress").value.trim(),
        emergencyContact: document.getElementById("patientEmergencyContact").value.trim(),
        emergencyPhone: document.getElementById("patientEmergencyPhone").value.trim(),
        active: true
    };
}

function validatePatientForm(data) {
    if (!data.firstName) return "Ingrese los nombres del paciente";
    if (!data.lastName) return "Ingrese los apellidos del paciente";
    if (!data.dni) return "Ingrese el DNI del paciente";
    return null;
}

async function savePatient() {
    if (editingPatientId) {
        await updatePatient();
    } else {
        await createPatient();
    }
}

async function createPatient() {
    const data = getPatientFormData();
    const validationError = validatePatientForm(data);

    if (validationError) {
        showPatientMessage(validationError, "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/patients`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showPatientMessage(result.message || "Error al guardar paciente", "error");
            return;
        }

        showPatientMessage("Paciente guardado correctamente", "success");

        clearPatientForm();
        await loadPatients();

    } catch (error) {
        showPatientMessage("Error de conexión con el servidor", "error");
    }
}

async function updatePatient() {
    const data = getPatientFormData();
    const validationError = validatePatientForm(data);

    if (validationError) {
        showPatientMessage(validationError, "error");
        return;
    }

    const currentPatient = patientsData.find(p => p.id === editingPatientId);
    data.active = currentPatient ? currentPatient.active : true;

    try {
        const response = await authFetch(`${baseUrl}/patients/${editingPatientId}`, {
            method: "PUT",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showPatientMessage(result.message || "Error al actualizar paciente", "error");
            return;
        }

        showPatientMessage("Paciente actualizado correctamente", "success");

        cancelPatientEdit();
        await loadPatients();

    } catch (error) {
        showPatientMessage("Error de conexión con el servidor", "error");
    }
}

async function loadPatients() {
    try {
        const response = await authFetch(`${baseUrl}/patients`);
        if (!response) return;

        patientsData = await response.json();
        currentPatientPage = 1;

        renderPatientTable(getFilteredPatients());

        showPatientMessage("Pacientes cargados correctamente", "success");

    } catch (error) {
        showPatientMessage("Error al listar pacientes", "error");
    }
}

function renderPatientTable(data) {
    const tbody = document.getElementById("patientTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / patientsPerPage) || 1;

    if (currentPatientPage > totalPages) {
        currentPatientPage = totalPages;
    }

    const start = (currentPatientPage - 1) * patientsPerPage;
    const end = start + patientsPerPage;
    const pageData = data.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align:center;">
                    No se encontraron pacientes
                </td>
            </tr>
        `;
    }

    pageData.forEach(patient => {
        const fullName = `${patient.firstName ?? ""} ${patient.lastName ?? ""}`.trim();

        tbody.innerHTML += `
            <tr>
                <td>${patient.id ?? ""}</td>
                <td>${patient.firstName ?? ""}</td>
                <td>${patient.lastName ?? ""}</td>
                <td>${patient.dni ?? ""}</td>
                <td>${patient.phone ?? ""}</td>
                <td>${patient.email ?? ""}</td>
                <td>
                    <span class="status-pill ${patient.active ? "active" : "inactive"}">
                        ${patient.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td>
                    <button class="btn-secondary" onclick="startEditPatient(${patient.id})">
                        Editar
                    </button>

                    <button class="${patient.active ? "btn-danger-soft" : "btn-secondary"}" 
                            onclick="togglePatientStatus(${patient.id}, ${patient.active})">
                        ${patient.active ? "Desactivar" : "Reactivar"}
                    </button>

                    <button class="btn-secondary" onclick="openClinicalHistory(${patient.id}, '${escapeText(fullName)}')">
                        Historia
                    </button>
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("patientPageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentPatientPage} de ${totalPages}`;
    }
}

function changePatientPage(direction) {
    const filteredData = getFilteredPatients();
    const totalPages = Math.ceil(filteredData.length / patientsPerPage) || 1;

    currentPatientPage += direction;

    if (currentPatientPage < 1) currentPatientPage = 1;
    if (currentPatientPage > totalPages) currentPatientPage = totalPages;

    renderPatientTable(filteredData);
}

function getFilteredPatients() {
    const searchInput = document.querySelector("#patientListModal .table-search");
    const search = searchInput ? searchInput.value.toLowerCase() : "";

    if (!search) return patientsData;

    return patientsData.filter(patient => {
        const text = `
            ${patient.id ?? ""}
            ${patient.firstName ?? ""}
            ${patient.lastName ?? ""}
            ${patient.dni ?? ""}
            ${patient.phone ?? ""}
            ${patient.email ?? ""}
            ${patient.active ? "Activo" : "Inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterPatientTable() {
    currentPatientPage = 1;
    renderPatientTable(getFilteredPatients());
}

async function togglePatientList() {
    const modal = document.getElementById("patientListModal");
    modal.classList.remove("hidden");
    await loadPatients();
}

function closePatientList() {
    document.getElementById("patientListModal").classList.add("hidden");
}

async function loadPatientOptions() {
    try {
        const response = await authFetch(`${baseUrl}/patients/active`);
        if (!response) return;

        const data = await response.json();

        const select = document.getElementById("appointmentPatientId");
        if (!select) return;

        select.innerHTML = `<option value="">Seleccione paciente</option>`;

        data.forEach(patient => {
            select.innerHTML += `
                <option value="${patient.id}">
                    ${patient.firstName} ${patient.lastName} - DNI: ${patient.dni}
                </option>
            `;
        });

    } catch (error) {
        console.error("Error cargando pacientes:", error);
    }
}

async function startEditPatient(id) {
    const patient = patientsData.find(p => p.id === id);

    if (!patient) {
        showPatientMessage("No se encontró el paciente seleccionado", "error");
        return;
    }

    editingPatientId = id;

    document.getElementById("patientFirstName").value = patient.firstName ?? "";
    document.getElementById("patientLastName").value = patient.lastName ?? "";
    document.getElementById("patientDni").value = patient.dni ?? "";
    document.getElementById("patientBirthDate").value = patient.birthDate ?? "";
    document.getElementById("patientGender").value = patient.gender ?? "";
    document.getElementById("patientPhone").value = patient.phone ?? "";
    document.getElementById("patientEmail").value = patient.email ?? "";
    document.getElementById("patientAddress").value = patient.address ?? "";
    document.getElementById("patientEmergencyContact").value = patient.emergencyContact ?? "";
    document.getElementById("patientEmergencyPhone").value = patient.emergencyPhone ?? "";

    document.getElementById("patientSaveButton").textContent = "Actualizar paciente";
    document.getElementById("patientCancelEditButton").style.display = "inline-block";

    closePatientList();

    document.getElementById("patients").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    showPatientMessage(`Editando paciente: ${patient.firstName} ${patient.lastName}`, "info");
}

function cancelPatientEdit() {
    editingPatientId = null;
    clearPatientForm();

    document.getElementById("patientSaveButton").textContent = "Guardar paciente";
    document.getElementById("patientCancelEditButton").style.display = "none";

    showPatientMessage("Edición cancelada", "info");
}

async function togglePatientStatus(id, isActive) {
    const actionText = isActive ? "desactivar" : "reactivar";
    const confirmText = isActive ? "Sí, desactivar" : "Sí, reactivar";

    const confirm = await Swal.fire({
        title: `¿Deseas ${actionText} este paciente?`,
        text: isActive
            ? "El paciente no se eliminará, solo quedará inactivo."
            : "El paciente volverá a estar disponible en el sistema.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: confirmText,
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/patients/${id}/toggle-active`, {
            method: "PATCH"
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showPatientMessage(result.message || "No se pudo cambiar el estado del paciente", "error");
            return;
        }

        await Swal.fire({
            icon: "success",
            title: "Estado actualizado",
            text: `Paciente ${result.active ? "reactivado" : "desactivado"} correctamente`,
            timer: 1600,
            showConfirmButton: false
        });

        await loadPatients();
        renderPatientTable(getFilteredPatients());

    } catch (error) {
        showPatientMessage("Error de conexión con el servidor", "error");
    }
}

function clearPatientForm() {
    document.getElementById("patientFirstName").value = "";
    document.getElementById("patientLastName").value = "";
    document.getElementById("patientDni").value = "";
    document.getElementById("patientBirthDate").value = "";
    document.getElementById("patientGender").value = "";
    document.getElementById("patientPhone").value = "";
    document.getElementById("patientEmail").value = "";
    document.getElementById("patientAddress").value = "";
    document.getElementById("patientEmergencyContact").value = "";
    document.getElementById("patientEmergencyPhone").value = "";
}

function showPatientMessage(message, type = "info") {
    const box = document.getElementById("patientResult");
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

function escapeText(text) {
    return String(text)
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'")
        .replaceAll('"', "&quot;")
        .replaceAll("\n", " ");
}

function prefillPatientFromLead(lead) {
    if (!lead) return;

    showSectionById("patients");

    const fullNameParts = (lead.fullName || "").trim().split(" ");

    const firstName = fullNameParts.slice(0, 2).join(" ");
    const lastName = fullNameParts.slice(2).join(" ");

    document.getElementById("patientFirstName").value = firstName || "";
    document.getElementById("patientLastName").value = lastName || "";
    document.getElementById("patientPhone").value = lead.phone || "";
    document.getElementById("patientEmail").value = lead.email || "";

    document.getElementById("patientDni").value = "";
    document.getElementById("patientBirthDate").value = "";
    document.getElementById("patientGender").value = "";
    document.getElementById("patientAddress").value = "";
    document.getElementById("patientEmergencyContact").value = "";
    document.getElementById("patientEmergencyPhone").value = "";

    showPatientMessage(
        "Datos cargados desde la pre-reserva. Completa DNI y apellidos si es necesario.",
        "info"
    );

    document.getElementById("patients").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}