let servicesData = [];
let currentServicePage = 1;
const servicesPerPage = 10;

let editingServiceId = null;

function getServiceFormData() {
    const priceValue = document.getElementById("servicePrice").value;
    const durationValue = document.getElementById("serviceDuration").value;

    return {
        name: document.getElementById("serviceName").value.trim(),
        description: document.getElementById("serviceDescription").value.trim(),
        price: priceValue === "" ? null : parseFloat(priceValue),
        durationMinutes: durationValue === "" ? null : parseInt(durationValue),
        active: true
    };
}

function validateServiceForm(data) {
    if (!data.name) return "Ingrese el nombre del servicio";
    if (data.price === null || isNaN(data.price) || data.price < 0) {
        return "Ingrese un costo válido";
    }
    if (data.durationMinutes === null || isNaN(data.durationMinutes) || data.durationMinutes <= 0) {
        return "Ingrese una duración válida en minutos";
    }
    return null;
}

async function saveService() {
    if (editingServiceId) {
        await updateService();
    } else {
        await createService();
    }
}

async function createService() {
    const data = getServiceFormData();
    const validationError = validateServiceForm(data);

    if (validationError) {
        showServiceMessage(validationError, "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/services`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showServiceMessage(result.message || "Error al guardar servicio", "error");
            return;
        }

        showServiceMessage("Servicio guardado correctamente", "success");

        clearServiceForm();
        await loadServices();
        await loadServiceOptions();

    } catch (error) {
        showServiceMessage("Error de conexión con el servidor", "error");
    }
}

async function updateService() {
    const data = getServiceFormData();
    const validationError = validateServiceForm(data);

    if (validationError) {
        showServiceMessage(validationError, "error");
        return;
    }

    const currentService = servicesData.find(s => s.id === editingServiceId);
    data.active = currentService ? currentService.active : true;

    try {
        const response = await authFetch(`${baseUrl}/services/${editingServiceId}`, {
            method: "PUT",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showServiceMessage(result.message || "Error al actualizar servicio", "error");
            return;
        }

        showServiceMessage("Servicio actualizado correctamente", "success");

        cancelServiceEdit();
        await loadServices();
        await loadServiceOptions();

    } catch (error) {
        showServiceMessage("Error de conexión con el servidor", "error");
    }
}

async function loadServices() {
    try {
        const response = await authFetch(`${baseUrl}/services`);
        if (!response) return;

        servicesData = await response.json();
        currentServicePage = 1;

        renderServiceTable(getFilteredServices());

        showServiceMessage("Servicios cargados correctamente", "success");

    } catch (error) {
        showServiceMessage("Error al listar servicios", "error");
    }
}

function renderServiceTable(data) {
    const tbody = document.getElementById("serviceTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / servicesPerPage) || 1;

    if (currentServicePage > totalPages) {
        currentServicePage = totalPages;
    }

    const start = (currentServicePage - 1) * servicesPerPage;
    const end = start + servicesPerPage;
    const pageData = data.slice(start, end);

    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">
                    No se encontraron servicios
                </td>
            </tr>
        `;
        return;
    }

    pageData.forEach(service => {
        tbody.innerHTML += `
            <tr>
                <td>${service.id ?? ""}</td>
                <td>${service.name ?? ""}</td>
                <td>${service.description ?? ""}</td>
                <td>S/ ${Number(service.price || 0).toFixed(2)}</td>
                <td>${service.durationMinutes ?? ""} min</td>
                <td>
                    <span class="status-pill ${service.active ? "active" : "inactive"}">
                        ${service.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td>
                    <button class="btn-secondary" onclick="startEditService(${service.id})">
                        Editar
                    </button>

                    <button class="${service.active ? "btn-danger-soft" : "btn-secondary"}"
                            onclick="toggleServiceStatus(${service.id}, ${service.active})">
                        ${service.active ? "Desactivar" : "Reactivar"}
                    </button>
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("servicePageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentServicePage} de ${totalPages}`;
    }
}

function getFilteredServices() {
    const input = document.querySelector("#serviceListModal .table-search");
    const search = input ? input.value.toLowerCase() : "";

    if (!search) return servicesData;

    return servicesData.filter(service => {
        const text = `
            ${service.id ?? ""}
            ${service.name ?? ""}
            ${service.description ?? ""}
            ${service.price ?? ""}
            ${service.durationMinutes ?? ""}
            ${service.active ? "Activo" : "Inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterServiceTable() {
    currentServicePage = 1;
    renderServiceTable(getFilteredServices());
}

function changeServicePage(direction) {
    const filteredData = getFilteredServices();
    const totalPages = Math.ceil(filteredData.length / servicesPerPage) || 1;

    currentServicePage += direction;

    if (currentServicePage < 1) currentServicePage = 1;
    if (currentServicePage > totalPages) currentServicePage = totalPages;

    renderServiceTable(filteredData);
}

async function toggleServiceList() {
    document.getElementById("serviceListModal").classList.remove("hidden");
    await loadServices();
}

function closeServiceList() {
    document.getElementById("serviceListModal").classList.add("hidden");
}

async function loadServiceOptions() {
    const select =
        document.getElementById("appointmentServiceId");

    if (!select) {
        return;
    }

    select.disabled = true;

    select.innerHTML = `
        <option value="">
            Cargando servicios...
        </option>
    `;

    try {
        const response =
            await authFetch(
                `${baseUrl}/services/active`
            );

        if (!response) {
            select.innerHTML = `
                <option value="">
                    No se pudieron cargar los servicios
                </option>
            `;

            return;
        }

        const data =
            await response.json();

        if (!response.ok) {
            select.innerHTML = `
                <option value="">
                    Error al cargar servicios
                </option>
            `;

            console.error(
                "Error del servidor al cargar servicios:",
                data
            );

            return;
        }

        const services =
            Array.isArray(data)
                ? data
                : [];

        select.innerHTML = `
            <option value="">
                Seleccione un servicio
            </option>
        `;

        services.forEach(service => {
            const option =
                document.createElement("option");

            const durationMinutes =
                Number(service.durationMinutes || 0);

            const price =
                Number(service.price || 0);

            option.value =
                String(service.id);

            /*
             * appointments.js utilizará este dato
             * para generar los bloques horarios.
             */
            option.dataset.durationMinutes =
                String(durationMinutes);

            option.textContent =
                `${service.name || "Servicio"} - ` +
                `S/ ${price.toFixed(2)} - ` +
                `${durationMinutes} min`;

            select.appendChild(option);
        });

        select.disabled = false;

        if (services.length === 0) {
            select.innerHTML = `
                <option value="">
                    No hay servicios activos
                </option>
            `;

            select.disabled = true;
        }

    } catch (error) {
        console.error(
            "Error cargando servicios:",
            error
        );

        select.innerHTML = `
            <option value="">
                Error de conexión
            </option>
        `;

        select.disabled = true;
    }
}

function startEditService(id) {
    const service = servicesData.find(s => s.id === id);

    if (!service) {
        showServiceMessage("No se encontró el servicio seleccionado", "error");
        return;
    }

    editingServiceId = id;

    document.getElementById("serviceName").value = service.name ?? "";
    document.getElementById("serviceDescription").value = service.description ?? "";
    document.getElementById("servicePrice").value = service.price ?? "";
    document.getElementById("serviceDuration").value = service.durationMinutes ?? "";

    document.getElementById("serviceSaveButton").textContent = "Actualizar servicio";
    document.getElementById("serviceCancelEditButton").style.display = "inline-block";

    closeServiceList();

    document.getElementById("services").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    showServiceMessage(`Editando servicio: ${service.name}`, "info");
}

function cancelServiceEdit() {
    editingServiceId = null;
    clearServiceForm();

    document.getElementById("serviceSaveButton").textContent = "Guardar servicio";
    document.getElementById("serviceCancelEditButton").style.display = "none";

    showServiceMessage("Edición cancelada", "info");
}

async function toggleServiceStatus(id, isActive) {
    const actionText = isActive ? "desactivar" : "reactivar";
    const confirmText = isActive ? "Sí, desactivar" : "Sí, reactivar";

    const confirm = await Swal.fire({
        title: `¿Deseas ${actionText} este servicio?`,
        text: isActive
            ? "El servicio no se eliminará, solo quedará inactivo."
            : "El servicio volverá a estar disponible para nuevas citas.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: confirmText,
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/services/${id}/toggle-active`, {
            method: "PATCH"
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showServiceMessage(result.message || "No se pudo cambiar el estado del servicio", "error");
            return;
        }

        await Swal.fire({
            icon: "success",
            title: "Estado actualizado",
            text: `Servicio ${result.active ? "reactivado" : "desactivado"} correctamente`,
            timer: 1600,
            showConfirmButton: false
        });

        await loadServices();
        await loadServiceOptions();

    } catch (error) {
        showServiceMessage("Error de conexión con el servidor", "error");
    }
}

function clearServiceForm() {
    document.getElementById("serviceName").value = "";
    document.getElementById("serviceDescription").value = "";
    document.getElementById("servicePrice").value = "";
    document.getElementById("serviceDuration").value = "";
}

function showServiceMessage(message, type = "info") {
    const box = document.getElementById("serviceResult");
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