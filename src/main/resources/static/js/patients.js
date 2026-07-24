/* =========================================================
   ESTADO DEL MÓDULO
========================================================= */

let patientsData = [];
let currentPatientPage = 1;

const patientsPerPage = 10;

let editingPatientId = null;
let patientRequestInProgress = false;


/* =========================================================
   OBTENER DATOS DEL FORMULARIO
========================================================= */

function getPatientFormData() {
    return {
        firstName: getPatientInputValue("patientFirstName"),
        lastName: getPatientInputValue("patientLastName"),
        dni: getPatientInputValue("patientDni"),

        birthDate:
            document.getElementById("patientBirthDate")?.value ||
            null,

        gender:
            document.getElementById("patientGender")?.value ||
            "",

        phone:
            getPatientInputValue("patientPhone"),

        email:
            getPatientInputValue("patientEmail"),

        address:
            getPatientInputValue("patientAddress"),

        // Campos opcionales
        emergencyContact:
            getPatientInputValue("patientEmergencyContact") ||
            null,

        emergencyPhone:
            getPatientInputValue("patientEmergencyPhone") ||
            null,

        active: true
    };
}

function getPatientInputValue(elementId) {
    const element = document.getElementById(elementId);

    if (!element) {
        return "";
    }

    return element.value.trim();
}


/* =========================================================
   VALIDAR FORMULARIO
========================================================= */

function validatePatientForm(data) {
    if (!data.firstName) {
        return "Ingrese los nombres del paciente.";
    }

    if (data.firstName.length < 2) {
        return "Los nombres deben tener al menos 2 caracteres.";
    }

    if (!data.lastName) {
        return "Ingrese los apellidos del paciente.";
    }

    if (data.lastName.length < 2) {
        return "Los apellidos deben tener al menos 2 caracteres.";
    }

    if (!data.dni) {
        return "Ingrese el DNI del paciente.";
    }

    if (!/^\d{8}$/.test(data.dni)) {
        return "El DNI debe contener exactamente 8 números.";
    }

    if (
        data.email &&
        !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)
    ) {
        return "Ingrese un correo electrónico válido.";
    }

    if (
        data.birthDate &&
        new Date(`${data.birthDate}T00:00:00`) > new Date()
    ) {
        return "La fecha de nacimiento no puede ser futura.";
    }

    /*
     * El contacto de emergencia es opcional.
     * Solo validamos el teléfono cuando el usuario escribe uno.
     */
    if (
        data.emergencyPhone &&
        !/^\d{7,15}$/.test(data.emergencyPhone)
    ) {
        return "El teléfono de emergencia debe contener entre 7 y 15 números.";
    }

    return null;
}


/* =========================================================
   GUARDAR PACIENTE
========================================================= */

async function savePatient() {
    if (patientRequestInProgress) {
        return;
    }

    if (editingPatientId) {
        await updatePatient();
        return;
    }

    await createPatient();
}


/* =========================================================
   CREAR PACIENTE
========================================================= */

async function createPatient() {
    const data = getPatientFormData();
    const validationError = validatePatientForm(data);

    if (validationError) {
        showPatientMessage(validationError, "error");
        return;
    }

    setPatientFormLoading(true);

    try {
        const response = await authFetch(
            `${baseUrl}/patients`,
            {
                method: "POST",
                body: JSON.stringify(data)
            }
        );

        if (!response) {
            return;
        }

        const result =
            await readPatientResponse(response);

        if (!response.ok) {
            showPatientMessage(
                result.message ||
                result.detail ||
                result.error ||
                "No se pudo registrar al paciente.",
                "error"
            );

            return;
        }

        clearPatientForm();

        showPatientMessage(
            "Paciente registrado correctamente.",
            "success"
        );

        await loadPatients(false);
        await loadPatientOptions();

    } catch (error) {
        console.error(
            "Error al registrar paciente:",
            error
        );

        showPatientMessage(
            "No se pudo conectar con el servidor.",
            "error"
        );

    } finally {
        setPatientFormLoading(false);
    }
}


/* =========================================================
   ACTUALIZAR PACIENTE
========================================================= */

async function updatePatient() {
    const data = getPatientFormData();
    const validationError = validatePatientForm(data);

    if (validationError) {
        showPatientMessage(validationError, "error");
        return;
    }

    const currentPatient =
        patientsData.find(
            patient =>
                Number(patient.id) ===
                Number(editingPatientId)
        );

    data.active =
        currentPatient
            ? Boolean(currentPatient.active)
            : true;

    setPatientFormLoading(true);

    try {
        const response = await authFetch(
            `${baseUrl}/patients/${editingPatientId}`,
            {
                method: "PUT",
                body: JSON.stringify(data)
            }
        );

        if (!response) {
            return;
        }

        const result =
            await readPatientResponse(response);

        if (!response.ok) {
            showPatientMessage(
                result.message ||
                result.detail ||
                result.error ||
                "No se pudo actualizar al paciente.",
                "error"
            );

            return;
        }

        finishPatientEdit();

        showPatientMessage(
            "Paciente actualizado correctamente.",
            "success"
        );

        await loadPatients(false);
        await loadPatientOptions();

    } catch (error) {
        console.error(
            "Error al actualizar paciente:",
            error
        );

        showPatientMessage(
            "No se pudo conectar con el servidor.",
            "error"
        );

    } finally {
        setPatientFormLoading(false);
    }
}


/* =========================================================
   CARGAR PACIENTES
========================================================= */

async function loadPatients(showSuccessMessage = false) {
    try {
        const response =
            await authFetch(`${baseUrl}/patients`);

        if (!response) {
            return;
        }

        const result =
            await readPatientResponse(response);

        if (!response.ok) {
            showPatientMessage(
                result.message ||
                result.detail ||
                result.error ||
                "No se pudieron cargar los pacientes.",
                "error"
            );

            return;
        }

        patientsData =
            Array.isArray(result)
                ? result
                : [];

        currentPatientPage = 1;

        renderPatientTable(
            getFilteredPatients()
        );

        /*
         * Por defecto no mostramos un aviso,
         * porque cargar la tabla es una acción automática.
         */
        if (showSuccessMessage) {
            showPatientMessage(
                "Lista de pacientes actualizada.",
                "success"
            );
        }

    } catch (error) {
        console.error(
            "Error al listar pacientes:",
            error
        );

        showPatientMessage(
            "No se pudo cargar la lista de pacientes.",
            "error"
        );
    }
}


/* =========================================================
   MOSTRAR TABLA
========================================================= */

function renderPatientTable(data) {
    const tbody =
        document.getElementById(
            "patientTableBody"
        );

    if (!tbody) {
        return;
    }

    const safeData =
        Array.isArray(data)
            ? data
            : [];

    tbody.innerHTML = "";

    const totalPages =
        Math.ceil(
            safeData.length /
            patientsPerPage
        ) || 1;

    if (currentPatientPage > totalPages) {
        currentPatientPage = totalPages;
    }

    if (currentPatientPage < 1) {
        currentPatientPage = 1;
    }

    const start =
        (currentPatientPage - 1) *
        patientsPerPage;

    const end =
        start + patientsPerPage;

    const pageData =
        safeData.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8">
                    <div class="patient-table-empty">
                        <div class="patient-table-empty-icon">
                            <i data-lucide="users-round"></i>
                        </div>

                        <strong>
                            No se encontraron pacientes
                        </strong>

                        <span>
                            Registra un paciente o modifica
                            los criterios de búsqueda.
                        </span>
                    </div>
                </td>
            </tr>
        `;
    }

    pageData.forEach(patient => {
        const patientId =
            Number(patient.id);

        tbody.insertAdjacentHTML(
            "beforeend",
            `
                <tr>
                    <td>
                        ${escapePatientHtml(
                            patient.id ?? ""
                        )}
                    </td>

                    <td>
                        <strong class="patient-table-name">
                            ${escapePatientHtml(
                                patient.firstName ?? ""
                            )}
                        </strong>
                    </td>

                    <td>
                        ${escapePatientHtml(
                            patient.lastName ?? ""
                        )}
                    </td>

                    <td>
                        <span class="patient-dni-value">
                            ${escapePatientHtml(
                                patient.dni ?? ""
                            )}
                        </span>
                    </td>

                    <td>
                        ${escapePatientHtml(
                            patient.phone || "No registrado"
                        )}
                    </td>

                    <td>
                        <span class="patient-email-value">
                            ${escapePatientHtml(
                                patient.email ||
                                "No registrado"
                            )}
                        </span>
                    </td>

                    <td>
                        <span class="status-pill ${
                            patient.active
                                ? "active"
                                : "inactive"
                        }">
                            ${
                                patient.active
                                    ? "Activo"
                                    : "Inactivo"
                            }
                        </span>
                    </td>

                    <td>
                        <div class="patient-table-actions">

                            <button
                                type="button"
                                class="btn-secondary"
                                onclick="startEditPatient(
                                    ${patientId}
                                )"
                            >
                                Editar
                            </button>

                            <button
                                type="button"
                                class="${
                                    patient.active
                                        ? "btn-danger-soft"
                                        : "btn-secondary"
                                }"
                                onclick="togglePatientStatus(
                                    ${patientId},
                                    ${Boolean(patient.active)}
                                )"
                            >
                                ${
                                    patient.active
                                        ? "Desactivar"
                                        : "Reactivar"
                                }
                            </button>

                            <button
                                type="button"
                                class="btn-secondary patient-history-button"
                                onclick="openPatientClinicalHistory(
                                    ${patientId}
                                )"
                            >
                                Historia
                            </button>

                        </div>
                    </td>
                </tr>
            `
        );
    });

    updatePatientPagination(
        totalPages
    );

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}


/* =========================================================
   PAGINACIÓN
========================================================= */

function updatePatientPagination(totalPages) {
    const pageInfo =
        document.getElementById(
            "patientPageInfo"
        );

    if (pageInfo) {
        pageInfo.textContent =
            `Página ${currentPatientPage} de ${totalPages}`;
    }
}

function changePatientPage(direction) {
    const filteredData =
        getFilteredPatients();

    const totalPages =
        Math.ceil(
            filteredData.length /
            patientsPerPage
        ) || 1;

    currentPatientPage += Number(direction);

    if (currentPatientPage < 1) {
        currentPatientPage = 1;
    }

    if (currentPatientPage > totalPages) {
        currentPatientPage = totalPages;
    }

    renderPatientTable(filteredData);
}


/* =========================================================
   FILTRAR PACIENTES
========================================================= */

function getFilteredPatients(searchValue = null) {
    const searchInput =
        document.querySelector(
            "#patientListModal .table-search"
        );

    const search =
        String(
            searchValue ??
            searchInput?.value ??
            ""
        )
            .trim()
            .toLowerCase();

    if (!search) {
        return patientsData;
    }

    return patientsData.filter(patient => {
        const text = `
            ${patient.id ?? ""}
            ${patient.firstName ?? ""}
            ${patient.lastName ?? ""}
            ${patient.dni ?? ""}
            ${patient.phone ?? ""}
            ${patient.email ?? ""}
            ${patient.active ? "activo" : "inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterPatientTable(searchValue = null) {
    currentPatientPage = 1;

    renderPatientTable(
        getFilteredPatients(searchValue)
    );
}


/* =========================================================
   ABRIR Y CERRAR LISTA
========================================================= */

async function togglePatientList() {
    const modal =
        document.getElementById(
            "patientListModal"
        );

    if (!modal) {
        return;
    }

    modal.classList.remove("hidden");
    document.body.classList.add("modal-open");

    await loadPatients(false);

    const searchInput =
        modal.querySelector(
            ".table-search"
        );

    window.setTimeout(() => {
        searchInput?.focus();
    }, 150);
}

function closePatientList() {
    const modal =
        document.getElementById(
            "patientListModal"
        );

    if (!modal) {
        return;
    }

    modal.classList.add("hidden");
    document.body.classList.remove("modal-open");
}


/* =========================================================
   CARGAR PACIENTES EN SELECT DE CITAS
========================================================= */

async function loadPatientOptions() {
    try {
        const response =
            await authFetch(
                `${baseUrl}/patients/active`
            );

        if (!response) {
            return;
        }

        const data =
            await readPatientResponse(response);

        if (!response.ok) {
            return;
        }

        const select =
            document.getElementById(
                "appointmentPatientId"
            );

        if (!select) {
            return;
        }

        select.innerHTML = "";

        const defaultOption =
            document.createElement("option");

        defaultOption.value = "";
        defaultOption.textContent =
            "Seleccione paciente";

        select.appendChild(defaultOption);

        if (!Array.isArray(data)) {
            return;
        }

        data.forEach(patient => {
            const option =
                document.createElement("option");

            option.value =
                String(patient.id ?? "");

            const fullName =
                getPatientFullName(patient);

            option.textContent =
                `${fullName} - DNI: ${
                    patient.dni || "Sin DNI"
                }`;

            select.appendChild(option);
        });

    } catch (error) {
        console.error(
            "Error cargando pacientes:",
            error
        );
    }
}


/* =========================================================
   EDITAR PACIENTE
========================================================= */

function startEditPatient(id) {
    const patient =
        patientsData.find(
            item =>
                Number(item.id) ===
                Number(id)
        );

    if (!patient) {
        showPatientMessage(
            "No se encontró el paciente seleccionado.",
            "error"
        );

        return;
    }

    editingPatientId =
        Number(id);

    setPatientInputValue(
        "patientFirstName",
        patient.firstName
    );

    setPatientInputValue(
        "patientLastName",
        patient.lastName
    );

    setPatientInputValue(
        "patientDni",
        patient.dni
    );

    setPatientInputValue(
        "patientBirthDate",
        patient.birthDate
    );

    setPatientInputValue(
        "patientGender",
        patient.gender
    );

    setPatientInputValue(
        "patientPhone",
        patient.phone
    );

    setPatientInputValue(
        "patientEmail",
        patient.email
    );

    setPatientInputValue(
        "patientAddress",
        patient.address
    );

    setPatientInputValue(
        "patientEmergencyContact",
        patient.emergencyContact
    );

    setPatientInputValue(
        "patientEmergencyPhone",
        patient.emergencyPhone
    );

    updatePatientFormMode();

    closePatientList();

    document
        .getElementById("patients")
        ?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });

    showPatientMessage(
        `Editando a ${getPatientFullName(patient)}.`,
        "info"
    );
}

function setPatientInputValue(
    elementId,
    value
) {
    const element =
        document.getElementById(elementId);

    if (element) {
        element.value =
            value ?? "";
    }
}


/* =========================================================
   CANCELAR EDICIÓN
========================================================= */

function cancelPatientEdit(showNotification = true) {
    finishPatientEdit();

    if (showNotification) {
        showPatientMessage(
            "Edición cancelada.",
            "info"
        );
    }
}

function finishPatientEdit() {
    editingPatientId = null;

    clearPatientForm();
    updatePatientFormMode();
}

function updatePatientFormMode() {
    const saveButton =
        document.getElementById(
            "patientSaveButton"
        );

    const cancelButton =
        document.getElementById(
            "patientCancelEditButton"
        );

    if (saveButton) {
        saveButton.innerHTML =
            editingPatientId
                ? `
                    <i data-lucide="save"></i>
                    Actualizar paciente
                `
                : `
                    <i data-lucide="save"></i>
                    Guardar paciente
                `;
    }

    if (cancelButton) {
        cancelButton.style.display =
            editingPatientId
                ? "inline-flex"
                : "none";
    }

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}


/* =========================================================
   ACTIVAR O DESACTIVAR
========================================================= */

async function togglePatientStatus(
    id,
    isActive
) {
    const actionText =
        isActive
            ? "desactivar"
            : "reactivar";

    const confirmText =
        isActive
            ? "Sí, desactivar"
            : "Sí, reactivar";

    const confirmation =
        await Swal.fire({
            title:
                `¿Deseas ${actionText} este paciente?`,
            text:
                isActive
                    ? "El paciente no será eliminado; quedará inactivo."
                    : "El paciente volverá a estar disponible en el sistema.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: confirmText,
            cancelButtonText: "Cancelar",
            confirmButtonColor:
                isActive
                    ? "#b84b52"
                    : "#164f7b",
            reverseButtons: true
        });

    if (!confirmation.isConfirmed) {
        return;
    }

    try {
        const response =
            await authFetch(
                `${baseUrl}/patients/${id}/toggle-active`,
                {
                    method: "PATCH"
                }
            );

        if (!response) {
            return;
        }

        const result =
            await readPatientResponse(response);

        if (!response.ok) {
            showPatientMessage(
                result.message ||
                result.detail ||
                result.error ||
                "No se pudo cambiar el estado del paciente.",
                "error"
            );

            return;
        }

        showPatientMessage(
            result.active
                ? "Paciente reactivado correctamente."
                : "Paciente desactivado correctamente.",
            "success"
        );

        await loadPatients(false);
        await loadPatientOptions();

    } catch (error) {
        console.error(
            "Error cambiando estado:",
            error
        );

        showPatientMessage(
            "No se pudo conectar con el servidor.",
            "error"
        );
    }
}


/* =========================================================
   LIMPIAR FORMULARIO
========================================================= */

function clearPatientForm() {
    [
        "patientFirstName",
        "patientLastName",
        "patientDni",
        "patientBirthDate",
        "patientGender",
        "patientPhone",
        "patientEmail",
        "patientAddress",
        "patientEmergencyContact",
        "patientEmergencyPhone"
    ].forEach(elementId => {
        const element =
            document.getElementById(elementId);

        if (element) {
            element.value = "";
        }
    });
}


/* =========================================================
   ESTADO DE CARGA DEL FORMULARIO
========================================================= */

function setPatientFormLoading(loading) {
    patientRequestInProgress =
        Boolean(loading);

    const saveButton =
        document.getElementById(
            "patientSaveButton"
        );

    const cancelButton =
        document.getElementById(
            "patientCancelEditButton"
        );

    if (saveButton) {
        saveButton.disabled = loading;

        saveButton.innerHTML =
            loading
                ? `
                    <span class="patient-button-spinner"></span>
                    Guardando...
                `
                : editingPatientId
                    ? `
                        <i data-lucide="save"></i>
                        Actualizar paciente
                    `
                    : `
                        <i data-lucide="save"></i>
                        Guardar paciente
                    `;
    }

    if (cancelButton) {
        cancelButton.disabled = loading;
    }

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}


/* =========================================================
   AVISOS FLOTANTES
========================================================= */

function showPatientMessage(
    message,
    type = "info"
) {
    if (!message) {
        return;
    }

    /*
     * Se conserva patientResult para compatibilidad,
     * aunque permanece oculto mediante CSS.
     */
    const resultBox =
        document.getElementById(
            "patientResult"
        );

    if (resultBox) {
        resultBox.textContent = message;

        resultBox.className =
            `patient-hidden-result message-${type}`;
    }

    /*
     * No mostrar este aviso automático.
     */
    if (
        message
            .toLowerCase()
            .includes(
                "pacientes cargados correctamente"
            )
    ) {
        return;
    }

    if (typeof Swal === "undefined") {
        console.log(message);
        return;
    }

    const icon =
        type === "success"
            ? "success"
            : type === "error"
                ? "error"
                : "info";

    Swal.fire({
        toast: true,
        position: "top-end",
        icon,
        title: message,
        showConfirmButton: false,
        timer:
            type === "error"
                ? 3200
                : 2100,
        timerProgressBar: true,
        customClass: {
            popup: "patient-toast"
        }
    });
}


/* =========================================================
   HISTORIA CLÍNICA
========================================================= */

function openPatientClinicalHistory(patientId) {
    const patient =
        patientsData.find(
            item =>
                Number(item.id) ===
                Number(patientId)
        );

    if (!patient) {
        showPatientMessage(
            "No se encontró el paciente.",
            "error"
        );

        return;
    }

    if (
        typeof openClinicalHistory !==
        "function"
    ) {
        showPatientMessage(
            "No se pudo abrir la historia clínica.",
            "error"
        );

        return;
    }

    openClinicalHistory(
        patient.id,
        getPatientFullName(patient)
    );
}


/* =========================================================
   DATOS DESDE PRE-RESERVA
========================================================= */

function prefillPatientFromLead(lead) {
    if (!lead) {
        return;
    }

    editingPatientId = null;
    updatePatientFormMode();

    if (
        typeof showSectionById ===
        "function"
    ) {
        showSectionById("patients");
    }

    const fullNameParts =
        String(lead.fullName || "")
            .trim()
            .split(/\s+/)
            .filter(Boolean);

    let firstName = "";
    let lastName = "";

    if (fullNameParts.length === 1) {
        firstName = fullNameParts[0];

    } else if (fullNameParts.length === 2) {
        firstName = fullNameParts[0];
        lastName = fullNameParts[1];

    } else {
        firstName =
            fullNameParts
                .slice(0, 2)
                .join(" ");

        lastName =
            fullNameParts
                .slice(2)
                .join(" ");
    }

    setPatientInputValue(
        "patientFirstName",
        firstName
    );

    setPatientInputValue(
        "patientLastName",
        lastName
    );

    setPatientInputValue(
        "patientPhone",
        lead.phone
    );

    setPatientInputValue(
        "patientEmail",
        lead.email
    );

    setPatientInputValue(
        "patientDni",
        ""
    );

    setPatientInputValue(
        "patientBirthDate",
        ""
    );

    setPatientInputValue(
        "patientGender",
        ""
    );

    setPatientInputValue(
        "patientAddress",
        ""
    );

    setPatientInputValue(
        "patientEmergencyContact",
        ""
    );

    setPatientInputValue(
        "patientEmergencyPhone",
        ""
    );

    showPatientMessage(
        "Datos cargados desde la pre-reserva. Complete la información restante.",
        "info"
    );

    document
        .getElementById("patients")
        ?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });

    window.setTimeout(() => {
        document
            .getElementById("patientDni")
            ?.focus();
    }, 500);
}


/* =========================================================
   FUNCIONES AUXILIARES
========================================================= */

function getPatientFullName(patient) {
    const fullName = `
        ${patient?.firstName || ""}
        ${patient?.lastName || ""}
    `
        .replace(/\s+/g, " ")
        .trim();

    return fullName || "Paciente";
}

function escapePatientHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/*
 * Se conserva porque puede ser utilizado
 * desde otros archivos antiguos.
 */
function escapeText(text) {
    return String(text ?? "")
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'")
        .replaceAll('"', "&quot;")
        .replaceAll("\n", " ");
}

async function readPatientResponse(response) {
    const responseText =
        await response.text();

    if (!responseText) {
        return {};
    }

    try {
        return JSON.parse(responseText);

    } catch (error) {
        return {
            message: responseText
        };
    }
}


/* =========================================================
   INICIALIZACIÓN DEL MÓDULO
========================================================= */

function initializePatientModule() {
    const dniInput =
        document.getElementById(
            "patientDni"
        );

    if (
        dniInput &&
        !dniInput.dataset.patientInitialized
    ) {
        dniInput.dataset.patientInitialized =
            "true";

        dniInput.addEventListener(
            "input",
            () => {
                dniInput.value =
                    dniInput.value
                        .replace(/\D/g, "")
                        .slice(0, 8);
            }
        );
    }

    const modal =
        document.getElementById(
            "patientListModal"
        );

    if (
        modal &&
        !modal.dataset.patientInitialized
    ) {
        modal.dataset.patientInitialized =
            "true";

        modal.addEventListener(
            "click",
            event => {
                if (event.target === modal) {
                    closePatientList();
                }
            }
        );
    }

    document.addEventListener(
        "keydown",
        event => {
            if (event.key === "Escape") {
                closePatientList();
            }
        },
        {
            once: false
        }
    );
}

if (document.readyState === "loading") {
    document.addEventListener(
        "DOMContentLoaded",
        initializePatientModule,
        {
            once: true
        }
    );
} else {
    initializePatientModule();
}